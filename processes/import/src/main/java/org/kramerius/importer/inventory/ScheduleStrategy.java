package org.kramerius.importer.inventory;

import cz.incad.kramerius.utils.conf.KConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Defines various strategies for scheduling items for indexation from an {@link ImportInventory}.
 * <p>
 * This enum implements the Strategy design pattern, where each enum constant represents a
 * specific algorithm for selecting items to be indexed. ors.
 * <p>
 * The primary purpose of these strategies is to take a full {@link ImportInventory} plan and
 * filter or transform it into a refined list of items that should be scheduled for the
 * actual indexing process.
 */
public enum ScheduleStrategy {


        /**
         * Schedules only the root items (top-level objects) of each discovered hierarchy
         * for a full tree indexation.
         * <p>
         * This strategy is suitable for scenarios where you want to re-index entire
         * logical objects (like books or periodicals) from scratch, regardless of
         * which part of the hierarchy was changed.
         */
        indexRoots {
            @Override
            public List<ImportInventoryItem> scheduleItems(ImportInventory plan) {
                List<ImportInventoryItem> roots = new ArrayList<>();
                for (ImportInventoryItem item : plan.getIndexationPlanItems()) {
                    ImportInventoryItem root = item.findRoot();
                    if (root != null) {
                        root = root.withIndexationPlan(ImportInventoryItem.TypeOfSchedule.TREE);
                        if (!roots.contains(root)) {roots.add(root);}
                    }
                }
                return roots;
            }
        },

        /**
         * Schedules new or recently imported items and their ancestors for indexation.
         * <p>
         * This strategy is designed for incremental imports. It finds the highest
         * ancestor that has been newly imported and schedules it for a full tree
         * indexation. Additionally, it schedules all its other ancestors for
         * a partial (object-level) indexation to update their metadata.
         */
        indexNewImported {
            @Override
            public List<ImportInventoryItem> scheduleItems(ImportInventory plan) {
                Set<ImportInventoryItem> trees = new LinkedHashSet<>();
                Set<ImportInventoryItem> objects = new LinkedHashSet<>();

                for (ImportInventoryItem item : plan.getIndexationPlanItems()) {
                    ImportInventoryItem notIndexedAncestor = item.findHighestNotImportedAncestor();
                    if (notIndexedAncestor != null) {
                        trees.add(notIndexedAncestor.withIndexationPlan(ImportInventoryItem.TypeOfSchedule.TREE));
                        List<ImportInventoryItem> allAncestors = notIndexedAncestor.findAllAncestors();
                        for (ImportInventoryItem anc: allAncestors) {
                            objects.add(anc.withIndexationPlan(ImportInventoryItem.TypeOfSchedule.OBJECT));
                        }
                    }
                }
                List<ImportInventoryItem> retval = new ArrayList<>();
                retval.addAll(trees); retval.addAll(objects);
                return retval;
            }
        };

        public abstract List<ImportInventoryItem> scheduleItems(ImportInventory plan);

        public static ScheduleStrategy fromArg(String strategy) {
            LOGGER.fine("Resolving strategy: " + strategy);
            if (strategy == null) {
                String  configuredIndexType = KConfiguration.getInstance().getProperty("ingest.indexType",ScheduleStrategy.indexRoots.name());
                LOGGER.fine("Returning default strategy "+ScheduleStrategy.valueOf(configuredIndexType).name());
                return ScheduleStrategy.valueOf(configuredIndexType);
            }
            for (ScheduleStrategy plan : ScheduleStrategy.values()) {
                if (plan.name().equalsIgnoreCase(strategy)) {
                    return plan;
                }
            }

            String  configuredIndexType = KConfiguration.getInstance().getProperty("ingest.indexType",ScheduleStrategy.indexRoots.name());
            LOGGER.fine("Returning default strategy "+ScheduleStrategy.valueOf(configuredIndexType).name());
            return ScheduleStrategy.valueOf(configuredIndexType);
            //return indexRoots;
        }


    public static final Logger LOGGER = Logger.getLogger(ScheduleStrategy.class.getName());
}
