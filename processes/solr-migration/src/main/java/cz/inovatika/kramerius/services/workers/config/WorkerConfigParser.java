package cz.inovatika.kramerius.services.workers.config;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.workers.config.destination.DestinationConfig;
import cz.inovatika.kramerius.services.workers.config.destination.DestinationConfigParser;
import cz.inovatika.kramerius.services.workers.config.request.RequestConfig;
import cz.inovatika.kramerius.services.workers.config.request.RequestConfigParser;
import org.w3c.dom.Element;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Static utility to build ReplicateWorkerConfig from the worker configuration XML element.
 */
public class WorkerConfigParser {

    private static final Logger LOGGER = Logger.getLogger(WorkerConfigParser.class.getName());

    /**
     * Parses the <worker> XML element and returns a configured Builder.
     * @param workerElm The root <worker> element from the configuration file.
     * @return A fully configured ReplicateWorkerConfig object.
     */
    public static WorkerConfig parse(Element workerElm) {
        
        WorkerConfig.Builder builder = new WorkerConfig.Builder();

        DestinationConfig destinationConfig = null;
        RequestConfig  requestConfig = null;

        Element workerFactoryElm = XMLUtils.findElement(workerElm, "workerFactory");
        if (workerFactoryElm != null) builder.factoryClz(workerFactoryElm.getAttribute("class"));


        Element destinationElm = XMLUtils.findElement(workerElm, "destination");
        if (destinationElm != null) {

            destinationConfig = DestinationConfigParser.parse(destinationElm);

            builder.destinationConfig(destinationConfig);
        }
        
        // ==========================================================
        // 2. Request Configuration
        // ==========================================================
        Element requestElm = XMLUtils.findElement(workerElm, "request");
        if (requestElm != null) {

            requestConfig = RequestConfigParser.parse(requestElm);
            builder.requestConfig(requestConfig);

//            // default field list
//            String fieldlistText = findSubElementText(requestElm, "fieldlist");
//            if (fieldlistText != null) {
//                builder.fieldList(fieldlistText);
//            }
//
//            // Id
//            String idText = findSubElementText(requestElm, "id");
//            if (idText != null) {
//                builder.idIdentifier(idText);
//            }
//
//            // transform (Using the new TransformFactory)
//            String transformFormatText = findSubElementText(requestElm, "trasfrom");
//            if (transformFormatText != null) {
//                builder.transform(transformFormatText);
//            }
//            // collection
//            String collectionText = findSubElementText(requestElm, "collection");
//            if (collectionText != null) {
//                builder.collectionField(collectionText);
//            }
//
//            // Composite id
//            String compositeIdText = findSubElementText(requestElm, "composite.id");
//            if (compositeIdText != null) {
//                try {
//                    boolean isComposite = Boolean.parseBoolean(compositeIdText);
//                    builder.compositeId(isComposite);
//
//                    if (isComposite) {
//                        String compositeRootText = findSubElementText(requestElm, "composite.root");
//                        if (compositeRootText != null) builder.rootOfComposite(compositeRootText);
//
//                        String compositeChildText = findSubElementText(requestElm, "composite.child");
//                        if (compositeChildText != null) builder.childOfComposite(compositeChildText);
//                    }
//                } catch (Exception e) {
//                    LOGGER.log(Level.WARNING, "Invalid boolean value for composite.id, assuming false.", e);
//                }
//            }
//
//            // Check url
//            String checkUrlText = findSubElementText(requestElm, "checkUrl");
//            if (checkUrlText != null) {
//                builder.checkUrl(checkUrlText);
//            }
//
//            // Check url endpoint
//            String checkEndpointText = findSubElementText(requestElm, "checkEndpoint");
//            if (checkEndpointText != null) {
//                builder.checkEndpoint(checkEndpointText);
//            }
        }
        
        return builder.build();
    }
    
    /**
     * Helper to find and get text content of a nested element safely.
     */
    private static String findSubElementText(Element parent, String tagName) {
        if (parent == null) return null;
        Element child = XMLUtils.findElement(parent, tagName);
        return (child != null) ? child.getTextContent() : null;
    }
}