

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

        // Root document
        search.addDocument(new SearchDocument(
                "c1",
                "p1/r1/c1",
                Collections.singletonList("existing-collection"),
                null,
                Collections.singletonList("p1/r1/c1")));

        // Descendant documents
        search.addDocument(new SearchDocument(
                "s1",
                "p1/r1/c1/s1",
                Collections.emptyList(),
                null,
                Collections.singletonList("p1/r1/c1/s1")));

        search.addDocument(new SearchDocument(
                "s2",
                "p1/r1/c1/s2",
                Collections.emptyList(),
                null,
                Collections.singletonList("p1/r1/c1/s2")));

        // Search hierarchy: c1 -> s1, s2
        search.setParent("s1", "c1");
        search.setParent("s2", "c1");

        synchronizer = new CDKCollectionSynchronizer(
                search,
                processing);
    }

    @Test
    public void shouldSynchronizeReferencedSubtree() {
        synchronizer.synchronize();

        SearchDocument root = search.get("c1");
        SearchDocument page1 = search.get("s1");
        SearchDocument page2 = search.get("s2");

        // Root gets both CDK memberships and preserves existing values.
        assertEquals(
                Arrays.asList(
                        "existing-collection",
                        "cdk-s1",
                        "cdk-s11"),
                root.getInCollections());

        assertEquals("cdk-s11", root.getInCollectionsDirect());

        // Descendants get memberships, but no direct membership.
        assertEquals(
                Arrays.asList("cdk-s1", "cdk-s11"),
                page1.getInCollections());

        assertNull(page1.getInCollectionsDirect());

        assertEquals(
                Arrays.asList("cdk-s1", "cdk-s11"),
                page2.getInCollections());

        assertNull(page2.getInCollectionsDirect());

        // Original paths remain and CDK paths are added.
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

        SearchDocument root = search.get("c1");
        SearchDocument page1 = search.get("s1");
        SearchDocument page2 = search.get("s2");

        // Save state after the first synchronization.
        assertEquals(2, root.getPidPaths().size());
        assertEquals(2, page1.getPidPaths().size());
        assertEquals(2, page2.getPidPaths().size());

        synchronizer.synchronize();

        // Paths must not be duplicated.
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

        // Memberships must remain unchanged.
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

        assertEquals("cdk-s11", root.getInCollectionsDirect());
        assertNull(page1.getInCollectionsDirect());
        assertNull(page2.getInCollectionsDirect());
    }
}
