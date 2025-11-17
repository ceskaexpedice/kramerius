package cz.inovatika.kramerius.services.workers.config.destination;

import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Element;

/**
 * Static utility for parsing the <destination> block and creating a DestinationConfig object.
 */
public class DestinationConfigParser {

    // Helper to find and get text content of a nested element safely (z WorkerConfigParser)
    private static String findSubElementText(Element parent, String tagName) {
        if (parent == null) return null;
        Element child = XMLUtils.findElement(parent, tagName);
        return (child != null) ? child.getTextContent() : null;
    }

    /**
     * Parses the destination-specific configuration, including Solr URL and event updates.
     * @param workerElm The root <worker> element, needed to find the <destination> block.
     * @return A fully configured DestinationConfig object.
     */
    public static DestinationConfig parse(Element workerElm) {
        
        DestinationConfig.Builder builder = new DestinationConfig.Builder();
        
        // A. Destination URL and general settings
        Element destElm = XMLUtils.findElement(workerElm, "destination");
        if (destElm != null) {
            String urlText = findSubElementText(destElm, "url");
            if (urlText != null) {
                builder.destinationUrl(urlText);
            }
            
            // B. Event configurations (onindex/onupdate)
            
            // on index event
            Element onindex = XMLUtils.findElement(destElm, "onindex");
            if (onindex != null) {
                Element updateFieldElement = XMLUtils.findElement(onindex, "update.dest.field");
                if(updateFieldElement != null) {
                    builder.onIndexEventUpdateElms(XMLUtils.getElements(updateFieldElement));
                }

                Element removeFieldElement = XMLUtils.findElement(onindex, "remove.dest.field");
                if (removeFieldElement != null) {
                    builder.onIndexEventRemoveElms(XMLUtils.getElements(removeFieldElement));
                }
                Element oIfieldlistElm = XMLUtils.findElement(onindex, "fieldlist");
                if (oIfieldlistElm != null) {
                    builder.onIndexedFieldList(oIfieldlistElm.getTextContent());
                }

            }

            // on update event
            Element onupdate = XMLUtils.findElement(destElm, "onupdate");
            if (onupdate != null) {
                Element updateFieldElement = XMLUtils.findElement(onupdate, "update.dest.field");
                if (updateFieldElement != null) {
                    builder.onUpdateUpdateElements(XMLUtils.getElements(updateFieldElement));
                }

                Element oIfieldlistElm = XMLUtils.findElement(onupdate, "fieldlist");
                if (oIfieldlistElm != null) {
                    builder.onUpdateFieldList(oIfieldlistElm.getTextContent());
                }

            }
        }

        // Kibana Logger configuration (REMOVED as requested)
        
        return builder.build();
    }
}