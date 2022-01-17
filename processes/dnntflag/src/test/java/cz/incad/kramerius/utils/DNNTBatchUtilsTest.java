package cz.incad.kramerius.utils;

import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.Assert;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.Arrays;
import java.util.List;

public class DNNTBatchUtilsTest {


    @Test
    public void testBatchUtils() throws ParserConfigurationException, TransformerException {
        //List<String> pidsAndVersions = Arrays.asList("uuid:ab7e5a25-bddb-11e0-bff9-0016e6840575",  "uuid:91721f50-80bb-11e0-8e3a-000d606f5dc6");

        List<String> pidsAndVersions  = Arrays.asList(
            "uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22",
            "uuid:40d779fa-b2d9-4a85-857b-ee4febc477f1",
            "uuid:948250f6-6cb8-4fff-915d-67797655024d",
                "uuid:1a5259e1-b93e-4f9a-a9ff-60cf433dcc3b",
                "uuid:0789cc9d-6fbd-4126-8aa5-46f6ed8205f8",
                "uuid:e24312f0-f83a-4c0c-87be-6ae1cccb1208",
                "uuid:ecb7b0e4-59fb-4a50-9cf3-387a386b055e",
                "uuid:7d42a902-4a7c-4328-9317-484c3ad17e18"
        );
        Document covid = DNNTBatchUtils.createLabeledDNNT(pidsAndVersions, "COVID", true, true);
        XMLUtils.print(covid, System.out);

//        Document covid = DNNTBatchUtils.createLabeledDNNT(pidsAndVersions, "COVID", true, true);
//        List<Element> docs = XMLUtils.getElements(covid.getDocumentElement(), new XMLUtils.ElementsFilter() {
//            @Override
//            public boolean acceptElement(Element element) {
//                return element.getNodeName().equals("doc");
//            }
//        });
//
//        XMLUtils.print(covid.getDocumentElement(), System.out);
//
//        Assert.assertTrue(docs.size() == 2);
//
//        checkBatch(docs.get(0), "uuid:ab7e5a25-bddb-11e0-bff9-0016e6840575", "COVID", "add");
//        checkBatch(docs.get(1), "uuid:91721f50-80bb-11e0-8e3a-000d606f5dc6", "COVID", "add");
//
//        KConfiguration.getInstance().getConfiguration().setProperty("dnnt.solr.label.addcommand","add-distinct");
//
//        covid = DNNTBatchUtils.createLabeledDNNT(pidsAndVersions, "COVID", true, true);
//        docs = XMLUtils.getElements(covid.getDocumentElement(), new XMLUtils.ElementsFilter() {
//            @Override
//            public boolean acceptElement(Element element) {
//                return element.getNodeName().equals("doc");
//            }
//        });
//
//        checkBatch(docs.get(0), "uuid:ab7e5a25-bddb-11e0-bff9-0016e6840575", "COVID", "add-distinct");
//        checkBatch(docs.get(1), "uuid:91721f50-80bb-11e0-8e3a-000d606f5dc6", "COVID", "add-distinct");
//
//        covid = DNNTBatchUtils.createLabeledDNNT(pidsAndVersions, "COVID", false, true);
//        docs = XMLUtils.getElements(covid.getDocumentElement(), new XMLUtils.ElementsFilter() {
//            @Override
//            public boolean acceptElement(Element element) {
//                return element.getNodeName().equals("doc");
//            }
//        });
//        checkBatch(docs.get(0), "uuid:ab7e5a25-bddb-11e0-bff9-0016e6840575", "COVID", "removeregex");
//        checkBatch(docs.get(1), "uuid:91721f50-80bb-11e0-8e3a-000d606f5dc6", "COVID", "removeregex");
//
//        KConfiguration.getInstance().getConfiguration().setProperty("dnnt.solr.label.removecommand","remove");
//        covid = DNNTBatchUtils.createLabeledDNNT(pidsAndVersions, "COVID", false, true);
//        docs = XMLUtils.getElements(covid.getDocumentElement(), new XMLUtils.ElementsFilter() {
//            @Override
//            public boolean acceptElement(Element element) {
//                return element.getNodeName().equals("doc");
//            }
//        });
//
//        checkBatch(docs.get(0), "uuid:ab7e5a25-bddb-11e0-bff9-0016e6840575", "COVID", "remove");
//        checkBatch(docs.get(1), "uuid:91721f50-80bb-11e0-8e3a-000d606f5dc6", "COVID", "remove");
    }

    private void checkBatch(Element doc, String pid, String dnntLabels, String dnntLabelsOp) {
        Assert.assertTrue(XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals("PID") ;
            }
        }).getTextContent().equals(pid));

        Assert.assertTrue(XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals("dnnt");
            }
        }).getTextContent().equals("true"));

        Assert.assertTrue(XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals("dnnt");
            }
        }).getAttribute("update").equals("set"));


        Assert.assertTrue(XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals("dnnt-labels");
            }
        }).getTextContent().equals(dnntLabels));

        Assert.assertTrue(XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getAttribute("name").equals("dnnt-labels");
            }
        }).getAttribute("update").equals(dnntLabelsOp));
    }
}
