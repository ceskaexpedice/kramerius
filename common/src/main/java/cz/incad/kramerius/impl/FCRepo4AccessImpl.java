package cz.incad.kramerius.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.jms.IllegalStateException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;
import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIM;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.impl.fedora.FedoraStreamUtils;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class FCRepo4AccessImpl extends AbstractFedoraAccess {

    
    private FedoraRepository repo;
    private String url = null;

    @Inject
    public FCRepo4AccessImpl(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog) throws IOException {
        super(configuration, accessLog);
        url = KConfiguration.getInstance().getConfiguration().getString("fc4.repo","http://localhost:18080/rest/");
        repo = new FedoraRepositoryImpl(url);
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
        return getDataStream(pid, FedoraUtils.IMG_PREVIEW_STREAM);
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return "4.X";
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
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

    
    
    public static String restPid(String pid) {
        if (pid.startsWith("uuid:")) {
            return pid.substring("uuid:".length());
        } else return pid;
    }
    
    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            pid = makeSureObjectPid(pid);
            String mimeType = getMimeTypeForStream(pid,datastreamName);
            ImageMimeType imageMimeType = ImageMimeType.loadFromMimeType(mimeType);
            if (imageMimeType != null) {
                InputStream is = RESTHelper.inputStream(this.url+"/"+restPid(pid)+"/"+datastreamName,null,null);
                return is;
            } else {
                FedoraObject object = this.repo.getObject(restPid(pid));
                FedoraDatastream datastream = this.repo.getDatastream(object.getPath()+"/"+datastreamName);
                InputStream iStream = datastream.getContent();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                IOUtils.copyStreams(iStream, true,bos,true);
                return new ByteArrayInputStream(bos.toByteArray());
            }
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        } catch (FedoraException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
            Document doc = XMLUtils.parseDocument(iStream,true);
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
            FedoraDatastream datastream = this.repo.getDatastream(restPid(pid)+"/"+datastreamName);
            return mimeType(datastream);
        } catch (FedoraException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private String mimeType(FedoraDatastream datastream) throws FedoraException {
        Iterator<Triple> properties2 = datastream.getProperties();
        while(properties2.hasNext()) {
            Triple triple = properties2.next();
            if (triple.getPredicate().toString().equals("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType")) {
                Node object = triple.getObject();
                if (object.isLiteral()) {
                    Object literalValue = object.getLiteralValue();
                    if (literalValue != null) return literalValue.toString();
                    
                }
            }
        }
        return null;
    }
    
    //http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType

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
            FedoraDatastream datastream = this.repo.getDatastream(restPid(pid)+"/"+stream);
            return datastream.getLastModifiedDate();
        } catch (FedoraException e) {
            throw new IOException(e.getMessage());
        }
    }
    

    
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
    
    
    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) {
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
                        String label = restOfPath(pid, tripleSubject.toString());
                        if (find(maps, label).isEmpty()) {
                            Map<String, String> map = createMap(label);
                            maps.add(map);
                        }
                    }
                    if (predicate.toString().equals("http://www.ebu.ch/metadata/ontologies/ebucore/ebucore#hasMimeType")) {
                        if (tripleObject.isLiteral()) {
                            Object objValue = tripleObject.getLiteral().getValue();
                            String label = restOfPath(pid, tripleSubject.toString());
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
    }

    private Map<String, String> createMap(String label) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("dsid", label);
        map.put("label", label);
        return map;
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        try {
            return this.repo.exists(restPid(pid)+"/"+FedoraUtils.IMG_PREVIEW_STREAM);
        } catch (FedoraException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            return this.repo.exists(restPid(pid)+"/"+streamName);
        } catch (FedoraException e) {
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
