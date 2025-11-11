package cz.inovatika.kramerius.services.workers.config.destination;

import org.w3c.dom.Element;
import java.util.Collections;
import java.util.List;

/**
 * Immutable configuration for the destination system (Solr/Kibana).
 */
public class DestinationConfig {

    // --- Configuration Fields (Final) ---
    private final String destinationUrl;
    private final String onIndexedFieldList;
    private final String onUpdateFieldList;

    private final List<Element> onIndexEventUpdateElms;
    private final List<Element> onIndexEventRemoveElms;
    private final List<Element> onUpdateUpdateElements;

    private DestinationConfig(Builder builder) {
        this.destinationUrl = builder.destinationUrl;
        this.onIndexEventUpdateElms = Collections.unmodifiableList(builder.onIndexEventUpdateElms);
        this.onIndexEventRemoveElms = Collections.unmodifiableList(builder.onIndexEventRemoveElms);
        this.onUpdateUpdateElements = Collections.unmodifiableList(builder.onUpdateUpdateElements);
        this.onIndexedFieldList = builder.onIndexedFieldList;
        this.onUpdateFieldList = builder.onUpdateFieldList;

    }
    
    // --- Getters ---

    public String getDestinationUrl() { return destinationUrl; }

    public String getOnIndexedFieldList() {
        return onIndexedFieldList;
    }

    public String getOnUpdateFieldList() {
        return onUpdateFieldList;
    }

    public List<Element> getOnIndexEventUpdateElms() { return onIndexEventUpdateElms; }
    public List<Element> getOnIndexEventRemoveElms() { return onIndexEventRemoveElms; }
    public List<Element> getOnUpdateUpdateElements() { return onUpdateUpdateElements; }

    
    public static class Builder {
        // --- Defaults ---
        private String onIndexedFieldList;
        private String onUpdateFieldList;

        private String destinationUrl = "";
        private List<Element> onIndexEventUpdateElms = Collections.emptyList();
        private List<Element> onIndexEventRemoveElms = Collections.emptyList();
        private List<Element> onUpdateUpdateElements = Collections.emptyList();
        
        // --- Fluid Setter Methods ---

        public Builder onIndexedFieldList(String onIndexedFieldList) {
            this.onIndexedFieldList = onIndexedFieldList;
            return this;
        }

        public Builder onUpdateFieldList(String onUpdateFieldList) {
            this.onUpdateFieldList = onUpdateFieldList;
            return this;
        }

        public Builder destinationUrl(String destinationUrl) {
            this.destinationUrl = destinationUrl;
            return this;
        }

        // Setters for XML Element Lists (unchanged)
        public Builder onIndexEventUpdateElms(List<Element> elms) {
            this.onIndexEventUpdateElms = (elms != null) ? elms : Collections.emptyList();
            return this;
        }

        public Builder onIndexEventRemoveElms(List<Element> elms) {
            this.onIndexEventRemoveElms = (elms != null) ? elms : Collections.emptyList();
            return this;
        }

        public Builder onUpdateUpdateElements(List<Element> elms) {
            this.onUpdateUpdateElements = (elms != null) ? elms : Collections.emptyList();
            return this;
        }

        // --- Build Method ---
        public DestinationConfig build() {
            return new DestinationConfig(this);
        }
    }

    @Override
    public String toString() {
        return "DestinationConfig{" +
                "destinationUrl='" + destinationUrl + '\'' +
                ", onIndexEventUpdateElms=" + onIndexEventUpdateElms +
                ", onIndexEventRemoveElms=" + onIndexEventRemoveElms +
                ", onUpdateUpdateElements=" + onUpdateUpdateElements +
                '}';
    }
}