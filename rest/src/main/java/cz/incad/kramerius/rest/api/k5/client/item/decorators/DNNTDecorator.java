package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.RightsReturnObject;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlag;
import cz.incad.kramerius.security.impl.criteria.ReadDNNTFlagIPFiltered;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class DNNTDecorator extends AbstractItemDecorator {

    public static final String DNNT_DECORATOR_KEY = AbstractItemDecorator.key("DNNT");
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    SolrAccess solrAccess;

    @Inject
    RightsResolver isActionAllowed;

    @Inject
    SolrMemoization memo;

    @Inject
    KConfiguration kconf;

    @Override
    public String getKey() {
        return DNNT_DECORATOR_KEY;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext) {
        try {
            if (containsPidInJSON(jsonObject)) {
                String pid = getPidFromJSON(jsonObject);
                if (!PIDSupport.isComposedPID(pid)) {
                    Element doc = this.memo.getRememberedIndexedDoc(pid);
                    if (doc != null ) doc = this.memo.askForIndexDocument(pid);
                    if (doc != null) {
                        Optional<Element> optional = Optional.of(doc);
                        Boolean value = SOLRUtils.value(doc, "dnnt",
                                Boolean.class);
                        if (value != null) {
                            jsonObject.put("dnnt", value);
                            Element element = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                                @Override
                                public boolean acceptElement(Element element) {
                                    if (element.getNodeName().equals("bool") &&
                                            element.getAttribute("name") != null &&
                                            element.getAttribute("name").equals("dnnt")) {

                                        try {
                                            ObjectPidsPath[] paths = solrAccess.getPidPaths(null, optional.get());
                                            for (ObjectPidsPath p : paths) {
                                                RightsReturnObject actionAllowed = isActionAllowed.isActionAllowed(SecuredActions.READ.getFormalName(), pid, ImageStreams.IMG_FULL.getStreamName(), p);
                                                if (actionAllowed.getRight() != null && actionAllowed.getRight().getCriteriumWrapper() != null) {
                                                    String qName = actionAllowed.getRight().getCriteriumWrapper().getRightCriterium().getQName();
                                                    if (qName.equals(ReadDNNTFlag.class.getName()) ||
                                                            qName.equals(ReadDNNTFlagIPFiltered.class.getName())) {
                                                        jsonObject.put("providedByDnnt", true);
                                                        break;

                                                    }
                                                }
                                            }
                                        } catch (IOException e) {
                                            LOGGER.log(Level.SEVERE,e.getMessage(),e);
                                        }

                                    }
                                    return false;
                                }
                            });
                        }
                    }

                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }

    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) throws JSONException {
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed() && tpath.getRestPath().isEmpty();
    }
}
