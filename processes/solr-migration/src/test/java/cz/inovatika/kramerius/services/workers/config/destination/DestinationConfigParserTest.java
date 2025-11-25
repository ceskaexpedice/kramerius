package cz.inovatika.kramerius.services.workers.config.destination;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.lang.model.element.Element;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class DestinationConfigParserTest {

    public static final String CONFIG=
            "        <destination>\n" +
            "            <!--\n" +
            "            <url>http://localhost:8983/solr/search/update</url>\n" +
            "            -->\n" +
            "            <url>http://localhost:8983/solr/logs/update</url>\n" +
            "            <!-- on index -->\n" +
            "            <onindex>\n" +
            "                <!-- remove from batch document -->\n" +
            "                <remove.dest.field>\n" +
            "                    <field name=\"_version_\"></field>\n" +
            "                    <field name=\"id_all\"></field>\n" +
            "                    <field name=\"licenses.facet\"></field>\n" +
            "                    <field name=\"licenses\"></field>\n" +
            "                </remove.dest.field>\n" +
            "            </onindex>\n" +
            "\n" +
            "            <onupdate>\n" +
            "                <fieldlist>id ip_address pid date.str licenses</fieldlist>\n" +
            "\n" +
            "                <update.dest.field>\n" +
            "                    <field name=\"all_pids\" update=\"add-distinct\">uuid:test</field>\n" +
            "                    <field name=\"user\" update=\"set\">lukas.stastny@gmail.com &amp; Ahoj </field>\n" +
            "                </update.dest.field>\n" +
            "            </onupdate>\n" +
            "        </destination>\n";


    @Test
    public void testConfigParser() throws ParserConfigurationException, IOException, SAXException {
        Document document = XMLUtils.parseDocument(new StringReader(CONFIG));
        Assert.assertNotNull(document);
        Assert.assertEquals("destination", document.getDocumentElement().getNodeName());

        DestinationConfig config = DestinationConfigParser.parse(document.getDocumentElement());
        System.out.println(config);

    }
}
