/*
 * Copyright (C) Jan 10, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.utils.CDKUtils;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public enum MetadataExport {

    
    oaiDc("oai_dc",
            "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
            "http://www.openarchives.org/OAI/2.0/oai_dc/") {
       
        @Override
        public Element performOnCDKSide(SolrAccess solrAccess,Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request,  Document owningDocument, OAIRecord oaiRec,OAISet set) {
            try {
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());
                InputStream directStreamDC = dcStream(solrAccess, userProvider, clientProvider, instances,
                        request, pid);
                if (directStreamDC != null) {
                    Document dc = XMLUtils.parseDocument(directStreamDC, true);
                    Element rootElement = dc.getDocumentElement();
                    owningDocument.adoptNode(rootElement);
                    return rootElement;
                } else {
                    return null;
                }
            } catch (IOException | LexerException | ProxyHandlerException | ParserConfigurationException | SAXException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new RuntimeException(e.getMessage());
            }
        }
    },

    
    edm("edm","http://www.europeana.eu/schemas/ese/","") {

        @Override
        public Element performOnCDKSide(SolrAccess solrAccess,Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request,  Document owningDocument, OAIRecord oaiRec,OAISet set) {

            try {
                
                String baseUrl = ApplicationURL.applicationURL(request);
                //rdf:about="uuid:6b182ad3-b9e9-11e1-1726-001143e3f55c"
                String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());
                InputStream directStreamDC = dcStream(solrAccess, userProvider, clientProvider, instances,
                        request, pid);
                if (directStreamDC != null) {
                    Document dc = XMLUtils.parseDocument(directStreamDC, true);
                    Element dcElement = dc.getDocumentElement();
                    
                    Element metadata = owningDocument.createElement("metadata");
                    metadata.setAttribute("xmlns:europeana", "http://www.europeana.eu/schemas/ese/");
                    metadata.setAttribute("xmlns:ore", "http://www.openarchives.org/ore/terms/");
                    metadata.setAttribute("xmlns:edm", "http://www.europeana.eu/schemas/edm/");
                    metadata.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                    metadata.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
                    metadata.setAttribute("xmlns:rdaGr2", "http://rdvocab.info/ElementsGr2/");
                    metadata.setAttribute("xmlns:skos", "http://www.w3.org/2004/02/skos/core#");
                    metadata.setAttribute("xmlns:oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
                    metadata.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
                    metadata.setAttribute("xmlns:dcterms", "http://purl.org/dc/terms/");
                    
                    Element rdf = owningDocument.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:RDF");
                    rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about",oaiRec.getIdentifier());
                    metadata.appendChild(rdf);

                    Element providedCHO = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:ProvidedCHO");
                    rdf.appendChild(providedCHO);
                    
                    List<Element> elements = XMLUtils.getElements(dcElement);
                    elements.stream().forEach(dcElm -> {
                       owningDocument.adoptNode(dcElm);
                       providedCHO.appendChild(dcElm);
                    });
                    
                    //rdf.appendChild(providedCHO);
                    
                    Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:type");
                    providedCHO.appendChild(type);
                    type.setTextContent("TEXT");
                    
                    Element webresource = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:WebResource");
                    webresource.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about",String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
                    metadata.appendChild(webresource);
                    
                    Element edmAggregation = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:Aggregation");
                    String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
                    if (clientUrl != null) {
                        edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about",clientUrl+(clientUrl.endsWith("/") ? "" : "/")+"uuid/"+pid);
                    } else {
                        edmAggregation.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about",baseUrl+(baseUrl.endsWith("/") ? "" : "/")+"/uuid/"+pid);
                    }
                    Element edmDataPrvovider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:dataProvider");
                    
                    // Data provider
                    // find data provider by acronym
                    String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym","");
                    String defaultEDMDataProvider = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.dataProvider",acronym);
//                    if (edmDataProvider != null) {
//                        edmDataPrvovider.setTextContent(edmDataProvider);
//                    }
                    metadataProvider(instances, oaiRec, edmDataPrvovider, defaultEDMDataProvider);
                    edmAggregation.appendChild(edmDataPrvovider);
                    
                    // dodat dle setu 
                    Element shownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:isShownAt");
                    if (clientUrl != null) {
                        shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource",clientUrl+(clientUrl.endsWith("/") ? "" : "/")+"uuid/"+pid);
                    } else {
                        shownAt.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource",baseUrl+(baseUrl.endsWith("/") ? "" : "/")+"/uuid/"+pid);
                    }
                    edmAggregation.appendChild(shownAt);
                    
                    // mapovani na licence
                    Element edmRights = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:rights");

                    if (clientUrl != null) {
                        edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", clientUrl+(clientUrl.endsWith("/") ? "" : "/")+"uuid/"+pid);
                    } else {
                        edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", baseUrl+(baseUrl.endsWith("/") ? "" : "/")+"/uuid/"+pid);
                    }


                    edmAggregation.appendChild(edmRights);
                    
                    
                    Element edmObject = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:object");
                    edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
                    edmAggregation.appendChild(edmObject);
                    
                    
                    // ceska digitalni kniovna 
                    Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:provider");
                    String edmProviderText = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.provider",acronym);
                    edmProvider.setTextContent(  edmProviderText); //"Czech digital library/Česká digitální knihovna");
                    edmAggregation.appendChild(edmProvider);
                    
                    metadata.appendChild(edmAggregation);
                    
                    return metadata;
                } else {
                    return null;
                }
            } catch (IOException | LexerException | ProxyHandlerException | ParserConfigurationException | SAXException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new RuntimeException(e.getMessage());
            }
        }
        
    },
    
    
    
    ese("ese",
            "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd",
            "http://www.europeana.eu/schemas/ese/") {

                
        
                @Override
                public Element performOnCDKSide(SolrAccess solrAccess,Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request,  Document owningDocument, OAIRecord oaiRec,OAISet set) {
                    try {
                        String baseUrl = ApplicationURL.applicationURL(request);
                        String pid = OAITools.pidFromOAIIdentifier(oaiRec.getIdentifier());

                        InputStream directStreamDC = dcStream(solrAccess, userProvider, clientProvider, instances,
                                request,pid);
                        if (directStreamDC != null) {
                            Document dc = XMLUtils.parseDocument(directStreamDC, true);
                            Element dcElement = dc.getDocumentElement();

                            Element record = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "record");
                            record.setAttribute("xmlns:dc", FedoraNamespaces.DC_NAMESPACE_URI);
                            
                            record.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","xsi:schemaLocation","http://www.europeana.eu/schemas/ese/ http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd http://purl.org/dc/elements/1.1/ http://www.dublincore.org/schemas/xmls/qdc/dc.xsd http://purl.org/dc/terms/ http://www.dublincore.org/schemas/xmls/qdc/dcterms.xsd http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
                            List<Element> dcElems = XMLUtils.getElements(dcElement);
                            dcElems.stream().forEach(dcElm-> { 
                                owningDocument.adoptNode(dcElm);
                                record.appendChild(dcElm);
                            });
                            
                            Element object = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "object");
                            String thumb = String.format(baseUrl+(baseUrl.endsWith("/")? "" : "/")+"api/client/v7.0/items/%s/image/thumb", pid);
                            object.setTextContent(thumb);
                            record.appendChild(object);
                            
                            Element provider = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "provider");
                            String acronym = KConfiguration.getInstance().getConfiguration().getString("acronym","");
                            String defaultDataProvider = KConfiguration.getInstance().getConfiguration().getString("oai.set.edm.dataProvider",acronym);

                            metadataProvider(instances, oaiRec, provider, defaultDataProvider);
                            
                            record.appendChild(provider);
                            
                            Element type = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "type");
                            type.setTextContent("TEXT");
                            record.appendChild(type);
                            
                            Element isShownAt = owningDocument.createElementNS("http://www.europeana.eu/schemas/ese/", "isShownAt");
                            String clientUrl = KConfiguration.getInstance().getConfiguration().getString("client");
                            if (clientUrl != null) {
                                isShownAt.setTextContent(clientUrl+(clientUrl.endsWith("/") ? "" : "/")+"uuid/"+pid);
                            } else {
                                isShownAt.setTextContent(baseUrl+(baseUrl.endsWith("/") ? "" : "/")+"/uuid/"+pid);
                            }
                            record.appendChild(isShownAt);
                            
                            
                            return record;
                        } else return null;
                        
                    } catch (IOException | LexerException | ProxyHandlerException | ParserConfigurationException | SAXException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        throw new RuntimeException(e.getMessage());
                    }
                }
    };

    /** disabled for now */
