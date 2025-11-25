package cz.inovatika.kramerius.services.workers.config.request;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import org.w3c.dom.Element;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Static utility for parsing the <request> block and creating a RequestConfig object.
 * This handles all configuration related to the source system and data fetching.
 */
public class RequestConfigParser {

    private static final Logger LOGGER = Logger.getLogger(RequestConfigParser.class.getName());

    /**
     * Helper to find and get text content of a nested element safely.
     */
    private static String findSubElementText(Element parent, String tagName) {
        if (parent == null) return null;
        Element child = XMLUtils.findElement(parent, tagName);
        return (child != null) ? child.getTextContent() : null;
    }

    /**
     * Parses the <request> configuration block.
     * @param workerElm The root <worker> element, used to find the <request> block.
     * @return A fully configured RequestConfig object.
     */
    public static RequestConfig parse(SolrIteratorConfig config, Element workerElm) {
        
        RequestConfig.Builder builder = new RequestConfig.Builder();
        Element requestElm = XMLUtils.findElement(workerElm, "request");

        if (requestElm != null) {

            // default field list
            String fieldlistText = findSubElementText(requestElm, "fieldlist");
            if (fieldlistText != null) builder.fieldList(fieldlistText);

            // Id
            String idText = findSubElementText(requestElm, "id");
            if (idText != null) builder.idIdentifier(idText);
            else {
                if (config != null)  builder.idIdentifier(config.getIdField());
            }

            // transform
            String transformFormatText = findSubElementText(requestElm, "trasfrom");
            if (transformFormatText != null) {
                builder.transform(transformFormatText);
            }
            // collection
            String collectionText = findSubElementText(requestElm, "collection");
            if (collectionText != null) builder.collectionField(collectionText);

            // Composite id
            String compositeIdText = findSubElementText(requestElm, "composite.id");
            if (compositeIdText != null) {
                try {
                    boolean isComposite = Boolean.parseBoolean(compositeIdText);
                    builder.compositeId(isComposite);

                    if (isComposite) {
                        String compositeRootText = findSubElementText(requestElm, "composite.root");
                        if (compositeRootText != null) builder.rootOfComposite(compositeRootText);
                        
                        String compositeChildText = findSubElementText(requestElm, "composite.child");
                        if (compositeChildText != null) builder.childOfComposite(compositeChildText);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Invalid boolean value for composite.id, assuming false.", e);
                }
            }


            //XMLUtils.findElement(requestElm, "response")

            String urlText = findSubElementText(requestElm, "url");
            if (urlText != null) builder.url(urlText);

            String endpointText = findSubElementText(requestElm, "endpoint");
            if (endpointText != null) builder.endpoint(endpointText);

            String batchsizeText = findSubElementText(requestElm, "batchsize");
            if (batchsizeText != null) {
                try {
                    int bSize = Integer.parseInt(batchsizeText);
                    builder.batchSize(bSize);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Invalid number for batchsize: " + batchsizeText);
                }
            }

            // Check url
            String checkUrlText = findSubElementText(requestElm, "checkUrl");
            if (checkUrlText != null) builder.checkUrl(checkUrlText);

            // Check url endpoint
            String checkEndpointText = findSubElementText(requestElm, "checkEndpoint");
            if (checkEndpointText != null) builder.checkEndpoint(checkEndpointText);

        }
        
        return builder.build();
    }
}