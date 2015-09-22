package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class SolrRightsFlag extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger.getLogger(SolrDataNode.class
            .getName());

    public static final String KEY = AbstractItemDecorator.key("DC_RIGHT");// "DC_RIGHT";


    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrAccess saccess;
    
    @Inject
    SolrMemoization solrMemo;
    
    
    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject,
            Map<String, Object> runtimeContext) {
        if (jsonObject.containsKey("pid")) {
            String pid = jsonObject.getString("pid");
            try {
                
                Element indexDoc = this.solrMemo.getRememberedIndexedDoc(pid);
                if (indexDoc == null) {
                    indexDoc = this.solrMemo.askForIndexDocument(pid);
                }
                if (indexDoc != null) {
                    String dostupnost = SOLRUtils.value(indexDoc, "dostupnost", String.class);
                    if (dostupnost != null) {
                        jsonObject.put("policy", dostupnost);
                    }
                }
                
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return (tpath.isParsed());
    }

}
