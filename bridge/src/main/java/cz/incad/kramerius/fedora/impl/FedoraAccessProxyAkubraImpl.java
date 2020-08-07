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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FedoraAccessProxyAkubraImpl extends AbstractFedoraAccess {

    private static Object INGEST_LOCK = new Object();

    public static final Logger LOGGER = Logger.getLogger(FedoraAccessProxyAkubraImpl.class.getName());

    private FedoraAccess akubra;
    private SolrAccess solrAccess;
    private CollectionsManager collectionsManager;
    private Client client;



    @Inject
    public FedoraAccessProxyAkubraImpl(KConfiguration configuration, @Nullable StatisticsAccessLog accessLog, @Named("akubraFedoraAccess")FedoraAccess acc, SolrAccess solrAccess,  @Named("fedora")CollectionsManager collectionsManager, Provider<HttpServletRequest> provider) throws IOException {
        super(configuration, accessLog);
        this.akubra = acc;
        this.solrAccess = solrAccess;
        this.collectionsManager = collectionsManager;
        this.client = Client.create();
    }




    protected List<String> window(String pid) {
        try {
            Document solrDataDocument = this.solrAccess.getSolrDataDocument(pid);

            String parentPid = SolrUtils.disectParentPid(solrDataDocument);
            List<String> list = ItemResourceUtils.solrChildrenPids(parentPid, new ArrayList<>(), this.solrAccess);

            int size = KConfiguration.getInstance().getConfiguration().getInt("cdk.collections.sources.window", 5);

            int index = list.indexOf(pid);
            if (index >= 0)  {
                int minIndex = Math.max(index-size, 0);
                size += size-(index-minIndex);

                int maxIndex = Math.min(index+size, list.size());
                return list.subList(minIndex, maxIndex);
            } else {
                List<String> retList = new ArrayList<>(list.subList(0,Math.min(list.size(), size)));
                retList.add(pid);
                return retList;
            }

        } catch (UniformInterfaceException ex2) {
            throw new RuntimeException(ex2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }


    protected InputStream batchFoxml(String url, String userName, String pswd) {
        LOGGER.info(String.format("Requesting %s", url));
        WebResource r = client.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));
        try {
            return r.accept("application/zip").get(InputStream.class);
        } catch (UniformInterfaceException ex2) {
            if (ex2.getResponse().getStatus() == 404) {
                LOGGER.log(Level.WARNING, "Call to {0} failed with message {1}. Skyping documents.",
                        new Object[]{url, ex2.getResponse().toString()});
                return null;
            } else {
                LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
                return r.accept("application/zip").get(InputStream.class);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
            return r.accept("application/zip").get(InputStream.class);
        }
    }


    protected InputStream foxml( String url, String userName, String pswd) {
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
        }
    }

    private void ingestIfNecessary(String pid) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException {
        synchronized (INGEST_LOCK) {
            Repository internalAPI = this.akubra.getInternalAPI();
            if(!internalAPI.objectExists(pid)) {
                List<String> window = window( pid).stream().filter((p) -> {
                    try {
                        return !internalAPI.objectExists(p);
                    } catch (RepositoryException e) {
                        LOGGER.warning(e.getMessage());
                        return false;
                    }
                }).collect(Collectors.toList());
                LOGGER.info("Requesting window "+window);
                onDemandIngest(window, internalAPI);
            }
        }
    }


    private  void onDemandIngest(List<String> window, Repository internalAPI) throws CollectionException, LexerException, IOException, RepositoryException, JAXBException, TransformerException {
        List<List<String>> sources = window.stream().map((pid) -> {
            try {
                return this.solrAccess.getSolrDataDocument(pid).getDocumentElement();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).map(CDKUtils::findSources).collect(Collectors.toList());

        Map<String,List<String>> map = new HashMap<>();
        for (int i = 0,ll=window.size(); i < ll; i++) {
            String pid = window.get(i);
            List<String> associatedSurces = sources.get(i);
            if (!associatedSurces.isEmpty()) {
                String firstSource = associatedSurces.get(0);
                if (!map.containsKey(firstSource)) {
                    map.put(firstSource, new ArrayList<>());
                }
                map.get(firstSource).add(pid);
            }
        }

        for (String vc : map.keySet()) {
            Collection collection = this.collectionsManager.getCollection(vc);

            PIDParser parser = new PIDParser(collection.getPid());
            parser.objectPid();
            String objectId = parser.getObjectId();

            String username = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".username");
            String password = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".pswd");

            boolean batch = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.collections.sources." + objectId + ".batch", false);

            if (StringUtils.isAnyString(username) && StringUtils.isAnyString(password)) {
                if (batch) {
                    String reduced = map.get(vc).stream().reduce("", (i, v) -> i.equals("") ? v : i+","+v);
                    String url = collection.getUrl()  +(collection.getUrl().endsWith("/") ? "" : "/")+ "api/v4.6/cdk/batch/foxmls?collection=" + collection.getPid()+"&pids="+reduced;

                    InputStream input = batchFoxml(url, username, password);
                    ZipInputStream zipInputStream = new ZipInputStream(input);
                    ZipEntry entry = null;
                    while((entry = zipInputStream.getNextEntry())!= null) {
                        String name = entry.getName();
                        Import.ingest(internalAPI, zipInputStream, name, null, null, true);
                    }
                } else {
                    for (String pid :  map.get(vc)) {
                        long start = System.currentTimeMillis();
                        String url = collection.getUrl()  +(collection.getUrl().endsWith("/") ? "" : "/")+ "api/v4.6/cdk/" + pid + "/foxml?collection=" + collection.getPid();
                        InputStream foxml = foxml(url, username, password);
                        Import.ingest(internalAPI, foxml, pid, null, null, true);
                        LOGGER.info(String.format("Whole ingest of %s took %d ms",pid, (System.currentTimeMillis() - start)));
                    }
                }
            } else throw new IOException("cannot read data from "+ collection.getUrl()+".  Missing property "+"cdk.collections.sources." + objectId + ".username or "+"cdk.collections.sources." + objectId + ".pswd");
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
        }
    }
}
