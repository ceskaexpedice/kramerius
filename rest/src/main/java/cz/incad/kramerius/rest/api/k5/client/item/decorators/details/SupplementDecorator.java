package cz.incad.kramerius.rest.api.k5.client.item.decorators.details;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.AbstractItemDecorator;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.BiblioModsUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.k5.client.utils.RELSEXTDecoratorUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static cz.incad.kramerius.rest.api.k5.client.item.decorators.CollectionsDecorator.findCollections;

/**
 * Created by pstastny on 11/7/2017.
 */
public class SupplementDecorator extends AbstractDetailDecorator {

    public static final String DETAILS_PERIODICAL_VOLUME = AbstractItemDecorator
            .key("DETAILS.SUPPLEMENT");

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrMemoization memo;

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext) {
        try {
            if (jsonObject.has("pid")) {
                String pid = jsonObject.getString("pid");
                if (!PIDSupport.isComposedPID(pid)) {
                    boolean jsonEnhanced = false;

                    Element doc = this.memo.getRememberedIndexedDoc(pid);
                    if (doc == null)
                        doc = this.memo.askForIndexDocument(pid);
                    if (doc != null) {
                        // date from index
                        List<String> array = SOLRUtils.array(doc, "details", String.class);
                        if (!array.isEmpty()) {
                            String[] details = super.details(array.get(0));
                            JSONObject detailsJSONObject = new JSONObject();
                            if (details.length > 0) {
                                detailsJSONObject.put("date", ItemResourceUtils.preventAutomaticConversion(details[0]));
                            }
                            boolean moreThanZero = detailsJSONObject.keys().hasNext();
                            if (moreThanZero) {
                                jsonObject.put("details", detailsJSONObject);
                            }
                            jsonEnhanced = true;
                        }
                    }
                    if (!jsonEnhanced) {
                        Document biblioDoc = BiblioModsUtils.getBiblioModsDocument(pid, context, this.fedoraAccess);
                        // it is not in index; we trying to get it from biblio mods
                        Element dateIssued = XMLUtils.findElement(biblioDoc.getDocumentElement(), (element) -> {
                            return (element.getLocalName().equals("dateIssued"));
                        });
                        JSONObject detailsJSONObject = new JSONObject();
                        if (dateIssued != null) {
                            detailsJSONObject.put("date", dateIssued.getTextContent());
                        }

                        boolean moreThanZero = detailsJSONObject.keys().hasNext();
                        if (moreThanZero) {
                            jsonObject.put("details", detailsJSONObject);
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
    public boolean apply(JSONObject jsonObject, String context) throws JSONException {
        String m = super.getModel(jsonObject);
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed() && m != null && m.equals("supplement");
    }
}
