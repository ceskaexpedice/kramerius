package cz.incad.kramerius.fedora.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.fedora.AbstractFedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.impl.Fedora4Repository;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class Fedora4AccessImpl extends AbstractFedoraAccess {


    private Repository repository;
    private ProcessingIndexFeeder feeder;

    @Inject
    public Fedora4AccessImpl(KConfiguration configuration, ProcessingIndexFeeder feeder,@Nullable StatisticsAccessLog accessLog) throws IOException {
        super(configuration, accessLog);
        try {
            this.feeder  = feeder;
            this.repository = Fedora4Repository.build(feeder, false );
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Repository getInternalAPI() throws RepositoryException  {
        return this.repository;
    }

    @Override
    public Repository getTransactionAwareInternalAPI() throws RepositoryException {
        return Fedora4Repository.build(feeder, true);
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
        return "4.X";
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
            RepositoryObject object = this.repository.getObject(pid);
            if (object != null) {
                RepositoryDatastream stream = object.getStream(datastreamName);
                if (stream != null) return stream.getContent();
                else throw new IOException("cannot find stream '"+datastreamName+"' for pid '"+pid+"'");
            } else {
                throw new IOException("cannot find pid '"+pid+"'");
            }
        } catch (RepositoryException e) {
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
            return getDataStream(makeSureObjectPid(pid),  FedoraUtils.IMG_FULL_STREAM);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }


    
    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        throw new UnsupportedOperationException("this is unsupported");
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
        try {
            InputStream iStream = getDataStream(pid, streamName);
            String rawConent = IOUtils.toString(iStream, "UTF-8");
            Document doc = XMLUtils.parseDocument(new StringReader(rawConent.trim()),true);
            return doc;
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
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
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        try {
            RepositoryObject repoObject = this.repository.getObject(makeSureObjectPid(pid));
            if (repoObject != null) {
                RepositoryDatastream stream = repoObject.getStream(datastreamName);
                if (stream != null) {
                    return stream.getMimeType();
                } else {
                    throw new IOException("cannot find stream '"+datastreamName+"' for pid '"+pid+"'");
                }
            } else {
                throw new IOException("cannot find pid '"+pid+"'");
            }
        } catch (RepositoryException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }
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
    public Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException {
        try {
            RepositoryObject repoObject = this.repository.getObject(makeSureObjectPid(pid));
            if (repoObject != null) {
                RepositoryDatastream ds = repoObject.getStream(stream);
                if (ds != null) {
                    return ds.getLastModified();
                } else {
                    throw new IOException("cannot find stream '"+stream+"' for pid '"+pid+"'");
                }
            } else {
                throw new IOException("cannot find pid '"+pid+"'");
            }
        } catch (RepositoryException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        try {
            RepositoryObject repoObject = this.repository.getObject(makeSureObjectPid(pid));
            if (repoObject != null) {
                return repoObject.getLastModified();
            } else {
                throw new IOException("cannot find pid '"+pid+"'");
            }
        } catch (RepositoryException e) {
            throw new IOException(e);
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }




    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        try {
            List<Map<String, String>> results = new ArrayList<>();
            RepositoryObject obj = this.repository.getObject(pid);
            return obj.getStreams().stream().filter((o)-> {
                try {
                    // policy stream -> should be ommited?
                    return (!o.getName().equals("POLICY"));
                } catch (RepositoryException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    return false;
                }
            }).map((o) -> {
                Map<String, String> map = null;
                try {
                    map = createMap(o.getName());
                    map.put("mimetype", o.getMimeType());
                } catch (RepositoryException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                return map;
            }).collect(Collectors.toList());
        } catch (RepositoryException e) {
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
            RepositoryObject object = this.repository.getObject(pid);
            return object.streamExists(streamName);
       } catch (RepositoryException e) {
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
    public InputStream getFoxml(String pid) throws IOException {
        try {
            if (this.repository.objectExists(pid)) {
                return this.repository.getObject(pid).getFoxml();
            } else throw new IOException("pid '"+pid+"' not found");
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }
}
