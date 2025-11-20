package cz.inovatika.kramerius.services.workers.batch;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.config.ProcessConfigParser;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class BatchTest {

    @Test
    public void testInsertBatch() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream solrResult = BatchTest.class.getResourceAsStream("result.xml");

        Assert.assertNotNull(solrResult);
        Document document = XMLUtils.parseDocument(solrResult);
        Element result = XMLUtils.findElement(document.getDocumentElement(), "result");
        Assert.assertNotNull(result);

        InputStream configStream = BatchTest.class.getResourceAsStream("config.xml");
        Document configDoc = XMLUtils.parseDocument(configStream);
        ProcessConfig config = ProcessConfigParser.parse(configDoc.getDocumentElement());

        UpdateSolrBatchCreator updateSolrBatchCreator = new UpdateSolrBatchCreator(config, result, null);
        Document batchForInsert = updateSolrBatchCreator.createBatchForInsert();
        List<Element> docs = XMLUtils.getElementsRecursive(batchForInsert.getDocumentElement(), paramElement -> paramElement.getNodeName().equals("doc"));
        for (Element doc : docs) {
            List<Element> fields = XMLUtils.getElements(doc, paramElement -> paramElement.getNodeName().equals("field"));
            for (Element field : fields) {
                String attribute = field.getAttribute("update");
                Assert.assertFalse(StringUtils.isAnyString(attribute));
            }
        }
   }

    @Test
    public void testUpdateBatch() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream solrResult = BatchTest.class.getResourceAsStream("result.xml");

        Assert.assertNotNull(solrResult);
        Document document = XMLUtils.parseDocument(solrResult);
        Element result = XMLUtils.findElement(document.getDocumentElement(), "result");
        Assert.assertNotNull(result);

        InputStream configStream = BatchTest.class.getResourceAsStream("config.xml");
        Document configDoc = XMLUtils.parseDocument(configStream);
        ProcessConfig config = ProcessConfigParser.parse(configDoc.getDocumentElement());


        UpdateSolrBatchCreator updateSolrBatchCreator = new UpdateSolrBatchCreator(config, result, null);
        Document batchForUpdate = updateSolrBatchCreator.createBatchForUpdate();
        List<Element> docs = XMLUtils.getElementsRecursive(batchForUpdate.getDocumentElement(), paramElement -> paramElement.getNodeName().equals("doc"));
        for (Element doc : docs) {
            List<Element> fields = XMLUtils.getElements(doc, paramElement -> paramElement.getNodeName().equals("field"));
            for (Element field : fields) {
                String name = field.getAttribute("name");
                String attribute = field.getAttribute("update");
                if (name.equals("compositeId")) {
                    Assert.assertFalse(StringUtils.isAnyString(attribute));
                } else {
                    Assert.assertEquals("set",attribute);
                }
            }
        }


        XMLUtils.print(batchForUpdate, System.out);
    }
}
