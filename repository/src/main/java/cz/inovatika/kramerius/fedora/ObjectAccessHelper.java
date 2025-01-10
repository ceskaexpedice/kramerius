package cz.inovatika.kramerius.fedora;

import org.apache.solr.client.solrj.SolrServerException;

import java.io.IOException;
import java.time.LocalDateTime;

public interface ObjectAccessHelper {

    public String getPropertyLabel(String pid) throws IOException, RepositoryException;

    public LocalDateTime getPropertyCreated(String pid) throws IOException, RepositoryException;

    public LocalDateTime getPropertyLastModified(String pid) throws IOException, RepositoryException;


}
