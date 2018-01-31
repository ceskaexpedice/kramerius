package cz.incad.kramerius.indexer;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.AsynchronousServerSocketChannel;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import junit.framework.TestCase;

public class SolrOperationsTest extends TestCase {

    @Ignore
    public void testPrepareOneDoc() throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, TransformerException {
        String docval = "<doc><field name=\"pid_path\">uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22</field><field name=\"parent_pid\">uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22</field><field name=\"rels_ext_index\">0</field><field name=\"model_path\">monograph</field><field name=\"root_title\">Když slunéčko svítí : verše</field><field name=\"root_pid\">uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22</field><field name=\"level\">0</field><field name=\"datum_str\">1889</field><field name=\"rok\">1889</field><field name=\"datum_begin\">1889</field><field name=\"datum_end\">1889</field></doc>";
        String prepareDocForIndexing = SolrOperations.prepareDocForIndexing(false, docval);
        Document parsed = XMLUtils.parseDocument(new StringReader(prepareDocForIndexing));
        Assert.assertNotNull(parsed);
        Assert.assertTrue(parsed.getDocumentElement().getNodeName().equals("add"));
    }

    public void testPrepareOneDocWithCompositeId() throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, TransformerException {
        KConfiguration.getInstance().getConfiguration().setProperty("indexer.compositeId", true);
        String docval = "<doc><field name=\"PID\">uuid:8040afd0-349e-4fa3-a121-c03fe4d27d46</field><field name=\"fedora.model\">periodical</field><field name=\"document_type\">title</field><field name=\"document_type\">text</field><field name=\"document_type\">periodical</field><field boost=\"2.0\" name=\"dc.title\">Le Journal de Charleroi</field><field name=\"title_sort\">LE JOURNAL DE H|ARLEROI</field><field name=\"dc.identifier\">uuid:8040afd0-349e-4fa3-a121-c03fe4d27d46</field><field name=\"status\">Active</field><field name=\"handle\"/><field name=\"created_date\">2014-10-27T11:47:23.550Z</field><field name=\"modified_date\">2016-12-22T09:20:17.895Z</field><field name=\"dostupnost\">public</field><field name=\"dc.identifier\">8040afd0-349e-4fa3-a121-c03fe4d27d46</field><field name=\"issn\"/><field name=\"mdt\"/><field name=\"ddt\"/><field name=\"browse_title\">LE JOURNAL DE H|ARLEROI##Le Journal de Charleroi</field>"
                +"<field name=\"pid_path\">uuid:8040afd0-349e-4fa3-a121-c03fe4d27d46</field><field name=\"parent_pid\">uuid:8040afd0-349e-4fa3-a121-c03fe4d27d46</field><field name=\"rels_ext_index\">0</field><field name=\"model_path\">periodical</field><field name=\"root_title\">Le Journal de Charleroi</field><field name=\"root_pid\">uuid:8040afd0-349e-4fa3-a121-c03fe4d27d46</field><field name=\"level\">0</field><field name=\"datum_str\">1848-1950</field><field name=\"datum_begin\">1848</field><field name=\"datum_end\">1950</field></doc>";
        String prepareDocForIndexing = SolrOperations.prepareDocForIndexing(true, docval);
        Document parsed = XMLUtils.parseDocument(new StringReader(prepareDocForIndexing));
        
        Element compositeId = XMLUtils.findElement(parsed.getDocumentElement(),new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element element) {
                String nodeName = element.getNodeName();
                if (nodeName.equals("field")) {
                    String attName = element.getAttribute("name");
                    return (attName != null && attName.equals("compositeId"));
                }
                return false;
            }
        });
        Assert.assertNotNull(compositeId);
    }
}
