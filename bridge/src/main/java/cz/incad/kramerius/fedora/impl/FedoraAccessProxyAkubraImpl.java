package cz.incad.kramerius.fedora.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.fedora.AbstractFedoraAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.SynchronizedRepository;
import cz.incad.kramerius.fedora.utils.CDKUtils;
import cz.incad.kramerius.rest.api.k5.client.item.utils.ItemResourceUtils;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.kramerius.utils.solr.SolrUtils;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kramerius.Import;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static cz.incad.kramerius.fedora.impl.FedoraAccessProxyUtils.*;

public class FedoraAccessProxyAkubraImpl extends AbstractFedoraAccess {

    public static Object INGESTING_LOCK = new Object();


    public static final Logger LOGGER = Logger.getLogger(FedoraAccessProxyAkubraImpl.class.getName());

    private FedoraAccess akubra;
    private SolrAccess solrAccess;
    private CollectionsManager collectionsManager;
    private Client client;

    private Repository internalRepository;


    @Inject
    public FedoraAccessProxyAkubraImpl(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog, @Named("akubraFedoraAccess")FedoraAccess acc, SolrAccess solrAccess,  @Named("fedora")CollectionsManager collectionsManager, Provider<HttpServletRequest> provider) throws IOException {
        super(configuration, accessLog);
        this.akubra = acc;
        this.solrAccess = solrAccess;
        this.collectionsManager = collectionsManager;
        this.client = Client.create();
    }







    @Override
    public Document getRelsExt(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return akubra.getRelsExt(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    private void ingestIfNecessary(String pid) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException, XPathExpressionException {
        if (!pid.startsWith(PIDParser.VC_PREFIX)) {
            Repository internalAPI = getInternalAPI();
            if (!internalAPI.objectExists(pid)) {
                // put in the queue and wait
                onDemandIngest(pid, internalAPI);
            }
        }
    }


    protected InputStream foxml(String url, String userName, String pswd) {
        LOGGER.info(String.format("Requesting %s", url));
        WebResource r = client.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));

        try {
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        } catch (UniformInterfaceException ex2) {
            if (ex2.getResponse().getStatus() == 404) {
                LOGGER.log(Level.WARNING, "Call to {0} failed with message {1}. Skyping document.",
                        new Object[]{url, ex2.getResponse().toString()});
                return null;
            } else {
                LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
                return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        }
    }


    // on demand request
    private  void onDemandIngest(String pid, Repository internalAPI) throws CollectionException, LexerException, IOException, RepositoryException, JAXBException, TransformerException {
        LOGGER.info(String.format("Requesting info %s", pid));
        try {
            long start = System.currentTimeMillis();
            Document solrDataDocument = this.solrAccess.getSolrDataDocument(pid);
            List<String> sources = CDKUtils.findSources(solrDataDocument.getDocumentElement());
            if (!sources.isEmpty()) {
                Collection collection = this.collectionsManager.getCollection(sources.get(0));

                PIDParser parser = new PIDParser(collection.getPid());
                parser.objectPid();
                String objectId = parser.getObjectId();

                String username = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".username");
                String password = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".pswd");

                if (StringUtils.isAnyString(username) && StringUtils.isAnyString(password)) {
                    String url = collection.getUrl()  +(collection.getUrl().endsWith("/") ? "" : "/")+ "api/v4.6/cdk/" + pid + "/foxml?collection=" + collection.getPid();

                    if (internalAPI.objectExists(pid)) return;
                    InputStream foxml = foxml(url, username, password);
                    long foxmlTime = System.currentTimeMillis();
                    // only ingesting is synchronized by shared lock
                    synchronized (INGESTING_LOCK) {
                        if (internalAPI.objectExists(pid)) return;
                        // tady by to melo byt synchronizovane
                        Import.ingest(internalAPI, foxml, pid, null, null, true);
                        LOGGER.info(String.format("Whole ingest of %s took %d ms (download foxml %d ms)",pid, (System.currentTimeMillis() - start), (System.currentTimeMillis() - foxmlTime) ));
                    }
                } else throw new IOException("Cannot read data from "+ collection.getUrl()+".  Missing property "+"cdk.collections.sources." + objectId + ".username or "+"cdk.collections.sources." + objectId + ".pswd  for pid  "+pid);

            }
        } catch (IOException | JAXBException | TransformerException | LexerException | RepositoryException |CollectionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }


    @Override
    public Document getBiblioMods(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getBiblioMods(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getDC(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getDC(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getSmallThumbnail(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getSmallThumbnailProfile(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getSmallThumbnailMimeType(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.isFullthumbnailAvailable(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getFullThumbnail(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getFullThumbnailMimeType(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getImageFULL(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getImageFULLProfile(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getImageFULLMimeType(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.isStreamAvailable(pid, streamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.isObjectAvailable(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.isContentAccessible(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Repository getInternalAPI() throws RepositoryException {
        return this.akubra.getInternalAPI();
    }

    @Override
    public Repository getTransactionAwareInternalAPI() throws RepositoryException {
        return this.akubra.getTransactionAwareInternalAPI();
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getDataStream(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException {
        try {
            ingestIfNecessary(pid);
            this.akubra.observeStreamHeaders(pid,datastreamName, streamObserver);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }

    }

    @Override
    public String getExternalStreamURL(String pid, String datastreamName) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getExternalStreamURL(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getDataStreamXml(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getDataStreamXmlAsDocument(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getMimeTypeForStream(pid,datastreamName);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return this.akubra.getFedoraVersion();
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getStreamProfile(pid,stream);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getObjectProfile(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getFedoraDataStreamsList(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getFedoraDataStreamsListAsDocument(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getStreamLastmodifiedFlag(pid,stream);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getObjectLastmodifiedFlag(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getStreamsOfObject(pid);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }

    @Override
    public InputStream getFoxml(String pid, boolean archive) throws IOException {
        try {
            ingestIfNecessary(pid);
            return this.akubra.getFoxml(pid, archive);
        } catch (RepositoryException e) {
            throw  new IOException(e);
        } catch (CollectionException e) {
            throw  new IOException(e);
        } catch (LexerException e) {
            throw  new IOException(e);
        } catch (JAXBException e) {
            throw  new IOException(e);
        } catch (TransformerException e) {
            throw  new IOException(e);
        } catch (XPathExpressionException e) {
            throw  new IOException(e);
        }
    }


    /*
    public synchronized Repository getSynchronizedInternalRepository() throws RepositoryException {
        if (this.internalRepository  == null ) {
            this.internalRepository = new SynchronizedRepository(this.akubra.getInternalAPI());
        }
        return this.internalRepository;
    }*/


}
