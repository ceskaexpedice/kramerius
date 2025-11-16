package cz.inovatika.kramerius.services.workers.config;


import cz.inovatika.kramerius.services.workers.config.destination.DestinationConfig;
import cz.inovatika.kramerius.services.workers.config.request.RequestConfig;

/**
 * Immutable configuration object for AbstractReplicateWorker, acting as the root
 * container for all worker configuration parts (Request, Destination, etc.).
 */
public class WorkerConfig {

    /** Factory class for creating worker instance */
    private final String factoryClz;

    // --- Encapsulated Configuration Objects ---
    private final RequestConfig requestConfig;
    private final DestinationConfig destinationConfig;


    // --- Constructor (used only by Builder) ---
    private WorkerConfig(Builder builder) {
        this.requestConfig = builder.requestConfig;
        this.destinationConfig = builder.destinationConfig;

        this.factoryClz = builder.factoryClz;
    }

    // --- Getters ---

    // Gettery pro zapouzdřené objekty
    public RequestConfig getRequestConfig() { return requestConfig; }
    public DestinationConfig getDestinationConfig() { return destinationConfig; }
    public String getFactoryClz() { return factoryClz; }


    // ==========================================================
    // BUILDER PATTERN
    // ==========================================================

    public static class Builder {
        // --- Encapsulated Configuration Objects (REQUIRED) ---
        private RequestConfig requestConfig;
        private DestinationConfig destinationConfig;

        // --- Remaining Fields with Default Values ---
        private String onIndexedFieldList = null;
        private String onUpdateFieldList = null;
        private String factoryClz = null;


        /**
         * Sets the Request configuration object.
         */
        public Builder requestConfig(RequestConfig requestConfig) {
            this.requestConfig = requestConfig;
            return this;
        }

        /**
         * Sets the Destination configuration object.
         */
        public Builder destinationConfig(DestinationConfig destinationConfig) {
            this.destinationConfig = destinationConfig;
            return this;
        }

        // --- Fluid Setter Methods for remaining fields ---

        public Builder onIndexedFieldList(String onIndexedFieldList) {
            this.onIndexedFieldList = onIndexedFieldList;
            return this;
        }

        public Builder onUpdateFieldList(String onUpdateFieldList) {
            this.onUpdateFieldList = onUpdateFieldList;
            return this;
        }

        public Builder factoryClz(String factoryClz) {
            this.factoryClz = factoryClz;
            return this;
        }

        // --- Build Method ---
        public WorkerConfig build() {
            if (this.requestConfig == null) {
                throw new IllegalStateException("RequestConfig must be set before building WorkerConfig.");
            }
            if (this.destinationConfig == null) {
                throw new IllegalStateException("DestinationConfig must be set before building WorkerConfig.");
            }
            return new WorkerConfig(this);
        }
    }

    @Override
    public String toString() {
        return "WorkerConfig{" +
                "requestConfig=" + requestConfig +
                ", destinationConfig=" + destinationConfig +
                ", factoryClz='" + factoryClz + '\'' +
                '}';
    }
}