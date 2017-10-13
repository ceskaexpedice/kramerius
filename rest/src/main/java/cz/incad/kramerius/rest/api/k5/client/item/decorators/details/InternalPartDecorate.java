package cz.incad.kramerius.rest.api.k5.client.item.decorators.details;

import com.google.inject.Inject;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.AbstractItemDecorator;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.rest.api.k5.client.utils.SOLRUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by pstastny on 9/15/2017.
 */
public class InternalPartDecorate extends  AbstractDetailDecorator{

    public static final String INTERNALPART = AbstractItemDecorator.key("DETAILS.INTERNALPART");

    @Inject
    SolrAccess solrAccess;

    @Inject
    SolrMemoization memo;

    @Override
    public String getKey() {
        return INTERNALPART;
    }

    @Override
    public void decorate(JSONObject jsonObject, Map<String, Object> runtimeContext) {
        if (jsonObject.has("pid")) {
            try {
                String pid = jsonObject.getString("pid");

                Element doc = this.memo.getRememberedIndexedDoc(pid);
                if (doc == null) {
                    doc = this.memo.askForIndexDocument(pid);
                }
                if (doc != null) {
                    List<String> array = SOLRUtils.array(doc, "details",
                            String.class);
                    if (!array.isEmpty()) {
                        String[] details = super.details(array.get(0));
                        JSONObject detailsJSONObject = new JSONObject();
                        if (details.length > 0) {
                            detailsJSONObject.put("pagenumber", ItemResourceUtils.preventAutomaticConversion(details[0]));
                        }
                        if (details.length > 1) {
                            detailsJSONObject.put("type", details[1]);
                        }
                        if (details.length > 2) {
                            detailsJSONObject.put("title", details[2]);
                        }
                        if (details.length > 3) {
                            detailsJSONObject.put("pageRange", details[3]);
                        }

                        boolean moreThanZero = detailsJSONObject.keys().hasNext();
                        if (moreThanZero) {
                            jsonObject.put("details", detailsJSONObject);
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
    }

    @Override
    public boolean apply(JSONObject jsonObject, String context) throws JSONException {
        String m = super.getModel(jsonObject);
        TokenizedPath tpath = super.itemContext(tokenize(context));
        return tpath.isParsed() && m != null && m.equals("internalpart");
    }
}
