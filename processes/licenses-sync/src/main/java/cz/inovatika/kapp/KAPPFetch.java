package cz.inovatika.kapp;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.sync.LicenseAPIFetcher;
import cz.inovatika.sync.SyncUtils;
import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KAPPFetch {

    public static final Logger LOGGER = Logger.getLogger(KAPPFetch.class.getName());

    public static final String CONFIG_ENDPOINT = "kapp.endpoint";
    public static final String CONFIG_ACRONYM = "kapp.check.acronym";
    public static final String CONFIG_CHECK_LOCAL_API = "kapp.check.local.api";
    public static final String CONFIG_CHECK_VERSION = "kapp.check.version";

    private static final String CONFIG_SOLR_HOST = "kapp.solrHost";
    private static final String CONFIG_ROWS = "kapp.fetch.rows";
    private static final String CONFIG_FROM = "kapp.fetch.from";
    private static final String CONFIG_TO = "kapp.fetch.to";
    private static final String CONFIG_ASSIGNED_LICENSE = "kapp.fetch.assignedLicense";
    private static final String CONFIG_DELETE_OLD = "kapp.deleteOld";
    private static final String CONFIG_QUERY_BY_ID = "kapp.query.byid";

    private static final String DEFAULT_SOURCE = "kapp";
    private static final String CHANGES_PATH = "/v1/changes";
    private static final String TYPE_SIMPLE_MONOGRAPHS = "simple-monographs";
    private static final String TYPE_SETS = "sets";
    private static final String LICENSE_PUBLIC = "public";
    private static final String LICENSE_ONSITE = "onsite";
    public static final String DEFAULT_VERSION = "v7";

    private static final int DEFAULT_ROWS = 1000;
    private static final int BATCH_SIZE = 10000;

    private enum KappSyncActionEnum {
        add_public(0),
        add_onsite(1),
        remove_public(2),
        remove_onsite(3),
        change_public_onsite(4),
        change_onsite_public(5),
        partial_change(6);

        private final int sortValue;

        KappSyncActionEnum(int sortValue) {
            this.sortValue = sortValue;
        }

        public Integer getValue() {
            return sortValue;
        }
    }

    public static void kappFetchMain() throws IOException, InterruptedException, SolrServerException {
        String solrHost = KConfiguration.getInstance().getConfiguration().getString(CONFIG_SOLR_HOST);
        if (solrHost == null) {
            throw new IllegalStateException("Missing configuration key '" + CONFIG_SOLR_HOST + "'");
        }

        String[] splitted = solrHost.split("/");
        String collection = splitted.length > 0 ? splitted[splitted.length - 1] : null;
        if (collection == null || collection.trim().isEmpty()) {
            throw new IllegalStateException("Cannot resolve Solr collection from '" + CONFIG_SOLR_HOST + "'");
        }

        int collectionIndex = solrHost.lastIndexOf(collection);
        String solrBaseUrl = collectionIndex > -1 ? solrHost.substring(0, collectionIndex) : solrHost;

        try (HttpSolrClient solrClient = new HttpSolrClient.Builder(solrBaseUrl).build();
             CloseableHttpClient httpClient = HttpClients.createDefault()) {
            process(solrClient, httpClient, collection);
        }
    }

    public static void process(HttpSolrClient solrClient, CloseableHttpClient httpClient, String collection)
            throws IOException, InterruptedException, SolrServerException {

        String endpoint = KConfiguration.getInstance().getConfiguration().getString(CONFIG_ENDPOINT);
        if (endpoint == null) {
            throw new IllegalStateException("Missing configuration key '" + CONFIG_ENDPOINT + "'");
        }
        endpoint = endpoint.trim();
        String acronym = KConfiguration.getInstance().getConfiguration().getString(CONFIG_ACRONYM);
        if (acronym == null || acronym.trim().isEmpty()) {
            throw new IllegalStateException("Missing configuration key '" + CONFIG_ACRONYM + "'");
        }
        acronym = acronym.trim();

        long start = System.currentTimeMillis();
        Date fetched = new Date(start);
        List<SolrInputDocument> batch = new ArrayList<>();
        Set<String> fetchedPids = new HashSet<>();
        Map<String, String> childToParent = new HashMap<>();

        LOGGER.info("Connecting KAPP list and iterating simple monographs");
        iterateKappType(httpClient, endpoint, acronym, TYPE_SIMPLE_MONOGRAPHS, fetched, batch, solrClient, collection,
                fetchedPids, childToParent);
        LOGGER.info("Connecting KAPP list and iterating sets");
        iterateKappType(httpClient, endpoint, acronym, TYPE_SETS, fetched, batch, solrClient, collection,
                fetchedPids, childToParent);

        if (!batch.isEmpty()) {
            writeBatch(solrClient, collection, batch);
        }

        boolean deleteOld = KConfiguration.getInstance().getConfiguration().getBoolean(CONFIG_DELETE_OLD, true);
        if (deleteOld) {
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(fetched.toInstant(), ZoneId.systemDefault());
            String fetchedLimit = DateTimeFormatter.ISO_INSTANT.format(offsetDateTime);
            solrClient.deleteByQuery(collection,
                    String.format("source:%s AND fetched:[* TO %s-1MINUTE]", DEFAULT_SOURCE, fetchedLimit));
            solrClient.commit(collection);
        }

        updateKrameriusState(solrClient, collection, fetchedPids, childToParent);

        LOGGER.info("KAPP list fetched; It took " + (System.currentTimeMillis() - start) + " ms");
    }

    private static void iterateKappType(
            CloseableHttpClient httpClient,
            String endpoint,
            String acronym,
            String type,
            Date fetched,
            List<SolrInputDocument> batch,
            HttpSolrClient solrClient,
            String collection,
            Set<String> fetchedPids,
            Map<String, String> childToParent) throws IOException, InterruptedException, SolrServerException {

        int configuredRows = KConfiguration.getInstance().getConfiguration().getInt(CONFIG_ROWS, DEFAULT_ROWS);
        int rows = Math.max(1, Math.min(configuredRows, DEFAULT_ROWS));
        String token = "*";
        String prevToken = "";
        int sum = 0;

        while (token != null && !token.equals(prevToken)) {
            String url = buildUrl(endpoint, type, acronym, rows, token);
            LOGGER.info("Contacting KAPP instance, type: " + type+", url: " + url);

            File file = SyncUtils.throttle(httpClient, url);
            String response = FileUtils.readFileToString(file, Charset.forName("UTF-8"));

            JSONObject responseObject = null;
            if (response.trim().startsWith("[")) {
                prevToken = token;
                token = null;
            } else {
                responseObject = new JSONObject(response);
                prevToken = token;
                token = responseObject.optString("nextCursor", null);
                if (token != null && token.trim().isEmpty()) {
                    token = null;
                }
            }

            JSONArray items = extractDocs(response, responseObject);
            sum += items.length();
            LOGGER.info("KAPP fetched batch size: " + items.length() + ", total: " + sum + ", type: " + type);

            for (int i = 0; i < items.length(); i++) {
                appendSolrDocuments(items.getJSONObject(i), fetched, batch, solrClient, collection, fetchedPids,
                        childToParent, null);
            }
        }
    }

    private static JSONArray extractDocs(String response, JSONObject responseObject) {
        if (response.trim().startsWith("[")) {
            return new JSONArray(response);
        }
        if (responseObject.has("items")) {
            return responseObject.getJSONArray("items");
        }
        if (responseObject.has("docs")) {
            return responseObject.getJSONArray("docs");
        }
        if (responseObject.has("response")) {
            JSONObject solrResponse = responseObject.getJSONObject("response");
            if (solrResponse.has("docs")) {
                return solrResponse.getJSONArray("docs");
            }
        }
        throw new IllegalStateException("KAPP response does not contain 'items', 'docs' or 'response.docs'");
    }

    private static String buildUrl(String endpoint, String type, String acronym, int rows, String token) {
        StringBuilder builder = new StringBuilder();
        builder.append(normalizeEndpoint(endpoint, type));
        appendParameter(builder, "library", acronym);
        appendParameter(builder, "rows", String.valueOf(rows));
        appendParameter(builder, "cursor", token);

        String from = KConfiguration.getInstance().getConfiguration().getString(CONFIG_FROM);
        if (from != null && !from.trim().isEmpty()) {
            appendParameter(builder, "from", from);
        }

        String to = KConfiguration.getInstance().getConfiguration().getString(CONFIG_TO);
        if (to != null && !to.trim().isEmpty()) {
            appendParameter(builder, "to", to);
        }

        String[] assignedLicenses = KConfiguration.getInstance().getConfiguration()
                .getStringArray(CONFIG_ASSIGNED_LICENSE);
        for (String assignedLicense : assignedLicenses) {
            if (assignedLicense != null && !assignedLicense.trim().isEmpty()) {
                appendParameter(builder, "assignedLicense", assignedLicense);
            }
        }

        return builder.toString();
    }

    private static String normalizeEndpoint(String endpoint, String type) {
        String normalized = endpoint;
        int queryIndex = normalized.indexOf('?');
        if (queryIndex > -1) {
            normalized = normalized.substring(0, queryIndex);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        int changesPathIndex = normalized.indexOf(CHANGES_PATH + "/");
        if (changesPathIndex > -1) {
            return normalized.substring(0, changesPathIndex) + CHANGES_PATH + "/" + type;
        }
        if (normalized.endsWith("/v1/changes/" + type)) {
            return normalized;
        }
        if (normalized.endsWith(CHANGES_PATH)) {
            return normalized + "/" + type;
        }
        return normalized + CHANGES_PATH + "/" + type;
    }

    private static void appendParameter(StringBuilder builder, String name, String value) {
        builder.append(builder.indexOf("?") > -1 ? "&" : "?");
        builder.append(encode(name));
        builder.append("=");
        builder.append(encode(value));
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void appendSolrDocuments(
            JSONObject item,
            Date fetched,
            List<SolrInputDocument> batch,
            HttpSolrClient solrClient,
            String collection,
            Set<String> fetchedPids,
            Map<String, String> childToParent,
            String parentPid) throws SolrServerException, IOException {

        String pid = item.getString("pid");
        if (parentPid != null) {
            childToParent.put(pid, parentPid);
        }
        fetchedPids.add(pid);

        batch.add(toSolrDocument(item, fetched, parentPid));
        flushIfNeeded(solrClient, collection, batch);

        JSONArray children = item.optJSONArray("children");
        if (children == null) {
            return;
        }

        String childrenParentPid = parentPid != null ? parentPid : pid;
        for (int i = 0; i < children.length(); i++) {
            appendSolrDocuments(children.getJSONObject(i), fetched, batch, solrClient, collection, fetchedPids,
                    childToParent, childrenParentPid);
        }
    }

    private static void flushIfNeeded(HttpSolrClient solrClient, String collection, List<SolrInputDocument> batch)
            throws SolrServerException, IOException {

        if (batch.size() >= BATCH_SIZE) {
            writeBatch(solrClient, collection, batch);
            batch.clear();
        }
    }

    private static SolrInputDocument toSolrDocument(JSONObject item, Date fetched, String parentPid) {
        SolrInputDocument doc = new SolrInputDocument();

        String pid = item.getString("pid");
        doc.setField("id", pid);
        doc.setField("pid", pid);
        doc.setField("source", DEFAULT_SOURCE);
        doc.setField("fetched", fetched);
        doc.setField("type", parentPid == null ? "main" : "granularity");
        doc.setField("has_granularity", item.optJSONArray("children") != null);
        if (parentPid != null) {
            doc.setField("parent_id", parentPid);
        }

        putIfPresent(doc, item, "root_pid");
        putIfPresent(doc, item, "model");
        putIfPresent(doc, item, "title");
        putIfPresent(doc, item, "state");

        if (item.has("date_issued_year") && !item.isNull("date_issued_year")) {
            doc.setField("date_issued_year", item.getInt("date_issued_year"));
        }

        putArray(doc, item, "digital_library");
        putArray(doc, item, "assigned_licenses", "assigned_licenses");
        putArray(doc, item, "assigned_licences", "assigned_licenses");

        return doc;
    }

    private static void updateKrameriusState(
            HttpSolrClient solrClient,
            String collection,
            Set<String> fetchedPids,
            Map<String, String> childToParent) throws IOException, SolrServerException {

        String localApi = KConfiguration.getInstance().getConfiguration()
                .getString(CONFIG_CHECK_LOCAL_API, KConfiguration.getInstance().getConfiguration().getString("api.point"));
        if (localApi == null || localApi.trim().isEmpty()) {
            LOGGER.info("KAPP Kramerius comparison skipped; missing configuration key '" + CONFIG_CHECK_LOCAL_API
                    + "' or 'api.point'");
            return;
        }
        String version = KConfiguration.getInstance().getConfiguration().getString(CONFIG_CHECK_VERSION, DEFAULT_VERSION);

        long kstart = System.currentTimeMillis();
        LicenseAPIFetcher apiFetcher = LicenseAPIFetcher.Versions.valueOf(version).build(localApi, version, false);
        Map<String, Map<String, Object>> fetchedObject = apiFetcher.check(fetchedPids);
        LOGGER.info("KAPP Kramerius documents fetched; It took " + (System.currentTimeMillis() - kstart) + " ms");

        List<String> fetchedIds = new ArrayList<>(fetchedPids);
        int numberOfIterations = fetchedIds.size() / BATCH_SIZE;
        if (fetchedIds.size() % BATCH_SIZE != 0) {
            numberOfIterations = numberOfIterations + 1;
        }

        LOGGER.info(String.format("KAPP updating Kramerius state; Number of iteration %d", numberOfIterations));
        for (int i = 0; i < numberOfIterations; i++) {
            int from = i * BATCH_SIZE;
            int to = Math.min((i + 1) * BATCH_SIZE, fetchedIds.size());
            List<String> subList = fetchedIds.subList(from, to);

            Map<String, SolrInputDocument> changes = new HashMap<>();
            for (String pid : subList) {
                SolrInputDocument update = new SolrInputDocument();
                update.setField("id", pid);
                fillRealKrameriusFields(update, fetchedObject.get(pid));
                changes.put(pid, update);
            }

            SolrDocumentList kappDocuments = getById(solrClient, subList, collection);
            for (SolrDocument kappDocument : kappDocuments) {
                String pid = kappDocument.getFieldValue("id").toString();
                SolrInputDocument update = changes.get(pid);
                if (update == null) {
                    continue;
                }

                List<String> realKramLicenses = licensesFromFetchedObject(fetchedObject.get(pid));
                List<String> expectedLicenses = distinctValues(kappDocument.getFieldValues("assigned_licenses"));
                boolean dirty = appendLicenseActions(update, expectedLicenses, realKramLicenses);

                if (dirty) {
                    String parentPid = childToParent.get(pid);
                    if (parentPid != null) {
                        SolrInputDocument parentUpdate = changes.get(parentPid);
                        if (parentUpdate == null) {
                            parentUpdate = new SolrInputDocument();
                            parentUpdate.setField("id", parentPid);
                            if (fetchedObject.containsKey(parentPid)) {
                                fillRealKrameriusFields(parentUpdate, fetchedObject.get(parentPid));
                            }
                            changes.put(parentPid, parentUpdate);
                        }

                        List<String> parentActions = distinctValues(parentUpdate.getFieldValues("sync_actions"));
                        if (!parentActions.contains(KappSyncActionEnum.partial_change.name())) {
                            appendAction(parentUpdate, KappSyncActionEnum.partial_change);
                        }
                    }
                }
            }

            writeAtomicUpdates(solrClient, collection, changes.values());
        }
    }

    private static List<String> licensesFromFetchedObject(Map<String, Object> fetchedObject) {
        if (fetchedObject == null) {
            return new ArrayList<>();
        }

        List<String> licenses = (List<String>) fetchedObject.get(LicenseAPIFetcher.FETCHER_LICENSES_KEY);
        if (licenses == null) {
            return new ArrayList<>();
        }
        return licenses.stream().distinct().collect(Collectors.toList());
    }

    private static void fillRealKrameriusFields(SolrInputDocument update, Map<String, Object> fetchedObject) {
        if (fetchedObject == null) {
            return;
        }

        List<String> pidLicenses = (List<String>) fetchedObject.get(LicenseAPIFetcher.FETCHER_LICENSES_KEY);
        if (pidLicenses != null) {
            for (String license : pidLicenses) {
                SyncUtils.atomicAddDistinct(update, license, "real_kram_licenses");
            }
        }

        List<String> titles = (List<String>) fetchedObject.get(LicenseAPIFetcher.FETCHER_TITLES_KEY);
        if (titles != null) {
            for (String title : titles) {
                SyncUtils.atomicAddDistinct(update, title, "real_kram_titles_search");
            }
        }

        SyncUtils.atomicOneValSet(update, true, "real_kram_exists");

        String date = (String) fetchedObject.get(LicenseAPIFetcher.FETCHER_DATE_KEY);
        if (date != null) {
            SyncUtils.atomicOneValSet(update, date, "real_kram_date");
        }

        String model = (String) fetchedObject.get(LicenseAPIFetcher.FETCHER_MODEL_KEY);
        if (model != null) {
            SyncUtils.atomicOneValSet(update, model, "real_kram_model");
        }
    }

    private static boolean appendLicenseActions(
            SolrInputDocument update,
            List<String> expectedLicenses,
            List<String> realKramLicenses) {

        boolean changePublicOnsite = expectedLicenses.contains(LICENSE_ONSITE)
                && realKramLicenses.contains(LICENSE_PUBLIC);
        boolean changeOnsitePublic = expectedLicenses.contains(LICENSE_PUBLIC)
                && realKramLicenses.contains(LICENSE_ONSITE);
        boolean dirty = false;
        dirty = appendMissingLicenseAction(update, expectedLicenses, realKramLicenses, LICENSE_PUBLIC) || dirty;
        dirty = appendMissingLicenseAction(update, expectedLicenses, realKramLicenses, LICENSE_ONSITE) || dirty;

        if (!changePublicOnsite && !expectedLicenses.contains(LICENSE_PUBLIC)
                && realKramLicenses.contains(LICENSE_PUBLIC)) {
            appendAction(update, KappSyncActionEnum.remove_public);
            dirty = true;
        }

        if (!changeOnsitePublic && !expectedLicenses.contains(LICENSE_ONSITE)
                && realKramLicenses.contains(LICENSE_ONSITE)) {
            SyncUtils.atomicAddDistinct(update, KappSyncActionEnum.remove_onsite.name(), "sync_actions");
            if (!realKramLicenses.contains(LICENSE_PUBLIC)) {
                SyncUtils.atomicOneValSet(update, KappSyncActionEnum.remove_onsite.getValue(), "sync_sort");
            }
            dirty = true;
        }

        return dirty;
    }

    private static boolean appendMissingLicenseAction(
            SolrInputDocument update,
            List<String> expectedLicenses,
            List<String> realKramLicenses,
            String expectedLicense) {

        if (!expectedLicenses.contains(expectedLicense) || realKramLicenses.contains(expectedLicense)) {
            return false;
        }

        if (LICENSE_ONSITE.equals(expectedLicense) && realKramLicenses.contains(LICENSE_PUBLIC)) {
            appendAction(update, KappSyncActionEnum.change_public_onsite);
        } else if (LICENSE_PUBLIC.equals(expectedLicense) && realKramLicenses.contains(LICENSE_ONSITE)) {
            appendAction(update, KappSyncActionEnum.change_onsite_public);
        } else if (LICENSE_ONSITE.equals(expectedLicense)) {
            appendAction(update, KappSyncActionEnum.add_onsite);
        } else {
            appendAction(update, KappSyncActionEnum.add_public);
        }

        return true;
    }

    private static void appendAction(SolrInputDocument update, KappSyncActionEnum action) {
        SyncUtils.atomicAddDistinct(update, action.name(), "sync_actions");
        SyncUtils.atomicOneValSet(update, action.getValue(), "sync_sort");
    }

    private static void writeAtomicUpdates(
            HttpSolrClient solrClient,
            String collection,
            Collection<SolrInputDocument> updates) {

        if (updates == null || updates.isEmpty()) {
            return;
        }

        UpdateRequest request = new UpdateRequest();
        updates.stream()
                .filter(update -> update.size() > 1)
                .forEach(request::add);
        if (request.getDocuments() == null || request.getDocuments().isEmpty()) {
            return;
        }
        LOGGER.fine(String.format("KAPP update batch with size %s", request.getDocuments().size()));
        try {
            UpdateResponse response = request.process(solrClient, collection);
            LOGGER.fine("qtime:" + response.getQTime());
            solrClient.commit(collection);
        } catch (SolrServerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static SolrDocumentList getById(HttpSolrClient solrClient, List<String> ids, String collection)
            throws SolrServerException, IOException {

        int getBatch = KConfiguration.getInstance().getConfiguration().getInt(CONFIG_QUERY_BY_ID,
                KConfiguration.getInstance().getConfiguration().getInt("sdnnt.query.byid", 20));
        return SyncUtils.getById(solrClient, ids, collection, getBatch);
    }

    private static List<String> distinctValues(Collection<Object> fieldValues) {
        return SyncUtils.distinctValues(fieldValues);
    }

    private static void putIfPresent(SolrInputDocument doc, JSONObject item, String field) {
        if (item.has(field) && !item.isNull(field)) {
            doc.setField(field, item.get(field));
        }
    }

    private static void putArray(SolrInputDocument doc, JSONObject item, String field) {
        putArray(doc, item, field, field);
    }

    private static void putArray(SolrInputDocument doc, JSONObject item, String sourceField, String targetField) {
        if (doc.getFieldValue(targetField) != null) {
            return;
        }

        JSONArray array = item.optJSONArray(sourceField);
        if (array == null) {
            return;
        }

        List<String> values = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            values.add(array.getString(i));
        }
        doc.setField(targetField, values);
    }

    private static void writeBatch(HttpSolrClient solrClient, String collection, List<SolrInputDocument> docs)
            throws SolrServerException, IOException {

        UpdateRequest request = new UpdateRequest();
        docs.forEach(request::add);
        request.process(solrClient, collection);
        solrClient.commit(collection);
        LOGGER.info("KAPP Solr batch committed, size: " + docs.size());
    }
}
