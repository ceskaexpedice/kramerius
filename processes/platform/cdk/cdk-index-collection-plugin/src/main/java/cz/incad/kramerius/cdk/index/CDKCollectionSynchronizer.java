package cz.incad.kramerius.cdk.index;

import java.util.*;

public class CDKCollectionSynchronizer {

    private final SearchIndexService searchIndex;
    private final CDKProcessingIndexService processingIndex;

    public CDKCollectionSynchronizer(SearchIndexService searchIndex, CDKProcessingIndexService processingIndex) {
        this.searchIndex = searchIndex;
        this.processingIndex = processingIndex;
    }

    /*
    public void clean() {
        List<SearchDocument> documents = searchIndex.getAllDocuments();
        for (SearchDocument document : documents) {
            // CDK collections samotné nečistíme.
            // Jejich hodnoty řeší CDKIndexer.
            if (processingIndex.isCDKCollection(document.getPid())) {
                continue;
            }
            List<String> inCollections = new ArrayList<>(document.getInCollections());
            String direct = document.getInCollectionsDirect();

            removeDeletedCDKValues(inCollections);

            String nonCDKDirect = direct;
            if (direct != null && processingIndex.isCDKCollection(direct) && searchIndex.get(direct) == null) {
                nonCDKDirect = null;
            }

            List<String> validCDKCollections = getValidCDKCollections(document.getPid());

            for (String cdkCollection : validCDKCollections) {
                if (!inCollections.contains(cdkCollection)) {
                    inCollections.add(cdkCollection);
                }
            }

            String cdkDirect = getDirectCDKCollection(document.getPid());
            String newDirect = nonCDKDirect;
            if (cdkDirect != null) {
                newDirect = cdkDirect;
            }
            searchIndex.updateCollectionsFields(document.getPid(), inCollections, newDirect);
        }
    }
*/
    /*
    private void removeDeletedCDKValues(List<String> inCollections) {

        Iterator<String> iterator = inCollections.iterator();
        while (iterator.hasNext()) {
            String collectionPid = iterator.next();

            if (!processingIndex.isCDKCollection(
                    collectionPid)) {
                continue;
            }

            if (searchIndex.get(collectionPid) == null) {
                iterator.remove();
            }
        }
    }*/

    public void clean() {
       // new DanglingCollectionReferenceCleanerSimple(searchIndex).clean();
        List<SearchDocument> documents = searchIndex.getAllDocuments();
        for (SearchDocument document : documents) {
            // CDK collections nečistíme.
            // Jejich vazby řeší samotný CDKIndexer.
            if (processingIndex.isCDKCollection(document.getPid())) {
                continue;
            }
            List<String> inCollections = new ArrayList<>(document.getInCollections());
            String direct = document.getInCollectionsDirect();

            // Odstraníme CDK hodnoty.
            // Původní hodnoty ze standardního indexu zachováme.
            removeCDKValues(inCollections);
            String nonCDKDirect = direct;
            if(direct != null && searchIndex.get(direct) == null) {
                nonCDKDirect = null;
            }else{
                if (direct != null && processingIndex.isCDKCollection(direct)) {
                    nonCDKDirect = null;
                }
            }

            // Znovu spočítáme, které CDK kolekce mají tento PID obsahovat.
            List<String> validCDKCollections = getValidCDKCollections(document.getPid());
            // Přidáme aktuálně platné CDK hodnoty.
            inCollections.addAll(validCDKCollections);

            // Spočítáme správnou CDK direct hodnotu.
            String cdkDirect = getDirectCDKCollection(document.getPid());
            String newDirect = nonCDKDirect;
            if (cdkDirect != null) {
                newDirect = cdkDirect;
            }

            searchIndex.updateCollectionsFields(document.getPid(), inCollections, newDirect);

        }
    }

