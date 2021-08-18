package cz.incad.kramerius.rest.api.k5.client.item.decorators;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Add donator information
 *
 * @author pavels
 */
public class DonatorDecorate extends AbstractItemDecorator {

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


    static List<Element> findDonators(Document doc) {
        List<Element> retval = new ArrayList<Element>();
        Element rdfElm = XMLUtils.findElement(doc.getDocumentElement(), "RDF", FedoraNamespaces.RDF_NAMESPACE_URI);
        if (rdfElm != null) {
            Element description = XMLUtils.findElement(rdfElm, "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
            if (description != null) {
                List<Element> elements = XMLUtils.getElements(description, new XMLUtils.ElementsFilter() {

                    @Override
                    public boolean acceptElement(Element element) {
                        return (element.getLocalName().equals("hasDonator")
                                && element.getNamespaceURI().equals(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI));
                    }
                });
                for (Element el : elements) {
                    retval.add(el);
                }
            }
        }
        return retval;
    }


    static String getDonatorString(Element elm) {
        Attr ref = elm.getAttributeNodeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
        if (ref != null) {
            try {
                PIDParser pidParser = new PIDParser(ref.getValue());
                pidParser.disseminationURI();
                return pidParser.getObjectPid();
            } catch (LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return null;
            }
        } else return null;
    }

    @Override
    public void decorate(JSONObject jsonObject,
                         Map<String, Object> runtimeContext) {
        try {
            if (containsPidInJSON(jsonObject)) {
                String pid = getPidFromJSON(jsonObject);
                if (!PIDSupport.isComposedPID(pid)) {
                    Document relsExt = RELSEXTDecoratorUtils.getRELSEXTPidDocument(pid, context, this.fedoraAccess);
                    List<Element> donators = findDonators(relsExt);
                    if (!donators.isEmpty()) {
                        if (donators.size() == 1) {
                            String donator = getDonatorString(donators.get(0));
                            if (donator != null) {
                                jsonObject.put("donator", donator);
                            }
                        } else {
                            JSONArray donatorsArray = new JSONArray();
                            for (Element donatorElement : donators) {
                                String donator = getDonatorString(donatorElement);
                                if (donator != null) {
                                    donatorsArray.put(donator);
                                }
                            }
                            if (donatorsArray.length() > 0) {
                                jsonObject.put("donator", donatorsArray);
                            }
                        }
                    }

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
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed() && tpath.getRestPath().isEmpty();
    }
}
