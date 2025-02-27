package cz.incad.kramerius.fedora.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.cdk.CDKUtils;
import cz.incad.kramerius.utils.BasicAuthenticationClientFilter;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.CollectionException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.utils.pid.LexerException;
import org.ceskaexpedice.akubra.utils.pid.PIDParser;
import org.kramerius.Import;
import org.w3c.dom.Document;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

public class OnDemandIngest {

    public static final Logger LOGGER = Logger.getLogger(OnDemandIngest.class.getName());

    public static Object INGESTING_LOCK = new Object();
    private final Client client;

    private SolrAccess solrAccess;

    @Inject
    public OnDemandIngest( @Named("new-index") SolrAccess solrAccess) throws IOException {
        this.solrAccess = solrAccess;
        this.client = Client.create();
    }
        // on demand request
    void onDemandIngest(String pid, AkubraRepository internalAPI) throws CollectionException, LexerException, IOException, RepositoryException, JAXBException, TransformerException {
        // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.info(String.format("Requesting info %s", pid));
        try {
            long start = System.currentTimeMillis();
            Document solrDataDocument = solrAccess.getSolrDataByPid(pid);
            String leader = CDKUtils.findCDKLeader(solrDataDocument.getDocumentElement());
            List<String> sources = CDKUtils.findSources(solrDataDocument.getDocumentElement());

            String source = leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
            LOGGER.info(String.format("Leader for pid %s is %s", pid, leader));
            if (source != null) {

            	String baseurl = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + source + ".baseurl");
                String username = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + source + ".username");
                String password = KConfiguration.getInstance().getConfiguration().getString("cdk.collections.sources." + source + ".pswd");
                if (StringUtils.isAnyString(username) && StringUtils.isAnyString(password)) {
                    String url = baseurl  +(baseurl.endsWith("/") ? "" : "/")+ "api/v4.6/cdk/" + pid + "/foxml?collection=" + source;
                    if (internalAPI.objectExists(pid)) {
                        LOGGER.info("Object exists");
                        return;
                    }
                    InputStream foxml = foxml(url, username, password);
                    long foxmlTime = System.currentTimeMillis();
                    // only ingesting is synchronized by shared lock
                    synchronized (INGESTING_LOCK) {
                        if (internalAPI.objectExists(pid)) return;
                        // tady by to melo byt synchronizovane
                        // import source
                        Import.ingest(internalAPI, foxml, pid, null, null, true);
                        // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.info(String.format("Whole ingest of %s took %d ms (download foxml %d ms)",pid, (System.currentTimeMillis() - start), (System.currentTimeMillis() - foxmlTime) ));
                    }
                } else throw new IOException("Cannot read data from "+ baseurl +".  Missing property "+"cdk.collections.sources." + source + ".username or "+"cdk.collections.sources." + source + ".pswd  for pid  "+pid);

            } else {
                LOGGER.warning(String.format("No source or leader for pid %s", pid));

                StringWriter stringWriter = new StringWriter();
                XMLUtils.print(solrDataDocument, stringWriter);

                LOGGER.info("document output "+stringWriter.toString());
            }
        } catch (IOException | JAXBException | TransformerException | LexerException | RepositoryException e) {
            // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
    }

    public void ingestIfNecessary(AkubraRepository internalAPI, String... pids) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException, XPathExpressionException {
        for (String pid : pids) {
            ingestIfNecessary(internalAPI, pid);
        }
    }

    public void ingestIfNecessary(AkubraRepository internalAPI, String pid) throws RepositoryException, IOException, CollectionException, LexerException, JAXBException, TransformerException, XPathExpressionException {
        if (!pid.startsWith(PIDParser.VC_PREFIX)) {
            //Repository internalAPI = fedoraAccessProxyAkubra.getInternalAPI();
            if (!internalAPI.objectExists(pid)) {
                // put in the queue and wait
                onDemandIngest(pid, internalAPI);
            }
        }
    }
    protected InputStream foxml(String url, String userName, String pswd) {
        // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.info(String.format("Requesting %s", url));
        WebResource r = client.resource(url);
        r.addFilter(new BasicAuthenticationClientFilter(userName, pswd));

        try {
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        } catch (UniformInterfaceException ex2) {
            if (ex2.getResponse().getStatus() == 404) {
                // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.log(Level.WARNING, "Call to {0} failed with message {1}. Skyping document.",
                        //new Object[]{url, ex2.getResponse().toString()});
                return null;
            } else {
                // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
                return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
            }
        } catch (Exception ex) {
            // TODO AK_NEW FedoraAccessProxyAkubraImpl.LOGGER.log(Level.WARNING, "Call to {0} failed. Retrying...", url);
            return r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        }
    }
}
