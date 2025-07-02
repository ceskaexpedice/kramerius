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
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;

import cz.incad.kramerius.rest.oai.metadata.utils.OAICDKUtils;
import cz.incad.kramerius.rest.oai.record.OAIRecordSupplement;
import cz.incad.kramerius.rest.oai.strategies.MetadataExportStrategy;
import cz.incad.kramerius.rest.oai.utils.OAITools;
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

import cz.incad.kramerius.SolrAccess;

import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Represents an OAI (Open Archives Initiative) record, which contains metadata
 * and supplements related to a specific identifier.
 * This class provides methods to manage supplements, convert the record to
 * metadata formats, and handle headers for OAI responses.
 */
public class OAIRecord {

    public static final Logger LOGGER = Logger.getLogger(OAIRecord.class.getName());

    private String identifier;
    private String solrIdentifier;
    private List<OAIRecordSupplement> supplements = new ArrayList<>();
    private String dateTimeStamp;

    /**
     * Constructs an OAIRecord with the specified identifier, solrIdentifier, and dateTimeStamp.
     *
     * @param solrIdentifier The identifier used in Solr.
     * @param identifier The OAI identifier for the record.
     * @param dateTimeStamp The timestamp of the record in ISO 8601 format.
     */
    public OAIRecord(String solrIdentifier, String identifier, String dateTimeStamp) {
        super();
        this.solrIdentifier = solrIdentifier;
        this.identifier = identifier;
        this.dateTimeStamp = dateTimeStamp;
    }


    /**
     * Adds a supplement to the OAI record.
     * @param supplement
     * @see OAIRecordSupplement
     */
    public void addSupplement(OAIRecordSupplement supplement) {
        this.supplements.add(supplement);
    }

    /**
     * Removes a supplement from the OAI record.
     * @param supplement
     * @see  OAIRecordSupplement
     */
    public void removeSupplement(OAIRecordSupplement supplement) {
        this.supplements.remove(supplement);
    }

    /**
     * Returns the list of supplements associated with this OAI record.
     * @return List of OAIRecordSupplement objects
     * @see OAIRecordSupplement
     */
    public List<OAIRecordSupplement> getSupplements() {
        return  this.supplements;
    }

    /**
     * Returns the identifier of the OAI record.
     * @return The OAI identifier as a String.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the Solr identifier of the OAI record.
     * This identifier is used to access the record in Solr.
     * @return The Solr identifier as a String.
     */
    public String getSolrIdentifier() {
        return solrIdentifier;
    }


    /**
     * Generates the metadata section of the OAI-PMH record for export. Uses on the local Kramerius instance.
     *
     * @param request The HTTP request object, used to determine the context of the export.
     * @param fa The Akubra repository instance, used to access the underlying data store.
     * @param doc   The Document object where the metadata will be written.
     * @param export The MetadataExportStrategy instance that defines how the metadata should be exported.
     * @param set The OAISet instance representing the set to which this record belongs, or null if not applicable.
     * @return  A list of XML elements representing the metadata for this OAI record.
     * @see cz.incad.kramerius.rest.oai.strategies.MetadataExportStrategy
     */
    public List<Element> toMetadataOnLocal(HttpServletRequest request, AkubraRepository fa, Document doc, MetadataExportStrategy export, OAISet set) {
        return export.perform(request, doc, fa, identifier, set);
    }

    /**
     * Generates the metadata section of the OAI-PMH record for export. Uses on the CDK side.
     * @param request              The HTTP request object, used to determine the context of the export.
     * @param instances            The Instances configuration used for distributed processing.
     * @param owningDocument       The Document object where the metadata will be written.
     * @param solrAccess           The SolrAccess instance used to access Solr data.
     * @param userProvider         A provider for the current user, used for access control and context.
     * @param apacheClientProvider A provider for the Apache HTTP client, used for making HTTP requests.
     * @param oaiIdentifier        The OAI identifier for the record, used to uniquely identify it in OAI-PMH responses.
     * @param export               The MetadataExportStrategy instance that defines how the metadata should be exported.
     * @param set                  The OAISet instance representing the set to which this record belongs, or null if not applicable.
     */
    public List<Element> toMetadataOnCDKSide(HttpServletRequest request, Instances instances, Document owningDocument, SolrAccess solrAccess, Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvider , String oaiIdentifier, MetadataExportStrategy export, OAISet set, CDKRequestCacheSupport support) {
        return export.performOnCDKSide( request, owningDocument, solrAccess, userProvider,  apacheClientProvider, instances,     this,  set, support);
	}


