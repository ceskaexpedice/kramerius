package cz.inovatika.kramerius.services.config;

// Importy pro zapouzdřené konfigurační objekty
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;

/**
 * Immutable root configuration object for a CDK replication process.
 * Encapsulates settings for the source (iterator) and the processing unit (worker).
 */
public class ProcessConfig {

    // --- Core Fields ---
    private final String sourceName;
    private final String name;
    private final int threads;

    private final String type;
    private final String workingTime;
    private final String timestampUrl;
    private final String introspectUrl;

    // --- Encapsulated Configs ---
    private final SolrIteratorConfig iteratorConfig;
    private final WorkerConfig workerConfig;

    private ProcessConfig(Builder builder) {
        this.sourceName = builder.sourceName;
        this.name = builder.name;
        this.threads = builder.threads;

        this.type = builder.type;
        this.workingTime = builder.workingTime;
        this.timestampUrl = builder.timestampUrl;
        this.introspectUrl = builder.introspectUrl;

        this.iteratorConfig = builder.iteratorConfig;
        this.workerConfig = builder.workerConfig;
    }

    // --- Getters ---
    public String getSourceName() { return sourceName; }
    public String getName() { return name; }
    public int getThreads() { return threads; }

    public String getType() { return type; }
    public String getWorkingTime() { return workingTime; }
    public String getTimestampUrl() { return timestampUrl; }
    public String getIntrospectUrl() { return introspectUrl; }

    // Factory class getters REMOVED
    public SolrIteratorConfig getIteratorConfig() { return iteratorConfig; }
    public WorkerConfig getWorkerConfig() { return workerConfig; }


    // ==========================================================
    // BUILDER PATTERN
    // ==========================================================

    public static class Builder {
        private String sourceName;
        private String name;
        private int threads = 1;
        // Factory class fields REMOVED

        private String type;
        private String workingTime;
        private String timestampUrl;
        private String introspectUrl;

        private SolrIteratorConfig iteratorConfig; 
        private WorkerConfig workerConfig;       

        // --- Fluid Setters ---
        public Builder sourceName(String sourceName) { this.sourceName = sourceName; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder threads(int threads) { this.threads = threads; return this; }
        public Builder type(String type) { this.type = type; return this; }

        public Builder workingTime(String workingTime) { this.workingTime = workingTime; return this; }
        public Builder timestampUrl(String timestampUrl) { this.timestampUrl = timestampUrl; return this; }
        public Builder introspectUrl(String introspectUrl) { this.introspectUrl = introspectUrl; return this; }

        public Builder iteratorConfig(SolrIteratorConfig config) { this.iteratorConfig = config; return this; }
        public Builder workerConfig(WorkerConfig config) { this.workerConfig = config; return this; }

        public ProcessConfig build() {
            if (sourceName == null || name == null || iteratorConfig == null || workerConfig == null) {
                // Upravená kontrola: již nevyžadujeme factory classy
                throw new IllegalStateException("CDKProcessConfig must contain sourceName, name, iteratorConfig, and workerConfig.");
            }
            return new ProcessConfig(this);
        }
    }

    @Override
    public String toString() {
        return "ProcessConfig{" +
                "sourceName='" + sourceName + '\'' +
                ", name='" + name + '\'' +
                ", threads=" + threads +
                ", type='" + type + '\'' +
                ", workingTime='" + workingTime + '\'' +
                ", timestampUrl='" + timestampUrl + '\'' +
                ", introspectUrl='" + introspectUrl + '\'' +
                ", iteratorConfig=" + iteratorConfig +
                ", workerConfig=" + workerConfig +
                '}';
    }
}