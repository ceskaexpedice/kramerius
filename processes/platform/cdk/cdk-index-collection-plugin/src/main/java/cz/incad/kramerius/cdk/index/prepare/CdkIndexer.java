package cz.incad.kramerius.cdk.index.prepare;

import java.util.ArrayList;
import java.util.List;

public class CdkIndexer {

    private static final String CDK_PREFIX = "cdk/";

    private final ProcessingIndexService processingIndexService;
    private final SearchIndexService searchIndex;

    public CdkIndexer(ProcessingIndexService processingIndexService, SearchIndexService searchIndex) {
        this.processingIndexService = processingIndexService;
        this.searchIndex = searchIndex;
    }

    /**
     * Zpracuje CDK collection, např. cdk-s1.
     * <p>
     * Předpokládá, že cdk-s1 a její podřízené
     * CDK collections už byly naindexovány běžným Indexerem.
     */
    public void index(String rootCollectionPid) {
        indexCollection(rootCollectionPid, new ArrayList<>());
    }

    /**
     * Zpracuje jednu CDK collection.
     * <p>
     * parentCollections obsahuje nadřazené CDK collections,
     * ale NE collectionPid samotné.
     */
    private void indexCollection(String collectionPid, List<String> parentCollections) {

        /*
         * CDK collections obsažené přímo v této collection.
         */
        List<String> childCollections = processingIndexService.getContainedCollections(collectionPid);

        /*
         * Reference typu:
         *
         *     cdk-s11 -> contains -> cdk/s1
         *
         * nebo:
         *
         *     cdk-s11 -> contains -> cdk/d1
         */
        List<String> references = processingIndexService.getContainedCdkReferences(collectionPid);

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
        collectionsForReference.addAll(parentCollections);

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
        List<String> newParentCollections = new ArrayList<>(parentCollections);
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
         * Indexeru MZK.
         */
        List<String> existing = document.getMultiValueField("in-collection");

        if (existing == null) {
            existing = new ArrayList<>();
        } else {
            existing = new ArrayList<>(existing);
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

        document.setField("in-collection", existing);

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
        if (directCdkCollection != null) {

            document.setField("in-collection-direct", directCdkCollection);
        }

        /*
         * Partial update pouze těchto dvou polí.
         * Ostatní pole dokumentu se nesmí měnit.
         */
        searchIndex.updateCollectionFields(document.getPid(), document.getField("in-collection"), document.getField("in-collection-direct"));
    }

    private String removeCdkPrefix(String pid) {

        if (!pid.startsWith(CDK_PREFIX)) {
            throw new IllegalArgumentException("Not a CDK reference: " + pid);
        }

        return pid.substring(CDK_PREFIX.length());
    }
}