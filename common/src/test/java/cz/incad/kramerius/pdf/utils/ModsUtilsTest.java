package cz.incad.kramerius.pdf.utils;

import cz.incad.kramerius.utils.XMLUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ModsUtilsTest {

    @Test
    public void identifiersFromMods() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        InputStream is = this.getClass().getResourceAsStream("mods.xml");
        Document mods = XMLUtils.parseDocument(is, true);
        Map<String, List<String>> map = ModsUtils.identifiersFromMods(mods);
        Assert.assertTrue(map.size() == 4);
        Assert.assertTrue(map.containsKey("uuid"));
        Assert.assertEquals(map.get("uuid") , (Arrays.asList("2d433337-df02-4666-bd68-117d86553a44")));
        Assert.assertEquals(map.get("urnnbn") , (Arrays.asList("urn:nbn:cz:aba007-0002ei")));
        Assert.assertEquals(map.get("ccnb") , (Arrays.asList("cnb002830834")));
        Assert.assertEquals(map.get("isbn") , (Arrays.asList("978-80-7363-745-3", "978-80-7464-830-4")));

        map = ModsUtils.identifiersFromMods(mods,"isbn");
        Assert.assertTrue(map.size() == 1);
        Assert.assertEquals(map.get("isbn"), (Arrays.asList("978-80-7363-745-3", "978-80-7464-830-4")));
    }
}
