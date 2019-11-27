/*
 * Copyright (C) 2012 Pavel Stastny
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
package cz.incad.kramerius;

import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.ObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is main point to access to fedora through REST-API
 *
 * @see FedoraAccessImpl
 * @see SecuredFedoraAccessImpl
 * @author pavels
 */
public interface FedoraAccess {

    /**
     * Returns parsed RELS-EXT
     *
     * @param pid Object pid
     * @return parsed RELS-EXT
     * @throws IOException IO error has been occurred
     */
    public Document getRelsExt(String pid) throws IOException;

    /**
     * Returns model's name of given object relsext
     *
     * @param relsExt parsed RELS-EXT stream
     * @return model's name
     * @throws IOException IO error has been occurred
     */
    public String getKrameriusModelName(Document relsExt) throws IOException;

    /**
     * Returns model's name of given pid
     *
     * @param pid Object's pid
     * @return model's name
     * @throws IOException IO error has been occurred
     */
    public String getKrameriusModelName(String pid) throws IOException;

    /**
     * Returns models parsed from given document
     *
     * @param relsExt RELS-EXT document
     * @return models from RELS-EXt
     */
    public List<String> getModelsOfRel(Document relsExt);

    /**
     * Returns models of given object
     *
     * @param pid Object's pid
     * @return models
     * @throws IOException IO error has been occurred
     */
    public List<String> getModelsOfRel(String pid) throws IOException;

    /**
     * Returns Donator parsed from given document
     *
     * @param relsExt RELS-EXT document
     * @return donator or empty string (if hasDonator relationship doesn't
     * exist)
     */
    public String getDonator(Document relsExt);

    /**
     * Returns Donator of given object
     *
     * @param pid Object's pid
     * @return donator or empty string (if hasDonator relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getDonator(String pid) throws IOException;
    
    /**
     * Returns pid of the first periodical Item parsed from given document
     * *
     * @param relsExt RELS-EXT document
     * @return pid of the first periodical Item or empty string (if hasItem relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstItemPid(Document relsExt) throws IOException;
    
    /**
     * Returns pid of the first periodical Item of given object
     *
     * @param pid Object's pid = pid of volume
     * @return pid of the first periodical Item or empty string (if hasItem relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstItemPid(String pid) throws IOException;
    
    /**
     * Returns pid of the first periodical Volume parsed from given document
     * *
     * @param relsExt RELS-EXT document
     * @return pid of the first periodical Volume or empty string (if hasVolume relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstVolumePid(Document relsExt) throws IOException;
    
    /**
     * Returns pid of the first periodical Volume of given object
     *
     * @param pid Object's pid = pid of periodical
     * @return pid of the first periodical Volume or empty string (if hasVolume relationship doesn't
     * exist)
     * @throws IOException IO error has been occurred
     */
    public String getFirstVolumePid(String pid) throws IOException;

    /**
     * Return parsed biblio mods stream
     *
     * @param pid Object's pid
     * @return return biblio mods stream
     * @throws IOException IO erro has been occurred
     */
    public Document getBiblioMods(String pid) throws IOException;

    /**
     * Returns DC stream
     *
     * @param pid Object's pid
     * @return parsed DC stream
     * @throws IOException IO erro has been occurred
     */
    public Document getDC(String pid) throws IOException;

    /**
     * Returns pid of first document through rels-ext, which has IMG_FULL
     * datastream
     *
     * @param pid Object's pid
     * @return first page pid
     * @throws IOException IO error has been occurred
     */
    public String findFirstViewablePid(String pid) throws IOException;

    /**
     * Fill path of pids and models up to first document throw rels-ext, which
     * has IMG_FULL datastream
     *
     * @param pids xxx
     * @param models xxx
     * @return true to stop recursion
     * @throws IOException IO error has been occurred
     */
    @Deprecated
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException;

    /**
     * Parse, find and returns all pages
     *
     * @param pid Object's pid
     * @param deep if should go into deep
     * @return all pages
     * @throws IOException IO error has been occurred
     */
    @Deprecated
    public List<Element> getPages(String pid, boolean deep) throws IOException;

    /**
     * Find and returns all pages
     *
     * @param pid pid of object
     * @param rootElementOfRelsExt Root element of RelsExt
     * @return all pages
     * @throws IOException IO error has been occurred
     */
    @Deprecated
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException;

    /**
     * Returns input stream of thumbnail
     *
     * @param pid Object's pid
     * @return IMG_THUMBs stream
     * @throws IOException IO error has been occurred
     */
    public InputStream getSmallThumbnail(String pid) throws IOException;

    /**
     * Returns profile for thumbnail
     *
     * @param pid PID of requested object
     * @return parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getSmallThumbnailProfile(String pid) throws IOException;

    /**
     * Returns mime type of mime type
     *
     * @param pid PID of requested object
     * @return mime type
     * @throws IOException IO error has been occurred
     * @throws XPathExpressionException Error during xpath evaluation has been
     * occurred
     */
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException;

