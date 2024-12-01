package cz.incad.kramerius.fedora.utils;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

public class CDKUtilsTest extends TestCase {

    public void testFindCDKLeader() throws IOException, SAXException, ParserConfigurationException {
        InputStream resourceAsStream = CDKUtilsTest.class.getResourceAsStream("cdksolrfile.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);
        String cdkLeader = CDKUtils.findCDKLeader(document.getDocumentElement());
        Assert.assertTrue(cdkLeader != null);
        Assert.assertTrue(cdkLeader.equals("vc:700a6cc7-1e5c-4487-b111-c425cbc51091"));
    }

    public void testFindSources() throws IOException, SAXException, ParserConfigurationException {
        InputStream resourceAsStream = CDKUtilsTest.class.getResourceAsStream("cdksolrfile.xml");
        Document document = XMLUtils.parseDocument(resourceAsStream);
        List<String> sources = CDKUtils.findSources(document.getDocumentElement());
        Assert.assertTrue(sources.size() == 1);
        Assert.assertTrue(sources.get(0).equals("vc:700a6cc7-1e5c-4487-b111-c425cbc51091"));
    }


}
