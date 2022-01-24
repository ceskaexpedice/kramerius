package cz.incad.kramerius.fedora.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.utils.CDKUtils;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.kramerius.virtualcollections.CollectionException;
import org.kramerius.Import;
import org.w3c.dom.Document;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;

public class OnDemandIngest {

    public static Object INGESTING_LOCK = new Object();
    private final Client client;

    private SolrAccess solrAccess;

    @Inject
    public OnDemandIngest( @Named("new-index") SolrAccess solrAccess) throws IOException {
        this.solrAccess = solrAccess;
        this.client = Client.create();
    }
        // on demand request
    void onDemandIngest(String pid, Repository internalAPI) throws CollectionException, LexerException, IOException, RepositoryException, JAXBException, TransformerException {
        FedoraAccessProxyAkubraImpl.LOGGER.info(String.format("Requesting info %s", pid));
        try {
            long start = System.currentTimeMillis();
            Document solrDataDocument = solrAccess.getSolrDataByPid(pid);
            String leader = CDKUtils.findCDKLeader(solrDataDocument.getDocumentElement());
            List<String> sources = CDKUtils.findSources(solrDataDocument.getDocumentElement());

            String source = leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
            if (source != null) {
                //Collection collection = this.collectionsManager.getCollection(sources.get(0));
                PIDParser parser = new PIDParser(source);
                parser.objectPid();
                String objectId = parser.getObjectId();

                String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".baseurl");

                String username = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".username");
                String password = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + objectId + ".pswd");

                if (StringUtils.isAnyString(username) && StringUtils.isAnyString(password)) {
                    String url = baseurl  +(baseurl.endsWith("/") ? "" : "/")+ "api/v4.6/cdk/" + pid + "/foxml?collection=" + parser.getObjectPid();

                    if (internalAPI.objectExists(pid)) return;
                    InputStream foxml = foxml(url, username, password);
                    long foxmlTime = System.currentTimeMillis();
                    // only ingesting is synchronized by shared lock
                    synchronized (INGESTING_LOCK) {
                        if (internalAPI.objectExists(pid)) return;
                        // tady by to melo byt synchronizovane
                        Import.ingest(internalAPI, foxml, pid, null, null, true);
                        FedoraAccessProxyAkubraImpl.LOGGER.info(String.format("Whole ingest of %s took %d ms (download foxml %d ms)",pid, (System.currentTimeMillis() - start), (System.currentTimeMillis() - foxmlTime) ));
                    }
                } else throw new IOException("Cannot read data from "+ baseurl +".  Missing property "+"cdk.collections.sources." + objectId + ".username or "+"cdk.collections.sources." + objectId + ".pswd  for pid  "+pid);

            }
        } catch (IOException | JAXBException | TransformerException | LexerException | RepositoryException e) {
            FedoraAccessProxyAkubraImpl.LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    public void ingestWholePathNecessary(Repository internalAPI, String pid) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException, XPathExpressionException {
        if (!pid.startsWith(PIDParser.VC_PREFIX)) {
            if (!internalAPI.objectExists(pid)) {
                ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
                for (ObjectPidsPath path : pidPaths) {
                    String[] pidsInPath = path.getPathFromLeafToRoot();
                    ingestIfNecessary(internalAPI, pidsInPath);
                }
            }
        }
    }

    public void ingestIfNecessary(Repository internalAPI, String... pids) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException, XPathExpressionException {
        for (String pid : pids) { ingestIfNecessary(internalAPI, pid); }
    }

    public void ingestIfNecessary(Repository internalAPI, String pid) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException, XPathExpressionException {
        if (!pid.startsWith(PIDParser.VC_PREFIX)) {
            //Repository internalAPI = fedoraAccessProxyAkubra.getInternalAPI();
            if (!internalAPI.objectExists(pid)) {
                // put in the queue and wait
                onDemandIngest(pid, internalAPI);
            }
        }
    }
    protected InputStream foxml(String url, String userName, String pswd) {
        FedoraAccessProxyAkubraImpl.LOGGER.info(String.format("Requesting %s", url));
        WebResource r = client.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));

        try {
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        } catch (UniformInterfaceException ex2) {
            if (ex2.getResponse().getStatus() == 404) {
                FedoraAccessProxyAkubraImpl.LOGGER.log(Level.WARNING, "Call to {0} failed with message {1}. Skyping document.",
                        new Object[]{url, ex2.getResponse().toString()});
                return null;
            } else {
                FedoraAccessProxyAkubraImpl.LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
                return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
            }
        } catch (Exception ex) {
            FedoraAccessProxyAkubraImpl.LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        }
    }
}
