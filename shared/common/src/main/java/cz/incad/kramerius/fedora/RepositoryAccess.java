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
package cz.incad.kramerius.fedora;

import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.fedora.om.repository.AkubraRepository;
import cz.incad.kramerius.fedora.om.repository.RepositoryException;
import cz.incad.kramerius.fedora.impl.SecuredFedoraAccessImpl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.utils.java.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is main point to access to fedora through REST-API
 *
 * @see SecuredFedoraAccessImpl
 * @author pavels
 * 
 */
public interface RepositoryAccess {


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
     * @return modelsget
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
     * TODO: Not used
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
     * @return true or false  - object objectExists or doesn't exist
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


    public AkubraRepository getInternalAPI() throws RepositoryException;

    public AkubraRepository getTransactionAwareInternalAPI() throws RepositoryException;


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
    public List<String> getPids(String pid) throws IOException;

    

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
     * Returns URL of external datastream data
     *
     * @param pid pid of reqested object
     * @param datastreamName datastream name
     * @return URL of external datastream data location
     * @throws IOException IO error has been occurred
     */
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException;

    /**
     * TODO: Not Used
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
     * TODO: Not used
     * Returns profile for stream
     *
     * @param pid Requested pid
     * @param stream Requested stream
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getStreamProfile(String pid, String stream) throws IOException;

    /**
     * TODO: Not used
     * Datastreams description document
     *
     * @param pid PID of requested object
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getObjectProfile(String pid) throws IOException;

    /**
     * TODO: Not used
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

    Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException;
    
    Date getObjectLastmodifiedFlag(String pid) throws IOException;

    @Deprecated
    List<Map<String, String>> getStreamsOfObject(String pid)  throws IOException;

    InputStream getFoxml(String pid, boolean archive) throws IOException;

    default void shutdown(){};


//    private Date lastModified(String pid, String stream) throws IOException {
//        Date date = null;
//        Document streamProfile = fedoraAccess.getStreamProfile(pid, stream);
//
//        Element elm = XMLUtils.findElement(streamProfile.getDocumentElement(),
//                "dsCreateDate",
//                FedoraNamespaces.FEDORA_MANAGEMENT_NAMESPACE_URI);
//        if (elm != null) {
//            String textContent = elm.getTextContent();
//            for (DateFormat df : XSD_DATE_FORMATS) {
//                try {
//                    date = df.parse(textContent);
//                    break;
//                } catch (ParseException e) {
//                    //
//                }
//            }
//        }
//        if (date == null) {
//            date = new Date();
//        }
//        return date;
//    }

    //----------------------------------------------------------------

    public static final String NAMESPACE_FOXML = "info:fedora/fedora-system:def/foxml#";

    /**
     * @se RepositoryApiTimestampFormatterTest
     */
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm:ss.")
            .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, false)
            .appendPattern("'Z'")
            .toFormatter();

    //TODO: methods for fetching other types of datastreams (redirect, external referenced, probably not managed)
    //TODO: methods for updating datastreams (new versions)

    //CREATE
    public void ingestObject(org.dom4j.Document foxmlDoc, String pid) throws RepositoryException, IOException;

    //READ
    public boolean objectExists(String pid) throws RepositoryException;

    public String getProperty(String pid, String propertyName) throws IOException, RepositoryException;

    public String getPropertyLabel(String pid) throws IOException, RepositoryException;

    public LocalDateTime getPropertyCreated(String pid) throws IOException, RepositoryException;

    public LocalDateTime getPropertyLastModified(String pid) throws IOException, RepositoryException;

    public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException;

    public boolean datastreamExists(String pid, String dsId) throws RepositoryException, IOException;

    public String getDatastreamMimetype(String pid, String dsId) throws RepositoryException, IOException;

    /**
     * @return part of FOXML that contains definition of the datastream. I.e. root element datastream with subelement(s) datastreamVersion.
     */
    public org.dom4j.Document getDatastreamXml(String pid, String dsId) throws RepositoryException, IOException;



    public String getTypeOfDatastream(String pid, String dsId) throws RepositoryException, IOException;


    public InputStream getLatestVersionOfDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public org.dom4j.Document getLatestVersionOfInlineXmlDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public String getLatestVersionOfManagedTextDatastream(String pid, String dsId) throws RepositoryException, IOException;

    public List<String> getPidsOfAllObjects() throws RepositoryException, IOException, SolrServerException;

    public List<String> getPidsOfObjectsByModel(String model) throws RepositoryException, IOException, SolrServerException;

    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException;

    public Pair<Long, List<String>> getPidsOfObjectsByModel(String model, String titlePrefix, int rows, int pageIndex) throws RepositoryException, IOException, SolrServerException;

    public RepositoryApi.TitlePidPairs getPidsOfObjectsWithTitlesByModel(String model, boolean ascendingOrder, int offset, int limit) throws RepositoryException, IOException, SolrServerException;

    public RepositoryApi.TitlePidPairs getPidsOfObjectsWithTitlesByModelWithCursor(String model, boolean ascendingOrder, String cursor, int limit) throws RepositoryException, IOException, SolrServerException;

    public Map<String, String> getDescription(String objectPid) throws RepositoryException, IOException, SolrServerException;

    public List<String> getTripletTargets(String sourcePid, String relation) throws RepositoryException, IOException, SolrServerException;

    public List<RepositoryApi.Triplet> getTripletTargets(String sourcePid) throws RepositoryException, IOException, SolrServerException;

    public List<String> getTripletSources(String relation, String targetPid) throws RepositoryException, IOException, SolrServerException;

    public List<RepositoryApi.Triplet> getTripletSources(String targetPid) throws RepositoryException, IOException, SolrServerException;


    public List<String> getDatastreamNames(String pid) throws RepositoryException, IOException, SolrServerException;


    //UPDATE
    public void updateInlineXmlDatastream(String pid, String dsId, org.dom4j.Document streamDoc, String formatUri) throws RepositoryException, IOException;

    public void updateBinaryDatastream(String pid, String streamName, String mimeType, byte[] byteArray) throws RepositoryException;


    public void deleteDatastream(String pid, String streamName) throws RepositoryException;

    /**
     * @param ds part of FOXML that contains definition of the datastream. I.e. root element datastream with subelement(s) datastreamVersion.
     */
    public void setDatastreamXml(String pid, String dsId, org.dom4j.Document ds) throws RepositoryException, IOException;

    //DELETE
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams) throws RepositoryException, IOException;


    class Triplet {
        public final String source;
        public final String relation;
        public final String target;

        public Triplet(String source, String relation, String target) {
            this.source = source;
            this.relation = relation;
            this.target = target;
        }

        @Override
        public String toString() {
            return String.format("%s -%s-> %s", source, relation, target);
        }
    }

    class TitlePidPairs {
        public List<Pair<String, String>> titlePidPairs;
        public String nextCursorMark;
    }

    //---------------KRRepAPI

    public static class KnownXmlFormatUris {
        public static final String RELS_EXT = "info:fedora/fedora-system:FedoraRELSExt-1.0";
        public static final String BIBLIO_MODS = "http://www.loc.gov/mods/v3";
        public static final String BIBLIO_DC = "http://www.openarchives.org/OAI/2.0/oai_dc/";
    }

    enum KnownDatastreams {
        RELS_EXT("RELS-EXT"),

        BIBLIO_MODS("BIBLIO_MODS"),
        BIBLIO_DC("DC"),

        OCR_ALTO("ALTO"),
        OCR_TEXT("TEXT_OCR"),

        IMG_FULL("IMG_FULL"),
        IMG_THUMB("IMG_THUMB"),
        IMG_PREVIEW("IMG_PREVIEW"),

        AUDIO_MP3("MP3"),
        AUDIO_OGG("OGG"),
        AUDIO_WAV("WAV"),

        // known but not used datastreams
        POLICY("POLICY"),
        MIGRATION("MIGRATION"),
        IMG_FULL_ADM("IMG_FULL_ADM"),
        AUDIT("AUDIT"),
        TEXT_OCR_ADM("TEXT_OCR_ADM"),

        COLLECTION_CLIPPINGS("COLLECTION_CLIPPINGS");


        private final String value;

        KnownDatastreams(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    enum KnownRelations {
        //own relations (define object tree)
        HAS_PAGE("hasPage"),
        HAS_UNIT("hasUnit"), //monograph -> monographUnit, convolute -> anything_top-level_but_collection
        HAS_VOLUME("hasVolume"), //periodical -> periodicalVolume
        HAS_ITEM("hasItem"), //periodical -> (periodicalItem, supplement)
        HAS_SOUND_UNIT("hasSoundUnit"), //soundRecording -> soundUnit
        HAS_TRACK("hasTrack"), //(soundRecording, soundUnit) -> track
        CONTAINS_TRACK("containsTrack"), //old version of HAS_TRACK
        HAS_INT_COMP_PART("hasIntCompPart"), //periodicalItem  -> (internalPart, article)
        //foster relations
        IS_ON_PAGE("isOnPage"), //(article, internalPart) -> page
        CONTAINS("contains"); //collection -> (monograph, periodical, ... anything, even other collection)
        //RDF relations, that don't connect two objects are not considered here
        //i.e. hasModel, hasDonator, contract, policy, itemId, handle

        private final String value;

        KnownRelations(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }


    public enum OwnRelationsMapping {

        page{
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_PAGE;
            }
        },

        unit {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_UNIT;
            }
        },
        periodicalvolume {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_VOLUME;
            }
        },
        volume {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_VOLUME;
            }
        },
        periodicalitem {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_ITEM;
            }
        },
        supplement {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_ITEM;
            }
        },
        soundunit {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_SOUND_UNIT;
                //return KnownRelations.CONTAINS_TRACK;
            }
        },
        soundrecording {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_SOUND_UNIT;
            }
        },

        internalpart {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_INT_COMP_PART;
            }
        },
        track {

            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.CONTAINS_TRACK;
            }

        },
        article {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation() {
                return KrameriusRepositoryApi.KnownRelations.HAS_INT_COMP_PART;
            }
        };

        ;

        public static KrameriusRepositoryApi.OwnRelationsMapping find(String name) {
            KrameriusRepositoryApi.OwnRelationsMapping[] values = values();
            for (KrameriusRepositoryApi.OwnRelationsMapping relMap :  values()) {
                if (relMap.name().equals(name)) {
                    return relMap;
                }
            }
            return null;
        }

        public abstract KrameriusRepositoryApi.KnownRelations relation();

    }


    public enum FosterRelationsMapping {
        page{
            @Override
            public KrameriusRepositoryApi.KnownRelations relation(String parentModel) {
                List<String> parent = Arrays.asList("article", "internalpart");
                if (parent.contains(parentModel)) {
                    return KrameriusRepositoryApi.KnownRelations.IS_ON_PAGE;
                } else return KrameriusRepositoryApi.KnownRelations.CONTAINS;
            }
        },
        anything {
            @Override
            public KrameriusRepositoryApi.KnownRelations relation(String parentModel) {
                return KrameriusRepositoryApi.KnownRelations.CONTAINS;
            }
        };

        public static KrameriusRepositoryApi.FosterRelationsMapping find(String name) {
            KrameriusRepositoryApi.FosterRelationsMapping[] values = KrameriusRepositoryApi.FosterRelationsMapping.values();
            for (KrameriusRepositoryApi.FosterRelationsMapping relMap : values) {
                if (relMap.name().equals(name)) return relMap;
            }
            return anything;

        }

        public abstract KrameriusRepositoryApi.KnownRelations relation(String parentModel);

    }


    List<KrameriusRepositoryApi.KnownRelations> OWN_RELATIONS = Arrays.asList(new KrameriusRepositoryApi.KnownRelations[]{
            KrameriusRepositoryApi.KnownRelations.HAS_PAGE, KrameriusRepositoryApi.KnownRelations.HAS_UNIT, KrameriusRepositoryApi.KnownRelations.HAS_VOLUME, KrameriusRepositoryApi.KnownRelations.HAS_ITEM,
            KrameriusRepositoryApi.KnownRelations.HAS_SOUND_UNIT, KrameriusRepositoryApi.KnownRelations.HAS_TRACK, KrameriusRepositoryApi.KnownRelations.CONTAINS_TRACK, KrameriusRepositoryApi.KnownRelations.HAS_INT_COMP_PART
    });
    List<KrameriusRepositoryApi.KnownRelations> FOSTER_RELATIONS = Arrays.asList(new KrameriusRepositoryApi.KnownRelations[]{
            KrameriusRepositoryApi.KnownRelations.IS_ON_PAGE, KrameriusRepositoryApi.KnownRelations.CONTAINS
    });

    static boolean isOwnRelation(String relation) {
        for (KrameriusRepositoryApi.KnownRelations knownRelation : OWN_RELATIONS) {
            if (relation.equals(knownRelation.toString())) {
                return true;
            }
        }
        for (KrameriusRepositoryApi.KnownRelations knownRelation : FOSTER_RELATIONS) {
            if (relation.equals(knownRelation.toString())) {
                return false;
            }
        }
        throw new IllegalArgumentException(String.format("unknown relation '%s'", relation));
    }

    //TODO: methods for updating datastream data (done for inline xml datastreams)

    /**
     * @return Low level repository API. Through that can be accessed any kind of datastream or property, regardless if it is used by Kramerius or not
     */
    public RepositoryApi getLowLevelApi();

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream RELS-EXT is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isRelsExtAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream RELS-EXT provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getRelsExt(String pid, boolean namespaceAware) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream BIBLIO_MODS is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isModsAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream BIBLIO_MODS provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getMods(String pid, boolean namespaceAware) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream DC is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isDublinCoreAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream DC provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getDublinCore(String pid, boolean namespaceAware) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream OCR_TEXT is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isOcrTextAvailable(String pid) throws IOException, RepositoryException;


    public boolean isPidAvailable(String pid) throws IOException, RepositoryException;

    public boolean isStreamAvailable(String pid, String dsId) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return latest version of object's datastream OCR_TEXT provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getOcrText(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream OCR_ALTO is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isOcrAltoAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid            Persistent identifier of the object
     * @param namespaceAware if false, namespaces will be removed from the resulting xml
     * @return latest version of object's datastream OCR_ALTO provided it is stored as inline XML (control-group X), null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public org.dom4j.Document getOcrAlto(String pid, boolean namespaceAware) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream IMG_FULL is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isImgFullAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream IMG_FULL if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getImgFullMimetype(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing image data from IMG_FULL if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getImgFull(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream IMG_THUMB is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isImgThumbAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream IMG_THUMB if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getImgThumbMimetype(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing image data from IMG_THUMB if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getImgThumb(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream IMG_PREVIEW is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isImgPreviewAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream IMG_PREVIEW if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getImgPreviewMimetype(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing image data from IMG_PREVIEW if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getImgPreview(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream MP3 is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isAudioMp3Available(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream MP3 if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getAudioMp3Mimetype(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing audio data from MP3 if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getAudioMp3(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream OGG is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isAudioOggAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream OGG if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getAudioOggMimetype(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing audio data from OGG if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getAudioOgg(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return if datastream WAV is available for the object
     * @throws IOException
     * @throws RepositoryException
     */
    public boolean isAudioWavAvailable(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return mime-type of datastream WAV if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public String getAudioWavMimetype(String pid) throws IOException, RepositoryException;

    /**
     * @param pid Persistent identifier of the object
     * @return InputStream stream containing audio data from WAV if such datastream exists, null otherwise
     * @throws IOException
     * @throws RepositoryException
     */
    public InputStream getAudioWav(String pid) throws IOException, RepositoryException;

    /**
     * @param objectPid
     * @return Model of the object
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public String getModel(String objectPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param objectPid
     * @return Pair of values: 1. Triplet of relation from own parent (or null if the object is top-level, i.e. has no parent), 2. Triplets of relations from foster parents
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> getParents(String objectPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param objectPid
     * @return Pair of values: 1. Triplets of relations to own children, 2. Triplets of relations to foster children
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> getChildren(String objectPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param collectionPid
     * @return Pids of items that are (directly) contained in collection
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public List<String> getPidsOfItemsInCollection(String collectionPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * @param itemPid
     * @return Pids of collections that (directly) contain the item
     * @throws RepositoryException
     * @throws IOException
     * @throws SolrServerException
     */
    public List<String> getPidsOfCollectionsContainingItem(String itemPid) throws RepositoryException, IOException, SolrServerException;

    /**
     * Appends new version of inline xml datastream RELS-EXT
     *
     * @param pid        Persistent identifier of the object
     * @param relsExtDoc New version of RELS-EXT
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateRelsExt(String pid, org.dom4j.Document relsExtDoc) throws IOException, RepositoryException;

    /**
     * Appends new version of inline xml datastream BIBLIO_MODS
     *
     * @param pid     Persistent identifier of the object
     * @param modsDoc New version of MODS
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateMods(String pid, org.dom4j.Document modsDoc) throws IOException, RepositoryException;

    /**
     * Appends new version of inline xml datastream DC
     *
     * @param pid   Persistent identifier of the object
     * @param dcDoc New version of Dublin Core
     * @throws IOException
     * @throws RepositoryException
     */
    public void updateDublinCore(String pid, org.dom4j.Document dcDoc) throws IOException, RepositoryException;


}
