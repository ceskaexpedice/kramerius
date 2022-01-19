package cz.incad.kramerius.utils;

import junit.framework.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Arrays;
import java.util.List;

public class DNNTBatchUtilsTest {


    @Test
    public void testBatchUtils() throws ParserConfigurationException, TransformerException {
        List<String> pids  = Arrays.asList(
            "uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22",
            "uuid:40d779fa-b2d9-4a85-857b-ee4febc477f1",
            "uuid:948250f6-6cb8-4fff-915d-67797655024d",
            "uuid:1a5259e1-b93e-4f9a-a9ff-60cf433dcc3b",
            "uuid:0789cc9d-6fbd-4126-8aa5-46f6ed8205f8",
            "uuid:e24312f0-f83a-4c0c-87be-6ae1cccb1208",
            "uuid:ecb7b0e4-59fb-4a50-9cf3-387a386b055e",
            "uuid:7d42a902-4a7c-4328-9317-484c3ad17e18"
        );
        Document covid = DNNTBatchUtils.createLabelsBatch(pids, "COVID", true);
        Assert.assertTrue(covid.getDocumentElement().getNodeName().equals("add"));

        NodeList childNodes = covid.getDocumentElement().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node doc = childNodes.item(i);
            Assert.assertTrue(doc.getNodeName().equals("doc"));
            String pid = XMLUtils.findElement((Element) doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String name = element.getAttribute("name");
                    return name.equals("pid");
                }
            }).getTextContent();
            Assert.assertTrue(pids.contains(pid));

        }


        covid = DNNTBatchUtils.createLabelsBatch(pids, "COVID", false);
        Assert.assertTrue(covid.getDocumentElement().getNodeName().equals("add"));

        childNodes = covid.getDocumentElement().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node doc = childNodes.item(i);
            Assert.assertTrue(doc.getNodeName().equals("doc"));
            String pid = XMLUtils.findElement((Element) doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String name = element.getAttribute("name");
                    return name.equals("pid");
                }
            }).getTextContent();
            Assert.assertTrue(pids.contains(pid));

            String dnntLabels = XMLUtils.findElement((Element) doc, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    String name = element.getAttribute("name");
                    return name.equals("licenses");
                }
            }).getTextContent();
            Assert.assertEquals(dnntLabels, "COVID");
        }

    }


}