    /**
     * Converts the OAI record to an XML header element for OAI-PMH responses on the CDK side.
     * This method includes checks for the existence of the record in Solr and handles redirects if necessary.
     *
     * @param doc The Document object where the header will be written.
     * @param set The OAISet instance representing the set to which this record belongs, or null if not applicable.
     * @param solrAccess The SolrAccess instance used to access Solr data.
     * @param userProvider A provider for the current user, used for access control and context.
     * @param apacheClientProvieder A provider for the Apache HTTP client, used for making HTTP requests.
     * @param instances Instances configuration used for distributed processing.
     * @param request The HTTP request object, used to determine the context of the export.
     * @param source The source of the request, typically "CDK".
     * @param cacheSupport Cache support for optimizing CDK-side exports.
     * @return An Element representing the header of the OAI-PMH record.
     */
    public Element toHeaderOnCDKSide(Document doc, OAISet set, SolrAccess solrAccess,Provider<User> userProvider, Provider<CloseableHttpClient> apacheClientProvieder, Instances instances, HttpServletRequest request, String source, CDKRequestCacheSupport cacheSupport) throws IOException {
        Element header = doc.createElement("header");

        Element identifier = doc.createElement("identifier");
        identifier.setTextContent(this.identifier);
        header.appendChild(identifier);
        
        Element datestamp = doc.createElement("datestamp");
        datestamp.setTextContent(this.dateTimeStamp);
        header.appendChild(datestamp);
        datestamp.setTextContent(this.dateTimeStamp);
        
        if (set != null) {
            Element setSpecElm = doc.createElement("setSpec");
            setSpecElm.setTextContent(set.getSetSpec());
            header.appendChild(setSpecElm);
        }

		try {
			String pid = OAITools.pidFromOAIIdentifier(this.identifier);
            org.w3c.dom.Document solrDataByPid = solrAccess.getSolrDataByPid(pid);
            ProxyItemHandler redirectHandler = OAICDKUtils.findRedirectHandler(solrDataByPid, solrAccess, userProvider, apacheClientProvieder, instances, request, pid, null);
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

    /**
     * Converts the OAI record to an XML header element for OAI-PMH responses on the local Kramerius instance.
     * This method checks if the record exists in the Akubra repository and sets the status accordingly.
     *
     * @param doc The Document object where the header will be written.
     * @param akubraRepository The AkubraRepository instance used to access the underlying data store.
     * @param set The OAISet instance representing the set to which this record belongs, or null if not applicable.
     * @return An Element representing the header of the OAI-PMH record.
     */
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

    

    /**
     * Saves the given data to the cache with the specified URL and PID.
     * This method creates a cache item and stores it in the provided cache support.
     *
     * @param data The data to be cached, typically in XML format.
     * @param url The URL associated with the cached data.
     * @param pid The PID (Persistent Identifier) of the record being cached.
     * @param cacheSupport The CDKRequestCacheSupport instance used for caching operations.
     */
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


    /**
     * Searches the cache for a CDKRequestItem by its PID and URL.
     * If a valid cache hit is found, it returns the item; otherwise, it returns null.
     *
     * @param url The URL associated with the cached item.
     * @param pid The PID (Persistent Identifier) of the record being searched.
     * @param cacheSupport The CDKRequestCacheSupport instance used for caching operations.
     * @return The cached CDKRequestItem if found and valid, or null if not found or expired.
     */
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
