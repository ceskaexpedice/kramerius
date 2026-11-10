package cz.incad.kramerius.statistics.accesslogs.solr;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.statistics.accesslogs.AccessLogFormat;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps the Solr core "logs" able to take records of the current format.
 *
 * An upgraded core writes fields the old schema does not know, and Solr then
 * rejects the whole record (schemaless configs fail on the copyField to
 * "*_str", classic configs on the unknown field), so an installation whose
 * schema was not updated with the core would silently stop logging. This guard
 * asks the Schema API which of the {@link AccessLogFormat#FORMAT_1_FIELDS} are
 * missing and adds them with the types fields.xml declares. It runs once before
 * the first record and again whenever Solr rejects a record. When the schema
 * cannot be changed (no managed schema, API not reachable), it says so once in
 * the log and the caller falls back to format 0 records.
 *
 * Disabled with statistics.solr.schema.autofields=false.
 */
public class SolrLogsSchemaGuard {

    public static final Logger LOGGER = Logger.getLogger(SolrLogsSchemaGuard.class.getName());

    /** configuration key; true (default) lets the core add missing fields to the logs schema */
    public static final String CONF_AUTOFIELDS = "statistics.solr.schema.autofields";

    private final Client client;
    private final String corePoint;

    /** how long records go out as format 0 after a rejection before the full record is tried again */
    static final long RECHECK_INTERVAL_MS = 10 * 60 * 1000L;

    private boolean checked = false;
    /** the schema is known to declare every format 1 field */
    private boolean complete = false;
    /** Solr rejected a full record and the fields could not be added; records go out as format 0 */
    private boolean rejected = false;
    private boolean warnedFallback = false;
    private long rejectedAt = 0L;

    public SolrLogsSchemaGuard(Client client, String corePoint) {
        this.client = client;
        this.corePoint = corePoint.endsWith("/") ? corePoint.substring(0, corePoint.length() - 1) : corePoint;
    }

    /**
     * Makes sure the schema has the format 1 fields; called before the first
     * record. Returns true when the schema is known to be complete.
     */
    public synchronized boolean ensureOnce() {
        if (!checked) {
            ensure();
        }
        return complete;
    }

    /**
     * Checks the schema again and adds what is missing; called after Solr
     * rejected a record. Returns true when the schema is complete afterwards.
     */
    public synchronized boolean ensure() {
        checked = true;
        if (!KConfiguration.getInstance().getConfiguration().getBoolean(CONF_AUTOFIELDS, true)) {
            complete = false;
            return false;
        }
        try {
            Set<String> existing = existingFields();
            List<AccessLogFormat.Field> missing = missingFields(existing);
            if (missing.isEmpty()) {
                markAccepted();
                return true;
            }
            addFields(missing);
            Set<String> after = existingFields();
            complete = missingFields(after).isEmpty();
            if (complete) {
                markAccepted();
                LOGGER.log(Level.WARNING, "Added missing access log fields to the Solr core {0}: {1}. "
                        + "Keep installation/_common_schema/logs/fields.xml in sync.", new Object[]{corePoint, names(missing)});
            } else {
                LOGGER.log(Level.WARNING, "Could not add the access log fields {0} to the Solr core {1}",
                        new Object[]{names(missingFields(after)), corePoint});
            }
            return complete;
        } catch (Exception e) {
            complete = false;
            LOGGER.log(Level.WARNING, "Could not check the schema of the Solr core " + corePoint + ": " + e.getMessage());
            return false;
        }
    }

    /** Logs the fallback to format 0 once, until the schema becomes complete. */
    public synchronized void warnFallback() {
        if (!warnedFallback) {
            warnedFallback = true;
            LOGGER.log(Level.SEVERE, "The Solr core {0} does not take access log records of format {1}; records are "
                    + "stored as format 0 (without {2}) until the fields are added to its schema "
                    + "(installation/_common_schema/logs/fields.xml).",
                    new Object[]{corePoint, cz.incad.kramerius.statistics.accesslogs.LogRecord.LOG_FORMAT,
                            AccessLogFormat.format1FieldNames()});
        }
    }

    public synchronized boolean isComplete() {
        return complete;
    }

    /** Solr accepted a full record (or the schema was found complete): back to normal. */
    public synchronized void markAccepted() {
        complete = true;
        rejected = false;
        warnedFallback = false;
    }

    /** Solr rejected a full record and the fields could not be added: records go out as format 0 for a while. */
    public synchronized void markRejected() {
        complete = false;
        rejected = true;
        rejectedAt = System.currentTimeMillis();
    }

    /** True while a rejection is fresh: send format 0 without trying the full record first. */
    public synchronized boolean skipFullRecord() {
        return rejected && !recheckDue(rejected, rejectedAt, System.currentTimeMillis());
    }

    /** True when a rejection is old enough to try a full record again (someone may have added the fields). */
    static boolean recheckDue(boolean rejected, long rejectedAt, long now) {
        return rejected && now - rejectedAt >= RECHECK_INTERVAL_MS;
    }

    /** Names of the format 1 fields the schema does not declare. */
    public static List<AccessLogFormat.Field> missingFields(Collection<String> existing) {
        List<AccessLogFormat.Field> missing = new ArrayList<>();
        for (AccessLogFormat.Field field : AccessLogFormat.FORMAT_1_FIELDS) {
            if (!existing.contains(field.getName())) {
                missing.add(field);
            }
        }
        return missing;
    }

    /** Schema API command adding the given fields the way fields.xml declares them. */
    public static JSONObject addFieldsCommand(List<AccessLogFormat.Field> fields) {
        JSONArray add = new JSONArray();
        for (AccessLogFormat.Field field : fields) {
            JSONObject def = new JSONObject();
            def.put("name", field.getName());
            def.put("type", field.getType());
            def.put("indexed", true);
            def.put("stored", true);
            def.put("docValues", true);
            def.put("multiValued", field.isMultiValued());
            add.put(def);
        }
        JSONObject command = new JSONObject();
        command.put("add-field", add);
        return command;
    }

    /** Field names from a Schema API /schema/fields answer. */
    public static Set<String> fieldNames(JSONObject schemaFields) {
        Set<String> names = new HashSet<>();
        JSONArray fields = schemaFields.optJSONArray("fields");
        if (fields != null) {
            for (int i = 0; i < fields.length(); i++) {
                names.add(fields.getJSONObject(i).getString("name"));
            }
        }
        return names;
    }

    private Set<String> existingFields() {
        WebResource r = client.resource(corePoint + "/schema/fields?wt=json");
        ClientResponse resp = r.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        String body = resp.getEntity(String.class);
        if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
            throw new IllegalStateException("GET /schema/fields answered " + resp.getStatus() + ": " + body);
        }
        return fieldNames(new JSONObject(body));
    }

    private void addFields(List<AccessLogFormat.Field> fields) {
        WebResource r = client.resource(corePoint + "/schema");
        ClientResponse resp = r.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                .entity(addFieldsCommand(fields).toString(), MediaType.APPLICATION_JSON).post(ClientResponse.class);
        String body = resp.getEntity(String.class);
        if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
            throw new IllegalStateException("POST /schema add-field answered " + resp.getStatus() + ": " + body);
        }
    }

    private static List<String> names(List<AccessLogFormat.Field> fields) {
        List<String> names = new ArrayList<>();
        for (AccessLogFormat.Field f : fields) {
            names.add(f.getName());
        }
        return names;
    }
}
