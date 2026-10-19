package cz.incad.kramerius;

import cz.incad.kramerius.utils.XMLUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Discovers collection membership using fields stored in the Solr index. */
public final class SearchIndexEffectiveCollectionsProvider implements EffectiveCollectionsProvider {
    private static final String COLLECTIONS_FIELD = "in_collections";
    private static final String DIRECT_COLLECTIONS_FIELD = "in_collections.direct";
    private static final String MODEL_FIELD = "model";
    private static final String COLLECTION_MODEL = "collection";

    private final SolrAccess searchIndex;

    public SearchIndexEffectiveCollectionsProvider(SolrAccess searchIndex) {
        if (searchIndex == null) {
            throw new IllegalArgumentException("searchIndex must not be null");
        }
        this.searchIndex = searchIndex;
    }

    @Override
    public Set<String> getAffectedCollections(String targetPid) {
        // pid na kterem se meni licence, vsechny pidy pod danym stromem maji licenci zdedenou, ne potreba zjistit
        // zda nektere
        Set<String> result = new LinkedHashSet<>();
        if (targetPid == null || targetPid.isEmpty()) {
            return Collections.emptySet();
        }

        try {
            Document targetDocument = searchIndex.getSolrDataByPid(targetPid,
                    MODEL_FIELD + "," + COLLECTIONS_FIELD + "," + DIRECT_COLLECTIONS_FIELD + ",own_pid_path");
            Element targetDoc = findSolrDoc(targetDocument);
            if (targetDoc == null) {
                return Collections.emptySet();
            }

            result.addAll(getFieldValues(targetDoc, COLLECTIONS_FIELD));
            result.addAll(getFieldValues(targetDoc, DIRECT_COLLECTIONS_FIELD));
            if (getFieldValues(targetDoc, MODEL_FIELD).contains(COLLECTION_MODEL)) {
                result.add(targetPid);
            }
            String ownPidPath = getFieldValues(targetDoc, "own_pid_path").stream().findFirst().orElse(null);
            if (ownPidPath != null) {
                result.addAll(getSubtreeCollections(ownPidPath));
            }
            return Collections.unmodifiableSet(result);
        } catch (IOException e) {
            throw new RuntimeException("Could not discover collections for " + targetPid, e);
        }
    }

    private Set<String> getSubtreeCollections(String ownPidPath) throws IOException {
            String query = "own_pid_path.children:\"" + escapeQueryValue(ownPidPath) + "\"";
            String request = "q=" + encode(query)
                    + "&rows=0&facet=true&facet.field=" + encode(COLLECTIONS_FIELD)
                    + "&facet.mincount=1";
            JSONObject response = searchIndex.requestWithSelectReturningJson(request, null);

            JSONObject facetCounts = response.getJSONObject("facet_counts");
            JSONObject facetFields = facetCounts.getJSONObject("facet_fields");
            JSONArray facetValues = facetFields.getJSONArray(COLLECTIONS_FIELD);
            Set<String> collections = new LinkedHashSet<>();
            for (int i = 0; i < facetValues.length(); i++) {
                Object value = facetValues.get(i);
                if (i % 2 == 0) {
                    collections.add(value.toString());
                }
            }
            return collections;
    }

    private String encode(String value) throws IOException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
    }

    private String escapeQueryValue(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Set<String> getFieldValues(Element document, String fieldName) {
        Set<String> values = new LinkedHashSet<>();
        Element field = XMLUtils.findElement(document, element ->
                element.hasAttribute("name") && fieldName.equals(element.getAttribute("name")));
        if (field == null) {
            return values;
        }

        List<Element> valueElements = "arr".equals(field.getNodeName())
                ? XMLUtils.getElements(field, element -> "str".equals(element.getNodeName()))
                : Collections.singletonList(field);
        for (Element valueElement : valueElements) {
            String value = valueElement.getTextContent().trim();
            if (!value.isEmpty()) {
                values.add(value);
            }
        }
        return values;
    }

    private Element findSolrDoc(Document response) {
        return response == null ? null : XMLUtils.findElement(response.getDocumentElement(), "doc");
    }
}
