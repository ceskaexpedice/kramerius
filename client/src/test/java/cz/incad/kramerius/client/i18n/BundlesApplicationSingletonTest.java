package cz.incad.kramerius.client.i18n;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.codehaus.jackson.schema.JsonSchema;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.IOUtils;
import junit.framework.Assert;
import junit.framework.TestCase;

public class BundlesApplicationSingletonTest extends TestCase {

//    public void testBundlesApp() {
//        Map<String, Object> init = new HashMap<String, Object>();
//        BundlesApplicationSingleton.tokenizKey(init,"application.title", "Appl title");
//        BundlesApplicationSingleton.tokenizKey(init,"application.help.title", "Appl help");
//        BundlesApplicationSingleton.tokenizKey(init,"application.help.navigation", "Appl navigation");
//        Assert.assertNotNull(init.get("application") != null);
//        Map<String, Object> retval = (Map<String, Object>) init.get("application");
//        Assert.assertNotNull(retval.get("title"));
//    }


    public void testBundleApp3() throws IOException, JSONException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        URL urlREs = BundlesApplicationSingletonTest.class.getResource("bundle.json");
        InputStream is = urlREs.openStream();
        IOUtils.copyStreams(is, bos);
        
        
        JSONObject json = new JSONObject(new String(bos.toByteArray(),"UTF-8"));
        JSONObject jsonBundle = json.getJSONObject("bundle");
        System.out.println(json);

        Map<String, BundleContent> bundle = BundlesApplicationSingleton.loadBundle(jsonBundle);
        Iterator keys = jsonBundle.keys();
        while(keys.hasNext()) {
            String oneKey = (String) keys.next();
            StringTokenizer tokenizer = new StringTokenizer(oneKey,".");
            String firstPart = tokenizer.nextToken();
            BundleContent bundleContent = bundle.get(firstPart);
            Assert.assertNotNull("Cannot find firstpart '"+firstPart+"'", bundleContent);
            Assert.assertNotNull("Cannot find full key '"+oneKey+"'",bundleContent.get(oneKey));

            String[] splitted = oneKey.split("\\.");
            BundleContent pcont = bundleContent;
            for (String string : splitted) {
                Object obj = pcont.get(string);
                if (obj instanceof BundleContent) {
                    pcont = (BundleContent) obj;
                    Assert.assertNotNull("Error while processing key '"+oneKey+"'",pcont);
                } else {
                    Assert.assertNotNull("Error while processing key '"+oneKey+"'",obj);
                }
            }
        }

        Assert.assertNotNull(bundle.get("mods").get("mods.page.partType.NormalPage"));
    }

    public void testBundleApp4() throws IOException, JSONException {
    }
}