//    drkramerius4("drkramerius4",
//            "http://registrdigitalizace.cz/schemas/drkramerius/v4/drkram.xsd",
//            "http://registrdigitalizace.cz/schemas/drkramerius/v4/") {
//                @Override
//                public Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier) {
//                    // find rdkramerius
//                    return null;
//                }
//    };
    
    public static final Logger LOGGER = Logger.getLogger(MetadataExport.class.getName());

    
    private MetadataExport(String metadataPrefix, String schema, String metadataNamespace) {
        this.metadataPrefix = metadataPrefix;
        this.schema = schema;
        this.metadataNamespace = metadataNamespace;
    }

    public String getMetadataNamespace() {
        return metadataNamespace;
    }

    public String getSchema() {
        return schema;
    }
    
    public String getMetadataPrefix() {
        return metadataPrefix;
    }
    

    public static MetadataExport findByPrefix(String prefix) {
        MetadataExport[] values = MetadataExport.values();
        for (MetadataExport me : values) {
            if (me.getMetadataPrefix().equals(prefix)) return me;
        }
        return null;
    }
    
    private static InputStream dcStream(SolrAccess solrAccess, Provider<User> userProvider,
            Provider<Client> clientProvider, Instances instances, HttpServletRequest request, String pid)
            throws LexerException, IOException, ProxyHandlerException {
        ProxyItemHandler redirectHandler = findRedirectHandler(solrAccess, userProvider, clientProvider, instances, request, pid, null);
        InputStream directStreamDC = redirectHandler.directStreamDC();
        return directStreamDC;
    }

    public Document getDC(ProxyItemHandler handler) throws ProxyHandlerException, ParserConfigurationException, SAXException, IOException {
        InputStream is = handler.directStreamDC();
        if (is != null) {
            Document document = XMLUtils.parseDocument(is, true);
            return document;
        } else return null;
        
    }
    
    private String metadataPrefix;
    private String schema;
    private String metadataNamespace;
    
    /*
     *     @Inject
    @Named("forward-client")
    Provider<Client> clientProvider;

     */
    
    public static ProxyItemHandler findRedirectHandler(SolrAccess solrAccess,Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request, String pid, String source) throws LexerException, IOException {
        if (source == null) {
            source = defaultDocumentSource(solrAccess, pid);
        }
        OneInstance found = instances.find(source);
        if (found!= null) {
            String remoteAddress = IPAddressUtils.getRemoteAddress(request, KConfiguration.getInstance().getConfiguration());
            ProxyItemHandler proxyHandler = found.createProxyItemHandler(userProvider.get(), clientProvider.get(), solrAccess, source, pid, remoteAddress);
            return proxyHandler;
        } else {
            return null;
        }
    }

    private static String defaultDocumentSource(SolrAccess solrAccess, String pid) throws IOException {
        org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
        String leader = CDKUtils.findCDKLeader(solrDataByPid.getDocumentElement());
        List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
        return leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
    }

    
    
    private static void metadataProvider(Instances instances, OAIRecord oaiRec, Element provider, String defaultValue) {
        List<String> cdkCollections = oaiRec.getCdkCollections();
        if (cdkCollections.size() > 0) {
            String acronym = cdkCollections.get(0);
            OneInstance found = instances.find(acronym.trim());
            if (found != null) {
                provider.setTextContent(found.getRegistrInfo().get(OneInstance.NAME_ENG));
            } else {
                provider.setTextContent(acronym.trim());
            }
            
        } else {
            provider.setTextContent(defaultValue);
        }
    }

    public abstract Element performOnCDKSide(SolrAccess solrAccess,Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, HttpServletRequest request,   Document owningDocument, OAIRecord oaiRec, OAISet set);
    
    //public abstract Element perform(HttpServletRequest request, ProxyItemHandler handler, Document owningDocument, String oaiIdentifier, OAISet set);

    /*
     * ProxyItemHandler handler,
     */
//    public static final Logger LOGGER = Logger.getLogger(MetadataExport.class.getName());
    
}
