package cz.incad.kramerius.fedora.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionException;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.pid.PIDParser;
import org.kramerius.Import;
import org.w3c.dom.Document;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;


public class IngestingThread extends Thread {

    public static Object INGESTING_LOCK = new Object();

    private static Logger LOGGER = Logger.getLogger(IngestingThread.class.getName());

    private AkubraRepository internalAPI;
    private SolrAccess solrAccess;
    private CollectionsManager collectionsManager;
    private Client client;
    private String pid;

    private CyclicBarrier barrier;

    public IngestingThread(AkubraRepository internalAPI, SolrAccess solrAccess, CollectionsManager collectionsManager, Client client, String pid, CyclicBarrier barrier) {
        this.internalAPI = internalAPI;
        this.solrAccess = solrAccess;
        this.collectionsManager = collectionsManager;
        this.client = client;
        this.pid = pid;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            long start = System.currentTimeMillis();
            Document solrDataDocument = this.solrAccess.getSolrDataByPid(pid);
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

                    if (this.internalAPI.exists(pid)) return;
                    InputStream foxml = foxml(url, username, password);
                    long foxmlTime = System.currentTimeMillis();
                    // only ingesting is synchronized by shared lock
                    synchronized (INGESTING_LOCK) {
                        if (this.internalAPI.exists(pid)) return;
                        // tady by to melo byt synchronizovane
                        Import.ingest(internalAPI, foxml, pid, null, null, true);
                        LOGGER.info(String.format("Whole ingest of %s took %d ms (download foxml %d ms)",pid, (System.currentTimeMillis() - start), (System.currentTimeMillis() - foxmlTime) ));
                    }
                } else throw new IOException("Cannot read data from "+ collection.getUrl()+".  Missing property "+"cdk.collections.sources." + objectId + ".username or "+"cdk.collections.sources." + objectId + ".pswd  for pid  "+pid);

            }
        } catch (IOException | JAXBException | TransformerException | LexerException | RepositoryException | CollectionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } finally {
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e );
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

}
