 /*
  * Copyright (C) 2025  Inovatika
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
 package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerSupport;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.cdk.cache.CDKRequestItem;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 * Abstract class representing a proxy handler for forwarding HTTP requests to a Kramerius instance.
 * This class provides an interface for handling various types of digital object requests,
 * including images, metadata, OCR text, and IIIF tiles.
 */
public abstract class ProxyItemHandler extends ProxyHandlerSupport {

    /** Logger instance. */
    public static final Logger LOGGER = Logger.getLogger(ProxyItemHandler.class.getName());

    public static final Logger CACHE_LOGGER = Logger.getLogger(ProxyItemHandler.class.getName()+".cache");


    /** Enum representing the request method type (HEAD or GET). */
    public static enum RequestMethodName {
        head,
        get
    }

    /** Persistent Identifier (PID) of the requested digital object. */
    protected String pid;

    /**
     * Constructor initializing the proxy handler with necessary dependencies.
     *
     * @param reharvestManager Manager for reharvesting digital objects.
     * @param instances Instance manager for connecting with remote libraries.
     * @param user Authenticated user making the request.
     * @param apacheClient HTTP client for forwarding requests.
     * @param solrAccess Interface for querying Solr index.
     * @param source Identifier of the data source.
     * @param pid Persistent identifier of the requested object.
     * @param deleteTriggerSupport DeleteTriggerSupport instance
     * @param remoteAddr Remote client IP address.
     */
    public ProxyItemHandler(CDKRequestCacheSupport cacheSupport, ReharvestManager reharvestManager, Instances instances, User user, CloseableHttpClient apacheClient, DeleteTriggerSupport deleteTriggerSupport, SolrAccess solrAccess, String source, String pid, String remoteAddr) {
        super(cacheSupport, reharvestManager, instances,user, apacheClient,deleteTriggerSupport,solrAccess,source, remoteAddr);
        this.source = source;
        this.pid = pid;
    }


