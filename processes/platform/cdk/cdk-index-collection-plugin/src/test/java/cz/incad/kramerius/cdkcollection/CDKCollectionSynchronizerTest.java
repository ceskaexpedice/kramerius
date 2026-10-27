
package cz.incad.kramerius.cdkcollection;

import org.ceskaexpedice.akubra.reference.CDKReference;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

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
         * cdk-sb11
         *     └── cdk/cis1
         * cdk-sb1
         * └── cdk/cis1
         * └── cdk-sb11
         * cdk-sb2
         * └── cdk/cis1
         * └── cdk/roc1
         * cdk-sb3
         * cdk-sb4
         *     └── cdk-sb11
         */

        // search index after MZK migration
        search.addDocument(
                new SearchDocument("sb1", "sb1",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("sb1"), false));
        search.addDocument(
                new SearchDocument("per1", "per1",
                        Collections.singletonList("sb1"), Collections.singletonList("sb1"),
                        Collections.singletonList("sb1/per1"), false));
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

        // CDK processing index + Akubra
        processing.addCollection("cdk-sb1", "cdk-sb11");
        processing.addCollection("cdk-sb3", null);
        processing.addCollection("cdk-sb4", "cdk-sb11");

        processing.addReference("cdk-sb1", new CDKReference("cis1").getPrefixedPid());
        processing.addReference("cdk-sb11", new CDKReference("cis1").getPrefixedPid());
        processing.addReference("cdk-sb2", new CDKReference("cis1").getPrefixedPid());
        processing.addReference("cdk-sb2", new CDKReference("roc1").getPrefixedPid());

        // search index after CDK indexing = MZK documents + CDK collections
        search.addDocument(
                new SearchDocument("cdk-sb1", "cdk-sb1",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("cdk-sb1"), true));
        search.addDocument(
                new SearchDocument("cdk-sb11", "cdk-sb1/cdk-sb11",
                        Collections.singletonList("cdk-sb1"), Collections.singletonList("cdk-sb1"),
                        Collections.singletonList("cdk-sb1/cdk-sb11"), true));
        search.setParent("cdk-sb11", "cdk-sb1");
        search.addDocument(
                new SearchDocument("cdk-sb2", "cdk-sb2",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("cdk-sb2"), true));
        search.addDocument(
                new SearchDocument("cdk-sb3", "cdk-sb3",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("cdk-sb3"), true));
        search.addDocument(
                new SearchDocument("cdk-sb4", "cdk-sb4",
                        Collections.emptyList(), Collections.emptyList(),
                        Collections.singletonList("cdk-sb4"), true));
        search.setParent("cdk-sb11", "cdk-sb4");

        synchronizer = new CDKCollectionSynchronizer(search, processing);
    }

    @Test
    public void testSynchronizeReferencedSubtree() {
        // sb1 before
        SearchDocument sb1 = search.get("sb1");
        assertTrue(sb1.getInCollections().isEmpty());
        assertTrue(sb1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1"), sb1.getPidPaths());

        // per1 before
        SearchDocument per1 = search.get("per1");
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        // roc1 before
        SearchDocument roc1 = search.get("roc1");
        assertEquals(Arrays.asList("sb1"), roc1.getInCollections());
        assertTrue(roc1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1"), roc1.getPidPaths());

        // cis1 before
        SearchDocument cis1 = search.get("cis1");
        assertEquals(Arrays.asList("sb1"), cis1.getInCollections());
        assertTrue(cis1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1"), cis1.getPidPaths());

        // st1 before
        SearchDocument st1 = search.get("st1");
        assertEquals(Arrays.asList("sb1"), st1.getInCollections());
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st1"), st1.getPidPaths());

        // st2 before
        SearchDocument st2 = search.get("st2");
        assertEquals(Arrays.asList("sb1"), st2.getInCollections());
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st2"), st2.getPidPaths());

        synchronizer.synchronize();

        // sb1
        assertTrue(sb1.getInCollections().isEmpty());
        assertTrue(sb1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1"), sb1.getPidPaths());

        // per1
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        // roc1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb2")),
                new HashSet<>(roc1.getInCollections())
        );
        assertEquals(Arrays.asList("cdk-sb2"), roc1.getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1", "cdk-sb2/roc1")),
                new HashSet<>(roc1.getPidPaths())
        );

        // cis1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-sb4")),
                new HashSet<>(cis1.getInCollections())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("cdk-sb11", "cdk-sb1", "cdk-sb2")),
                new HashSet<>(cis1.getInCollectionsDirect())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1",
                        "cdk-sb11/cis1",
                        "cdk-sb1/cis1", "cdk-sb1/cdk-sb11/cis1",
                        "cdk-sb2/cis1", "cdk-sb2/roc1/cis1",
                        "cdk-sb4/cdk-sb11/cis1"
                )),
                new HashSet<>(cis1.getPidPaths())
        );

        // st1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-sb4")),
                new HashSet<>(st1.getInCollections())
        );
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1/st1",
                        "cdk-sb11/cis1/st1",
                        "cdk-sb1/cis1/st1", "cdk-sb1/cdk-sb11/cis1/st1",
                        "cdk-sb2/cis1/st1", "cdk-sb2/roc1/cis1/st1",
                        "cdk-sb4/cdk-sb11/cis1/st1"
                )),
                new HashSet<>(st1.getPidPaths())
        );

        // st2
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-sb4")),
                new HashSet<>(st2.getInCollections())
        );
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1/st2",
                        "cdk-sb11/cis1/st2",
                        "cdk-sb1/cis1/st2", "cdk-sb1/cdk-sb11/cis1/st2",
                        "cdk-sb2/cis1/st2", "cdk-sb2/roc1/cis1/st2",
                        "cdk-sb4/cdk-sb11/cis1/st2"
                )),
                new HashSet<>(st2.getPidPaths())
        );
    }

    @Test
    public void testRepeatedSynchronization() {
        // sb1 before
        SearchDocument sb1 = search.get("sb1");
        assertTrue(sb1.getInCollections().isEmpty());
        assertTrue(sb1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1"), sb1.getPidPaths());

        // per1 before
        SearchDocument per1 = search.get("per1");
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        // roc1 before
        SearchDocument roc1 = search.get("roc1");
        assertEquals(Arrays.asList("sb1"), roc1.getInCollections());
        assertTrue(roc1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1"), roc1.getPidPaths());

        // cis1 before
        SearchDocument cis1 = search.get("cis1");
        assertEquals(Arrays.asList("sb1"), cis1.getInCollections());
        assertTrue(cis1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1"), cis1.getPidPaths());

        // st1 before
        SearchDocument st1 = search.get("st1");
        assertEquals(Arrays.asList("sb1"), st1.getInCollections());
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st1"), st1.getPidPaths());

        // st2 before
        SearchDocument st2 = search.get("st2");
        assertEquals(Arrays.asList("sb1"), st2.getInCollections());
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st2"), st2.getPidPaths());

        // Synchronize
        synchronizer.synchronize();
        // Synchronize again
        synchronizer.synchronize();

        // sb1
        assertTrue(sb1.getInCollections().isEmpty());
        assertTrue(sb1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1"), sb1.getPidPaths());

        // per1
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        // roc1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb2")),
                new HashSet<>(roc1.getInCollections())
        );
        assertEquals(Arrays.asList("cdk-sb2"), roc1.getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1", "cdk-sb2/roc1")),
                new HashSet<>(roc1.getPidPaths())
        );

        // cis1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-sb4")),
                new HashSet<>(cis1.getInCollections())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("cdk-sb11", "cdk-sb1", "cdk-sb2")),
                new HashSet<>(cis1.getInCollectionsDirect())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1",
                        "cdk-sb11/cis1",
                        "cdk-sb1/cis1", "cdk-sb1/cdk-sb11/cis1",
                        "cdk-sb2/cis1", "cdk-sb2/roc1/cis1",
                        "cdk-sb4/cdk-sb11/cis1"
                )),
                new HashSet<>(cis1.getPidPaths())
        );

        // st1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-sb4")),
                new HashSet<>(st1.getInCollections())
        );
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1/st1",
                        "cdk-sb11/cis1/st1",
                        "cdk-sb1/cis1/st1", "cdk-sb1/cdk-sb11/cis1/st1",
                        "cdk-sb2/cis1/st1", "cdk-sb2/roc1/cis1/st1",
                        "cdk-sb4/cdk-sb11/cis1/st1"
                )),
                new HashSet<>(st1.getPidPaths())
        );

        // st2
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-sb4")),
                new HashSet<>(st2.getInCollections())
        );
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1/st2",
                        "cdk-sb11/cis1/st2",
                        "cdk-sb1/cis1/st2", "cdk-sb1/cdk-sb11/cis1/st2",
                        "cdk-sb2/cis1/st2", "cdk-sb2/roc1/cis1/st2",
                        "cdk-sb4/cdk-sb11/cis1/st2"
                )),
                new HashSet<>(st2.getPidPaths())
        );
    }

    @Test
    public void testRemoveReference() {
        synchronizer.synchronize();
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
         * cdk-sb11
         *     └── cdk/cis1 <------ REMOVE
         * cdk-sb1
         * └── cdk/cis1
         * └── cdk-sb11
         * cdk-sb2
         * └── cdk/cis1
         * └── cdk/roc1
         * cdk-sb3
         * cdk-sb4
         *     └── cdk-sb11
         */
        processing.removeReference("cdk-sb11", new CDKReference("cis1").getPrefixedPid());
        synchronizer.synchronize();

        // sb1
        SearchDocument sb1 = search.get("sb1");
        assertTrue(sb1.getInCollections().isEmpty());
        assertTrue(sb1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1"), sb1.getPidPaths());

        // per1
        SearchDocument per1 = search.get("per1");
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        // roc1
        SearchDocument roc1 = search.get("roc1");
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb2")),
                new HashSet<>(roc1.getInCollections())
        );
        assertEquals(Arrays.asList("cdk-sb2"), roc1.getInCollectionsDirect());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1", "cdk-sb2/roc1")),
                new HashSet<>(roc1.getPidPaths())
        );

        // cis1
        SearchDocument cis1 = search.get("cis1");
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb1", "cdk-sb2")),
                new HashSet<>(cis1.getInCollections())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("cdk-sb1", "cdk-sb2")),
                new HashSet<>(cis1.getInCollectionsDirect())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1",
                        "cdk-sb1/cis1",
                        "cdk-sb2/cis1", "cdk-sb2/roc1/cis1"
                )),
                new HashSet<>(cis1.getPidPaths())
        );

        // st1
        SearchDocument st1 = search.get("st1");
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb1", "cdk-sb2")),
                new HashSet<>(st1.getInCollections())
        );
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1/st1",
                        "cdk-sb1/cis1/st1",
                        "cdk-sb2/cis1/st1", "cdk-sb2/roc1/cis1/st1"
                )),
                new HashSet<>(st1.getPidPaths())
        );

        // st2
        SearchDocument st2 = search.get("st2");
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb1", "cdk-sb2")),
                new HashSet<>(st2.getInCollections())
        );
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1/roc1/cis1/st2",
                        "cdk-sb1/cis1/st2",
                        "cdk-sb2/cis1/st2", "cdk-sb2/roc1/cis1/st2"
                )),
                new HashSet<>(st2.getPidPaths())
        );
    }

    @Test
    public void testBackToInitialState() {
        synchronizer.synchronize();
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
         * cdk-sb11 <------ REMOVE
         *     └── cdk/cis1
         * cdk-sb1
         * └── cdk/cis1 <------ REMOVE
         * └── cdk-sb11 <------ REMOVE
         * cdk-sb2      <------ REMOVE
         * └── cdk/cis1
         * └── cdk/roc1
         * cdk-sb3
         * cdk-sb4
         *     └── cdk-sb11 <------ REMOVE
         */
        processing.removeReference("cdk-sb11", new CDKReference("cis1").getPrefixedPid());
        processing.removeCollection("cdk-sb11");
        processing.removeReference("cdk-sb1", new CDKReference("cis1").getPrefixedPid());
        processing.removeReference("cdk-sb2", new CDKReference("cis1").getPrefixedPid());
        processing.removeReference("cdk-sb2", new CDKReference("roc1").getPrefixedPid());
        processing.removeCollection("cdk-sb2");
        processing.getParents().clear();

        synchronizer.synchronize();

        // sb1 before
        SearchDocument sb1 = search.get("sb1");
        assertTrue(sb1.getInCollections().isEmpty());
        assertTrue(sb1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1"), sb1.getPidPaths());

        // per1 before
        SearchDocument per1 = search.get("per1");
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        // roc1 before
        SearchDocument roc1 = search.get("roc1");
        assertEquals(Arrays.asList("sb1"), roc1.getInCollections());
        assertTrue(roc1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1"), roc1.getPidPaths());

        // cis1 before
        SearchDocument cis1 = search.get("cis1");
        assertEquals(Arrays.asList("sb1"), cis1.getInCollections());
        assertTrue(cis1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1"), cis1.getPidPaths());

        // st1 before
        SearchDocument st1 = search.get("st1");
        assertEquals(Arrays.asList("sb1"), st1.getInCollections());
        assertTrue(st1.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st1"), st1.getPidPaths());

        // st2 before
        SearchDocument st2 = search.get("st2");
        assertEquals(Arrays.asList("sb1"), st2.getInCollections());
        assertTrue(st2.getInCollectionsDirect().isEmpty());
        assertEquals(Arrays.asList("sb1/per1/roc1/cis1/st2"), st2.getPidPaths());
    }

    @Test
    public void testPreserveOtherDirectMemberships() {
        synchronizer.synchronize();

        SearchDocument cis1 = search.get("cis1");
        cis1.setCollections(cis1.getInCollections(), Arrays.asList("cdk-sb11", "cdk-sb1", "cdk-sb2", "cdk-other"));
        processing.addReference("cdk-other", new CDKReference("cis1").getPrefixedPid());

        synchronizer.synchronize();

        assertTrue(cis1.getInCollectionsDirect().contains("cdk-sb11"));
        assertTrue(cis1.getInCollectionsDirect().contains("cdk-other"));
    }

    @Test
    public void testAddAndRemoveMemberOfNonCDKCollection() {
        // per1 before
        SearchDocument per1 = search.get("per1");
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        synchronizer.synchronize();

        // per1
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

        processing.addReference("cdk-sb3", new CDKReference("per1").getPrefixedPid());
        synchronizer.synchronize();

        // per1
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb3")),
                new HashSet<>(per1.getInCollections())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("sb1", "cdk-sb3")),
                new HashSet<>(per1.getInCollectionsDirect())
        );
        assertEquals(
                new HashSet<>(Arrays.asList("sb1/per1", "cdk-sb3/per1")),
                new HashSet<>(per1.getPidPaths())
        );

        processing.removeReference("cdk-sb3", new CDKReference("per1").getPrefixedPid());
        synchronizer.synchronize();

        // per1
        assertEquals(Arrays.asList("sb1"), per1.getInCollections());
        assertEquals(Arrays.asList("sb1"), per1.getInCollectionsDirect());
        assertEquals(Arrays.asList("sb1/per1"), per1.getPidPaths());

    }

}
