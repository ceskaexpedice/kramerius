package cz.incad.kramerius.fedora.om.impl;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.fcrepo.client.FcrepoClient;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.fcrepo.client.FcrepoResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Created by pstastny on 10/13/2017.
 */
public class Fedora4Datastream implements RepositoryDatastream {


    private final FcrepoClient client;
    private final List<String> path;
    private final Fedora4Repository repo;

    public Fedora4Datastream(Fedora4Repository repo, FcrepoClient client, List<String> path) {
        super();
        this.client = client;
        this.path = path;
        this.repo = repo;
    }

    public InputStream getContent() throws RepositoryException {
        URI uri = URI.create(Fedora4Utils.endpoint() + Fedora4Utils.path(path));
        try (FcrepoResponse response = client.get(uri).perform()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(response.getBody(), bos);
            return new ByteArrayInputStream(bos.toByteArray());
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getMetadata() throws RepositoryException {
        URI uri = URI.create(Fedora4Utils.endpoint() + Fedora4Utils.path(path)+"/fcr:metadata");
        try (FcrepoResponse response = client.get(uri).perform()) {
            return XMLUtils.parseDocument(response.getBody(), true);
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public String getMimeType() throws RepositoryException {
        URI uri = URI.create(Fedora4Utils.endpoint() + Fedora4Utils.path(this.path)+"/fcr:metadata");
        try (FcrepoResponse response = client.get(uri).accept("application/rdf+xml").perform()) {
            InputStream body = response.getBody();
            return Fedora4Utils.extractText(body, "hasMimeType", FedoraNamespaces.EBUCORE_NAMESPACE_URI);
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Date getLastModified() throws RepositoryException {
        URI uri = URI.create(Fedora4Utils.endpoint() + Fedora4Utils.path(this.path)+"/fcr:metadata");
        try (FcrepoResponse response = client.get(uri).accept("application/rdf+xml").perform()) {
            InputStream body = response.getBody();
            Date date  = Fedora4Utils.extractDate(body, "lastModified", FedoraNamespaces.FEDORA_NAMESPACE_URI);
            return date;
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (ParseException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
}
