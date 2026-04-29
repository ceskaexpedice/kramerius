package cz.incad.kramerius.rest.apiNew.client.v70.redirection.source;

import java.io.IOException;

/**
 * Service for identifying the source library (acronym) of a document.
 */
public interface CDKDocumentSourceProvider {

    /**
     * Returns the library acronym where the document with the given PID is located.
     * * @param pid Persistent identifier of the document (e.g., uuid:...)
     * @return Library acronym (e.g., mzk, nkp) or null if not found.
     * @throws IOException If there is an error communicating with the Solr index.
     */
    String getDocumentSource(String pid) throws IOException;
}