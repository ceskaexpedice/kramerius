package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.utils.RelsExtHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Add donator information
 * @author pavels
 */
public class DonatorDecorate  extends AbstractItemDecorator{

    public static final String DONATOR_DECORATOR_KEY = AbstractItemDecorator.key("DONATOR");

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    KConfiguration kconf;

    
    @Override
    public String getKey() {
        return DONATOR_DECORATOR_KEY;
    }

    
    @Override
    public void decorate(JSONObject jsonObject,
            Map<String, Object> runtimeContext) {
        try {
            if (containsPidInJSON(jsonObject)) {
                String pid = getPidFromJSON(jsonObject);
                if (!PIDSupport.isComposedPID(pid)) {
                    Document relsExt = RELSEXTDecoratorUtils.getRELSEXTPidDocument(pid, context, this.fedoraAccess);
                    String donator = RelsExtHelper.getDonator(relsExt);
                    if (donator != null) {
                        jsonObject.put("donator",donator);
                    }
                }
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
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
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed() && tpath.getRestPath().isEmpty();
    }
}
