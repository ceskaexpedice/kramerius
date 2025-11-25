package cz.inovatika.kramerius.services.workers.config;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ResponseHandlingConfig;
import cz.inovatika.kramerius.services.config.ResponseHandlingParserUtil;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
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
    public static WorkerConfig parse(SolrIteratorConfig config, Element workerElm) {
        
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
            requestConfig = RequestConfigParser.parse(config, requestElm);
            builder.requestConfig(requestConfig);
        }

        Element responseHandlingElm = XMLUtils.findElement(workerElm, "response-handling");
        if (responseHandlingElm != null) {
            ResponseHandlingConfig responseHandling = ResponseHandlingParserUtil.parse(responseHandlingElm);
            builder.responseHandlingConfig(responseHandling);
        }

        return builder.build();
    }
    
}