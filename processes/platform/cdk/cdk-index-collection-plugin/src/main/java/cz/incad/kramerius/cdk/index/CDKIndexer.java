package cz.incad.kramerius.cdk.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CDKIndexer {

    private static final String CDK_PREFIX = "cdk/";

    private final CDKProcessingIndexService cdkProcessingIndexService;
    private final SearchIndexService searchIndex;

    public CDKIndexer(CDKProcessingIndexService cdkProcessingIndexService, SearchIndexService searchIndex) {
        this.cdkProcessingIndexService = cdkProcessingIndexService;
        this.searchIndex = searchIndex;
    }

    /**
     * Zpracuje CDK collection, např. cdk-s1.
     * <p>
     * Předpokládá, že cdk-s1 a její podřízené
     * CDK collections už byly naindexovány běžným Indexerem.
     */
    public void index(String rootCollectionPid) {
        List<String> parentCollections = getParentCollections(rootCollectionPid);
        indexCollection(rootCollectionPid, parentCollections);
    }

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
    }

    /**
     * Zpracuje jednu CDK collection.
     * <p>
     * parentCollections obsahuje nadřazené CDK collections,
     * ale NE collectionPid samotné.
     */
    private void indexCollection(String collectionPid, List<String> ancestorCollections) {

        /*
         * CDK collections obsažené přímo v této collection.
         */
        List<String> childCollections = cdkProcessingIndexService.getCollections(collectionPid);

        /*
         * Reference typu:
         *
         *     cdk-s11 -> contains -> cdk/s1
         *
         * nebo:
         *
         *     cdk-s11 -> contains -> cdk/d1
         */
        List<String> references = cdkProcessingIndexService.getCDKReferences(collectionPid);

        /*
         * Pro objekt, na který reference ukazuje, jsou
         * nadřazené CDK collections:
         *
         *     collectionPid
         *     + parentCollections
         *
         * Např.:
         *
         *     cdk-s1
         *       |
         *       +-- cdk-s11
         *             |
         *             +-- cdk/s1
         *
         * reference s1 tedy dostane:
         *
         *     [cdk-s11, cdk-s1]
         */
        List<String> collectionsForReference = new ArrayList<>();

        collectionsForReference.add(collectionPid);
        collectionsForReference.addAll(ancestorCollections);

        /*
         * Zpracování všech objektů odkazovaných
         * z této CDK collection.
         */
        for (String cdkReference : references) {
            String sourcePid = removeCdkPrefix(cdkReference);
            indexSourceTree(sourcePid, collectionPid, collectionsForReference);
        }

        /*
         * Rekurzivní pokračování v CDK collection stromu.
         */
        List<String> newParentCollections = new ArrayList<>(ancestorCollections);
        newParentCollections.add(collectionPid);
        for (String childCollection : childCollections) {
            indexCollection(childCollection, newParentCollections);
        }
    }

    /**
     * Zpracuje objekt ze zdrojového search indexu
     * a celý jeho podstrom.
     * <p>
     * Např. pokud reference ukazuje na s1:
     * <p>
     * s1
     * └── s11
     * └── d1
     * └── d11
     * <p>
     * všechny tyto objekty dostanou CDK collections.
     */
    private void indexSourceTree(String pid, String directCdkCollection, List<String> cdkCollections) {
        SearchDocument document = searchIndex.get(pid);
        if (document == null) {
            /*
             * Objekt zatím není v search indexu.
             *
             * Podle skutečného požadavku může být vhodné
             * pouze zalogovat a pokračovat.
             */
            return;
        }

        /*
         * U kořenového objektu reference je
         * directCdkCollection vyplněna.
         *
         * U jeho potomků je null.
         */
        updateCollectionFields(document, directCdkCollection, cdkCollections);

        /*
         * Pokračujeme přes parentPid -> children.
         */
        List<String> children = searchIndex.getChildren(pid);

        for (String childPid : children) {
            indexSourceTree(childPid, null, cdkCollections);
        }
    }

    /**
     * Přidá CDK collections ke stávajícím
     * in-collection hodnotám.
     * <p>
     * Existující hodnoty NESMÍ být odstraněny.
     */
    private void updateCollectionFields(SearchDocument document, String directCdkCollection, List<String> cdkCollections) {

        /*
         * Původní hodnoty například:
         *
         *     [s11, s1]
         *
         * přičemž tyto hodnoty pocházejí z původního
         * Indexeru MZK a z minulych pruchodu CDK indexeru
         */
        List<String> existing = document.getInCollections();

        // Odstraníme staré CDK values.
        Iterator<String> iterator = existing.iterator();
        while (iterator.hasNext()) {
            String collection = iterator.next();
            if (cdkProcessingIndexService.isCDKCollection(collection)) {
                iterator.remove();
            }
        }
        /*
         * Přidáme CDK collections, ale bez duplicit.
         *
         * Např.:
         *
         *     [s11, s1]
         *
         * +   [cdk-s11, cdk-s1]
         *
         * =
         *
         *     [s11, s1, cdk-s11, cdk-s1]
         */
        for (String cdkCollection : cdkCollections) {
            if (!existing.contains(cdkCollection)) {
                existing.add(cdkCollection);
            }
        }

        document.setInCollections(existing);

        /*
         * Pouze kořen reference dostane
         * in-collection-direct.
         *
         * Např.:
         *
         *     cdk-s11 -> cdk/s1
         *
         * => s1.in-collection-direct = cdk-s11
         *
         * Ale s11, d1, d11 už tuto hodnotu
         * z této reference nedostanou.
         */
        String direct = document.getInCollectionsDirect();
        if (directCdkCollection != null) {
            direct = directCdkCollection;
        } else if (direct != null && cdkProcessingIndexService.isCDKCollection(direct)) {
            // There used to be a direct CDK membership, but there isn't one now.
            // Preserve a source direct collection if there is one.
            direct = null;
        }
        document.setInCollectionsDirect(direct);

        /*
         * Partial update pouze těchto dvou polí.
         * Ostatní pole dokumentu se nesmí měnit.
         */
        searchIndex.updateCollectionsFields(document.getPid(), existing, document.getInCollectionsDirect());
    }

    private String removeCdkPrefix(String pid) {
        if (!pid.startsWith(CDK_PREFIX)) {
            throw new IllegalArgumentException("Not a CDK reference: " + pid);
        }
        return pid.substring(CDK_PREFIX.length());
    }

}