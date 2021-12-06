package cz.incad.kramerius.fedora.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.fedora.AbstractFedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.om.impl.AkubraRepository;
import cz.incad.kramerius.fedora.om.impl.AkubraUtils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import org.ehcache.CacheManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class FedoraAccessAkubraImpl extends AbstractFedoraAccess {

    private AkubraDOManager manager;
    private Repository repository;
    private ProcessingIndexFeeder feeder;


    @Inject
    public FedoraAccessAkubraImpl(KConfiguration configuration, ProcessingIndexFeeder feeder, @Nullable StatisticsAccessLog accessLog, @Named("akubraCacheManager") CacheManager cacheManager) throws IOException {
        super(configuration, accessLog);
        try {
            this.manager = new AkubraDOManager(configuration, cacheManager);
            this.feeder = feeder;
            this.repository = AkubraRepository.build(feeder, this.manager);


        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    @Override
    public Repository getInternalAPI() throws RepositoryException {
        return this.repository;
    }

    @Override
    public Repository getTransactionAwareInternalAPI() throws RepositoryException {
        throw new RepositoryException("Transactions not supported in Akubra");
    }

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            return getStream(makeSureObjectPid(pid), FedoraUtils.BIBLIO_MODS_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_PREVIEW_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }


    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_THUMB_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return "Akubra";
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {
            // consider to change to metadata
            return getStream(makeSureObjectPid(pid), FedoraUtils.RELS_EXT_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        throw new UnsupportedOperationException("unsupported");
    }


    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        return super.isImageFULLAvailable(pid);
    }


    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            DigitalObject object = manager.readObjectFromStorage(pid);
            if (object != null) {
                DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, datastreamName);
                if (stream != null) {
                    return AkubraUtils.getStreamContent(stream, manager);
                } else {
                    throw new IOException("cannot find stream '" + datastreamName + "' for pid '" + pid + "'");
                }
            } else {
                throw new IOException("cannot find pid '" + pid + "'");
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    @Override
    public Document getDC(String pid) throws IOException {
        try {
            return getStream(makeSureObjectPid(pid), FedoraUtils.DC_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }


    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        try {
            return getDataStream(makeSureObjectPid(pid), FedoraUtils.IMG_FULL_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }


    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, datastreamName);
            if (stream != null) {
                if (stream.getContentLocation() != null && "URL".equals(stream.getContentLocation().getTYPE())) {
                    return stream.getContentLocation().getREF();
                }
            }
        }
        return null;
    }



    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    private Document getStream(String pid, String streamName) throws IOException {

        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {

            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, streamName);

            if (stream != null) {
                if (stream.getXmlContent() != null) {
                    List<Element> elementList = stream.getXmlContent().getAny();
                    if (!elementList.isEmpty()) {
                        return elementList.get(0).getOwnerDocument();
                    } else {
                        throw new IOException("Datastream not found: " + pid + " - " + streamName);
                    }

                } else {
                    throw new IOException("Expected XML datastream: " + pid + " - " + streamName);
                }
            }
            throw new IOException("Datastream not found: " + pid + " - " + streamName);
        }
        throw new IOException("Object not found: " + pid);

        //InputStream iStream = getDataStream(pid, streamName);
        //String rawConent = IOUtils.toString(iStream, "UTF-8");
        //Document doc = XMLUtils.parseDocument(new StringReader(rawConent.trim()),true);
        //return doc;
        //} catch (ParserConfigurationException e) {
        //   LOGGER.log(Level.SEVERE, e.getMessage(), e);
        //   throw new IOException(e);

    }


    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_FULL_STREAM);
    }

    @Override
    public String getMimeTypeForStream(String pid, String streamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {

            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, streamName);

            if (stream != null) {
                return stream.getMIMETYPE();
            }
            throw new IOException("Datastream not found: " + pid + " - " + streamName);
        }
        throw new IOException("Object not found: " + pid);
    }


    @Override
    public Document getObjectProfile(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return getMimeTypeForStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public Date getStreamLastmodifiedFlag(String pid, String streamName) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {

            DatastreamVersionType stream = AkubraUtils.getLastStreamVersion(object, streamName);

            if (stream != null) {
                if (stream.getCREATED() == null) {
                    return new Date();
                } else {
                    return stream.getCREATED().toGregorianCalendar().getTime();
                }
            }
            throw new IOException("Datastream not found: " + pid + " - " + streamName);
        }
        throw new IOException("Object not found: " + pid);
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        DigitalObject object = manager.readObjectFromStorage(pid);
        if (object != null) {
            return AkubraUtils.getLastModified(object);
        }
        throw new IOException("Object not found: " + pid);
    }


    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        try {
            List<Map<String, String>> results = new ArrayList<>();
            DigitalObject obj = manager.readObjectFromStorage(pid);

            return obj.getDatastream().stream().filter((o) -> {
                try {
                    // policy stream -> should be ommited?
                    return (!o.getID().equals("POLICY"));
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    return false;
                }
            }).map((o) -> {
                Map<String, String> map = null;
                try {
                    map = createMap(o.getID());
                    List<DatastreamVersionType> datastreamVersionList = o.getDatastreamVersion();
                    map.put("mimetype", datastreamVersionList.get(datastreamVersionList.size() - 1).getMIMETYPE());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                return map;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private Map<String, String> createMap(String label) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dsid", label);
        map.put("label", label);
        return map;
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        return this.isStreamAvailable(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            DigitalObject object = manager.readObjectFromStorage(pid);
            return AkubraUtils.streamExists(object, streamName);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        return true;
    }

    @Override
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver)
            throws IOException {
        throw new UnsupportedOperationException("unsupported operation");

    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        try {
            return this.repository.objectExists(pid);
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }


    @Override
    public InputStream getFoxml(String pid, boolean archive) throws IOException {
        try {
            if (archive){
                DigitalObject obj = manager.readObjectFromStorage(pid);
                manager.resolveArchivedDatastreams(obj);
                return this.manager.marshallObject(obj);
            }else {
                return this.manager.retrieveObject(pid);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void shutdown() {
        manager.shutdown();
    }



//    @Override
//    public String getFirstItemPid(Document relsExt) throws IOException {
//        return null;
//    }
//
//    @Override
//    public String getFirstItemPid(String pid) throws IOException {
//        return null;
//    }
//
//    @Override
//    public String getFirstVolumePid(Document relsExt) throws IOException {
//        return null;
//    }
//
//    @Override
//    public String getFirstVolumePid(String pid) throws IOException {
//        return null;
//    }
}
