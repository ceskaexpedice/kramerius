package cz.incad.kramerius.cdk.index;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CDKIndexerTest {

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

        FakeCDKProcessingIndexService fakeProcessingIndexService = new FakeCDKProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();
        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);

        prepareData(fakeProcessingIndexService, fakeSearchIndexService);

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

        CDKIndexer indexer = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer.index("cdk-s1");

        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", fakeSearchIndexService.get("s1").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("s1").getInCollections()));
        //assertEquals(Arrays.asList("cdk-s11", "cdk-s1"), fakeSearchIndexService.get("s1").getInCollections());
        // s11
        assertEquals("s1", fakeSearchIndexService.get("s11").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("s11").getInCollections()));
      //  assertEquals(Arrays.asList("s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("s11").getInCollections());
        // d1
        assertEquals("s11", fakeSearchIndexService.get("d1").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("d1").getInCollections()));
//        assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("d1").getInCollections());
        // d11
        assertNull(fakeSearchIndexService.get("d11").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("d11").getInCollections()));
//        assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("d11").getInCollections());
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

        FakeCDKProcessingIndexService fakeProcessingIndexService = new FakeCDKProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();
        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);

        prepareData(fakeProcessingIndexService, fakeSearchIndexService);

        CDKIndexer indexer = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer.index("cdk-s1");
        // index again
        CDKIndexer indexer1 = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer1.index("cdk-s1");

        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", fakeSearchIndexService.get("s1").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("s1").getInCollections()));
//        assertEquals(Arrays.asList("cdk-s11", "cdk-s1"), fakeSearchIndexService.get("s1").getInCollections());
        // s11
        assertEquals("s1", fakeSearchIndexService.get("s11").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("s11").getInCollections()));
//        assertEquals(Arrays.asList("s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("s11").getInCollections());
        // d1
        assertEquals("s11", fakeSearchIndexService.get("d1").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("d1").getInCollections()));
//        assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("d1").getInCollections());
        // d11
        assertNull(fakeSearchIndexService.get("d11").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("d11").getInCollections()));
//        assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("d11").getInCollections());
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
        FakeCDKProcessingIndexService fakeProcessingIndexService = new FakeCDKProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();
        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);

        // 1. index without d12
        prepareData(fakeProcessingIndexService, fakeSearchIndexService);
        CDKIndexer indexer = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer.index("cdk-s1");

        // 2. simulate migration update process when d12 was migrated
        fakeSearchIndexService.addDocument("d12");
        fakeSearchIndexService.addChild("d1", "d12");
        fakeSearchIndexService.get("d12").setInCollections(Arrays.asList("s11", "s1"));
        fakeSearchIndexService.get("d12").setInCollectionsDirect(null);

        // d12 before
        assertNull(fakeSearchIndexService.get("d12").getInCollectionsDirect());
        assertEquals(Arrays.asList("s11", "s1"), fakeSearchIndexService.get("d12").getInCollections());

        // 3. index again with d12
        CDKIndexer indexer1 = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer1.index("cdk-s1");

        // d12 after
        assertNull(fakeSearchIndexService.get("d12").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("d12").getInCollections()));
        //assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), fakeSearchIndexService.get("d12").getInCollections());
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
        FakeCDKProcessingIndexService fakeProcessingIndexService = new FakeCDKProcessingIndexService();
        FakeSearchIndexService fakeSearchIndexService = new FakeSearchIndexService();
        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(fakeSearchIndexService, fakeProcessingIndexService);

        // 1. index with cdk/s1 present
        prepareData(fakeProcessingIndexService, fakeSearchIndexService);
        CDKIndexer indexer = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer.index("cdk-s1");
        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", fakeSearchIndexService.get("s1").getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("cdk-s11", "cdk-s1")),
                new HashSet<>(fakeSearchIndexService.get("s1").getInCollections()));
//        assertEquals(Arrays.asList("cdk-s11", "cdk-s1"), fakeSearchIndexService.get("s1").getInCollections());

        // 2. remove cdk/s1 from processing index
        fakeProcessingIndexService.removeCDKReference("cdk-s11", "cdk/s1");
        CDKIndexer indexer1 = new CDKIndexer(fakeProcessingIndexService, fakeSearchIndexService, cleaner);
        indexer1.index("cdk-s1");

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

    private static void prepareData(FakeCDKProcessingIndexService fakeProcessingIndexService, FakeSearchIndexService fakeSearchIndexService) {
        // CDK Akubra: cdk-s1 (Rels-ext: cdk-s11); cdk-s11 (Rels-ext: cdk/s1)
        // CDK processing index
        fakeProcessingIndexService.addCollection("cdk-s1", "cdk-s11");
        fakeProcessingIndexService.addCDKReference("cdk-s11", "cdk/s1");

        // CDK search index po behu originalniho Indexeru
        // CDK collections
        fakeSearchIndexService.addDocument("cdk-s1");
        fakeSearchIndexService.addDocument("cdk-s11");
        fakeSearchIndexService.addChild("cdk-s1", "cdk-s11");
        fakeSearchIndexService.addChild("cdk-s11", "s1");
        fakeSearchIndexService.get("cdk-s1").setInCollectionsDirect(null);
        fakeSearchIndexService.get("cdk-s1").setInCollections(Collections.emptyList());
        fakeSearchIndexService.get("cdk-s11").setInCollectionsDirect("cdk-s1");
        fakeSearchIndexService.get("cdk-s11").setInCollections(Arrays.asList("cdk-s1"));
        // MZK search tree
        fakeSearchIndexService.addDocument("s1");
        fakeSearchIndexService.addDocument("s11");
        fakeSearchIndexService.addDocument("d1");
        fakeSearchIndexService.addDocument("d11");
        fakeSearchIndexService.addChild("s1", "s11");
        fakeSearchIndexService.addChild("s11", "d1");
        fakeSearchIndexService.addChild("d1", "d11");
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