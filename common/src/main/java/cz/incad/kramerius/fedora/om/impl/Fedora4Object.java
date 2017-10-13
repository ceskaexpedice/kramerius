/*
 * Copyright (C) 2016 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package cz.incad.kramerius.fedora.om.impl;

import static cz.incad.kramerius.fedora.utils.Fedora4Utils.*;

import java.io.*;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.utils.XMLUtils;
import org.apache.commons.io.IOUtils;
import org.fcrepo.client.*;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author pavels
 *
 */
public class Fedora4Object implements RepositoryObject {

    public  static final Logger LOGGER = Logger.getLogger(Fedora4Object.class.getName());

    //private URI path;
    private List<String> path;
    private FcrepoClient client;
    private Fedora4Repository repo;


    public Fedora4Object(Fedora4Repository repo, FcrepoClient client, List<String> path) {
        super();
        this.client = client;
        this.path = path;
        this.repo = repo;
    }
    
    


    @Override
    public void setModel(String model) throws RepositoryException {
    }


    @Override
    public String getModel() throws RepositoryException {
        return null;
    }

    @Override
    public String getPath() {
        return Fedora4Utils.path(this.path);
    }


    @Override
    public RepositoryDatastream createRedirectedStream(String streamId, String url) throws RepositoryException {
        //curl -X PUT -H"Content-Type: message/external-body; access-type=URL; URL=\"http://www.example.com/file\"" "http://localhost:8080/rest/node/to/create"
        URI childUri = URI.create(endpoint()+(endpoint().endsWith("/")? "" : "/")+Fedora4Utils.path(this.path)+"/"+streamId);
        try (FcrepoResponse response = client.put(childUri).body(new ByteArrayInputStream("".getBytes()), "message/external-body; access-type=URL; URL=\""+url+"\"").perform()) {
            return new Fedora4Datastream(this.repo,this.client, new ArrayList<String>(this.path) {{
                add(streamId);
            }});
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    /* (non-Javadoc)
             * @see cz.incad.fcrepo.RepositoryObject#createStream(java.lang.String, java.lang.String, java.io.InputStream)
             */
    // type of stream - standard; redirected
    @Override
    public RepositoryDatastream createStream(String streamId, String mimeType, InputStream input) throws RepositoryException {
        try {
            ByteOutputStream bos = new ByteOutputStream();
            IOUtils.copy(input, bos);

            URI childUri = URI.create(endpoint()+(endpoint().endsWith("/")? "" : "/")+Fedora4Utils.path(this.path)+"/"+streamId);
            if (streamId.equals("RELS-EXT")) {
                mimeType = "text/xml";
            }
            if (!repo.exists(childUri)) {
                try (FcrepoResponse response = new PutBuilder(childUri, client).body(new ByteArrayInputStream(bos.getBytes()), mimeType).perform()) {
                    if (response.getStatusCode() == 201) {
                        URI location = response.getLocation();
                        if (streamId.equals("RELS-EXT")) {
                            // process rels-ext and create all children and relations
                            SPARQLBuilder sparqlBuilder = new SPARQLBuilderImpl();
                            String sparql = sparqlBuilder.sparqlProps(new String(bos.getBytes(), "UTF-8").trim(), (object)->{
                                RepositoryObject created = repo.createOrFindObject(object);
                                return "/"+this.repo.getBoundContext()+created.getPath();
                            });

                            URI updatingPath = URI.create(endpoint()+Fedora4Utils.path(this.path));
                            LOGGER.info("Updating path "+updatingPath);
                            try (FcrepoResponse streamResp = new PatchBuilder(updatingPath, client).body(new ByteArrayInputStream(sparql.getBytes("UTF-8"))).perform()) {
                                if (streamResp.getStatusCode() != 204) {
                                    String s = IOUtils.toString(streamResp.getBody(), "UTF-8");
                                    throw new RepositoryException("Cannot update properties for  stream "+streamId+" due to "+s);
                                }
                            } catch (FcrepoOperationFailedException e) {
                                throw new RepositoryException(e);
                            }
                        }
                    } else {
                        throw new RepositoryException("Cannot create  stream "+streamId);
                    }
                    return new Fedora4Datastream(this.repo,this.client, new ArrayList<String>(this.path) {{
                        add(streamId);
                    }});
                } catch (FcrepoOperationFailedException e) {
                    throw new RepositoryException(e);
                } catch (SAXException e) {
                    throw new RepositoryException(e);
                } catch (ParserConfigurationException e) {
                    throw new RepositoryException(e);
                }
            } else {
                throw new RepositoryException("stream '"+streamId+"' already exists");
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public RepositoryDatastream getStream(String streamId) throws RepositoryException {
        URI childUri = URI.create(endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path) + "/" + streamId);
        if (repo.exists(childUri)) {
            return new Fedora4Datastream(this.repo,this.client, new ArrayList<String>(this.path) {{
                add(streamId);
            }});
        } else return null;
    }


    @Override
    public Date getLastModified() throws RepositoryException {
        URI uri = URI.create(endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path) + "/fcr:metadata");
        try (FcrepoResponse response = client.get(uri).accept("application/rdf+xml").perform()) {
            InputStream body = response.getBody();
            return extractDate(body, "lastModified", FedoraNamespaces.FEDORA_NAMESPACE_URI);
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
    public Document getMetadata() throws RepositoryException {
        URI uri = URI.create(endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path) + "/fcr:metadata");
        try (FcrepoResponse response = client.get(uri).accept("application/rdf+xml").perform()) {
            InputStream body = response.getBody();
            return XMLUtils.parseDocument(body, true);
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
}
