package cz.inovatika.kramerius.services.workers.batch;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.config.ProcessConfigParser;
import cz.inovatika.kramerius.services.workers.batch.impl.CopyTransformation;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

public class BatchTest {

    @Test
    public void testBatch() throws ParserConfigurationException, IOException, SAXException, TransformerException {
        InputStream solrResult = BatchTest.class.getResourceAsStream("result.xml");


        Assert.assertNotNull(solrResult);
        Document document = XMLUtils.parseDocument(solrResult);
        Element result = XMLUtils.findElement(document.getDocumentElement(), "result");
        Assert.assertNotNull(result);

        InputStream configStream = BatchTest.class.getResourceAsStream("config.xml");
        Document configDoc = XMLUtils.parseDocument(configStream);
        ProcessConfig config = ProcessConfigParser.parse(configDoc.getDocumentElement());


        Batch batch = new Batch(config, new CopyTransformation(), null);
        Document batchDocument = batch.create(result);

        XMLUtils.print(batchDocument, System.out);
    }
}
