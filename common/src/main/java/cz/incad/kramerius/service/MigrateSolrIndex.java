package cz.incad.kramerius.service;

/**
 * Migration solr service; It can migration from  
 * @author pstastny
 */
public interface MigrateSolrIndex {
    
    /**
     * Migrate data from one solr to another
     * @param targetSolrHost
     */
    public void migrate() throws MigrateSolrIndexException;
}
