package cz.incad.kramerius.cdk.index;

import java.util.*;

public class DanglingCollectionReferenceCleanerSimple {

    private final SearchIndexService searchIndex;

    public DanglingCollectionReferenceCleanerSimple(SearchIndexService searchIndex) {
        this.searchIndex = searchIndex;
    }

    public void clean() {
        List<SearchDocument> documents = searchIndex.getAllDocuments();

        /*
         * Sesbíráme všechny PIDy, na které se
         * z collection fields odkazuje.
         */
        Set<String> referencedPids = new HashSet<>();

        for (SearchDocument document : documents) {
            List<String> inCollections = document.getInCollections();
            if (inCollections != null) {
                referencedPids.addAll(inCollections);
            }
            String direct = document.getInCollectionsDirect();
            if (direct != null) {
                referencedPids.add(direct);
            }
        }

        /*
         * Zjistíme, které z těchto PIDů skutečně
         * existují v search indexu.
         */
        Set<String> existingPids = searchIndex.getExistingPids(referencedPids);

        /*
         * Odstraníme pouze odkazy, které ukazují
         * na neexistující dokument.
         */
        for (SearchDocument document : documents) {
            List<String> inCollections = document.getInCollections();
            String direct = document.getInCollectionsDirect();
            boolean changed = false;
            List<String> cleanedInCollections = inCollections == null ? new ArrayList<>() : new ArrayList<>(inCollections);
            Iterator<String> iterator = cleanedInCollections.iterator();

            while (iterator.hasNext()) {
                String collectionPid = iterator.next();
                if (!existingPids.contains(collectionPid)) {
                    iterator.remove();
                    changed = true;
                }
            }

            String cleanedDirect = direct;

            if (direct != null && !existingPids.contains(direct)) {
                cleanedDirect = null;
                changed = true;
            }
            if (changed) {
                document.setInCollections(cleanedInCollections);
                document.setInCollectionsDirect(cleanedDirect);
                searchIndex.updateCollectionsFields(document.getPid(), cleanedInCollections, cleanedDirect);
            }
        }
    }
}