
package cz.incad.kramerius.cdk.index1;

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

        // CDK hierarchy: cdk-s1 -> cdk-s11 -> reference c1
        processing.addCDKCollection("cdk-s1");
        processing.addCollection("cdk-s1", "cdk-s11");
        processing.addCDKReference("cdk-s11", "c1");

        search.addDocument(new SearchDocument(
                "c1",
                "p1/r1/c1",
                Collections.singletonList("existing-collection"),
                Collections.emptyList(),
                Collections.singletonList("p1/r1/c1")));

        search.addDocument(new SearchDocument(
                "s1",
                "p1/r1/c1/s1",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList("p1/r1/c1/s1")));

        search.addDocument(new SearchDocument(
                "s2",
                "p1/r1/c1/s2",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList("p1/r1/c1/s2")));

        search.setParent("s1", "c1");
        search.setParent("s2", "c1");

        synchronizer = new CDKCollectionSynchronizer(search, processing);
    }

    @Test
    public void shouldSynchronizeReferencedSubtree() {
        synchronizer.synchronize();

        SearchDocument root = search.get("c1");
        SearchDocument page1 = search.get("s1");
        SearchDocument page2 = search.get("s2");

        assertEquals(
                Arrays.asList(
                        "existing-collection",
                        "cdk-s1",
                        "cdk-s11"),
                root.getInCollections());

        assertEquals(
                Collections.singletonList("cdk-s11"),
                root.getInCollectionsDirect());

        assertEquals(
                Arrays.asList("cdk-s1", "cdk-s11"),
                page1.getInCollections());

        assertTrue(page1.getInCollectionsDirect().isEmpty());

        assertEquals(
                Arrays.asList("cdk-s1", "cdk-s11"),
                page2.getInCollections());

        assertTrue(page2.getInCollectionsDirect().isEmpty());

        assertEquals(
                Arrays.asList(
                        "p1/r1/c1",
                        "cdk-s1/cdk-s11/p1/r1/c1"),
                root.getPidPaths());

        assertEquals(
                Arrays.asList(
                        "p1/r1/c1/s1",
                        "cdk-s1/cdk-s11/p1/r1/c1/s1"),
                page1.getPidPaths());

        assertEquals(
                Arrays.asList(
                        "p1/r1/c1/s2",
                        "cdk-s1/cdk-s11/p1/r1/c1/s2"),
                page2.getPidPaths());
    }

    @Test
    public void repeatedSynchronizationShouldNotAddDuplicates() {
        synchronizer.synchronize();
        synchronizer.synchronize();

        SearchDocument root = search.get("c1");
        SearchDocument page1 = search.get("s1");
        SearchDocument page2 = search.get("s2");

        assertEquals(2, root.getPidPaths().size());
        assertEquals(2, page1.getPidPaths().size());
        assertEquals(2, page2.getPidPaths().size());

        assertEquals(
                Arrays.asList(
                        "existing-collection",
                        "cdk-s1",
                        "cdk-s11"),
                root.getInCollections());

        assertEquals(
                Arrays.asList("cdk-s1", "cdk-s11"),
                page1.getInCollections());

        assertEquals(
                Arrays.asList("cdk-s1", "cdk-s11"),
                page2.getInCollections());

        assertEquals(
                Collections.singletonList("cdk-s11"),
                root.getInCollectionsDirect());

        assertTrue(page1.getInCollectionsDirect().isEmpty());
        assertTrue(page2.getInCollectionsDirect().isEmpty());
    }

    @Test
    public void shouldRemoveReferenceAndItsPaths() {
        synchronizer.synchronize();

        processing.removeCDKReference("cdk-s11", "c1");

        synchronizer.synchronize();

        SearchDocument root = search.get("c1");
        SearchDocument page1 = search.get("s1");
        SearchDocument page2 = search.get("s2");

        assertEquals(
                Collections.singletonList("p1/r1/c1"),
                root.getPidPaths());

        assertEquals(
                Collections.singletonList("p1/r1/c1/s1"),
                page1.getPidPaths());

        assertEquals(
                Collections.singletonList("p1/r1/c1/s2"),
                page2.getPidPaths());

        assertEquals(
                Collections.singletonList("existing-collection"),
                root.getInCollections());

        assertTrue(root.getInCollectionsDirect().isEmpty());
        assertTrue(page1.getInCollections().isEmpty());
        assertTrue(page2.getInCollections().isEmpty());
    }

    @Test
    public void shouldPreserveOtherDirectMemberships() {
        synchronizer.synchronize();

        SearchDocument root = search.get("c1");

        root.setCollections(
                root.getInCollections(),
                Arrays.asList("cdk-s11", "cdk-other"));

        processing.addCDKCollection("cdk-other");
        processing.addCDKReference("cdk-other", "c1");

        synchronizer.synchronize();

        assertTrue(
                root.getInCollectionsDirect().contains("cdk-s11"));

        assertTrue(
                root.getInCollectionsDirect().contains("cdk-other"));
    }
}
