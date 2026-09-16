package cz.incad.kramerius.statistics.accesslogs.solr;

import cz.incad.kramerius.utils.DCUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class LogFieldsTest {
    private static final String FOXML_FILE = "test-foxml.xml";

    private Document loadFoXml() throws Exception {
        try (InputStream is = getClass().getResourceAsStream(FOXML_FILE)) {
            assertNotNull("Test XML not found: " + FOXML_FILE, is);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(is);
        }
    }

    @Test
    public void testGetDC() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        Document dc = logFields.getDC();
        assertNotNull(dc);
        assertNotNull(dc.getDocumentElement());
        Element root = dc.getDocumentElement();
        assertEquals("http://www.openarchives.org/OAI/2.0/oai_dc/", root.getNamespaceURI());
        assertEquals("dc", root.getLocalName());
        NodeList languages = root.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "language");
        assertEquals(1, languages.getLength());
        assertEquals("cze", languages.item(0).getTextContent());
        NodeList titles = root.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
        assertEquals(2, titles.getLength());
        assertEquals("Illustrovaný průvodce Jičín a Prachovské skály", titles.item(0).getTextContent());
        assertEquals("Jičín a Prachovské skály", titles.item(1).getTextContent());
    }

    @Test
    public void testGetDCDate() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        String date = logFields.getDCDate();
        assertEquals("1922", date);
    }

    @Test
    public void testGetDCLanguage() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        String language = logFields.getDCLanguage();
        assertEquals("cze", language);
    }

    @Test
    public void testGetDCTitle() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        String title = logFields.getDCTitle();
        assertEquals("Illustrovaný průvodce Jičín a Prachovské skály", title);
    }

    @Test
    public void testGetDCCreators() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        String[] dcCreators = logFields.getDCCreators();
        assertEquals("Klub československých turistů (Jičín, Česko)", dcCreators[0]);
    }

    @Test
    public void testGetDCPublishers() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        String[] dcPublishers = logFields.getDCPublishers();
        assertEquals("Nákladem Ciz. kom. při Odboru K.Č.S.T.", dcPublishers[0]);
    }

    @Test
    public void testGetMods() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        Document mods = logFields.getMods();
        assertNotNull(mods);
        assertNotNull(mods.getDocumentElement());
        Element root = mods.getDocumentElement();
        assertEquals("http://www.loc.gov/mods/v3", root.getNamespaceURI());
        assertEquals("modsCollection", root.getLocalName());
        NodeList identifiers = root.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "identifier");
        assertEquals(2, identifiers.getLength());
        assertEquals("uuid", identifiers.item(0).getAttributes().getNamedItem("type").getNodeValue());
        assertEquals("5035a48a-5e2e-486c-8127-2fa650842e46", identifiers.item(0).getTextContent());
        assertEquals("ccnb", identifiers.item(1).getAttributes().getNamedItem("type").getNodeValue());
        assertEquals("cnb000573734", identifiers.item(1).getTextContent());
    }

    @Test
    public void testGetModsIdentifiers() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        Map<String, List<String>> modsIdentifiers = logFields.getModsIdentifiers();
        assertEquals(2, modsIdentifiers.size());
    }

    @Test
    public void testGetRelsExt() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        Document relsExt = logFields.getRelsExt();
        assertNotNull(relsExt);
        assertNotNull(relsExt.getDocumentElement());
        Element root = relsExt.getDocumentElement();
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#", root.getNamespaceURI());
        assertEquals("RDF", root.getLocalName());
        NodeList licenses = root.getElementsByTagNameNS("http://www.nsdl.org/ontologies/relationships#", "license");
        assertEquals(2, licenses.getLength());
        assertEquals("dnnto", licenses.item(0).getTextContent());
        assertEquals("onsite-sheetmusic", licenses.item(1).getTextContent());
        NodeList models = root.getElementsByTagNameNS("info:fedora/fedora-system:def/model#", "hasModel");
        assertEquals(1, models.getLength());
        assertEquals("info:fedora/model:monograph", models.item(0).getAttributes().getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getNodeValue());
    }

    @Test
    public void testGetRelsExtModel() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        String relsExtModel = logFields.getRelsExtModel();
        assertEquals("monograph", relsExtModel);
    }

    @Test
    public void testDocumentsAreCached() throws Exception {
        Document foXml = loadFoXml();
        LogFields logFields = new LogFields(foXml);
        Document dc1 = logFields.getDC();
        Document dc2 = logFields.getDC();
        assertSame(dc1, dc2);
        Document mods1 = logFields.getMods();
        Document mods2 = logFields.getMods();
        assertSame(mods1, mods2);
        Document rels1 = logFields.getRelsExt();
        Document rels2 = logFields.getRelsExt();
        assertSame(rels1, rels2);
    }
}