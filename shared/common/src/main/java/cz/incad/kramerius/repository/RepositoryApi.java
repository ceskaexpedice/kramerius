package cz.incad.kramerius.repository;

import cz.incad.kramerius.fedora.om.RepositoryException;
import org.dom4j.Document;

import java.io.IOException;

/**
 * Interface for accessing data in repository (Akubra, formerly Fedora).
 * Only subset of Fedora's API (API-A, API-M) is implemented, as needed.
 * This is independent from domain-specific logic built on the repository (i.e. Kramerius),
 * as it should be with regard to separation of abstracion levels.
 *
 * @link https://wiki.lyrasis.org/display/FEDORA38/REST+API
 * @see cz.incad.kramerius.repository.KrameriusRepositoryApi
 */
public interface RepositoryApi {

    //TODO: methods for fetching other types of datastreams (redirect, external referenced, probably not managed)
    //TODO: methods for updating datastreams (new versions)

    public void ingestObject(Document foxmlDoc) throws RepositoryException;

    public boolean objectExists(String pid) throws RepositoryException;

    public String getObjectProperty(String pid, String propertyName) throws IOException, RepositoryException;

    public Document getObjectFoxml(String pid) throws RepositoryException, IOException;

    public boolean datastreamExists(String pid, String dsId) throws RepositoryException, IOException;

    public Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public void deleteObject(String pid) throws RepositoryException;

}
