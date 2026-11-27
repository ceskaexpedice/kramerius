package cz.incad.kramerius.statistics.accesslogs;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The one place that says which fields of the access log record belong to the
 * current record format (see {@link LogRecord#LOG_FORMAT}) and how they are
 * declared in the Solr core "logs" (installation/_common_schema/logs/fields.xml).
 *
 * Everything that has to agree on this set reads it from here: the schema guard
 * that adds missing fields to a running Solr core, the fallback that strips a
 * rejected record down to format 0, and the test that checks the list against
 * fields.xml. When a field is added to the format, it is added here and to
 * fields.xml, nowhere else.
 */
public final class AccessLogFormat {

    /** A format 1 field and how it is declared in the logs schema. */
    public static final class Field {
        private final String name;
        private final String type;
        private final boolean multiValued;

        Field(String name, String type, boolean multiValued) {
            this.name = name;
            this.type = type;
            this.multiValued = multiValued;
        }

        public String getName() {
            return name;
        }

        /** Solr field type name as used in the logs schema (string, int, ...). */
        public String getType() {
            return type;
        }

        public boolean isMultiValued() {
            return multiValued;
        }
    }

    /** Fields that exist only in records of format 1 and newer. */
    public static final List<Field> FORMAT_1_FIELDS = Collections.unmodifiableList(new ArrayList<>(java.util.Arrays.asList(
            new Field("log_format", "int", false),
            new Field("stream_name", "string", false),
            new Field("stream_mime", "string", false),
            new Field("image_kind", "string", false),
            new Field("hrh_kramerius_client_url", "string", false),
            new Field("hrh_kramerius_client_app", "string", false),
            new Field("hrh_kramerius_client_version", "string", false),
            new Field("hrh_kramerius_intent", "string", true)
    )));

    private static final Set<String> FORMAT_1_NAMES;

    static {
        Set<String> names = new LinkedHashSet<>();
        for (Field f : FORMAT_1_FIELDS) {
            names.add(f.getName());
        }
        FORMAT_1_NAMES = Collections.unmodifiableSet(names);
    }

    private AccessLogFormat() {
    }

    public static Set<String> format1FieldNames() {
        return FORMAT_1_NAMES;
    }

    public static boolean isFormat1Field(String name) {
        return FORMAT_1_NAMES.contains(name);
    }

    /**
     * Removes every format 1 field from a Solr update document built by
     * {@link LogRecord#toSolrBatch}, leaving a record a core with the old schema
     * accepts (format 0: no log_format, no new fields). The document is changed
     * in place and returned.
     */
    public static Document stripToFormat0(Document batch) {
        NodeList fields = batch.getElementsByTagName("field");
        List<Node> remove = new ArrayList<>();
        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);
            if (isFormat1Field(field.getAttribute("name"))) {
                remove.add(field);
            }
        }
        for (Node node : remove) {
            node.getParentNode().removeChild(node);
        }
        return batch;
    }
}
