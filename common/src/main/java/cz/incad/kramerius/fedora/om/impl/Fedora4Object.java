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
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.sun.xml.messaging.saaj.util.ByteOutputStream;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.solr.client.solrj.SolrServerException;
import org.fcrepo.client.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * @author pavels
 *
 */
public class Fedora4Object implements RepositoryObject {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public  static final Logger LOGGER = Logger.getLogger(Fedora4Object.class.getName());

    //private URI path;
    private List<String> path;
    private FcrepoClient client;
    private Fedora4Repository repo;
    private String pid;
    private ProcessingIndexFeeder feeder;


    public Fedora4Object(Fedora4Repository repo, FcrepoClient client, List<String> path, String pid, ProcessingIndexFeeder feeder) {
        super();
        this.client = client;
        this.path = path;
        this.repo = repo;
        this.pid = pid;
        this.feeder = feeder;
    }


    public String getPid() {
        return pid;
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
            }},streamId, Fedora4Datastream.Type.INDIRECT);
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public List<RepositoryDatastream> getStreams() throws RepositoryException {
        List<RepositoryDatastream> list = new ArrayList<>();
        Document metadata = getMetadata();
        List<Element> elms =  XMLUtils.getElementsRecursive(metadata.getDocumentElement(),(element) -> {
            if (element.getLocalName().equals("contains") && element.getNamespaceURI().equals("http://www.w3.org/ns/ldp#")) {
                return true;
            } else return false;
        });

        for (Element elm : elms) {
            String resource = elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
            List<String> path = Fedora4Utils.link(resource);
            RepositoryDatastream ds = new Fedora4Datastream(this.repo, this.client,path,path.get(path.size()-1), Fedora4Datastream.Type.DIRECT);
            list.add(ds);
        }
        return list;
    }

    /* (non-Javadoc)
                 * @see cz.incad.fcrepo.RepositoryObject#createStream(java.lang.String, java.lang.String, java.io.InputStream)
                 */
    // type of stream - standard; redirected
    @Override
    public RepositoryDatastream createStream(String streamId, String mimeType, InputStream input) throws RepositoryException {
        try {
            ByteOutputStream bos = new ByteOutputStream();
            int length = IOUtils.copy(input, bos);

            URI childUri = URI.create(endpoint()+(endpoint().endsWith("/")? "" : "/")+Fedora4Utils.path(this.path)+"/"+streamId);
            if (streamId.equals("RELS-EXT")) {
                mimeType = "text/xml";
            }

            Fedora4Datastream ds = new Fedora4Datastream(this.repo,this.client, new ArrayList<String>(this.path) {{
                add(streamId);
            }}, streamId, Fedora4Datastream.Type.DIRECT);

            if (!repo.exists(childUri)) {
                try (FcrepoResponse response = new PutBuilder(childUri, client).body(new ByteArrayInputStream(Arrays.copyOf(bos.getBytes(), length)), mimeType).perform()) {
                    if (response.getStatusCode() == 201) {
                        URI location = response.getLocation();
                        if (streamId.equals(FedoraUtils.RELS_EXT_STREAM)) {
                            // process rels-ext and create all children and relations
                            RELSEXTSPARQLBuilder sparqlBuilder = new RELSEXTSPARQLBuilderImpl();
                            String sparql = sparqlBuilder.sparqlProps(new String(Arrays.copyOf(bos.getBytes(), length), "UTF-8").trim(), (object, localName)->{

                                if(localName.equals("hasModel")) {
                                    try {
                                        // TODO: dc.title
                                        if (this.streamExists(FedoraUtils.DC_STREAM)) {
                                            try {
                                                InputStream stream = this.getStream(FedoraUtils.DC_STREAM).getContent();
                                                Element title = XMLUtils.findElement(XMLUtils.parseDocument(stream, true).getDocumentElement(), "title", FedoraNamespaces.DC_NAMESPACE_URI);
                                                if (title != null) {
                                                    this.indexDescription(object,title.getTextContent());
                                                } else {
                                                    this.indexDescription(object,"");
                                                }
                                            } catch (ParserConfigurationException e) {
                                                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                                                this.indexDescription(object,"");
                                            } catch (SAXException e) {
                                                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                                                this.indexDescription(object,"");
                                            }
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    } catch (SolrServerException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    try {
                                        this.indexRelation(localName, object);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    } catch (SolrServerException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                RepositoryObject created = repo.createOrFindObject(object);
                                return "/"+this.repo.getBoundContext()+created.getPath();
                            });

                            // spread properties from relsext
                            URI updatingPath = URI.create(endpoint()+Fedora4Utils.path(this.path));
                            try (FcrepoResponse streamResp = new PatchBuilder(updatingPath, client).body(new ByteArrayInputStream(sparql.getBytes("UTF-8"))).perform()) {
                                if (streamResp.getStatusCode() != 204) {
                                    String s = IOUtils.toString(streamResp.getBody(), "UTF-8");
                                    throw new RepositoryException("Cannot update properties for  stream "+streamId+" due to "+s);
                                }
                            } catch (FcrepoOperationFailedException e) {
                                throw new RepositoryException(e);
                            }

                            ds.updateSPARQL(Fedora4Repository.UPDATE_INDEXING_SPARQL());
                        }
                    } else {
                        throw new RepositoryException("Cannot create  stream "+streamId);
                    }
                    return ds;
                } catch (FcrepoOperationFailedException e) {
                    throw new RepositoryException(e);
                } catch (SAXException e) {
                    throw new RepositoryException(e);
                } catch (ParserConfigurationException e) {
                    throw new RepositoryException(e);
                }
            } else {
                throw new RepositoryException("stream '"+streamId+"' already objectExists");
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    private void indexRelation(String localName, String object) throws IOException, SolrServerException {
        this.feeder.feedRelationDocument( this.getPid(), localName, object);
    }

    private void indexDescription(String model, String dctitle) throws IOException, SolrServerException {
        this.feeder.feedDescriptionDocument(this.getPid(), model, dctitle);
    }

    public void deleteProcessingIndex() throws IOException, SolrServerException {
        feeder.deleteByPid(this.getPid());
    }


    @Override
    public boolean streamExists(String streamId) throws RepositoryException {
        URI childUri = URI.create(endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path) + "/" + streamId);
        return repo.exists(childUri);
    }

    @Override
    public RepositoryDatastream getStream(String streamId) throws RepositoryException {
        URI childUri = URI.create(endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path) + "/" + streamId);
        return new Fedora4Datastream(this.repo,this.client, new ArrayList<String>(this.path) {{
            add(streamId);
        }}, streamId, Fedora4Datastream.Type.DIRECT);
    }

    @Override
    public void updateSPARQL(String sparql) throws RepositoryException {
        URI updatingPath = URI.create(endpoint()+Fedora4Utils.path(this.path));
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
        String link = endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path);
        return getMetadataByLink(link);
//        URI uri = URI.create(endpoint() + (endpoint().endsWith("/") ? "" : "/") + Fedora4Utils.path(this.path) + "/fcr:metadata");
//        try (FcrepoResponse response = client.get(uri).accept("application/rdf+xml").perform()) {
//            InputStream body = response.getBody();
//            return XMLUtils.parseDocument(body, true);
//        } catch (FcrepoOperationFailedException e) {
//            throw new RepositoryException(e);
//        } catch (SAXException e) {
//            throw new RepositoryException(e);
//        } catch (ParserConfigurationException e) {
//            throw new RepositoryException(e);
//        } catch (IOException e) {
//            throw new RepositoryException(e);
//        }
    }

    private Document getMetadataByLink(String link) throws RepositoryException {
        URI uri = URI.create(link + "/fcr:metadata");
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

    // aggregate attribute
    public static class FOXMLStream {

        private String templateName;
        private String mimetype;
        private String id;
        private String date;
        private String data;

        public FOXMLStream() {
        }


        public String getTemplateName() {
            return templateName;
        }

        public String getMimetype() {
            return mimetype;
        }

        public String getId() {
            return id;
        }

        public String getDate() {
            return date;
        }

        public String getData() {
            return data;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
        }

        public void setMimetype(String mimetype) {
            this.mimetype = mimetype;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    @Override
    public InputStream getFoxml() throws RepositoryException {
        try {
            InputStream stream = this.getClass().getResourceAsStream("res/foxml.stg");
            String string = IOUtils.toString(stream, Charset.forName("UTF-8"));
            StringTemplateGroup tmplGroup = new StringTemplateGroup(new StringReader(string), DefaultTemplateLexer.class);
            StringTemplate foxml = tmplGroup.getInstanceOf("FOXML");

            List<FOXMLStream> foxmlStreams = new ArrayList<>();
            List<RepositoryDatastream> dataStreams = this.getStreams().stream().filter(s-> {
                try {
                    return !s.getName().equals(FedoraUtils.POLICY_STREAM) && !s.getName().equals(FedoraUtils.RELS_EXT_STREAM);
                } catch(RepositoryException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage());
                    return false;
                }
            }).collect(Collectors.toList());

            FOXMLStream relsExt = new FOXMLStream();
            relsExt.setId(FedoraUtils.RELS_EXT_STREAM);
            relsExt.setMimetype(this.getStream(FedoraUtils.RELS_EXT_STREAM).getMimeType());

            Document metadata = this.getMetadata();
            XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (elm) -> elm.getLocalName().equals("Description") && elm.getNamespaceURI().equals(FedoraNamespaces.RDF_NAMESPACE_URI)).stream().forEach(e -> e.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "about", PIDParser.INFO_FEDORA_PREFIX+pid));
            XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (elm) -> elm.getLocalName().equals("contains") && elm.getNamespaceURI().equals(FedoraNamespaces.LDP_NAMESPACE_URI)).stream().forEach(e -> e.getParentNode().removeChild(e));
            XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (elm) -> elm.getLocalName().equals("type") && elm.getNamespaceURI().equals(FedoraNamespaces.RDF_NAMESPACE_URI)).stream().forEach(e -> e.getParentNode().removeChild(e));
            XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (elm) -> elm.getNamespaceURI().equals(FedoraNamespaces.FEDORA_NAMESPACE_URI)).stream().forEach(e -> e.getParentNode().removeChild(e));
            XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (elm) -> StringUtils.isAnyString(elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "datatype"))).stream().forEach(e -> e.removeAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "datatype"));

            //rdf. resource
            XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (elm) -> StringUtils.isAnyString(elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource"))).stream().forEach(e -> {
               String link = e.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                try {
                    Document metadataByLink = getMetadataByLink(link);
                    Element pidElm = XMLUtils.findElement(metadataByLink.getDocumentElement(), (subE) -> {
                        return  subE.getLocalName().equals("PID");
                    });
                    if (pidElm != null) {
                        e.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource", PIDParser.INFO_FEDORA_PREFIX+pidElm.getTextContent());
                    }
                } catch (RepositoryException e1) {
                    throw new IllegalArgumentException("Cannot read meatdata from '"+link+"'");
                }});


            StringWriter stringWriter = new StringWriter();
            XMLUtils.print(metadata, stringWriter);
            String xml = this.removeXmlInstruction(stringWriter.toString());
            relsExt.setData(xml);
            relsExt.setDate(SIMPLE_DATE_FORMAT.format(this.getStream(FedoraUtils.RELS_EXT_STREAM).getLastModified()));
            relsExt.setTemplateName("xmlcontent");


            for (RepositoryDatastream dataStream :  dataStreams) {
                String mimeType = dataStream.getMimeType();

                FOXMLStream foxmlStream = new FOXMLStream();
                foxmlStream.setId(dataStream.getName());
                foxmlStream.setMimetype(dataStream.getMimeType());
                foxmlStream.setDate(SIMPLE_DATE_FORMAT.format(dataStream.getLastModified()));

                LOGGER.info("processing stream "+dataStream.getName());

                if (mimeType.equals("text/xml") || mimeType.equals("application/xml")) {
                    InputStream content = dataStream.getContent();
                    BOMInputStream bomIn = new BOMInputStream(content);
                    String rawData = IOUtils.toString(bomIn, "UTF-8");
                    stringWriter = new StringWriter();
                    Document doc = XMLUtils.parseDocument(new StringReader(rawData.trim()), true);
                    XMLUtils.print(doc, stringWriter);
                    xml = this.removeXmlInstruction(stringWriter.toString());
                    foxmlStream.setData(xml);
                    foxmlStream.setTemplateName("xmlcontent");

                } else if (mimeType.startsWith("message/external-body")) {
                    String[] parts = mimeType.split(";");
                    for (String part :
                            parts) {
                        if (part.startsWith("url")) {
                            String[] values = part.split("=");
                            if (values .length >= 2) {
                                foxmlStream.setData(values[1]);
                            }
                        }
                    }
                    foxmlStream.setTemplateName("redirectcontent");

                } else {
                    byte[] bytes = IOUtils.toByteArray(dataStream.getContent());

                    String data = Base64.getEncoder().encodeToString(bytes);
                    foxmlStream.setData(data);
                    foxmlStream.setTemplateName("binary");
                }

                foxmlStreams.add(foxmlStream);
            }

            foxmlStreams.add(relsExt);

            foxml.setAttribute("date",SIMPLE_DATE_FORMAT.format(this.getLastModified()));
            foxml.setAttribute("pid",this.pid);
            foxml.setAttribute("streams",foxmlStreams);

            return new ByteArrayInputStream(foxml.toString().getBytes("UTF-8"));

        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }

    private String removeXmlInstruction(String readAsString) {
        if (readAsString.trim().startsWith("<?")) {
            int endIndex = readAsString.indexOf("?>");
            return readAsString.substring(endIndex+2);
        } else return readAsString;
    }



}
