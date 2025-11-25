package cz.inovatika.kramerius.services.config;

// Importy pro zapouzdřené konfigurační objekty
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;
import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;

/**
 * Immutable root configuration object for a replication process.
 * Encapsulates settings for the source (iterator) and the processing unit (worker).
 */
public class  ProcessConfig {

    /** Default source name **/
    public static final String DEFAULT_SOURCE_NAME="default";
    /** Default name */
    public static final String DEFAULT_NAME="default";

    // --- Core Fields ---
    /** source name; name of index or formal name of source; only information */
    private final String sourceName;
    /** Name of process; only information */
    private final String name;
    /** Type of migration; only information */
    private final String type;

    /** Threads - number of workers */
    private final int threads;

    /** working time; stops process if current time is not in the window */
    private final String workingTime;

    /** Timestamps; for incremental processing; */
    private final String timestampUrl;

    /** Introspect url for detecting conflicts */
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

    /**
     * Returns the name of the source being processed.
     * <p>
     * This value typically corresponds to the physical entity being iterated over,
     * which can be:
     * <ul>
     * <li>The **index name** (e.g., Solr core or collection) in standard processes.</li>
     * <li>A **logical identifier** like the **library name** in complex workflows (e.g., CDK migration),
     * used by workers to differentiate processing logic per source.</li>
     * </ul>
     * @return The source name.
     */
    public String getSourceName() { return sourceName; }

    /**
     * Returns name of the migration process
     * @return The process name
     */
    public String getName() { return name; }

    /**
     * Returns the descriptive type or name of the underlying process.
     * This value is used by more complex workers to distinguish between various
     * process types (beyond simple migration) and may influence their operational logic.
     * @return The process type identifier.
     */
    public String getType() { return type; }

    /**
     * Retrieves the count of worker threads to be initialized upon startup.
     * @return The number of workers.
     */
    public int getThreads() { return threads; }

    /**
     * Returns the defined working time window for the worker.
     * <p>
     * This value specifies a **timeframe** (if configured) during which the worker
     * is permitted to perform its tasks. If not defined, the worker typically operates continuously.
     * @return The configured working time string, or {@code null} if not specified.
     */
    public String getWorkingTime() { return workingTime; }
    /**
     * Returns the optional URL configured for fetching a control timestamp or marker.
     * <p>
     * This URL is queried to obtain the **starting point** (e.g., the last processed time)
     * used to define the range for incremental downloading of new or updated documents from Solr.
     * @return The timestamp source URL string, or {@code null} if not configured.
     */
    public String getTimestampUrl() { return timestampUrl; }

    /**
     * Returns the optional URL configured for fetching a control timestamp or marker.
     * <p>
     * This URL is queried to obtain the **starting point** (e.g., the last processed time)
     * used to define the range for incremental downloading of new or updated documents from Solr.
     * @return The timestamp source URL string, or {@code null} if not configured.
     */
    public String getIntrospectUrl() { return introspectUrl; }

    // Factory class getters REMOVED
    /**
     * Returns the immutable configuration object used to set up the Solr iterator.
     * <p>
     * This configuration encapsulates all necessary parameters (URLs, queries, sorting, etc.)
     * required for the worker to initialize and run the Solr iteration process.
     * @return The {@link SolrIteratorConfig} instance.
     */
    public SolrIteratorConfig getIteratorConfig() { return iteratorConfig; }

    /**
     * Returns the configuration object specific to the worker process.
     * <p>
     * This configuration typically contains parameters related to worker execution,
     * such as the number of threads, working time windows, source names, and type identifiers.
     * @return The {@code WorkerConfig} instance.
     */
    public WorkerConfig getWorkerConfig() { return workerConfig; }


    // ==========================================================
    // BUILDER PATTERN
    // ==========================================================

    public static class Builder {
        private String sourceName = DEFAULT_SOURCE_NAME;
        private String name = DEFAULT_NAME;
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
                throw new IllegalStateException("ProcessConfig must contain sourceName, name, iteratorConfig, and workerConfig.");
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