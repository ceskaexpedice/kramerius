package cz.inovatika.kramerius.services.config;

import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.iterators.config.SolrConfigParser;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import cz.inovatika.kramerius.services.workers.config.WorkerConfigParser; // Pro worker
import org.w3c.dom.Element;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main parser for the <cdkprocess> configuration XML structure.
 * Orchestrates the parsing of iteration and worker sub-configurations using specialized parsers.
 */
public class ProcessConfigParser {

    private static final Logger LOGGER = Logger.getLogger(ProcessConfigParser.class.getName());

    /**
     * Parses the <cdkprocess> XML element and builds the complete configuration tree.
     * @param processElm The root <cdkprocess> XML element.
     * @return A fully configured CDKProcessConfig object.
     */
    public static ProcessConfig parse(Element processElm) {

        ProcessConfig.Builder builder = new ProcessConfig.Builder();
        Element sourceNameElm = XMLUtils.findElement(processElm, "source-name");
        if (sourceNameElm != null) {
            builder.sourceName(sourceNameElm.getTextContent());
        }
        Element nameElm = XMLUtils.findElement(processElm, "name");
        if (nameElm != null) {
            builder.name(nameElm.getTextContent());
        }

        // Threads
        try {
            Element threadsElm = XMLUtils.findElement(processElm, "threads");
            if (threadsElm != null) {
                builder.threads(Integer.parseInt(threadsElm.getTextContent()));
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid value for <threads>, using default 1.", e);
        }

        Element typeElm = XMLUtils.findElement(processElm, "type");
        if (typeElm != null) {
            builder.type(typeElm.getTextContent());
        }

        Element workingTimeElm = XMLUtils.findElement(processElm, "working-time");
        if (workingTimeElm != null) {
            builder.workingTime(workingTimeElm.getTextContent());
        }

        Element timestampElm = XMLUtils.findElement(processElm, "timestamp");
        if (timestampElm != null) {
            builder.timestampUrl(timestampElm.getTextContent());
        }

        Element introspectElm = XMLUtils.findElement(processElm, "introspect");
        if (introspectElm != null) {
            builder.introspectUrl(introspectElm.getTextContent());
        }

        // --- 2. Iteration Configuration (Delegated) ---
        Element iterationElm = XMLUtils.findElement(processElm, "iteration");
        SolrIteratorConfig iteratorConfig = null;
        if (iterationElm != null) {
            iteratorConfig = SolrConfigParser.parse(iterationElm, null);
            builder.iteratorConfig(iteratorConfig);
        } else {
             LOGGER.log(Level.SEVERE, "Required <iteration> element not found in configuration.");
        }

        // --- 3. Worker Configuration (Delegated) ---
        Element workerElm = XMLUtils.findElement(processElm, "worker");
        if (workerElm != null) {
            // Používáme WorkerConfigParser pro parsování <worker>
            WorkerConfig workerConfig = WorkerConfigParser.parse(iteratorConfig,  workerElm);
            builder.workerConfig(workerConfig);
        } else {
            LOGGER.log(Level.SEVERE, "Required <worker> element not found in configuration.");
        }

        return builder.build();
    }
}