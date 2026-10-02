package cz.incad.kramerius.cdk.index;

public class CDKIndexer {

    public static final String CDK_PREFIX = "cdk/";

    private final CDKProcessingIndexService cdkProcessingIndexService;
    private final SearchIndexService searchIndex;
    private final CDKCollectionSynchronizer cleaner;

    public CDKIndexer(
            CDKProcessingIndexService processingIndexService,
            SearchIndexService searchIndex,
            CDKCollectionSynchronizer cleaner) {

        this.cdkProcessingIndexService = processingIndexService;
        this.searchIndex = searchIndex;
        this.cleaner = cleaner;
    }

    /**
     * Zpracuje CDK collection
     */
    public void index(String rootCollectionPid) {
        if(cleaner != null){
            cleaner.clean();
        }
       // List<String> parentCollections = getParentCollections(rootCollectionPid);
       // indexCollection(rootCollectionPid, parentCollections);
    }

    /*
    private List<String> getParentCollections(String collectionPid) {
        List<String> result = new ArrayList<>();
        String current = collectionPid;
        while (true) {
            String parent = cdkProcessingIndexService.getParentCollection(current);
            if (parent == null) {
                break;
            }
            result.add(parent);
            current = parent;
        }
        return result;
    }*/
/*
    private void indexCollection(String collectionPid, List<String> ancestorCollections) {
        List<String> childCollections = cdkProcessingIndexService.getCollections(collectionPid);
        List<String> references = cdkProcessingIndexService.getCDKReferences(collectionPid);

        List<String> collectionsForReference = new ArrayList<>();
        collectionsForReference.add(collectionPid);
        collectionsForReference.addAll(ancestorCollections);

        for (String cdkReference : references) {
            String sourcePid = removeCdkPrefix(cdkReference);
            indexSourceTree(sourcePid, collectionPid, collectionsForReference);
        }

        List<String> newParentCollections = new ArrayList<>(ancestorCollections);
        newParentCollections.add(collectionPid);
        for (String childCollection : childCollections) {
            indexCollection(childCollection, newParentCollections);
        }
    }
*/
    /**
     * Zpracuje objekt ze zdrojového search indexu a celý jeho podstrom.
     */
  /*
    private void indexSourceTree(String pid, String directCdkCollection, List<String> cdkCollections) {
        SearchDocument document = searchIndex.get(pid);
        if (document == null) {
            return;
        }
        updateCollectionFields(document, directCdkCollection, cdkCollections);
        List<String> children = searchIndex.getChildren(pid);
        for (String childPid : children) {
            indexSourceTree(childPid, null, cdkCollections);
        }
    }

   */
/*
    private void updateCollectionFields(SearchDocument document, String directCdkCollection, List<String> cdkCollections) {
        List<String> existing = document.getInCollections();
        Iterator<String> iterator = existing.iterator();
        while (iterator.hasNext()) {
            String collection = iterator.next();
            if (cdkProcessingIndexService.isCDKCollection(collection)) {
                iterator.remove();
            }
        }
        for (String cdkCollection : cdkCollections) {
            if (!existing.contains(cdkCollection)) {
                existing.add(cdkCollection);
            }
        }
        document.setInCollections(existing);

        String direct = document.getInCollectionsDirect();
        if (directCdkCollection != null) {
            direct = directCdkCollection;
        } else if (direct != null && cdkProcessingIndexService.isCDKCollection(direct)) {
            direct = null;
        }
        document.setInCollectionsDirect(direct);

        searchIndex.updateCollectionsFields(document.getPid(), existing, document.getInCollectionsDirect());
    }
*/
    /*
    private String removeCdkPrefix(String pid) {
        if (!pid.startsWith(CDK_PREFIX)) {
            throw new IllegalArgumentException("Not a CDK reference: " + pid);
        }
        return pid.substring(CDK_PREFIX.length());
    }*/

}