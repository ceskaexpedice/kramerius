package cz.incad.kramerius.rest.api.k5.client.item.decorators.details;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.impl.SolrMemoizationImpl;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pstastny on 9/15/2017.
 */
public class InternalPartDecorateTest extends TestCase {

    @Test
    public void testDecorator() throws IOException, ParserConfigurationException, SAXException, JSONException, TransformerException {
        URL res = InternalPartDecorateTest.class.getResource("solr-internalpart.xml");
        Document document = XMLUtils.parseDocument(res.openStream(), true);

        SolrMemoizationImpl memoization = new SolrMemoizationImpl();
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);

        EasyMock.expect(sa.getSolrDataDocument("uuid:0bf8e0f3-9c1a-40ca-a23f-c8a52e9e0359")).andReturn(document).anyTimes();

        EasyMock.replay(sa);

        InternalPartDecorate dec = new InternalPartDecorate();

        dec.solrAccess = sa;
        dec.memo = memoization;

        memoization.setSolrAccess(sa);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", "uuid:0bf8e0f3-9c1a-40ca-a23f-c8a52e9e0359");
        Map<String, Object> runtimeContext = new HashMap<String, Object>();

        dec.decorate(jsonObject, runtimeContext);

        Assert.assertNotNull(jsonObject.getJSONObject("details"));
        Assert.assertNotNull(jsonObject.getJSONObject("details").getString("pageRange"));
        Assert.assertNotNull(jsonObject.getJSONObject("details").getString("type"));
        Assert.assertNotNull(jsonObject.getJSONObject("details").getString("title"));
    }


}
