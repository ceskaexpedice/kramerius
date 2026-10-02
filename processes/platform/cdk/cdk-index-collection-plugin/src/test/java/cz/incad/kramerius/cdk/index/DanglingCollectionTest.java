package cz.incad.kramerius.cdk.index;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DanglingCollectionTest {

    @Test
    public void shouldRemoveDanglingCollectionReferences() {
        FakeSearchIndexService search = new FakeSearchIndexService();
        FakeCDKProcessingIndexService fakeProcessingIndexService = new FakeCDKProcessingIndexService();

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
        cleaner.clean();

        assertEquals(Arrays.asList("s11", "s1", "cdk-s1"), search.get("d1").getInCollections());
        assertEquals("cdk-s1", search.get("d1").getInCollectionsDirect());
    }

    @Test
    public void shouldKeepExistingCollectionReferences() {
        FakeSearchIndexService search = new FakeSearchIndexService();
        FakeCDKProcessingIndexService fakeProcessingIndexService = new FakeCDKProcessingIndexService();

        search.addDocument("s1");
        search.addDocument("cdk-s1");
        search.addDocument("d1");
        fakeProcessingIndexService.addCDKReference("cdk-s1", "d1");
        SearchDocument d1 = search.get("d1");
        d1.setInCollections(Arrays.asList("s1", "cdk-s1"));
        d1.setInCollectionsDirect("cdk-s1");

        CDKCollectionSynchronizer cleaner = new CDKCollectionSynchronizer(search, fakeProcessingIndexService);
        cleaner.clean();

        assertEquals(Arrays.asList("s1", "cdk-s1"), search.get("d1").getInCollections());
        assertEquals("cdk-s1", search.get("d1").getInCollectionsDirect());
    }
}