    /**
     * Tests whether IMG_PREVIEW is available
     *
     * @param pid PID of reqested object
     * @return true if IMG_PREVIEW is available
     * @throws IOException IO error has been occurred
     */
    public boolean isFullthumbnailAvailable(String pid) throws IOException;

    /**
     * Returns data of IMG_PREVIEW stream
     *
     * @param pid PID of requested object
     * @return data of IMG_PREVIEW stream
     * @throws IOException IO error has been occurred
     */
    public InputStream getFullThumbnail(String pid) throws IOException;

    /**
     * Returns mime type of requested IMG_PREVIEW
     *
     * @param pid PID of reqested object
     * @return mimetype
     * @throws IOException IO error has been occurred
     * @throws XPathExpressionException Error during xpath evaluation has been
     * occurred
     */
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException;

    /**
     * Returns data of IMG_FULL stream
     *
     * @param pid Requested pid
     * @return IMG_FULL data
     * @throws IOException IO error has been occurred
     */
    public InputStream getImageFULL(String pid) throws IOException;

    /**
     * Returns IMG_FULL profile
     *
     * @param pid Requested pid
     * @return parsed profile
     * @throws IOException IO error has been occurred
     */
    public Document getImageFULLProfile(String pid) throws IOException;

    /**
     * Returns IMG_FULL mimetype
     *
     * @param pid Requested object
     * @return mime type
     * @throws IOException IO error has been occurred
     * @throws XPathExpressionException Error during xpath evaluation has been
     * occurred
     */
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException;

    /**
     * Check whether IMG_FULL is available, is present and accessible
     *
     * @param pid Requested object
     * @return true if IMG_FULL is available
     * @throws IOException IO error has been occurred
     */
    public boolean isImageFULLAvailable(String pid) throws IOException;

    /**
     * Check whether stream is available
     *
     * @param pid Requested object
     * @param streamName Stream name
     * @return true if stream is available
     * @throws IOException IO error has been occurred
     */
    public boolean isStreamAvailable(String pid, String streamName) throws IOException;

    /**
     * Check if the object is available
     * @param pid Pid of object 
     * @return true or false  - object exists or doesn't exist
     * @throws IOException
     */
    public boolean isObjectAvailable(String pid) throws IOException;
    
    /**
     * Checks whether content is acessiable
     *
     * @param pid Tested object
     * @return true if object is accessible
     * @throws IOException IO error has been occurred
     */
    public boolean isContentAccessible(String pid) throws IOException;

    /**
     * Creates and returns API-A stub
     *
     * @return API-A stub
     */
    public FedoraAPIA getAPIA();

    /**
     * Creates and returns API-M stub
     *
     * @return API-M stub
     */
    public FedoraAPIM getAPIM();

    /**
     * Creates and returns objectFactory
     *
     * @return {@link ObjectFactory}
     */
    public ObjectFactory getObjectFactory();

    /**
     * Process fedora object tree
     *
     * @param pid PID of processing object
     * @param processor Processing tree handler (receives callbacks)
     * @throws ProcessSubtreeException Something happened during tree walking
     * @throws IOException IO error has been occurred
     */
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException;

    /**
     * Collects and returns subtree as one set
     *
     * @param pid Root pid
     * @return all subtree as set
     * @throws IOException IO error has been occurred
     */
    public Set<String> getPids(String pid) throws IOException;

    
    
    
    /**
     * Returns data from datastream
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return data
     * @throws IOException IO error has been occurred
     */
    public InputStream getDataStream(String pid, String datastreamName) throws IOException;

    /**
     * For observe HTTP headers
     * @param pid Requested pid
     * @param datastreamName Data stream name
     * @param streamObserver Header fileds observer
     * @throws IOException
     */
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException;

    
    /**
     * Returns xml containing datastream data
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return datastream xml as stored in Fedora
     * @throws IOException IO error has been occurred
     */
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException;

    /**
     * Returns xml containing datastream data
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return datastream xml as stored in Fedora
     * @throws IOException IO error has been occurred
     */
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException;

    /**
     * Returns mimeType of given datastream
     *
     * @param pid pid of requested object
     * @param datastreamName Datastream name
     * @return mimetype of datastream
     * @throws IOException IO error has been occurred
     */
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException;

    /**
     * Returns current version of fedora
     *
     * @return version
     * @throws IOException Cannot detect current version
     */
    public String getFedoraVersion() throws IOException;

    /**
     * Returns profile for stream
     *
     * @param pid Requested pid
     * @param stream Requested stream
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getStreamProfile(String pid, String stream) throws IOException;

    /**
     * Datastreams description document
     *
     * @param pid PID of requested object
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getObjectProfile(String pid) throws IOException;

    /**
     * Returns document which describes datastrem of requested pid
     *
     * @param pid requested pid
     * @return returns list of datastreams
     * @throws IOException IO error has been occurred
     */
    InputStream getFedoraDataStreamsList(String pid) throws IOException;

    /**
     * Returns document which describes datastrem of requested pid
     *
     * @param pid requested pid
     * @return returns list of datastreams in document
     * @throws IOException IO error has been occurred
     */
    Document getFedoraDataStreamsListAsDocument(String pid) throws IOException;

    
    
}
