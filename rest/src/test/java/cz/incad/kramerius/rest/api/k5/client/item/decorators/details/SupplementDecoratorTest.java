package cz.incad.kramerius.rest.api.k5.client.item.decorators.details;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.impl.SolrMemoizationImpl;
import cz.incad.kramerius.rest.api.k5.client.utils.BiblioModsUtils;
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
 * Created by pstastny on 11/7/2017.
 */
public class SupplementDecoratorTest extends TestCase {

    @Test
    public void testDecorator() throws IOException, ParserConfigurationException, SAXException, JSONException, TransformerException {

        URL biblioModsRes = SupplementDecoratorTest.class.getResource("supplement-mods.xml");
        Document biblioMods = XMLUtils.parseDocument(biblioModsRes.openStream(), true);

        URL solrRes = SupplementDecoratorTest.class.getResource("supplement-solr.xml");
        Document solr = XMLUtils.parseDocument(solrRes.openStream(), true);

        SolrMemoizationImpl memoization = new SolrMemoizationImpl();
        SolrAccess sa = EasyMock.createMock(SolrAccess.class);

        FedoraAccess fa = EasyMock.createMock(FedoraAccess.class);
        EasyMock.expect(fa.getBiblioMods("uuid:0bf8e0f3-9c1a-40ca-a23f-c8a52e9e0359")).andReturn(biblioMods).anyTimes();

        EasyMock.expect(sa.getSolrDataDocument("uuid:0bf8e0f3-9c1a-40ca-a23f-c8a52e9e0359")).andReturn(solr).anyTimes();

        EasyMock.replay(sa,fa);

        SupplementDecorator dec = new SupplementDecorator();
        dec.memo = memoization;
        dec.fedoraAccess = fa;
        memoization.setSolrAccess(sa);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pid", "uuid:0bf8e0f3-9c1a-40ca-a23f-c8a52e9e0359");

        String key = BiblioModsUtils.BIBLIOMODS_PID_DOCUMENT_KEY + "_" + "uuid:0bf8e0f3-9c1a-40ca-a23f-c8a52e9e0359";

        Map<String, Object> runtimeContext = new HashMap<>();
        runtimeContext.put(key, biblioMods);


        dec.decorate(jsonObject, runtimeContext);

        Assert.assertNotNull(jsonObject.getJSONObject("details"));
        Assert.assertNotNull(jsonObject.getJSONObject("details").getString("date"));
    }

}
