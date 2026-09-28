package cz.incad.kramerius.cdk.index.prepare;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CdkIndexerTest {

    @Test
    public void shouldAddCdkCollectionToMigratedSourceTree() throws Exception {

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

        FakeProcessingIndexService relations = new FakeProcessingIndexService();
        FakeSearchIndexService search = new FakeSearchIndexService();

        // CDK collections po běhu původního Indexeru
        search.addDocument("cdk-s1");
        search.addDocument("cdk-s11");
        search.addChild("cdk-s1", "cdk-s11");
        search.get("cdk-s1").setInCollection(Collections.<String>emptyList());
        search.get("cdk-s11").setInCollection(Arrays.asList("cdk-s1"));

        relations.addCollection("cdk-s1", "cdk-s11");
        relations.addReference("cdk-s11", "cdk/s1");

        // MZK search tree
        search.addDocument("s1");
        search.addDocument("s11");
        search.addDocument("d1");
        search.addDocument("d11");

        search.addChild("s1", "s11");
        search.addChild("s11", "d1");
        search.addChild("d1", "d11");

        // Stav vytvořený původním Indexerem
        search.get("s1").setInCollection(Collections.<String>emptyList());
        search.get("s11").setInCollection(Arrays.asList("s1"));
        search.get("d1").setInCollection(Arrays.asList("s11", "s1"));
        search.get("d11").setInCollection(Arrays.asList("s11", "s1"));

        CdkIndexer indexer = new CdkIndexer(relations, search);
        indexer.index("cdk-s1");

        // s1 je přímo vloženo do cdk-s11
        assertEquals("cdk-s11", search.get("s1").getInCollectionDirect());
        assertEquals(Arrays.asList("cdk-s11", "cdk-s1"), search.get("s1").getInCollection());

        // s11
        assertNull(search.get("s11").getInCollectionDirect());
        assertEquals(Arrays.asList("s1", "cdk-s11", "cdk-s1"), search.get("s11").getInCollection());

        // d1
        assertNull(search.get("d1").getInCollectionDirect());
        assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), search.get("d1").getInCollection());

        // d11
        assertNull(search.get("d11").getInCollectionDirect());
        assertEquals(Arrays.asList("s11", "s1", "cdk-s11", "cdk-s1"), search.get("d11").getInCollection());
    }


    @Test
    public void shouldNotDuplicateExistingCdkCollections() throws Exception {
        FakeProcessingIndexService relations = new FakeProcessingIndexService();
        FakeSearchIndexService search = new FakeSearchIndexService();

        relations.addCollection("cdk-s1", "cdk-s11");
        relations.addReference("cdk-s11", "cdk/s1");

        search.addDocument("s1");

        search.get("s1").setInCollection(Arrays.asList("cdk-s11", "cdk-s1"));

        CdkIndexer indexer = new CdkIndexer(relations, search);
        indexer.index("cdk-s1");

        assertEquals(Arrays.asList("cdk-s11", "cdk-s1"), search.get("s1").getInCollection());
        assertEquals("cdk-s11", search.get("s1").getInCollectionDirect());
    }


    @Test
    public void shouldProcessReferenceToDocument() throws Exception {

        /*
         * CDK:
         *
         * cdk-s1
         * └── cdk/d1
         *
         * MZK:
         *
         * d1
         * └── d11
         */

        FakeProcessingIndexService relations = new FakeProcessingIndexService();
        FakeSearchIndexService search = new FakeSearchIndexService();
        relations.addReference("cdk-s1", "cdk/d1");

        search.addDocument("d1");
        search.addDocument("d11");

        search.addChild("d1", "d11");

        search.get("d1").setInCollection(Arrays.asList("s11", "s1"));
        search.get("d11").setInCollection(Arrays.asList("s11", "s1"));

        CdkIndexer indexer = new CdkIndexer(relations, search);
        indexer.index("cdk-s1");

        // d1 je kořen reference
        assertEquals("cdk-s1", search.get("d1").getInCollectionDirect());
        assertEquals(Arrays.asList("s11", "s1", "cdk-s1"), search.get("d1").getInCollection());

        // d11 je pouze potomek
        assertNull(search.get("d11").getInCollectionDirect());

        assertEquals(Arrays.asList("s11", "s1", "cdk-s1"), search.get("d11").getInCollection());
    }
}