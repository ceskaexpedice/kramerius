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
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.utils.XMLUtils;

public class RelsExtRightsFlag extends AbstractItemDecorator {

    public static final Logger LOGGER = Logger.getLogger(SolrDataNode.class
            .getName());

    public static final String KEY = AbstractItemDecorator.key("DC_RIGHT");// "DC_RIGHT";

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

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
                Document relsExt = RELSEXTDecoratorUtils.getRELSEXTPidDocument(
                        pid, runtimeContext, this.fedoraAccess);
                Element topElm = XMLUtils.findElement(
                        relsExt.getDocumentElement(), "Description",
                        FedoraNamespaces.RDF_NAMESPACE_URI);
                Element publicElm = XMLUtils.findElement(topElm, "policy",
                        FedoraNamespaces.KRAMERIUS_URI);
                if (publicElm != null) {
                    jsonObject.put("policy", publicElm.getTextContent());
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
