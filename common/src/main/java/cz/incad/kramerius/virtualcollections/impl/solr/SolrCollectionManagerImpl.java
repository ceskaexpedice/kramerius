package cz.incad.kramerius.virtualcollections.impl.solr;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.impl.AbstractCollectionManager;

public class SolrCollectionManagerImpl extends AbstractCollectionManager {

    public static final Logger LOGGER = Logger.getLogger(SolrCollectionManagerImpl.class.getName());

    @Override
    public List<Collection> getCollections() throws CollectionException {
        try {
            List<Collection> vcs = new ArrayList<Collection>();
            String query = "/terms?terms=true&terms.fl=collection&terms.limit=1000&terms.sort=index&wt=json";
            String solrHost = KConfiguration.getInstance().getSolrHost();
            String uri = solrHost + query;
            InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
            JSONObject json = new JSONObject(IOUtils.readAsString(inputStream, Charset.forName("UTF-8"), true));
            JSONArray ja = json.getJSONObject("terms").getJSONArray("collection");
            String pid = "";
            for (int i = 0; i < ja.length(); i = i + 2) {
                try {
                    pid = ja.getString(i);
                    if (!"".equals(pid)) {
                        Collection vc = super.getCollection(pid);
                        if (vc != null) {
                            vcs.add(vc);
                        }
                    }
                } catch (Exception vcex) {
                    LOGGER.log(Level.WARNING, "Could not get virtual collection for  " + pid + ": " + vcex.toString());
                }
            }
            return vcs;
        } catch (JSONException e) {
            throw new CollectionException(e);
        } catch (IOException e) {
            throw new CollectionException(e);
        }
    }
}
