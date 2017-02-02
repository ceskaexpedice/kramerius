package cz.incad.kramerius.virtualcollections.impl;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import cz.incad.kramerius.virtualcollections.impl.CDKVirtualCollectionsGetImpl;

public class CDKVirtualCollectionsTest {

    @Test
    public void testDisect() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        URL urlRes = CDKVirtualCollectionsTest.class.getResource("DC.xml");
        Document document = XMLUtils.parseDocument(urlRes.openStream(),true);
        String url = CDKVirtualCollectionsGetImpl.disectURL(document);
        Assert.assertEquals("http://localhost:8080/search", url);
    }
    
    @Test
    public void testAppendVCPoint() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        URL urlRes = CDKVirtualCollectionsTest.class.getResource("DC.xml");
        Document document = XMLUtils.parseDocument(urlRes.openStream(),true);
        String vcpoint = CDKVirtualCollectionsGetImpl.appendVCPoint(CDKVirtualCollectionsGetImpl.disectURL(document));
        Assert.assertEquals("http://localhost:8080/search/api/v5.0/vc", vcpoint);
    }
    
    @Test
    public void testVCFromJSON() throws IOException, JSONException {
        URL urlRes = CDKVirtualCollectionsTest.class.getResource("vc.json");
        String str = IOUtils.readAsString(urlRes.openStream(), Charset.forName("UTF-8"), true);
        JSONObject jobj = new JSONObject(str);
        VirtualCollection vc = CDKVirtualCollectionsGetImpl.collectionFromJSON(jobj);
        Assert.assertEquals(vc.getPid(), "vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
        Assert.assertEquals(vc.getLabel(), "vc:f73dee31-ae76-4dbc-b7b9-d986df497596");
        Assert.assertEquals(vc.isCanLeave(), true);
        Map<String, String> map = vc.getDescriptionsMap();
        Assert.assertTrue(map.size() == 2);
        Assert.assertTrue(map.containsKey("en"));
        Assert.assertTrue(map.containsKey("cs"));
        
    }


}
