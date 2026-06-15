package cz.incad.kramerius.plugin;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CDKDeleteLibraryTest {

    @Test
    public void deleteBatchCreatesSolrDeleteXml() throws Exception {
        Document batch = CDKDeleteLibrary.deleteBatch(Arrays.asList(
                "uuid:root!uuid:pid1",
                "uuid:root!uuid:pid2"));

        Assert.assertEquals(
                "<delete><id>uuid:root!uuid:pid1</id><id>uuid:root!uuid:pid2</id></delete>",
                compactXml(batch));
    }

    @Test
    public void updateBatchRemovesLibrarySpecificFieldsAndKeepsOtherLibraries() throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("cdk.collection", Arrays.asList("vkol", "mzk"));
        doc.put("cdk.leader", "vkol");
        doc.put("cdk.licenses", Arrays.asList("vkol_dnnto", "vkol_onsite", "mzk_public"));
        doc.put("cdk.contains_licenses", Arrays.asList("vkol_onsite", "mzk_public"));
        doc.put("cdk.licenses_of_ancestors", Arrays.asList("vkol_dnnto", "mzk_onsite"));
        doc.put("cdk.has_tiles_vkol", "true");
        doc.put("cdk.count_page_vkol", "10");
        doc.put("cdk.has_tiles_mzk", "false");

        CDKDeleteLibrary.UpdateItem item = CDKDeleteLibrary.updateItem(
                "vkol",
                "uuid:root!uuid:pid",
                doc,
                Arrays.asList("vkol", "mzk"));

        Document batch = CDKDeleteLibrary.updateBatch("vkol", "compositeId", Arrays.asList(item));

        Assert.assertEquals(
                "<add><doc>"
                        + "<field name=\"compositeId\">uuid:root!uuid:pid</field>"
                        + "<field name=\"cdk.collection\" update=\"remove\">vkol</field>"
                        + "<field name=\"cdk.count_page_vkol\" null=\"true\" update=\"set\"/>"
                        + "<field name=\"cdk.has_tiles_vkol\" null=\"true\" update=\"set\"/>"
                        + "<field name=\"cdk.licenses\" update=\"set\">mzk_public</field>"
                        + "<field name=\"licenses\" update=\"set\">public</field>"
                        + "<field name=\"cdk.contains_licenses\" update=\"set\">mzk_public</field>"
                        + "<field name=\"contains_licenses\" update=\"set\">public</field>"
                        + "<field name=\"cdk.licenses_of_ancestors\" update=\"set\">mzk_onsite</field>"
                        + "<field name=\"licenses_of_ancestors\" update=\"set\">onsite</field>"
                        + "<field name=\"cdk.leader\" update=\"set\">mzk</field>"
                        + "</doc></add>",
                compactXml(batch));

        Assert.assertEquals("add", batch.getDocumentElement().getNodeName());
        Assert.assertEquals(Arrays.asList("mzk"), fieldValues(batch, "cdk.leader"));
        Assert.assertEquals(Arrays.asList("vkol"), fieldValues(batch, "cdk.collection"));
        Assert.assertEquals(Arrays.asList("mzk_public"), fieldValues(batch, "cdk.licenses"));
        Assert.assertEquals(Arrays.asList("public"), fieldValues(batch, "licenses"));
        Assert.assertEquals(Arrays.asList("mzk_public"), fieldValues(batch, "cdk.contains_licenses"));
        Assert.assertEquals(Arrays.asList("public"), fieldValues(batch, "contains_licenses"));
        Assert.assertEquals(Arrays.asList("mzk_onsite"), fieldValues(batch, "cdk.licenses_of_ancestors"));
        Assert.assertEquals(Arrays.asList("onsite"), fieldValues(batch, "licenses_of_ancestors"));

        assertNullSet(batch, "cdk.count_page_vkol");
        assertNullSet(batch, "cdk.has_tiles_vkol");
        Assert.assertTrue(fieldValues(batch, "cdk.has_tiles_mzk").isEmpty());
    }

    @Test
    public void updateBatchClearsLicenseFieldsWhenNoCdkValuesRemain() throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("cdk.collection", Arrays.asList("vkol", "mzk"));
        doc.put("cdk.licenses", Arrays.asList("vkol_dnnto"));

        CDKDeleteLibrary.UpdateItem item = CDKDeleteLibrary.updateItem(
                "vkol",
                "uuid:root!uuid:pid",
                doc,
                Arrays.asList("vkol", "mzk"));

        Document batch = CDKDeleteLibrary.updateBatch("vkol", "compositeId", Arrays.asList(item));

        assertNullSet(batch, "cdk.licenses");
        assertNullSet(batch, "licenses");
    }

    private static List<String> fieldValues(Document batch, String fieldName) {
        return XMLUtils.getElementsRecursive(batch.getDocumentElement(), element ->
                "field".equals(element.getNodeName()) && fieldName.equals(element.getAttribute("name")))
                .stream()
                .map(Element::getTextContent)
                .filter(value -> value != null && !value.isEmpty())
                .collect(Collectors.toList());
    }

    private static void assertNullSet(Document batch, String fieldName) {
        List<Element> fields = XMLUtils.getElementsRecursive(batch.getDocumentElement(), element ->
                "field".equals(element.getNodeName()) && fieldName.equals(element.getAttribute("name")));
        Assert.assertEquals(1, fields.size());
        Assert.assertEquals("set", fields.get(0).getAttribute("update"));
        Assert.assertEquals("true", fields.get(0).getAttribute("null"));
    }

    private static String compactXml(Document batch) throws Exception {
        StringWriter writer = new StringWriter();
        XMLUtils.print(batch, writer);
        return writer.toString()
                .replaceFirst("^<\\?xml[^>]*>", "")
                .replaceAll(">\\s+<", "><")
                .replaceAll("\\s*/>", "/>")
                .trim();
    }
}
