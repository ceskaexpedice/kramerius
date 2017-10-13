package cz.incad.kramerius.fedora.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nullable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.AbstractFedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.impl.Fedora4Repository;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import org.apache.commons.io.IOUtils;
import org.fcrepo.client.*;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class Fedora4AccessImpl extends AbstractFedoraAccess {


    private Repository repository;

    @Inject
    public Fedora4AccessImpl(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog) throws IOException {
        super(configuration, accessLog);
        this.repository = new Fedora4Repository();
    }

    @Override
    public FedoraAPIA getAPIA() {
        throw new UnsupportedOperationException("unsupported operation!");
    }

    @Override
    public FedoraAPIM getAPIM() {
        throw new UnsupportedOperationException("unsupported operation!");
    }

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        return getStream(pid, FedoraUtils.BIBLIO_MODS_STREAM);
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }

    
    
    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        return getDataStream(pid, FedoraUtils.IMG_THUMB_STREAM);
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return "4.X";
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        // consider to change to metadata
        return getStream(pid, FedoraUtils.RELS_EXT_STREAM);
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
        return getStream(pid, FedoraUtils.DC_STREAM);
    }

    
    
    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        return getDataStream(pid,  FedoraUtils.IMG_FULL_STREAM);
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
            RepositoryObject repoObject = this.repository.getObject(pid);
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
            RepositoryObject repoObject = this.repository.getObject(pid);
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
        }
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        try {
            RepositoryObject repoObject = this.repository.getObject(pid);
            if (repoObject != null) {
                return repoObject.getLastModified();
            } else {
                throw new IOException("cannot find pid '"+pid+"'");
            }
        } catch (RepositoryException e) {
            throw new IOException(e);
        }
    }


    /*
    private String restOfPath(String pid, String path) {
       int indexOf = path.indexOf(pid);
       return path.substring(indexOf+pid.length()+1);
    }
    
    private List<Map<String, String>> find(List<Map<String,String>> maps, String dsId) {
        for (Map<String, String> m : maps) {
            if (m.containsKey("dsid") && m.get("dsid").equals(dsId)) {
                return Arrays.asList(m);
            }
        }
        return new ArrayList<Map<String, String>>();
    }
    */


    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) {
        throw new UnsupportedOperationException();
        /*
        try {
            List<Map<String,String>> maps = new ArrayList<Map<String,String>>();
            FedoraObject object = this.repo.getObject(restPid(pid));
            Iterator<Triple> objProps = object.getProperties();
            while(objProps.hasNext()) {
                Triple next = objProps.next();
                Node tripleObject = next.getObject();
                Node predicate = next.getPredicate();
                Node tripleSubject = next.getSubject();
                
                if (predicate.toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                    if (tripleObject.toString().equals("http://fedora.info/definitions/v4/repository#Binary")) {
                        String label = restOfPath(restPid(pid), tripleSubject.toString());
                        if (find(maps, label).isEmpty()) {
                            Map<String, String> map = createMap(label);
                            maps.add(map);
                        }
                    }
                    if (predicate.toString().equals("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType")) {
                        if (tripleObject.isLiteral()) {
                            Object objValue = tripleObject.getLiteral().getValue();
                            String label = restOfPath(restPid(pid), tripleSubject.toString());
                            List<Map<String, String>> collected = find(maps, label);
                            Map<String, String> map = collected.isEmpty() ? null : collected.get(0);
                            if (map == null) {
                                map = createMap(label);
                                maps.add(map);
                            }
                            map.put("mimeType", objValue.toString());
                        }
                    }
                }
            }
            return maps;
        } catch (FedoraException e1) {
            LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            return new ArrayList<Map<String,String>>();
        }
        */
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
            if (object != null) return object.getStream(streamName) != null;
            else return false;
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
    
}
