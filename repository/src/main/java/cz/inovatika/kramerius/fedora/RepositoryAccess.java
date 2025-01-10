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
package cz.inovatika.kramerius.fedora;

import cz.inovatika.kramerius.fedora.impl.SecuredFedoraAccessImpl;
import cz.inovatika.kramerius.fedora.impl.SupportedFormats;
import cz.inovatika.kramerius.fedora.om.repository.Repository;
import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
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

/**
 * This is main point to access to fedora through REST-API
 *
 * @see SecuredFedoraAccessImpl
 * @author pavels
 * 
 */
public interface RepositoryAccess {

    // object

    ObjectAccessHelper getObjectAccessHelper();

    boolean objectExists(String pid);

    RepositoryObjectWrapper getFoxml(String pid);

    void ingestObject(org.dom4j.Document foxmlDoc, String pid);

    void deleteObject(String pid, boolean deleteDataOfManagedDatastreams);

    // datastream

    DatastreamAccessHelper getDatastreamAccessHelper();

    String getTypeOfDatastream(String pid, String dsId);

    boolean datastreamExists(String pid, KnownDatastreams dsId);

    org.dom4j.Document getDatastreamXml(String pid, String dsId);

    String getDatastreamMimetype(String pid, KnownDatastreams dsId);

    DatastreamContentWrapper getDatastreamContent(String pid, KnownDatastreams dsId);

    List<String> getDatastreamNames(String pid);

    void updateInlineXmlDatastream(String pid, KnownDatastreams dsId, org.dom4j.Document streamDoc, String formatUri);

    /**
     * @param ds part of FOXML that contains definition of the datastream. I.e. root element datastream with subelement(s) datastreamVersion.
     */
    void setDatastreamXml(String pid, KnownDatastreams dsId, org.dom4j.Document ds);

    public void updateBinaryDatastream(String pid, KnownDatastreams dsId, String mimeType, byte[] byteArray);

    public void deleteDatastream(String pid, KnownDatastreams dsId);

    // Processing index
    ProcessingIndexAccessHelper getProcessingIndexAccessHelper();

    <T> T queryProcessingIndex(ProcessingIndexQueryParameters params, ProcessingIndexResultMapper<T> mapper);

    //------------------------------------------------------------
    //public org.dom4j.Document getFoxml(String pid) throws RepositoryException, IOException;


    /**
     * Check if the object is available
     * @param pid Pid of object 
     * @return true or false  - object objectExists or doesn't exist
     * @throws IOException
     */
    //public boolean isObjectAvailable(String pid) throws IOException;
    
    /**
     * Checks whether content is acessiable
     *
     * @param pid Tested object
     * @return true if object is accessible
     * @throws IOException IO error has been occurred
     */
    public boolean isContentAccessible(String pid) throws IOException;


    public Repository getInternalAPI() throws RepositoryException;

    public Repository getTransactionAwareInternalAPI() throws RepositoryException;


    /**
     * Collects and returns subtree as one set
     *
     * @param pid Root pid
     * @return all subtree as set
     * @throws IOException IO error has been occurred
     */
    public List<String> getPids(String pid) throws IOException;

    


    /**
     * Returns current version of fedora
     *
     * @return version
     * @throws IOException Cannot detect current version
     */
    public String getFedoraVersion() throws IOException;


    /**
     * TODO: Not used
     * Datastreams description document
     *
     * @param pid PID of requested object
     * @return Parsed profile
     * @throws IOException IO error has been occurred
     */
    Document getObjectProfile(String pid) throws IOException;


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

    //READ

    public String getProperty(String pid, String propertyName) throws IOException, RepositoryException;








    //DELETE

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
   // public RepositoryApi getLowLevelApi();





}