    /**
     * Returns the full image stream.
     * @param method Request method.
     * @return HTTP response with image data.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response image(RequestMethodName method,ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns the preview image stream.
     * @param method Request method.
     * @return HTTP response with preview image.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response imagePreview(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns the thumbnail image stream.
     * @param method Request method.
     * @return HTTP response with thumbnail image.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response imageThumb(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns IIIF image descriptor.
     * @param method Request method.
     * @param pid Persistent identifier of the object.
     * @return HTTP response with IIIF metadata.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response iiifInfo(RequestMethodName method, String pid, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns a specific IIIF tile.
     * @param method Request method.
     * @param pid Persistent identifier of the object.
     * @param region Region coordinates.
     * @param size Image size.
     * @param rotation Rotation angle.
     * @param qf Quality factor.
     * @return HTTP response with the requested tile.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response iiifTile(RequestMethodName method, String pid,  String region,  String size, String rotation, String qf, ApiCallEvent event) throws ProxyHandlerException;


    /**
     * Returns Zoomify image properties.
     * @param method Request method.
     * @return HTTP response image properties.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response zoomifyImageProperties(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns a specific zoomify tile.
     * @param tileGroupStr Tile group.
     * @param tileStr Tile specification.
     * @return HTTP response with the requested tile.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response zoomifyTile(String tileGroupStr, String tileStr, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns OCR stream
     * @param method Request method.
     * @return HTTP response with the requested OCR.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response textOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns ALTO XML stream
     * @param method Request method.
     * @return HTTP response with the requested ALTO.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response altoOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns BIBLIO_MODS xml
     * @param method Request method.
     * @return HTTP response with the requested BIBLO_MODS xml.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response mods(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns DublinCore xml
     * @param method Request method.
     * @return HTTP response with the requested DC xml.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response dc(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException;


    /**
     * Returns info information about pid
     * @return HTTP response with the information.
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response info(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns information about the image in JSON representation
     * @return JSON information
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response infoImage(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns structure information
     * @return JSON structure information
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response infoStructure(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns data information
     * @return JSON data information
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response infoData(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns providedByLicense information
     * @return Provided by license JSON information
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response providedByLicenses(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns mp3 stream
     * @return Returns mp3 stream
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response audioMP3(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns ogg stream
     * @return Returns ogg stream
     * @throws ProxyHandlerException if forwarding fails.
     */
    public abstract Response audioOGG(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * REturns wav stream
     * @return Returns wav stream
     * @throws ProxyHandlerException
     */
    public abstract Response audioWAV(ApiCallEvent event) throws ProxyHandlerException;


    // helper methods for direct access to dc stream

    /**
     * Returns an InputStream containing Dublin Core (DC) metadata.
     *
     * @return an InputStream with Dublin Core metadata
     * @throws ProxyHandlerException if an error occurs while obtaining the stream
     */
    public abstract InputStream directStreamDC(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns an InputStream containing BIBLIO_MODS metadata.
     *
     * @return an InputStream with BIBLIO_MODS metadata
     * @throws ProxyHandlerException if an error occurs while obtaining the stream
     */
    public abstract InputStream directStreamBiblioMods(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns true if DC stream exists
     * @return True if dc stream exists
     * @throws ProxyHandlerException if an error occurs while obtaining the stream
     */
    public abstract boolean isStreamDCAvaiable(ApiCallEvent event) throws ProxyHandlerException;

    /**
     * Returns true if BIBLIO_MODS stream exists
     * @return True if BIBLIO-MODS stream exists
     * @throws ProxyHandlerException if an error occurs while obtaining the stream
     */
    public abstract boolean isStreamBiblioModsAvaiable(ApiCallEvent event) throws ProxyHandlerException;



    /**
     * Checks if thumbnail redirection for this instance is enabled.
     * @return True if redirection is enabled, false otherwise.
     */
    public boolean imageThumbForceRedirection() {
        boolean redirection = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + this.source + ".thumb", false);
        return redirection;
    }

    protected CDKRequestItem cacheItemHit_PID_USER(String url, String pid, boolean user, String cacheModifier, ApiCallEvent event) {
        long start = System.currentTimeMillis();
        int days = KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item",30);
        String userIdentification = user ? this.userCacheIdentification() : null;
        LOGGER.log(Level.FINE, String.format("this.cacheSupport.find(\"%s\", \"%s\",\"%s\", \"%s\")", this.source, url, pid, userIdentification));
        List<CDKRequestItem> cdkRequestItems = this.cacheSupport.find(this.source, url, pid, userIdentification);
        if (!cdkRequestItems.isEmpty() && !cdkRequestItems.get(0).isExpired(days)) {
            LOGGER.fine(String.format("Found in cache %s",cdkRequestItems.get(0)));
            long stop = System.currentTimeMillis();
            List<Triple<String, Long, Long>> triples = event.getGranularTimeSnapshots() != null ? event.getGranularTimeSnapshots() : null;
            if (triples != null) {
                triples.add(Triple.of(String.format("cache/%s", cacheModifier), start, stop));
            }
            return cdkRequestItems.get(0);
        }
        return null;
    }

    protected String cacheStringHit_PID_USER(String url, String pid, boolean user, String cacheModifier,ApiCallEvent event) {
        long start = System.currentTimeMillis();
        int days = KConfiguration.getInstance().getConfiguration().getInt("cdk.cache.item",30);
        String userIdentification = user ? this.userCacheIdentification() : null;
        LOGGER.log(Level.FINE, String.format("this.cacheSupport.find(\"%s\", \"%s\",\"%s\", \"%s\")", this.source, url, pid, userIdentification));
        List<CDKRequestItem> cdkRequestItems = this.cacheSupport.find(this.source, url, pid, userIdentification);
        if (!cdkRequestItems.isEmpty() && !cdkRequestItems.get(0).isExpired(days)) {
            LOGGER.fine(String.format("Found in cache %s",cdkRequestItems.get(0)));
            String data = (String) cdkRequestItems.get(0).getData();
            long stop = System.currentTimeMillis();
            List<Triple<String, Long, Long>> triples = event.getGranularTimeSnapshots() != null ? event.getGranularTimeSnapshots() : null;
            if (triples != null) {
                triples.add(Triple.of(String.format("cache/%s", cacheModifier), start, stop));
            }

            return data;
        }
        return null;
    }

}


