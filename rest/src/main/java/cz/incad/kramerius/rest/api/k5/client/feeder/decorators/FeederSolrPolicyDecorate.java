package cz.incad.kramerius.rest.api.k5.client.feeder.decorators;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;

public class FeederSolrPolicyDecorate extends AbstractFeederDecorator {
    public static final Logger LOGGER = Logger.getLogger(FeederSolrAuthorDecorate.class.getName());
    public static final String KEY = AbstractFeederDecorator.key("SOLRPOLICY");
    @Inject
    SolrMemoization solrMemo;
    @Override
    public String getKey() {
        return KEY;
    }
    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext) {
        try {
            String pid = jsonObject.getString("pid");
            Element doc = this.solrMemo.getRememberedIndexedDoc(pid);
            if(doc == null){
                doc = this.solrMemo.askForIndexDocument(pid);
            }

            if (doc == null) {
                throw new IllegalStateException("Document could not be loaded from SOLR. Pid: " + pid);
            }

            String policy = SOLRUtils.value(doc, "dostupnost", String.class);
            if(policy != null) {
                jsonObject.put("policy", policy);
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
