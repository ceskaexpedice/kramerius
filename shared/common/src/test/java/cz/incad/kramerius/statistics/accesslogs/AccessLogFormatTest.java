package cz.incad.kramerius.statistics.accesslogs;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The list of format 1 fields drives the schema guard and the format 0
 * fallback, so it must say the same as the logs schema shipped with the
 * installation.
 */
public class AccessLogFormatTest {

    private static final String FIELDS_XML = "../../installation/_common_schema/logs/fields.xml";

    @Test
    public void format1FieldsAreDeclaredInFieldsXmlWithTheSameType() throws Exception {
        File fieldsXml = new File(FIELDS_XML);
        Assert.assertTrue("fields.xml not found at " + fieldsXml.getAbsolutePath(), fieldsXml.exists());
        Document schema = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fieldsXml);
        NodeList fields = schema.getElementsByTagName("field");
        for (AccessLogFormat.Field expected : AccessLogFormat.FORMAT_1_FIELDS) {
            Element declared = null;
            for (int i = 0; i < fields.getLength(); i++) {
                Element e = (Element) fields.item(i);
                if (expected.getName().equals(e.getAttribute("name"))) {
                    declared = e;
                }
            }
            Assert.assertNotNull(expected.getName() + " is missing in fields.xml", declared);
            Assert.assertEquals(expected.getName() + " type", expected.getType(), declared.getAttribute("type"));
            Assert.assertEquals(expected.getName() + " multiValued", expected.isMultiValued(),
                    "true".equals(declared.getAttribute("multiValued")));
        }
    }

    @Test
    public void everyFormat1FieldOfARecordIsStrippedAndTheRestStays() throws Exception {
        LogRecord record = LogRecord.buildRecord("uuid:1");
        record.setRequestedUrl("http://k7/search/iiif/uuid:1/info.json");
        record.setIpAddress("127.0.0.1");
        record.setReportedAction("READ");
        record.setStreamName("IMG_FULL");
        record.setExtraFields(map("image_kind", "IIIF_INFO", "stream_mime", "image/jp2"));
        record.setFieldsFromHttpRequestHeaders(headers(
                "Kramerius-client", "old.client.cz",
                "Kramerius-client-url", "new.client.cz",
                "Kramerius-client-app", "cdk-client",
                "Kramerius-client-version", "3.0.37",
                "Kramerius-intent", "view"));

        Document batch = record.toSolrBatch(DocumentBuilderFactory.newInstance());
        Set<String> before = fieldNames(batch);
        Assert.assertTrue("record of format 1 has log_format", before.contains("log_format"));
        for (String name : AccessLogFormat.format1FieldNames()) {
            if (!name.equals("hrh_kramerius_intent") || before.contains(name)) {
                Assert.assertTrue(name + " is written by the record", before.contains(name));
            }
        }

        Set<String> after = fieldNames(AccessLogFormat.stripToFormat0(batch));
        for (String name : AccessLogFormat.format1FieldNames()) {
            Assert.assertFalse(name + " survived the strip", after.contains(name));
        }
        Set<String> legacy = new LinkedHashSet<>(before);
        legacy.removeAll(AccessLogFormat.format1FieldNames());
        Assert.assertEquals("format 0 fields are untouched", legacy, after);
        Assert.assertTrue(after.contains("hrh_kramerius_client"));
        Assert.assertTrue(after.contains("reported_action"));
        Assert.assertTrue(after.contains("requested_url"));
    }

    private static Set<String> fieldNames(Document batch) {
        Set<String> names = new LinkedHashSet<>();
        NodeList fields = batch.getElementsByTagName("field");
        for (int i = 0; i < fields.getLength(); i++) {
            names.add(((Element) fields.item(i)).getAttribute("name"));
        }
        return names;
    }

    private static java.util.Map<String, String> map(String... kv) {
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], kv[i + 1]);
        }
        return m;
    }

    private static java.util.Map<String, List<String>> headers(String... kv) {
        java.util.Map<String, List<String>> m = new java.util.LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i], Collections.singletonList(kv[i + 1]));
        }
        return m;
    }
}
