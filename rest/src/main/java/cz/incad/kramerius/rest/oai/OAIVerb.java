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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.client.v60.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.oai.exceptions.OAIException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public enum OAIVerb {
    // metadata formats 
    ListMetadataFormats {
        @Override
        public void perform(Provider<User> userProvider, Provider<Client> clientProvider, Instances instances, ConfigManager configManager,ProxyFilter proxyFilter, SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws OAIException{

            Element requestElement = OAITools.requestElement(doc, OAIVerb.ListMetadataFormats,null,ApplicationURL.applicationURL(request),null);
            doc.getDocumentElement().appendChild(requestElement);
            
            Element listMetadataPrefix = doc.createElement("ListMetadataFormats");
            doc.getDocumentElement().appendChild(listMetadataPrefix);
            MetadataExport[] values = MetadataExport.values();
            for (MetadataExport metadataExport : values) {
                Element metadataFormat= doc.createElement("metadataFormat");

                Element metadataPrefix = doc.createElement("metadataPrefix");
                metadataPrefix.setTextContent(metadataExport.getMetadataPrefix());
                metadataFormat.appendChild(metadataPrefix);
                
                Element schema = doc.createElement("schema");
                schema.setTextContent(metadataExport.getSchema());
                metadataFormat.appendChild(schema);

                Element metadataNamespace = doc.createElement("metadataNamespace");
                metadataNamespace.setTextContent(metadataExport.getMetadataNamespace());
                metadataFormat.appendChild(metadataNamespace);
                
                listMetadataPrefix.appendChild(metadataFormat);
            }
            
            doc.getDocumentElement().appendChild(listMetadataPrefix);

        }
    },
    ListSets {
        @Override
        public void perform(Provider<User> userProvider,Provider<Client> clientProvider, Instances instances, ConfigManager configManager,ProxyFilter proxyFilter, SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws OAIException{
            try {
                Element requestElement = OAITools.requestElement(doc, OAIVerb.ListSets, null,ApplicationURL.applicationURL(request), null);
                doc.getDocumentElement().appendChild(requestElement);

                String baseUrl = ApplicationURL.applicationURL(request);
                URL urlObject = new URL(baseUrl);

                OAISets sets = new OAISets(configManager, urlObject.getHost());

                Element listSets = doc.createElement("ListSets");
                doc.getDocumentElement().appendChild(listSets);
                List<OAISet> oaiSets = sets.getAOISets();
                
                for (OAISet oaiIterationSet : oaiSets) {
                    if (!oaiIterationSet.getSetSpec().equals(OAISet.DEFAULT_SET_KEYWORD)) {
                        Element setDefinition= doc.createElement("set");

                        Element setSpec = doc.createElement("setSpec");
                        setSpec.setTextContent(oaiIterationSet.getSetSpec());
                        setDefinition.appendChild(setSpec);

                        Element setName = doc.createElement("setName");
                        setName.setTextContent(oaiIterationSet.getSetName());
                        setDefinition.appendChild(setName);
                        
                        Element setDescription = doc.createElement("setDescription");
                        if (oaiIterationSet.getSetDescription() != null) {
                            setDescription.setTextContent(oaiIterationSet.getSetDescription());
                        }
                        setDefinition.appendChild(setDescription);
                        
                        listSets.appendChild(setDefinition);
                    }
                }
                
                doc.getDocumentElement().appendChild(listSets);
            } catch (MalformedURLException | DOMException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new OAIException(ErrorCode.badArgument, OAIVerb.ListIdentifiers,null, ApplicationURL.applicationURL(request),null);
            }
        }
    },

    Identify {

        @Override
        public void perform(Provider<User> userProvider,Provider<Client> clientProvider, Instances instances, ConfigManager configManager,ProxyFilter proxyFilter,  SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws OAIException {

            try {
                String url = ApplicationURL.applicationURL(request);

                Element requestElement = OAITools.requestElement(doc, OAIVerb.Identify, null,ApplicationURL.applicationURL(request), null);
                doc.getDocumentElement().appendChild(requestElement);
                
                
                String oaiName = KConfiguration.getInstance().getConfiguration().getString(REPOSITORY_NAME,"kramerius");
                String baseUrl = KConfiguration.getInstance().getConfiguration().getString(REPOSITORY_BASE_URL,url);
                String adminEmail = KConfiguration.getInstance().getConfiguration().getString(REPOSITORY_ADMIN_EMAIL,"-none-");
                String earliestDateTimestamp = KConfiguration.getInstance().getConfiguration().getString("oai.earliestDatestamp","2012-06-30T22:26:40Z");
                
                URL urlObject = new URL(baseUrl);
                
                
                String protocolVersion = KConfiguration.getInstance().getConfiguration().getString("oai.protocolVersion","2.0");
                String deletedRecord = KConfiguration.getInstance().getConfiguration().getString("oai.deletedRecord","transient");
                String granularity = KConfiguration.getInstance().getConfiguration().getString("oai.granularity","YYYY-MM-DDThh:mm:ssZ");
                
                Element identify = doc.createElement("Identify");
                    
                Element repositoryNameElm = doc.createElement("repositoryName");
                repositoryNameElm.setTextContent(oaiName);
                identify.appendChild(repositoryNameElm);
                
                Element baseURLElm = doc.createElement("baseURL");
                baseURLElm.setTextContent(baseUrl);
                identify.appendChild(baseURLElm);
                
                Element protocolVersionElm = doc.createElement("protocolVersion");
                protocolVersionElm.setTextContent(protocolVersion);
                identify.appendChild(protocolVersionElm);
                
                Element adminEmailElm = doc.createElement("adminEmail");
                adminEmailElm.setTextContent(adminEmail);
                identify.appendChild(adminEmailElm);

                Element earliestDatestampElm = doc.createElement("earliestDatestamp");
                earliestDatestampElm.setTextContent(earliestDateTimestamp);
                identify.appendChild(earliestDatestampElm);

                Element deleteRecordsElm = doc.createElement("deletedRecord");
                deleteRecordsElm.setTextContent(deletedRecord);
                identify.appendChild(deleteRecordsElm);

                Element granularityElm = doc.createElement("granularity");
                granularityElm.setTextContent(granularity);
                identify.appendChild(granularityElm);
                
                Element descriptionElm = doc.createElement("description");
                identify.appendChild(descriptionElm);
                
                Element oaiIdentifierElm = doc.createElementNS("http://www.openarchives.org/OAI/2.0/oai-identifier", "oai-identifier");
                descriptionElm.appendChild(oaiIdentifierElm);
                
                Element schemeElm = doc.createElement("scheme");
                schemeElm.setTextContent("oai");
                descriptionElm.appendChild(schemeElm);

                Element repositoryIdentifierElm = doc.createElement("repositoryIdentifier");
                repositoryIdentifierElm.setTextContent(urlObject.getHost());
                descriptionElm.appendChild(schemeElm);

                Element delimiterElm = doc.createElement("delimiter");
                delimiterElm.setTextContent(":");
                descriptionElm.appendChild(delimiterElm);
                
                Element sampleIdentifierElm = doc.createElement("sampleIdentifier");
                sampleIdentifierElm.setTextContent("oai:"+urlObject.getHost()+":uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22");
                descriptionElm.appendChild(sampleIdentifierElm);

                doc.getDocumentElement().appendChild(identify);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new OAIException(ErrorCode.badArgument, OAIVerb.ListIdentifiers,null, ApplicationURL.applicationURL(request),null);
            }
        }
        
    },
    
    ListRecords {
        @Override
        public void perform(Provider<User> userProvider,Provider<Client> clientProvider, Instances instances, ConfigManager configManager,ProxyFilter proxyFilter,  SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws OAIException{

            OAISet selectedSet =  null;
            MetadataExport selectedMetadata = null;
            try {
                String baseUrl = ApplicationURL.applicationURL(request);
                URL urlObject = new URL(baseUrl);
                OAISets sets = new OAISets(configManager, urlObject.getHost());
                String set = request.getParameter("set");

                String resumptionToken = request.getParameter("resumptionToken");
                String metadataPrefix = request.getParameter("metadataPrefix");
                if (metadataPrefix != null || resumptionToken != null) {
                    int rows = KConfiguration.getInstance().getConfiguration().getInt(REPOSITORY_ROWS_IN_RESULTS,600);
                    if (set != null) {
                        selectedSet = sets.findBySet(set);
                        if (selectedSet ==null) {
                            throw new OAIException(ErrorCode.badArgument, OAIVerb.ListRecords, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                        }
                    } else if (resumptionToken != null){
                        selectedSet = sets.findByToken(resumptionToken);
                        metadataPrefix = OAITools.metadataFromResumptionToken(resumptionToken);
                        if (metadataPrefix == null || MetadataExport.findByPrefix(metadataPrefix) == null) {
                            throw new OAIException(ErrorCode.badResumptionToken, OAIVerb.ListRecords, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                        }
                    }

                    selectedMetadata = MetadataExport.findByPrefix(metadataPrefix);
                    
                    if (selectedMetadata != null) {
                        if (selectedSet == null) {
                            selectedSet =  sets.getDefaultSet();
                        }

                        Element requestElement = OAITools.requestElement(doc, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                        doc.getDocumentElement().appendChild(requestElement);

                        Element identify = doc.createElement("ListRecords");
                        OAIResults results = null;
                        
                        if (resumptionToken != null) {
                            String solrCursor = OAITools.solrCursorMarkFromResumptionToken(resumptionToken);
                            results = selectedSet.findRecords(proxyFilter, solrAccess, solrCursor,metadataPrefix,rows);
                            for (OAIRecord oaiRec : results.getRecords()) { 

                                Element record= doc.createElement("record");
                                Element header = oaiRec.toHeader(doc, selectedSet.getSetSpec());
                                
                                //  Instances instances, HttpServletRequest request,   Document owningDocument, String oaiIdentifier, MetadataExport export, OAISet set
                                
                                Element metadata = doc.createElement("metadata");
                                Element metadataElm = oaiRec.toMetadata(solrAccess, userProvider, clientProvider, instances, request,  doc, oaiRec.getIdentifier(), selectedMetadata,selectedSet);
                                if (metadataElm != null) {
                                    metadata.appendChild(metadataElm);
                                } else {
                                    record.setAttribute("deleted","true");
                                }
                                
                                record.appendChild(header);
                                record.appendChild(metadata);
                                
                                identify.appendChild(record);
                                
                                
                            }
                        } else {
                            results = selectedSet.findRecords(proxyFilter, solrAccess,"*", metadataPrefix,rows);
                            for (OAIRecord oaiRec : results.getRecords()) { 

                                Element record= doc.createElement("record");
                                Element header = oaiRec.toHeader(doc, selectedSet.getSetSpec());
                                
                                
                                Element metadata = doc.createElement("metadata");
                                metadata.appendChild(oaiRec.toMetadata(solrAccess, userProvider, clientProvider, instances, request,  doc, oaiRec.getIdentifier(), selectedMetadata,selectedSet));

                                record.appendChild(header);
                                record.appendChild(metadata);
                                
                                identify.appendChild(record);
                            }
                        }

                        if (results.getResumptionToken()!= null) {
                            Element resToken = doc.createElement("resumptionToken");
                            resToken.setAttribute("completeListSize", ""+results.getCompleteListSize());
                            resToken.setTextContent(results.getResumptionToken());
                            identify.appendChild(resToken);
                        }

                        doc.getDocumentElement().appendChild(identify);
                    } else {
                        throw new OAIException(ErrorCode.noMetadataFormats, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                    }
                    
                } else {
                    throw new OAIException(ErrorCode.noMetadataFormats, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                }

            } catch ( IOException | SAXException | ParserConfigurationException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new OAIException(ErrorCode.badArgument, OAIVerb.ListIdentifiers,selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
            }
        }
    },
    ListIdentifiers {
        @Override
        public void perform(Provider<User> userProvider,Provider<Client> clientProvider, Instances instances, ConfigManager configManager,ProxyFilter proxyFilter,  SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws OAIException{

            OAISet selectedSet =  null;
            MetadataExport selectedMetadata = null;
            try {
                String baseUrl = ApplicationURL.applicationURL(request);


                URL urlObject = new URL(baseUrl);
                OAISets sets = new OAISets(configManager, urlObject.getHost());
                String set = request.getParameter("set");
                String resumptionToken = request.getParameter("resumptionToken");
                String metadataPrefix = request.getParameter("metadataPrefix");
                if (metadataPrefix != null || resumptionToken != null) {
                    int rows = KConfiguration.getInstance().getConfiguration().getInt(REPOSITORY_ROWS_IN_RESULTS,600);
                    
                    if (set != null) {
                        selectedSet = sets.findBySet(set);
                    } else if (resumptionToken != null){
                        selectedSet = sets.findByToken(resumptionToken);
                        metadataPrefix = OAITools.metadataFromResumptionToken(resumptionToken);
                        if ( metadataPrefix == null || MetadataExport.findByPrefix(metadataPrefix) == null) {
                            throw new OAIException(ErrorCode.badResumptionToken, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                        }
                    }

                    selectedMetadata = MetadataExport.findByPrefix(metadataPrefix);
                    
                    if (selectedMetadata != null) {
                        if (selectedSet == null) {
                            selectedSet =  sets.getDefaultSet();
                        }

                        Element requestElement = OAITools.requestElement(doc, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                        doc.getDocumentElement().appendChild(requestElement);

                        Element identify = doc.createElement("ListIdentifiers");
                        OAIResults results = null;
                        if (resumptionToken != null) {
                            String solrCursor = OAITools.solrCursorMarkFromResumptionToken(resumptionToken);
                            results = selectedSet.findRecords(proxyFilter, solrAccess, solrCursor,metadataPrefix,rows);
                            for (OAIRecord oaiRec : results.getRecords()) { identify.appendChild(oaiRec.toHeader(doc, selectedSet.getSetSpec()));}
                        } else {
                            results = selectedSet.findRecords(proxyFilter, solrAccess,"*", metadataPrefix,rows);
                            for (OAIRecord oaiRec : results.getRecords()) { identify.appendChild(oaiRec.toHeader(doc, selectedSet.getSetSpec()));}
                        }

                        if (results.getResumptionToken()!= null) {
                            Element resToken = doc.createElement("resumptionToken");
                            resToken.setAttribute("completeListSize", ""+results.getCompleteListSize());
                            resToken.setTextContent(results.getResumptionToken());
                            identify.appendChild(resToken);
                        }

                        doc.getDocumentElement().appendChild(identify);
                    } else {
                        throw new OAIException(ErrorCode.noMetadataFormats, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                    }
                    
                } else {
                    throw new OAIException(ErrorCode.noMetadataFormats, OAIVerb.ListIdentifiers, selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
                }

            } catch ( IOException | SAXException | ParserConfigurationException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new OAIException(ErrorCode.badArgument, OAIVerb.ListIdentifiers,selectedSet, ApplicationURL.applicationURL(request),selectedMetadata);
            }
        }
    },
    GetRecord {
        @Override
        public void perform(Provider<User> userProvider,Provider<Client> clientProvider, Instances instances, ConfigManager configManager, ProxyFilter proxyFilter, SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws OAIException{
            
            MetadataExport selectedMetadata = null;
            try {
                String baseUrl = ApplicationURL.applicationURL(request);

                String identifier = request.getParameter("Identifier");
                String metadataPrefix = request.getParameter("metadataPrefix");
                if (metadataPrefix == null || MetadataExport.findByPrefix(metadataPrefix) == null) {
                    throw new OAIException(ErrorCode.cannotDisseminateFormat, OAIVerb.GetRecord, null, ApplicationURL.applicationURL(request),selectedMetadata);
                }

                selectedMetadata = MetadataExport.findByPrefix(metadataPrefix);
                
                if (selectedMetadata != null) {

                    Element requestElement = OAITools.requestElement(doc, OAIVerb.GetRecord, null, ApplicationURL.applicationURL(request),selectedMetadata);
                    doc.getDocumentElement().appendChild(requestElement);

                    Element identify = doc.createElement("GetRecord");
                    OAIRecord oaiRec = OAIRecord.findRecord(solrAccess,identifier);
                    if (oaiRec != null) {

                        Element record= doc.createElement("record");
                        Element header = oaiRec.toHeader(doc, null);
                        
                        
                        Element metadata = doc.createElement("metadata");
                        Element metadataElm = oaiRec.toMetadata(solrAccess, userProvider, clientProvider, instances, request,  doc, oaiRec.getIdentifier(), selectedMetadata,null);
                        if (metadataElm != null) {
                            metadata.appendChild(metadataElm);
                        } else {
                            record.setAttribute("deleted", "true");
                        }

                        record.appendChild(header);
                        record.appendChild(metadata);
                        
                        identify.appendChild(record);
                        
                    } else {
                        throw new OAIException(ErrorCode.idDoesNotExist, OAIVerb.GetRecord, null, ApplicationURL.applicationURL(request),selectedMetadata);
                    }
                    doc.getDocumentElement().appendChild(identify);
                } else {
                    throw new OAIException(ErrorCode.cannotDisseminateFormat, OAIVerb.GetRecord, null, ApplicationURL.applicationURL(request),selectedMetadata);
                }

            } catch ( IOException | SAXException | ParserConfigurationException  e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new OAIException(ErrorCode.badArgument, OAIVerb.GetRecord,null, ApplicationURL.applicationURL(request),selectedMetadata);
            }
            
        }
    };

    private static final String REPOSITORY_ADMIN_EMAIL = "oai.adminEmail";
    private static final String REPOSITORY_BASE_URL = "oai.baseUrl";
    private static final String REPOSITORY_NAME = "oai.repositoryName";
    private static final String REPOSITORY_ROWS_IN_RESULTS = "oai.rowsInResults";

    
    // PERFORM 
    public abstract void perform( Provider<User> userPRovider, Provider<Client> clientProvider, Instances instances, ConfigManager configManager, ProxyFilter proxyFilter,  SolrAccess solrAccess, HttpServletRequest request, Document doc, Element rootElement) throws Exception;
    
    public static Logger LOGGER = Logger.getLogger(OAIVerb.class.getName());
}
