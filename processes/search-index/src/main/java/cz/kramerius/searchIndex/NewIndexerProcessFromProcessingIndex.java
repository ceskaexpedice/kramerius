package cz.kramerius.searchIndex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.execution.Counters;
import cz.kramerius.searchIndex.indexer.execution.IndexationType;
import cz.kramerius.searchIndex.indexer.execution.Indexer;
import cz.kramerius.searchIndex.indexer.execution.ProgressListener;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.processingindex.CursorItemsPair;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Builds a search index in a selected Solr collection from processing-index top models. */
public final class NewIndexerProcessFromProcessingIndex {
    private static final Logger LOGGER = Logger.getLogger(NewIndexerProcessFromProcessingIndex.class.getName());
    private static final String TOP_MODELS_CONFIG_KEY = "search.index.topModels";
    private static final int DEFAULT_BATCH_SIZE = 1000;
    private static final int MAX_BATCH_SIZE = 10000;

    private NewIndexerProcessFromProcessingIndex() { }

    public static void indexMain(String destinationUrl, Integer requestedBatchSize,
                                 Boolean ignoreInconsistentObjects, String requestedTopModels) {
        if (destinationUrl == null || destinationUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("destinationUrl is required");
        }
        int batchSize = requestedBatchSize == null ? DEFAULT_BATCH_SIZE : requestedBatchSize;
        if (batchSize < 1 || batchSize > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("batchSize must be between 1 and " + MAX_BATCH_SIZE);
        }
        List<String> topModels = resolveTopModels(requestedTopModels);

        String destination = destinationUrl.trim();
        PluginContext pluginContext = PluginContextHolder.getContext();
        pluginContext.updateProcessName("Search index from processing index to " + destination);

        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        AkubraRepository repository = injector.getInstance(Key.get(AkubraRepository.class));
        Indexer indexer = new Indexer(repository, new SolrConfig(destination), System.out,
                Boolean.TRUE.equals(ignoreInconsistentObjects));

        try {
            LOGGER.info("Building search index at " + destination + " for top models: " + topModels);
            Counters counters = new Counters();
            long indexedRoots = 0;
            long[] indexedObjects = {0};

            for (String model : topModels) {
                LOGGER.info("Reading top-level PIDs for model " + model);
                String cursor = "*";
                while (cursor != null) {
                    CursorItemsPair page = repository.pi().getByModelWithCursor(model, true, cursor, batchSize);
                    for (ProcessingIndexItem item : page.items()) {
                        String pid = item.source();
                        if (pid == null || pid.trim().isEmpty()) {
                            continue;
                        }
                        indexer.indexByObjectPid(pid, IndexationType.TREE_AND_FOSTER_TREES, counters, false,
                                new ProgressListener() {
                                    @Override
                                    public void onProgress(int processed) {
                                        indexedObjects[0]++;
                                    }

                                    @Override
                                    public void onFinished(int processed) { }
                                });
                        indexedRoots++;
                        if (indexedRoots % 100 == 0) {
                            LOGGER.info(String.format(Locale.ROOT,
                                    "Reindex progress: processed %s root PIDs | objects %s",
                                    formatCount(indexedRoots), formatCount(indexedObjects[0])));
                        }
                    }

                    String nextCursor = page.nextCursor();
                    cursor = nextCursor == null || cursor.equals(nextCursor) ? null : nextCursor;
                }
            }

            indexer.summary(Collections.emptyList(), counters);
            indexer.commmit(counters);
            LOGGER.info(String.format(Locale.ROOT,
                    "Reindex complete: processed %s root PIDs from models %s",
                    formatCount(indexedRoots), topModels));
        } catch (Exception e) {
            throw new RuntimeException("Search indexing from processing index failed", e);
        }
    }

    private static List<String> resolveTopModels(String requestedTopModels) {
        String configuredTopModels = KConfiguration.getInstance().getConfiguration().getString(TOP_MODELS_CONFIG_KEY);
        String source = requestedTopModels != null && !requestedTopModels.trim().isEmpty()
                ? requestedTopModels : configuredTopModels;
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("topModels is required as a process parameter or " + TOP_MODELS_CONFIG_KEY + " configuration value");
        }

        Set<String> models = Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(model -> !model.isEmpty())
                .map(model -> model.startsWith("model:") ? model.substring("model:".length()) : model)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (models.isEmpty()) {
            throw new IllegalArgumentException("topModels does not contain any model names");
        }
        return new ArrayList<>(models);
    }

    private static String formatCount(long count) {
        return String.format(Locale.ROOT, "%,d", count);
    }
}
