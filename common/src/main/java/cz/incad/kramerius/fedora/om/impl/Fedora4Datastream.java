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
import org.fcrepo.client.PatchBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static cz.incad.kramerius.fedora.utils.Fedora4Utils.endpoint;

/**
 * Created by pstastny on 10/13/2017.
 */
public class Fedora4Datastream implements RepositoryDatastream {

    public static enum Type {
        DIRECT,
        INDIRECT;
    }

    public static final Logger LOGGER = Logger.getLogger(Fedora4Datastream.class.getName());

    private final FcrepoClient client;
    private final List<String> path;
    private final Fedora4Repository repo;
    private final String name;
    private final Type type;

    public Fedora4Datastream(Fedora4Repository repo, FcrepoClient client, List<String> path, String name, Type type) {
        super();
        this.client = client;
        this.path = path;
        this.repo = repo;
        this.name = name;
        this.type = type;
    }

    public Fedora4Datastream(FcrepoClient client, List<String> path, Fedora4Repository repo, String name) {
        this.client = client;
        this.path = path;
        this.repo = repo;
        this.name = name;
        this.type = Type.DIRECT;
    }

    @Override
    public String getName() throws RepositoryException {
        return this.name;
    }

    public InputStream getContent() throws RepositoryException {
        URI uri = URI.create(Fedora4Utils.endpoint() + Fedora4Utils.path(path));
        try (FcrepoResponse response = client.get(uri).perform()) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int length = IOUtils.copy(response.getBody(), bos);
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
            String mimeType = externalMimeType(Fedora4Utils.extractText(body, "hasMimeType", FedoraNamespaces.EBUCORE_NAMESPACE_URI));
            return mimeType;
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

    private String externalMimeType(String mimeType) throws IOException {
        String[] split = mimeType.split(";");
        if (split.length >0) {
            if(split[0].equals("message/external-body")) {
                if (split.length == 3 && split[1].equals("access-type=URL")) {
                    String url = split[2];
                    if (url.startsWith("url=\"")||url.startsWith("url='")) {
                        String surl = url.substring("url='".length(), url.length() - 1);
                        try (FcrepoResponse response = client.head(URI.create(surl)).perform()) {
                            return response.getContentType();
                        } catch (FcrepoOperationFailedException e) {
                            throw new IOException(e);
                        }
                    } else throw new IOException("unsupported mimetype "+mimeType);
                } else throw new IOException("unsupported mimetype "+mimeType);
            } else return mimeType;
        } else {
            return mimeType;
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

    @Override
    public void updateSPARQL(String sparql) throws RepositoryException {
        URI updatingPath = URI.create(endpoint()+Fedora4Utils.path(this.path)+"/fcr:metadata");
        LOGGER.info("Updating path "+updatingPath);
        try (FcrepoResponse streamResp = new PatchBuilder(updatingPath, client).body(new ByteArrayInputStream(sparql.getBytes("UTF-8"))).perform()) {
            if (streamResp.getStatusCode() != 204) {
                String s = IOUtils.toString(streamResp.getBody(), "UTF-8");
                throw new RepositoryException("Cannot update properties for  stream "+this.path+" due to "+s);
            }
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }

    }
}
