
package cz.incad.kramerius.cdkcollection;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class CDKCollectionSynchronizerTest {

    private FakeSearchIndexService search;
    private FakeProcessingIndexService processing;
    private CDKCollectionSynchronizer synchronizer;

    @Before
    public void setUp() {
        search = new FakeSearchIndexService();
        processing = new FakeProcessingIndexService();
        /*
         * MZK:
         *
         * sb1
         * └── per1
         *     └── roc1
         *         └── cis1
         *             └── st1
         *             └── st2
         *
         * CDK:
         *
         * cdk-sb1
         * └── cdk-sb11
         *     └── cdk/cis1
         */

        // MZK (migrated from source library)
        search.addDocument(
                new SearchDocument("sb1", "sb1",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("sb1"), false));
        search.addDocument(
                new SearchDocument("per1", "per1",
                        Collections.singletonList("sb1"), Collections.singletonList("sb1"),
                        Collections.singletonList("sb1/p1"), false));
        search.addDocument(
                new SearchDocument("roc1", "per1/roc1",
                        Collections.singletonList("sb1"), Collections.emptyList(),
                        Collections.singletonList("sb1/per1/roc1"), false));
        search.addDocument(
                new SearchDocument("cis1", "per1/roc1/cis1",
                        Collections.singletonList("sb1"), Collections.emptyList(),
                        Collections.singletonList("sb1/per1/roc1/cis1"), false));
        search.addDocument(
                new SearchDocument("st1", "per1/roc1/cis1/st1",
                        Collections.singletonList("sb1"), Collections.emptyList(),
                        Collections.singletonList("sb1/per1/roc1/cis1/st1"), false));
        search.addDocument(
                new SearchDocument("st2", "per1/roc1/cis1/st2",
                        Collections.singletonList("sb1"), Collections.emptyList(),
                        Collections.singletonList("sb1/per1/roc1/cis1/st2"), false));

        search.setParent("per1", "sb1");
        search.setParent("roc1", "per1");
        search.setParent("cis1", "roc1");
        search.setParent("st1", "cis1");
        search.setParent("st2", "cis1");

        // CDK collections before sync
        processing.addCDKCollection("cdk-sb1");
        processing.addCollection("cdk-sb1", "cdk-sb11");
        processing.addCDKReference("cdk-sb11", "cis1");
        search.addDocument(
                new SearchDocument("cdk-sb1", "cdk-sb1",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("cdk-sb1"), true));
        search.addDocument(
                new SearchDocument("cdk-sb11", "cdk-sb1/cdk-sb11",
                        Collections.singletonList("cdk-sb1"), Collections.singletonList("cdk-sb1"),
                        Collections.singletonList("cdk-sb1/cdk-sb11"), true));
        search.setParent("cdk-sb11", "cdk-sb1");

        synchronizer = new CDKCollectionSynchronizer(search, processing);
    }

    @Test
    public void shouldSynchronizeReferencedSubtree() {
        SearchDocument cis1 = search.get("cis1");
        assertEquals(Arrays.asList("sb1"), cis1.getInCollections());
        assertTrue(cis1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1"), cis1.getPidPaths());

        SearchDocument st1 = search.get("st1");
        assertEquals(Arrays.asList("sb1"), st1.getInCollections());
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st1"), st1.getPidPaths());

        SearchDocument st2 = search.get("st2");
        assertEquals(Arrays.asList("sb1"), st2.getInCollections());
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st2"), st2.getPidPaths());

        synchronizer.synchronize();

        assertEquals(Arrays.asList("sb1", "cdk-sb1", "cdk-sb11"), cis1.getInCollections());
        assertEquals(Collections.singletonList("cdk-sb11"), cis1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1", "cdk-sb1/cdk-sb11/cis1"), cis1.getPidPaths());

        assertEquals(Arrays.asList("sb1", "cdk-sb1", "cdk-sb11"), st1.getInCollections());
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st1", "cdk-sb1/cdk-sb11/cis1/st1"), st1.getPidPaths());

        assertEquals(Arrays.asList("sb1", "cdk-sb1", "cdk-sb11"), st2.getInCollections());
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st2", "cdk-sb1/cdk-sb11/cis1/st2"), st2.getPidPaths());
    }

    @Test
    public void repeatedSynchronizationShouldNotAddDuplicates() {
        SearchDocument cis1 = search.get("cis1");
        SearchDocument st1 = search.get("st1");
        SearchDocument st2 = search.get("st2");

        synchronizer.synchronize();

        assertEquals(2, cis1.getPidPaths().size());
        assertEquals(2, st1.getPidPaths().size());
        assertEquals(2, st2.getPidPaths().size());

        synchronizer.synchronize();

        assertEquals(2, cis1.getPidPaths().size());
        assertEquals(2, st1.getPidPaths().size());
        assertEquals(2, st2.getPidPaths().size());

        assertEquals(Arrays.asList("sb1", "cdk-sb1", "cdk-sb11"), cis1.getInCollections());
        assertEquals(Arrays.asList("sb1", "cdk-sb1", "cdk-sb11"), st1.getInCollections());
        assertEquals(Arrays.asList("sb1", "cdk-sb1", "cdk-sb11"), st2.getInCollections());
        assertEquals(Collections.singletonList("cdk-sb11"), cis1.getInCollectionsDirect());
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertTrue(st2.getInCollectionsDirect().isEmpty());
    }

    @Test
    public void shouldRemoveReferenceAndItsPaths() {
        synchronizer.synchronize();

        processing.removeCDKReference("cdk-sb11", "cis1");

        synchronizer.synchronize();

        SearchDocument cis1 = search.get("cis1");
        SearchDocument st1 = search.get("st1");
        SearchDocument st2 = search.get("st2");

        assertEquals(Collections.singletonList("sb1/per1/roc1/cis1"), cis1.getPidPaths());
        assertEquals(Collections.singletonList("sb1/per1/roc1/cis1/st1"), st1.getPidPaths());
        assertEquals(Collections.singletonList("sb1/per1/roc1/cis1/st2"), st2.getPidPaths());
        assertEquals(Collections.singletonList("sb1"), cis1.getInCollections());
        assertTrue(cis1.getInCollectionsDirect().isEmpty());
        assertEquals(Collections.singletonList("sb1"), st1.getInCollections());
        assertEquals(Collections.singletonList("sb1"), st2.getInCollections());
    }

    @Test
    public void shouldPreserveOtherDirectMemberships() {
        synchronizer.synchronize();

        SearchDocument cis1 = search.get("cis1");

        cis1.setCollections(cis1.getInCollections(), Arrays.asList("cdk-sb11", "cdk-other"));

        processing.addCDKCollection("cdk-other");
        processing.addCDKReference("cdk-other", "cis1");

        synchronizer.synchronize();

        assertTrue(cis1.getInCollectionsDirect().contains("cdk-sb11"));
        assertTrue(cis1.getInCollectionsDirect().contains("cdk-other"));
    }
}
