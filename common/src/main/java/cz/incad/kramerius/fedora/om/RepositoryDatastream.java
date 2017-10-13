package cz.incad.kramerius.fedora.om;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;

/**
 * Created by pstastny on 10/13/2017.
 */
public interface RepositoryDatastream {

    public Document getMetadata() throws RepositoryException;

    public InputStream getContent() throws RepositoryException;

    public String getMimeType() throws RepositoryException;

    public Date getLastModified()throws RepositoryException;

}
