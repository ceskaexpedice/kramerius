package cz.incad.kramerius.fedora.om;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;

/**
 * Represents datastream
 */
public interface RepositoryDatastream {

    /**
     * Return name
     * @return
     * @throws RepositoryException
     */
    public String getName() throws RepositoryException;

    /**
     * Return metadata document
     * @return
     * @throws RepositoryException
     */
    public Document getMetadata() throws RepositoryException;

    /**
     * Return content of the stream
     * @return
     * @throws RepositoryException
     */
    public InputStream getContent() throws RepositoryException;

    /**
     * Return mimetype
     * @return
     * @throws RepositoryException
     */
    public String getMimeType() throws RepositoryException;

    /**
     * Return last modified flag
     * @return
     * @throws RepositoryException
     */
    public Date getLastModified()throws RepositoryException;

    /**
     * Update sparql properties
     * @param sparql
     * @throws RepositoryException
     */
    public void updateSPARQL(String sparql) throws RepositoryException;
}
