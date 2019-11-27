package cz.incad.kramerius.rest.api.k5.client.feeder.decorators;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;

public class FeederSolrAuthorDecorate extends AbstractFeederDecorator {
    public static Logger LOGGER = Logger.getLogger(FeederSolrAuthorDecorate.class.getName());
    @Inject
    SolrAccess solrAccess;
    @Inject
    SolrMemoization memo;
    public static final String KEY = AbstractFeederDecorator.key("SOLRAUTHOR");

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext) {
        try {
            String pid = jsonObject.getString("pid");
            Element doc = this.memo.getRememberedIndexedDoc(pid);
            if (doc == null) {
                doc = this.memo.askForIndexDocument(pid);
            }
            if (doc != null) {
                List<String> authors = SOLRUtils.narray(doc, "dc.creator", String.class);
                if (authors != null && !authors.isEmpty()) {
                    jsonObject.put("author", authors);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.feederContext(tokenize(context));
        return tpath.isParsed();
    }
}
