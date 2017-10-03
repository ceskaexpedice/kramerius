package cz.incad.kramerius.security.impl.criteria;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class MovingWallPatternTest extends TestCase {

    public void testPatterns() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        XPathFactory xpathfact = XPathFactory.newInstance();
        InputStream is = MovingWallTest.class.getClassLoader().getResourceAsStream("cz/incad/kramerius/fedora/res/dateIssued.mods.xml");
        Assert.assertNotNull(is);
        String[] mODS_XPATHS = MovingWall.MODS_XPATHS;
        String val = null;
        for (String xpath : mODS_XPATHS) {
            Node node = (Node) MovingWall.findDateString(XMLUtils.parseDocument(is,true), xpath, xpathfact);
            if (node != null) {
                val = node.getTextContent();
                break;
            }
        }
        Assert.assertNotNull(val);
        Assert.assertEquals("1832", val);
    }
}