    /**
     * Vrátí CDK kolekce, které mají obsahovat daný source PID
     * v in-collection.
     */
    private List<String> getValidCDKCollections(String sourcePid) {
        List<String> result = new ArrayList<>();
        List<SearchDocument> documents = searchIndex.getAllDocuments();
        for (SearchDocument document : documents) {
            String cdkCollection = document.getPid();
            if (!processingIndex.isCDKCollection(cdkCollection)) {
                continue;
            }
            Set<String> validSourcePids = new HashSet<>();
            Set<String> visitedCDKCollections = new HashSet<>();
            collectCDKSourceTrees(cdkCollection, visitedCDKCollections, validSourcePids);
            if (validSourcePids.contains(sourcePid)) {
                result.add(cdkCollection);
            }
        }
        return result;
    }

    /**
     * Projde celý CDK podstrom.
     * <p>
     * Například:
     * <p>
     * cdk-s1
     * └── cdk-s11
     * └── cdk/s1
     * <p>
     * Pro cdk-s1 tedy najde referenci cdk/s1,
     * i když ji nemá cdk-s1 přímo, ale až jeho potomek cdk-s11.
     */
    private void collectCDKSourceTrees(String cdkCollection, Set<String> visitedCDKCollections, Set<String> validSourcePids) {
        if (!visitedCDKCollections.add(cdkCollection)) {
            return;
        }
        /*
         * Nejprve reference této CDK kolekce.
         */
        List<String> references = processingIndex.getCDKReferences(cdkCollection);
        for (String reference : references) {
            String sourcePid = removeCdkPrefix(reference);
            collectSourceTree(sourcePid, validSourcePids);
        }

        /*
         * Potom všechny podřízené CDK kolekce.
         */
        List<String> children = processingIndex.getCollections(cdkCollection);
        for (String child : children) {
            collectCDKSourceTrees(child, visitedCDKCollections, validSourcePids);
        }
    }

    /**
     * Projde celý source strom od daného PID.
     * <p>
     * Například reference:
     * <p>
     * cdk/s1
     * <p>
     * znamená celý strom:
     * <p>
     * s1
     * └── s11
     * └── d1
     * └── d11
     */
    private void collectSourceTree(String rootPid, Set<String> result) {
        if (!result.add(rootPid)) {
            return;
        }
        List<String> children = searchIndex.getChildren(rootPid);
        for (String child : children) {
            collectSourceTree(child, result);
        }
    }

    /**
     * Vrátí CDK kolekci, která má být direct pro daný source PID.
     * <p>
     * Důležité:
     * pro direct se berou pouze reference přímo z dané CDK kolekce.
     * <p>
     * Takže:
     * <p>
     * cdk-s1
     * └── cdk-s11
     * └── cdk/s1
     * <p>
     * dává:
     * <p>
     * s1.in-collection-direct = cdk-s11
     * <p>
     * ale ne:
     * <p>
     * s11.in-collection-direct = cdk-s11
     */
    private String getDirectCDKCollection(String sourcePid) {
        List<SearchDocument> documents = searchIndex.getAllDocuments();
        for (SearchDocument document : documents) {
            String cdkCollection = document.getPid();
            if (!processingIndex.isCDKCollection(cdkCollection)) {
                continue;
            }
            List<String> references = processingIndex.getCDKReferences(cdkCollection);
            for (String reference : references) {
                String referencedSourcePid = removeCdkPrefix(reference);
                if (sourcePid.equals(referencedSourcePid)) {
                    return cdkCollection;
                }
            }
        }
        return null;
    }

    private void removeCDKValues(List<String> inCollections) {
        Iterator<String> iterator = inCollections.iterator();
        while (iterator.hasNext()) {
            String collection = iterator.next();
            if (searchIndex.get(collection) == null || processingIndex.isCDKCollection(collection)) {
                iterator.remove();
            }
        }
    }

    private String removeCdkPrefix(String reference) {
        if (reference.startsWith(CDKIndexer.CDK_PREFIX)) {
            return reference.substring(4);
        }
        return reference;
    }
}