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
                public Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier) {
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
    
    ese("ese",
            "http://www.europeana.eu/schemas/ese/ESE-V3.2.xsd",
            "http://www.europeana.eu/schemas/ese/") {
                @Override
                public Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier) {
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
    
    
    public abstract Element perform(HttpServletRequest request, FedoraAccess fa, Document owningDocument, String oaiIdentifier);
    
//    public static final Logger LOGGER = Logger.getLogger(MetadataExport.class.getName());
    
}
