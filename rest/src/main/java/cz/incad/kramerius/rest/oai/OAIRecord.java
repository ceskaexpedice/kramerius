/*
 * Copyright (C) Jan 14, 2024 Pavel Stastny
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import cz.incad.kramerius.utils.ApplicationURL;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.cdk.cache.impl.CDKRequestItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.utils.XMLUtils;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class OAIRecord {
    public static final Logger LOGGER = Logger.getLogger(OAIRecord.class.getName());
    /*
    private String identifier;
    private String solrIdentifier;
    */
    
    private String identifier;
    private String solrIdentifier;
    
    // CDK extension
    
    private List<String> cdkCollections = new ArrayList<>();
    
    private String dateTimeStamp;
    private CDKRequestCacheSupport cacheSupport;
    
    public OAIRecord(String solrIdentifier, String identifier, String dateTimeStamp, CDKRequestCacheSupport support) {
        super();
        this.solrIdentifier = solrIdentifier;
        this.identifier = identifier;
        this.dateTimeStamp = dateTimeStamp;
        this.cacheSupport = support;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public String getSolrIdentifier() {
        return solrIdentifier;
    }
    
    // specific to cdk.server = true
    public List<String> getCdkCollections() {
        return cdkCollections;
    }
	
	// specific to cdk.server = true
    public void setCdkCollections(List<String> cdkCollections) {
        this.cdkCollections = cdkCollections;
    }

    /** find oai record */
    public static OAIRecord findRecord(SolrAccess solrAccess,String oaiIdentifier, CDKRequestCacheSupport support) throws IOException, ParserConfigurationException, SAXException  {
        String pid = OAITools.pidFromOAIIdentifier(oaiIdentifier);

        String encodedQuery = URLEncoder.encode(String.format("pid:\"%s\"", pid),"UTF-8");
        String query = String.format("q=%s", encodedQuery);
        String solrResponseXml = solrAccess.requestWithSelectReturningString(query, "xml", null);
        Document document = XMLUtils.parseDocument(new StringReader(solrResponseXml));
        
        Element result = XMLUtils.findElement(document.getDocumentElement(), new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return element.getNodeName().equals("result");
            }
        });
        
        if (result != null) {
            List<Element> docs = XMLUtils.getElements(result, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    return element.getNodeName().equals("doc");
                }
            });

            if (docs.size() > 0) {
                    
                Element doc = docs.get(0);
                
                Element pidElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) { 
                        String name = element.getAttribute("name");
                        return name.equals("pid");
                        
                    }
                });
                
                List<Element> collections  = XMLUtils.getElements(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) { 
                        String name = element.getAttribute("name");
                        return name.equals("cdk.collection");
                        
                    }
                });

                Element dateElm = XMLUtils.findElement(doc, new XMLUtils.ElementsFilter() {
                    @Override
                    public boolean acceptElement(Element element) { 
                        String name = element.getAttribute("name");
                        return name.equals("indexed");
                        
                    }
                });

                OAIRecord oaiRecord = new OAIRecord(pidElm.getTextContent(), oaiIdentifier, dateElm != null ? dateElm.getTextContent() : "", support);
                oaiRecord.setCdkCollections(collections.stream().map(Element::getTextContent).collect(Collectors.toList()));
                return oaiRecord;
                
            } else return null;
        } else {
            return null;
        }

    }

    /** render metadata */
    public List<Element> toMetadataOnLocal(HttpServletRequest request, AkubraRepository fa, Document doc, MetadataExport export, OAISet set) {
        return export.perform(request, fa, doc, identifier, set);
    }
    public List<Element> toMetadataOnCDKSide(SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider , Instances instances, HttpServletRequest request, Document owningDocument, String oaiIdentifier, MetadataExport export, OAISet set,CDKRequestCacheSupport support) {
        return export.performOnCDKSide(solrAccess,userProvider,  apacheClientProvider, instances, request,   owningDocument, this,  set, support);
	}
    
    public Element toHeaderOnCDKSide(Document doc, OAISet set, SolrAccess solrAccess,Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvieder, Instances instances, HttpServletRequest request, String source, CDKRequestCacheSupport cacheSupport) throws IOException {
        Element header = doc.createElement("header");

        Element identifier = doc.createElement("identifier");
        identifier.setTextContent(this.identifier);
        header.appendChild(identifier);
        
        //OffsetDateTime now = OffsetDateTime.now();
        Element datestamp = doc.createElement("datestamp");
        datestamp.setTextContent(this.dateTimeStamp);
        header.appendChild(datestamp);
        datestamp.setTextContent(this.dateTimeStamp);
        
        if (set != null) {
            Element setSpecElm = doc.createElement("setSpec");
            setSpecElm.setTextContent(set.getSetSpec());
            header.appendChild(setSpecElm);
        }
		
//		// local 
//		String pid = OAITools.pidFromOAIIdentifier(this.identifier);
//		if (!fa.isObjectAvailable(pid) && (!pid.contains("_"))) {
//			header.setAttribute("status", "deleted");
//		}

		try {
			String pid = OAITools.pidFromOAIIdentifier(this.identifier);
            org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
            ProxyItemHandler redirectHandler = MetadataExport.findRedirectHandler(solrDataByPid, solrAccess, userProvider, apacheClientProvieder, instances, request, pid, null);
			if (!pid.contains("_")) {

                String baseUrl = ApplicationURL.applicationURL(request);
                InputStream directStreamDC = null;
                String cacheURl = baseUrl+"/dc";

                if (redirectHandler != null) {
                    CDKRequestItem hit = cacheSearchHitByPid(cacheURl, pid, cacheSupport);
                    if (hit != null) {
                        directStreamDC = new ByteArrayInputStream(hit.getData().toString().getBytes(Charset.forName("UTF-8")));
                    } else {
                        InputStream  dc = redirectHandler.directStreamDC(null);
                        String remoteData = IOUtils.toString(dc, "UTF-8");
                        saveToCache(remoteData, cacheURl, pid, cacheSupport);
                        directStreamDC = new ByteArrayInputStream(remoteData.getBytes("UTF-8"));
                    }
                }

				if (redirectHandler != null &&  directStreamDC == null) {
					header.setAttribute("status", "deleted");
				// co s tim ??
				} else if (redirectHandler == null){
					header.setAttribute("status", "deleted");
					
				}
			}
		} catch (DOMException | LexerException | IOException | ProxyHandlerException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		}
        return header;
	}
	
    public Element toHeaderOnLocal(Document doc, AkubraRepository akubraRepository, OAISet set ) throws IOException {
        Element header = doc.createElement("header");

        Element identifier = doc.createElement("identifier");
        identifier.setTextContent(this.identifier);
        header.appendChild(identifier);
        
        //OffsetDateTime now = OffsetDateTime.now();
        Element datestamp = doc.createElement("datestamp");
        datestamp.setTextContent(this.dateTimeStamp);
        header.appendChild(datestamp);
        datestamp.setTextContent(this.dateTimeStamp);
        
        if (set != null) {
            Element setSpecElm = doc.createElement("setSpec");
            setSpecElm.setTextContent(set.getSetSpec());
            header.appendChild(setSpecElm);
        }
		
		// local 
		String pid = OAITools.pidFromOAIIdentifier(this.identifier);
		if (!akubraRepository.exists(pid) && (!pid.contains("_"))) {
			header.setAttribute("status", "deleted");
		}
        return header;
	}	

    


    protected void saveToCache(String data, String url, String pid, CDKRequestCacheSupport cacheSupport) {
        try {
            CDKRequestItem<String> cacheItem = (CDKRequestItem<String>)  CDKRequestItemFactory.createCacheItem(
                    data,
                    "text/xml",
                    url,
                    pid,
                    null,
                    LocalDateTime.now(),
                    null
            );

            LOGGER.info( String.format("Storing cache item %s", cacheItem.toString()));
            cacheSupport.save(cacheItem);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }


    protected CDKRequestItem cacheSearchHitByPid(String url, String pid,  CDKRequestCacheSupport cacheSupport) {
        int days = KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item",30);
        LOGGER.log(Level.INFO, String.format("this.cacheSupport.find(\"%s\", \"%s\",\"%s\", \"%s\")", null, url, pid, null));
        List<CDKRequestItem> cdkRequestItems = cacheSupport.find(null, url, pid, null);
        if (!cdkRequestItems.isEmpty() && !cdkRequestItems.get(0).isExpired(days)) {
            LOGGER.log(Level.INFO, String.format("this.cacheSupport.found(\"%s\", \"%s\",\"%s\", \"%s\")", null, url, pid, null));
            return cdkRequestItems.get(0);
        }
        return null;
    }

}
