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
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.rest.oai.exceptions.OAIException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public enum MetadataExport {

    
    oaiDc("oai_dc",
            "http://www.openarchives.org/OAI/2.0/oai_dc.xsd",
            "http://www.openarchives.org/OAI/2.0/oai_dc/") {
       
        @Override
                public Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier,OAISet set) {
                    try {
                        String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
                        Document dc = fa.getDC(pid);
                        if (dc != null) {
                            Element rootElement = dc.getDocumentElement();
                            owningDocument.adoptNode(rootElement);
                            return rootElement;
                        }  else return null;
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        throw new RuntimeException(e.getMessage());
                    }
                }
    },

    
    edm("edm","http://www.europeana.eu/schemas/ese/","") {

        @Override
        public Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument,
                String oaiIdentifier,OAISet set) {

            try {
                
                String baseUrl = ApplicationURL.applicationURL(request);
                //rdf:about="uuid:6b182ad3-b9e9-11e1-1726-001143e3f55c"
                String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
                Document dc = fa.getDC(pid);
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
                rdf.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:about",oaiIdentifier);
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
                String edmDataProvider = set.getAdditionalsInfo().get("edm:dataProvider");
                if (edmDataProvider != null) {
                    edmDataPrvovider.setTextContent(edmDataProvider);
                } else {
                    edmDataPrvovider.setTextContent("Academy of Sciences Library/Knihovna Akademie věd ČR");
                }
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
                edmRights.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", "https://cdk.lib.cas.cz/uuid/"+pid);
                edmAggregation.appendChild(edmRights);
                
                
                Element edmObject = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:object");
                edmObject.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", String.format("%s/api/client/v7.0/items/%s/image", baseUrl, pid));
                edmAggregation.appendChild(edmObject);
                
                
                // ceska digitalni kniovna 
                Element edmProvider = owningDocument.createElementNS("http://www.europeana.eu/schemas/edm/","edm:provider");
                edmProvider.setTextContent("Czech digital library/Česká digitální knihovna");
                edmAggregation.appendChild(edmProvider);
                
                metadata.appendChild(edmAggregation);
                
                return metadata;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new RuntimeException(e.getMessage());
            }
        }
        
    },
    
    
    
    ese("ese",
            "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd",
            "http://www.europeana.eu/schemas/ese/") {
                @Override
                public Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier,OAISet set) {
                    try {
                        String baseUrl = ApplicationURL.applicationURL(request);
                        String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);
                        Document dc = fa.getDC(pid);
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
                        provider.setTextContent("Academy of Sciences Library"); //TODO: To configuration
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
                        
                    } catch (IOException e) {
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
    
    private String metadataPrefix;
    private String schema;
    private String metadataNamespace;
    
    
    public abstract Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier, OAISet set);
    
//    public static final Logger LOGGER = Logger.getLogger(MetadataExport.class.getName());
    
}
