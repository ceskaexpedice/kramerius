package cz.incad.kramerius.statistics.accesslogs.solr;

import cz.incad.kramerius.statistics.accesslogs.AccessLogFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure parts of the schema guard: what is missing and what gets sent to the Schema API. */
public class SolrLogsSchemaGuardTest {

    @Test
    public void readsFieldNamesFromTheSchemaApiAnswer() {
        JSONObject answer = new JSONObject("{\"responseHeader\":{\"status\":0},\"fields\":["
                + "{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"stream_name\",\"type\":\"string\"}]}");
        Assert.assertEquals(new HashSet<>(Arrays.asList("id", "stream_name")), SolrLogsSchemaGuard.fieldNames(answer));
    }

    @Test
    public void missingFieldsAreTheFormat1FieldsTheSchemaLacks() {
        Set<String> existing = new HashSet<>(Arrays.asList("id", "pid", "stream_name", "log_format"));
        List<AccessLogFormat.Field> missing = SolrLogsSchemaGuard.missingFields(existing);
        Set<String> names = new HashSet<>();
        for (AccessLogFormat.Field f : missing) {
            names.add(f.getName());
        }
        Set<String> expected = new HashSet<>(AccessLogFormat.format1FieldNames());
        expected.removeAll(existing);
        Assert.assertEquals(expected, names);
        Assert.assertTrue(SolrLogsSchemaGuard.missingFields(AccessLogFormat.format1FieldNames()).isEmpty());
    }

    @Test
    public void fullRecordIsTriedAgainOnlyAfterTheIntervalSinceTheRejection() {
        long rejectedAt = 1000L;
        Assert.assertFalse("fresh rejection",
                SolrLogsSchemaGuard.recheckDue(true, rejectedAt, rejectedAt + SolrLogsSchemaGuard.RECHECK_INTERVAL_MS - 1));
        Assert.assertTrue("interval passed",
                SolrLogsSchemaGuard.recheckDue(true, rejectedAt, rejectedAt + SolrLogsSchemaGuard.RECHECK_INTERVAL_MS));
        Assert.assertFalse("no rejection, nothing to recheck",
                SolrLogsSchemaGuard.recheckDue(false, rejectedAt, rejectedAt + 2 * SolrLogsSchemaGuard.RECHECK_INTERVAL_MS));
    }

    @Test
    public void addFieldCommandDeclaresFieldsLikeFieldsXml() {
        JSONObject command = SolrLogsSchemaGuard.addFieldsCommand(SolrLogsSchemaGuard.missingFields(Collections.<String>emptySet()));
        JSONArray add = command.getJSONArray("add-field");
        Assert.assertEquals(AccessLogFormat.FORMAT_1_FIELDS.size(), add.length());
        JSONObject intent = null;
        JSONObject format = null;
        for (int i = 0; i < add.length(); i++) {
            JSONObject def = add.getJSONObject(i);
            Assert.assertTrue(def.getBoolean("indexed"));
            Assert.assertTrue(def.getBoolean("stored"));
            Assert.assertTrue(def.getBoolean("docValues"));
            if ("hrh_kramerius_intent".equals(def.getString("name"))) {
                intent = def;
            }
            if ("log_format".equals(def.getString("name"))) {
                format = def;
            }
        }
        Assert.assertNotNull(intent);
        Assert.assertTrue(intent.getBoolean("multiValued"));
        Assert.assertEquals("string", intent.getString("type"));
        Assert.assertNotNull(format);
        Assert.assertFalse(format.getBoolean("multiValued"));
        Assert.assertEquals("int", format.getString("type"));
    }
}
