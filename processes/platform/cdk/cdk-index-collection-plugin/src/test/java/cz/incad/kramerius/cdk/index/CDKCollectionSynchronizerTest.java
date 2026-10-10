package cz.incad.kramerius.cdk.index;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CDKCollectionSynchronizerTest {

    @Test
    public void addCDKCollectionsWithMZKRelation() {
        /*
         * MZK:
         *
         * s1
         * └── s11
         *     └── d1
         *         └── d11
         *
         * CDK:
         *
         * cdk-s1
         * └── cdk-s11
         *     └── cdk/s1
         */
        FakeSearchIndexService search = new FakeSearchIndexService();
        FakeProcessingIndexService processing = new FakeProcessingIndexService();
        prepareData(processing, search);

        // s1
        assertNull(search.get("s1").getInCollectionsDirect());
        assertEquals(0, search.get("s1").getInCollections().size());
        // s11
        assertEquals("s1", search.get("s11").getInCollectionsDirect());
        assertEquals(Arrays.asList("s1"), search.get("s11").getInCollections());
        // d1
        assertEquals("s11", search.get("d1").getInCollectionsDirect());
        assertEquals(Arrays.asList("s11", "s1"), search.get("d1").getInCollections());
        // d11
        assertNull(search.get("d11").getInCollectionsDirect());
        assertEquals(Arrays.asList("s11", "s1"), search.get("d11").getInCollections());

        CDKCollectionSynchronizer synchronizer = new CDKCollectionSynchronizer(search, processing, 2);
        synchronizer.synchronize();

        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", search.get("s1").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("cdk-s1", "cdk-s11")), new HashSet<>(search.get("s1").getInCollections()));
        // s11
        assertEquals("s1", search.get("s11").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("cdk-s1", "cdk-s11", "s1")), new HashSet<>(search.get("s11").getInCollections()));
        // d1
        assertEquals("s11", search.get("d1").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("cdk-s1", "cdk-s11", "s1", "s11")), new HashSet<>(search.get("d1").getInCollections()));
        // d11
        assertNull(search.get("d11").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("cdk-s1", "cdk-s11", "s1", "s11")), new HashSet<>(search.get("d11").getInCollections()));
    }

    @Test
    public void reindexExistingCDKCollections() {
        /*
         * MZK:
         *
         * s1
         * └── s11
         *     └── d1
         *         └── d11
         *
         * CDK:
         *
         * cdk-s1
         * └── cdk-s11
         *     └── cdk/s1
         */

        FakeProcessingIndexService fakeProcessingIndexService = new FakeProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();
        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);

        prepareData(fakeProcessingIndexService, fakeSearchIndexService);

        CDKCollectionSynchronizer indexer = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);
        indexer.synchronize();
        // index again
        CDKCollectionSynchronizer indexer1 = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);
        indexer1.synchronize();

        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", fakeSearchIndexService.get("s1").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("cdk-s11", "cdk-s1")), new HashSet<>(fakeSearchIndexService.get("s1").getInCollections()));
        // s11
        assertEquals("s1", fakeSearchIndexService.get("s11").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("s1", "cdk-s11", "cdk-s1")), new HashSet<>(fakeSearchIndexService.get("s11").getInCollections()));
        // d1
        assertEquals("s11", fakeSearchIndexService.get("d1").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")), new HashSet<>(fakeSearchIndexService.get("d1").getInCollections()));
        // d11
        assertNull(fakeSearchIndexService.get("d11").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")), new HashSet<>(fakeSearchIndexService.get("d11").getInCollections()));
    }

    @Test
    public void newMZKBookAddedAndMigrated() {
        /*
         * MZK:
         *
         * s1
         * └── s11
         *     └── d1
         *         └── d11
         *         └── d12 - pridano pri update knihovny - ne pres admin prostredi
         *
         * CDK:
         *
         * cdk-s1
         * └── cdk-s11
         *     └── cdk/s1
         */
        FakeProcessingIndexService fakeProcessingIndexService = new FakeProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();

        // 1. index without d12
        prepareData(fakeProcessingIndexService, fakeSearchIndexService);
        CDKCollectionSynchronizer indexer = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);
        indexer.synchronize();

        // 2. simulate migration update process when d12 was migrated
        fakeSearchIndexService.addDocument("d12");
        fakeSearchIndexService.setParent("d12", "d1");
        fakeSearchIndexService.get("d12").setInCollections(Arrays.asList("s11", "s1"));
        fakeSearchIndexService.get("d12").setInCollectionsDirect(null);

        // d12 before
        assertNull(fakeSearchIndexService.get("d12").getInCollectionsDirect());
        assertEquals(Arrays.asList("s11", "s1"), fakeSearchIndexService.get("d12").getInCollections());

        // 3. index again with d12
        CDKCollectionSynchronizer indexer1 = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);
        indexer1.synchronize();

        // d12 after
        assertNull(fakeSearchIndexService.get("d12").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")), new HashSet<>(fakeSearchIndexService.get("d12").getInCollections()));
    }

    @Test
    public void removeCDKRelation() {
        /*
         * MZK:
         *
         * s1
         * └── s11
         *     └── d1
         *         └── d11
         *
         * CDK:
         *
         * cdk-s1
         * └── cdk-s11
         *     └──cdk/s1 - removed
         */
        FakeProcessingIndexService fakeProcessingIndexService = new FakeProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();

        // 1. index with cdk/s1 present
        prepareData(fakeProcessingIndexService, fakeSearchIndexService);
        CDKCollectionSynchronizer indexer = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);
        indexer.synchronize();
        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", fakeSearchIndexService.get("s1").getInCollectionsDirect());
        assertEquals(new HashSet<>(Arrays.asList("cdk-s11", "cdk-s1")), new HashSet<>(fakeSearchIndexService.get("s1").getInCollections()));

        // 2. remove cdk/s1 from processing index
        fakeProcessingIndexService.removeCDKReference("cdk-s11", "cdk/s1");
        CDKCollectionSynchronizer indexer1 = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);
        indexer1.synchronize();

        // the original MZK tree must be restored to is original values
        // s1
        assertNull(fakeSearchIndexService.get("s1").getInCollectionsDirect());
        assertEquals(0, fakeSearchIndexService.get("s1").getInCollections().size());
        // s11
        assertEquals("s1", fakeSearchIndexService.get("s11").getInCollectionsDirect());
        assertEquals(Arrays.asList("s1"), fakeSearchIndexService.get("s11").getInCollections());
        // d1
        assertEquals("s11", fakeSearchIndexService.get("d1").getInCollectionsDirect());
        assertEquals(Arrays.asList("s11", "s1"), fakeSearchIndexService.get("d1").getInCollections());
        // d11
        assertNull(fakeSearchIndexService.get("d11").getInCollectionsDirect());
        assertEquals(Arrays.asList("s11", "s1"), fakeSearchIndexService.get("d11").getInCollections());
    }

    @Test
    public void shouldRemoveDanglingCollectionReferences() {
        FakeSearchIndexService search = new FakeSearchIndexService();
        FakeProcessingIndexService fakeProcessingIndexService = new FakeProcessingIndexService();

        search.addDocument("s1");
        search.addDocument("s11");
        search.addDocument("d1");
        search.addDocument("d11");
        search.addDocument("cdk-s1");
        fakeProcessingIndexService.addCDKReference("cdk-s1", "d1");

        SearchDocument d1 = search.get("d1");
        d1.setInCollections(Arrays.asList("s11", "s1", "cdk-s1", "cdk-deleted"));
        d1.setInCollectionsDirect("cdk-deleted");

        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(search, fakeProcessingIndexService);
        cleaner.synchronize();

        assertEquals(Arrays.asList("s11", "s1", "cdk-s1"), search.get("d1").getInCollections());
        assertEquals("cdk-s1", search.get("d1").getInCollectionsDirect());
    }

    @Test
    public void shouldKeepExistingCollectionReferences() {
        FakeSearchIndexService search = new FakeSearchIndexService();
        FakeProcessingIndexService fakeProcessingIndexService = new FakeProcessingIndexService();

        search.addDocument("s1");
        search.addDocument("cdk-s1");
        search.addDocument("d1");
        fakeProcessingIndexService.addCDKReference("cdk-s1", "d1");
        SearchDocument d1 = search.get("d1");
        d1.setInCollections(Arrays.asList("s1", "cdk-s1"));
        d1.setInCollectionsDirect("cdk-s1");

        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(search, fakeProcessingIndexService);
        cleaner.synchronize();

        assertEquals(Arrays.asList("s1", "cdk-s1"), search.get("d1").getInCollections());
        assertEquals("cdk-s1", search.get("d1").getInCollectionsDirect());
    }

