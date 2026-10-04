package cz.incad.kramerius.cdk.index;

import java.util.List;

public class SearchDocumentPage {

    private final List<SearchDocument> documents;
    private final String nextCursor;
    private final boolean hasMore;

    public SearchDocumentPage(List<SearchDocument> documents, String nextCursor, boolean hasMore) {
        this.documents = documents;
        this.nextCursor = nextCursor;
        this.hasMore = hasMore;
    }

    public List<SearchDocument> getDocuments() {
        return documents;
    }

    public String getNextCursor() {
        return nextCursor;
    }

    public boolean hasMore() {
        return hasMore;
    }
}