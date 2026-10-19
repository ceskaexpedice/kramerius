
package cz.incad.kramerius.cdkcollection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

//@Ignore
public class SolrSearchServiceTest_integration {

    @Before
    public void setUp() {
    }

    @Test
    public void testGet() {
        SolrSearchService searchService = new SolrSearchService();
        SearchDocument searchDocument = searchService.get("uuid:12993b4a-71b4-4f19-8953-0701243cc25d");
        System.out.println(searchDocument);
    }

    @Test
    public void testFindUnlinkedSubtree() {
        SolrSearchService searchService = new SolrSearchService();
        List<SearchDocument> unlinkedSubtree = new ArrayList<>();
        searchService.findUnlinkedSubtree(
                "uuid:5035a48a-5e2e-486c-8127-2fa650842e46",
                List.of(
                        "uuid:82d015ad-8b5b-4d4d-8349-1f8e387e18ef",
                        "uuid:3b358ab4-33a6-4234-bf12-4161df3e0147"
                ),
                unlinkedSubtree::addAll
        );
        System.out.println(unlinkedSubtree.size());
    }

    @Test
    public void testFindByDirectCollection() {
        SolrSearchService searchService = new SolrSearchService();
        List<SearchDocument> byDirectCollection = new ArrayList<>();
        searchService.findByDirectCollection(
                "uuid:3b358ab4-33a6-4234-bf12-4161df3e0147",
                byDirectCollection::addAll
        );
        System.out.println(byDirectCollection.size());
    }

    @Test
    public void testFindByPidPathPrefix() {
        SolrSearchService searchService = new SolrSearchService();
        List<SearchDocument> byPidPathPrefix = new ArrayList<>();
        searchService.findByPidPathPrefix(
                "uuid:82d015ad-8b5b-4d4d-8349-1f8e387e18ef/uuid:3b358ab4-33a6-4234-bf12-4161df3e0147",
                byPidPathPrefix::addAll
        );
        System.out.println(byPidPathPrefix.size());
    }

    @Test
    public void testGetDirectCollectionValues() {
        SolrSearchService searchService = new SolrSearchService();
        List<String> directCollectionValues = searchService.getDirectCollectionValues();
        System.out.println(directCollectionValues);
    }

}
