package cz.incad.kramerius.virtualcollections.impl;

import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import junit.framework.Assert;

public class CDKVirtualCollectionsMockTest {

	
    @Test
    public void testGetVirtualcollection() throws IOException, JSONException, ParserConfigurationException, SAXException {
    	
    	// knav - vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26
    	// mzk - vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea
    	
        URL knavUrlRes = CDKVirtualCollectionsTest.class.getResource("knav.json");
        String knavSource = IOUtils.readAsString(knavUrlRes.openStream(), Charset.forName("UTF-8"), true);
        
        URL knavDCRes = CDKVirtualCollectionsTest.class.getResource("knav.dc");
        String knavDCSource = IOUtils.readAsString(knavDCRes.openStream(), Charset.forName("UTF-8"), true);
        
        URL mzkUrlRes = CDKVirtualCollectionsTest.class.getResource("mzk.json");
        String mzkSource = IOUtils.readAsString(mzkUrlRes.openStream(), Charset.forName("UTF-8"), true);
        
        URL mzkDCRes = CDKVirtualCollectionsTest.class.getResource("mzk.dc");
        String mzkDCSource = IOUtils.readAsString(mzkDCRes.openStream(), Charset.forName("UTF-8"), true);
    	
    	CDKResourcesFilter fi = EasyMock.createMock(CDKResourcesFilter.class);
    	CDKVirtualCollectionsGetImpl get = createMockBuilder(CDKVirtualCollectionsGetImpl.class)
    			.withConstructor()
    			.addMockedMethod("createResourceFilter")
    			.addMockedMethod("collectionAsJSONArrayRawRequest")
    			.createMock();


        EasyMock.expect(get.createResourceFilter()).andReturn(fi).anyTimes();
        
        
        EasyMock.expect(fi.getHidden()).andReturn(new ArrayList<String>()).anyTimes();
        EasyMock.expect(fi.getResources()).andReturn(Arrays.asList("vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26","vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea")).anyTimes();
        EasyMock.expect(fi.isHidden("vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26")).andReturn(false).anyTimes();
        EasyMock.expect(fi.isHidden("vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea")).andReturn(false).anyTimes();
        
        
        StatisticsAccessLog sa = EasyMock.createMock(StatisticsAccessLog.class);
        FedoraAccessImpl fedora = createMockBuilder(FedoraAccessImpl.class)
        .withConstructor(KConfiguration.getInstance(),sa)
        .addMockedMethod("getDC")
        .createMock();

        EasyMock.expect(fedora.getDC("vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea")).andReturn(XMLUtils.parseDocument(new StringReader(mzkDCSource), true)).anyTimes();
        EasyMock.expect(fedora.getDC("vc:c4bb27af-3a51-4ac2-95c7-fd393b489e26")).andReturn(XMLUtils.parseDocument(new StringReader(knavDCSource), true)).anyTimes();

        EasyMock.expect(get.collectionAsJSONArrayRawRequest("http://kramerius.mzk.cz/search/api/v5.0/vc")).andReturn(new JSONArray(mzkSource)).times(1);
        EasyMock.expect(get.collectionAsJSONArrayRawRequest("https://kramerius.lib.cas.cz/search/api/v5.0/vc")).andReturn(new JSONArray(knavSource)).times(1);
        
        replay(fi, get, fedora);
        get.setFedoraAccess(fedora);


        List<VirtualCollection> virtualCollections = get.virtualCollections();
        Assert.assertTrue(virtualCollections.size() == 79);
        
        for (VirtualCollection vc : virtualCollections) {
        	Assert.assertNotNull(vc.getPid());
        	Assert.assertTrue(vc.getPid().startsWith("vc:"));
		}

        // second call must be from cache
        List<VirtualCollection> anotherCall = get.virtualCollections();
        Assert.assertNotNull(anotherCall);
        Assert.assertTrue(anotherCall.size() == 79);
        
        List<VirtualCollection> mzkCols = get.virtualCollectionsFromResource("vc:44679769-b5bb-4ac7-ad27-a0c44698c2ea");
        Assert.assertNotNull(mzkCols);
        Assert.assertTrue(mzkCols.size() == 17);
    }

}
