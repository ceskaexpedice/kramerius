package cz.inovatika.kramerius.services.iterators.config;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.services.config.ResponseHandlingConfig;
import cz.inovatika.kramerius.services.config.ResponseHandlingParserUtil;
import org.w3c.dom.Element;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for parsing XML elements and building SolrIteratorConfig.
 * Does not include dynamic logic like timestamp fetching.
 */
public class SolrConfigParser {

    private static final Logger LOGGER = Logger.getLogger(SolrConfigParser.class.getName());

    /**
     * Builds a SolrIteratorConfig.Builder from the <iteration> XML element.
     * @param iteration Root <iteration> element.
     * @param filterQueryOverride Optional override for the fquery (e.g., with applied timestamp).
     * @return A ready-to-use SolrIteratorConfig.Builder.
     */
    public static SolrIteratorConfig parse(Element iteration, String filterQueryOverride) {

        Element urlElm = XMLUtils.findElement(iteration, "url");
        String url = urlElm != null ? urlElm.getTextContent() : null;
        
        Element idElm = XMLUtils.findElement(iteration, "id");
        String id = idElm != null ? idElm.getTextContent() : null;
        
        if (url == null || id == null) {
            throw new IllegalStateException("Iterator configuration must contain 'url' and 'id' elements.");
        }

        SolrIteratorConfig.Builder builder = new SolrIteratorConfig.Builder(url, id);


        Element fqueryElm = XMLUtils.findElement(iteration, "fquery");
        String xmlFilterQuery = fqueryElm != null ? fqueryElm.getTextContent() : "";
        
        String finalFilterQuery = StringUtils.isAnyString(filterQueryOverride) ? filterQueryOverride : xmlFilterQuery;
        builder.filterQuery(finalFilterQuery);

        // B. Field List
        Element fieldListElm = XMLUtils.findElement(iteration, "fieldlist");
        builder.fieldList(fieldListElm != null ? fieldListElm.getTextContent() : "");

        // C. Endpoint
        Element endpointElm = XMLUtils.findElement(iteration, "endpoint");
        builder.endpoint(endpointElm != null ? endpointElm.getTextContent() : "");

        // D. Sort
        Element sortElm = XMLUtils.findElement(iteration, "sort");
        if (sortElm != null) builder.sort(sortElm.getTextContent());

        // E. Rows
        Element rowsElm = XMLUtils.findElement(iteration, "rows");
        if (rowsElm != null) {
            try {
                builder.rows(Integer.parseInt(rowsElm.getTextContent()));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Invalid value for 'rows', default will be used.", e);
            }
        }
        
        Element typeElm = XMLUtils.findElement(iteration, "type");
        if (typeElm != null) {
            try {
                builder.typeOfIteration(TypeOfIteration.valueOf(typeElm.getTextContent()));
            } catch (IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Invalid iteration type, CURSOR will be used.", e);
            }
        }

        Element iterationFactoryElm = XMLUtils.findElement(iteration, "iteratorFactory");
        String clz = iterationFactoryElm.getAttribute("class");
        builder.factoryClz(clz);

        Element timstampFieldElm = XMLUtils.findElement(iteration, "timestamp_field");
        if (timstampFieldElm != null) {
            builder.timestampField(timstampFieldElm.getTextContent());
        }

        Element responseHandlingElm = XMLUtils.findElement(iteration, "response-handling");
        if (responseHandlingElm != null) {
            ResponseHandlingConfig responseHandling = ResponseHandlingParserUtil.parse(responseHandlingElm);
            builder.responseHandlingConfig(responseHandling);
        }

        return builder.build();
    }
}