//    @Test
//    public void shouldPreserveNonCDKCollections() {
//        FakeSearchIndexService search = new FakeSearchIndexService();
//
//        search.addDocument("s1", Arrays.asList("normal-collection"), "normal-collection");
//
//        FakeProcessingIndexService processing = new FakeProcessingIndexService();
//
//        processing.addCDKCollection("cdk-s1");
//
//        processing.addCDKReference("cdk-s1", "cdk/s1");
//
//        CDKCollectionSynchronizer synchronizer = new CDKCollectionSynchronizer(search, processing);
//
//        synchronizer.synchronize();
//
//        assertEquals(new HashSet<>(Arrays.asList("normal-collection", "cdk-s1")), new HashSet<String>(search.get("s1").getInCollections()));
//
//        /*
//         * CDK direct overrides the previous direct.
//         */
//        assertEquals("cdk-s1", search.get("s1").getInCollectionsDirect());
//    }

//    @Test
//    public void shouldRemoveOldCDKMembership() {
//        FakeSearchIndexService search = new FakeSearchIndexService();
//
//        search.addDocument("s1", Arrays.asList("normal-collection", "cdk-old"), "cdk-old");
//
//        FakeProcessingIndexService processing = new FakeProcessingIndexService();
//
//        /*
//         * cdk-old už v processing indexu neexistuje.
//         *
//         * Proto se nemá znovu objevit.
//         */
//        processing.addCDKCollection("cdk-new");
//
//        processing.addCDKReference("cdk-new", "cdk/s1");
//
//        CDKCollectionSynchronizer synchronizer = new CDKCollectionSynchronizer(search, processing);
//
//        synchronizer.synchronize();
//
//        assertEquals(Arrays.asList("normal-collection", "cdk-new"), search.get("s1").getInCollections());
//
//        assertEquals("cdk-new", search.get("s1").getInCollectionsDirect());
//    }
//

    private static void prepareData(FakeProcessingIndexService fakeProcessingIndexService, FakeSearchIndexService fakeSearchIndexService) {
        // CDK Akubra: cdk-s1 (Rels-ext: cdk-s11); cdk-s11 (Rels-ext: cdk/s1)
        // CDK processing index
        fakeProcessingIndexService.addCollection("cdk-s1", "cdk-s11");
        fakeProcessingIndexService.addCDKReference("cdk-s11", "cdk/s1");

        // CDK search index po behu originalniho Indexeru
        // CDK collections
        fakeSearchIndexService.addDocument("cdk-s1");
        fakeSearchIndexService.addDocument("cdk-s11");
        fakeSearchIndexService.setParent("cdk-s11", "cdk-s1");
        fakeSearchIndexService.get("cdk-s1").setInCollectionsDirect(null);
        fakeSearchIndexService.get("cdk-s1").setInCollections(Collections.emptyList());
        fakeSearchIndexService.get("cdk-s11").setInCollectionsDirect("cdk-s1");
        fakeSearchIndexService.get("cdk-s11").setInCollections(Arrays.asList("cdk-s1"));
        // MZK search tree
        fakeSearchIndexService.addDocument("s1");
        fakeSearchIndexService.addDocument("s11");
        fakeSearchIndexService.addDocument("d1");
        fakeSearchIndexService.addDocument("d11");
        fakeSearchIndexService.setParent("s11", "s1");
        fakeSearchIndexService.setParent("d1", "s11");
        fakeSearchIndexService.setParent("d11", "d1");
        // in_collections, in_collections.direct
        fakeSearchIndexService.get("s1").setInCollections(Collections.emptyList());
        fakeSearchIndexService.get("s1").setInCollectionsDirect(null);
        fakeSearchIndexService.get("s11").setInCollections(Arrays.asList("s1"));
        fakeSearchIndexService.get("s11").setInCollectionsDirect("s1");
        fakeSearchIndexService.get("d1").setInCollections(Arrays.asList("s11", "s1"));
        fakeSearchIndexService.get("d1").setInCollectionsDirect("s11");
        fakeSearchIndexService.get("d11").setInCollections(Arrays.asList("s11", "s1"));
        fakeSearchIndexService.get("d11").setInCollectionsDirect(null);
    }

}