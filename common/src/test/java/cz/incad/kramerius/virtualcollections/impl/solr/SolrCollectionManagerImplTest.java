package cz.incad.kramerius.virtualcollections.impl.solr;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;

import cz.incad.kramerius.utils.IOUtils;
import junit.framework.TestCase;

public class SolrCollectionManagerImplTest extends TestCase {

    public void testResults() throws JSONException, IOException {
        InputStream resource = SolrCollectionManagerImplTest.class.getResourceAsStream("collections.json");
        Assert.assertTrue(resource != null);
        JSONObject jsonObject = new JSONObject(IOUtils.readAsString(resource, Charset.forName("UTF-8"), true));
        Assert.assertNotNull(jsonObject);
        
        JSONArray arr = SolrCollectionManagerImpl.GetCollectionsType.FACET.disectArray(jsonObject);
        Assert.assertNotNull(arr);
        Assert.assertTrue(arr.length() == 4);
        
    }
}
