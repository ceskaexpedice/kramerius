package cz.incad.kramerius.service;

import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;

import java.io.IOException;

public interface DeleteService {
    
    /**
     * Delete the tree of Kramerius objects including the root with given PID 
     *
     * @param internalAPI
     * @param pid PID of the tree root object
     * @param pidPath PID_PATH to the root object
     * @param message Message for the FOXML auction log
     * @param deleteEmptyParents check if the parent is empty and delete it as well
     * @throws IOException
     */
    public void deleteTree(AkubraRepository internalAPI, String pid, String pidPath, String message, boolean deleteEmptyParents, boolean spawnIndexer) throws IOException, RepositoryException, ResourceIndexException, SolrServerException;

}
