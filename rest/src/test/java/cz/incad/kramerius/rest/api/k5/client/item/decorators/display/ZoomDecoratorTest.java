package cz.incad.kramerius.rest.api.k5.client.item.decorators.display;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Provider;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.CollectionsDecoratorTest;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ZoomDecoratorTest {


	@Test
	public void testDecorateZoomify() throws ParserConfigurationException, SAXException, IOException, JSONException {
		URL res = CollectionsDecoratorTest.class.getResource("rels-ext2.xml");
		Document document = XMLUtils.parseDocument(res.openStream(), true);
		
        FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
        EasyMock.expect(fa.getRelsExt("uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7")).andReturn(document).anyTimes();
        
        KConfiguration conf = EasyMock.createMock(KConfiguration.class);
        EasyMock.expect(conf.getProperty("zoom.viewer","zoomify")).andReturn("zoomify").anyTimes();

        final HttpServletRequest reqMock = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(reqMock.getRequestURL()).andReturn(new StringBuffer("http://kramerius.mzk.cz/search/i.jsp?pid=uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7")).anyTimes();

        EasyMock.expect(reqMock.getHeader("x-forwarded-host")).andReturn(null).anyTimes();
        
        Provider<HttpServletRequest> reqProvider = new Provider<HttpServletRequest>() {

			@Override
			public HttpServletRequest get() {
				return reqMock;
			}
        };
        
        
        EasyMock.replay(fa,conf, reqMock);

        
        ZoomDecorate zoomDecorate = new ZoomDecorate();
        zoomDecorate.fedoraAccess = fa;
        zoomDecorate.kconf = conf;
        zoomDecorate.requestProvider = reqProvider;
        //zoomDecorate.kconf
        
        

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", "uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7");
    	Map<String, Object> runtimeContext = new HashMap<String, Object>();	
    	zoomDecorate.decorate(jsonObject, runtimeContext);

    	String addr = jsonObject.getJSONObject("zoom").getString("url");
    	Assert.assertEquals("http://kramerius.mzk.cz/search/zoomify/uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7", addr);
	}


	@Test
	public void testDecorateDeepzoom() throws ParserConfigurationException, SAXException, IOException, JSONException {
		URL res = CollectionsDecoratorTest.class.getResource("rels-ext2.xml");
		Document document = XMLUtils.parseDocument(res.openStream(), true);
		
        FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
        EasyMock.expect(fa.getRelsExt("uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7")).andReturn(document).anyTimes();
        
        KConfiguration conf = EasyMock.createMock(KConfiguration.class);
        EasyMock.expect(conf.getProperty("zoom.viewer","zoomify")).andReturn("deepzoom").anyTimes();

        final HttpServletRequest reqMock = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(reqMock.getRequestURL()).andReturn(new StringBuffer("http://kramerius.mzk.cz/search/i.jsp?pid=uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7")).anyTimes();

        EasyMock.expect(reqMock.getHeader("x-forwarded-host")).andReturn(null).anyTimes();
        
        Provider<HttpServletRequest> reqProvider = new Provider<HttpServletRequest>() {

			@Override
			public HttpServletRequest get() {
				return reqMock;
			}
        };
        
        
        EasyMock.replay(fa,conf, reqMock);

        
        ZoomDecorate zoomDecorate = new ZoomDecorate();
        zoomDecorate.fedoraAccess = fa;
        zoomDecorate.kconf = conf;
        zoomDecorate.requestProvider = reqProvider;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", "uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7");
    	Map<String, Object> runtimeContext = new HashMap<String, Object>();	
    	zoomDecorate.decorate(jsonObject, runtimeContext);
    	
    	String addr = jsonObject.getJSONObject("zoom").getString("url");
    	Assert.assertEquals("http://kramerius.mzk.cz/search/deepZoom/uuid:24330953-cccc-4c06-b4f9-859dd01fa2e7", addr);
	}
}
