package cz.incad.kramerius;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.BatchUtils;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.MigrationUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DNNTWorker implements Runnable {
    public static Logger LOGGER = Logger.getLogger(DNNTWorker.class.getName());

    private String parentPid;
    private FedoraAccess fedoraAccess;
    private Client client;

    public DNNTWorker(String parentPid, FedoraAccess fedoraAccess, Client client) {
        this.parentPid = parentPid;
        this.fedoraAccess = fedoraAccess;
        this.client = client;
    }

    @Override
    public void run() {

        try {
            String q = KConfiguration.getInstance().getConfiguration().getString("root_pid:\""+this.parentPid+"\"", DNNTFlag.DNNT_QUERY);
            String masterQuery = URLEncoder.encode(q,"UTF-8");
            setDNNTFlag(fedoraAccess, this.parentPid);
            List<String> all = new ArrayList<>();
            if (configuredUseCursor()) {
                try {
                    IterationUtils.cursorIteration(client, MigrationUtils.configuredSourceServer(),masterQuery,(em, i) -> {
                        List<String> pp = MigrationUtils.findAllPids(em);
                        System.out.println(pp);
                        all.addAll(pp);
                    }, ()->{});
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (MigrateSolrIndexException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (BrokenBarrierException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }


            } else {
                try {
                    IterationUtils.queryFilterIteration(client, MigrationUtils.configuredSourceServer(),masterQuery,(em, i) -> {
                        List<String> pp = MigrationUtils.findAllPids(em);
                        System.out.println(pp);
                        all.addAll(pp);
                    }, ()->{});
                } catch (MigrateSolrIndexException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (SAXException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (BrokenBarrierException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }

            int batchSize = KConfiguration.getInstance().getConfiguration().getInt(".dnnt.solr.batchsize", 100);
            int numberOfBatches = all.size() / batchSize;
            if (all.size() % batchSize > 0) {
                numberOfBatches += 1;
            }
            for(int i=0;i<numberOfBatches;i++) {
                int start = i * batchSize;
                List<String> sublist = all.subList(start, Math.min(start + batchSize, all.size()));
                System.out.println(sublist.size());
                int index  = all.indexOf(sublist.get(0));
                System.out.println("Current index "+index);
                try {
                    Document batch = DNNTBatchUtils.createBatch(sublist);
                    sendToDest(client, batch);
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            commit(client);
        }

    }

    public static boolean configuredUseCursor() {
        boolean useCursor = KConfiguration.getInstance().getConfiguration().getBoolean("dnnt.usecursor", false);
        LOGGER.info("Use cursor "+useCursor);
        return useCursor;
    }

    static void setDNNTFlag(FedoraAccess fedoraAccess, String pid) {
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String dnntFlag = FedoraNamespaces.KRAMERIUS_URI+"dnnt";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, dnntFlag);
        if (relationships.isEmpty()) {
            apim.addRelationship(pid, dnntFlag,"true", true, null);
        } else {
            apim.purgeRelationship(pid, dnntFlag, relationships.get(0).getObject(), relationships.get(0).isIsLiteral(), relationships.get(0).getDatatype());
            apim.addRelationship(pid, dnntFlag,"true", true, null);
        }
    }

    public static void commit(Client client) {
        String shost = updateUrl()+"?commit=true";
        WebResource r = client.resource(shost);
        ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).post(ClientResponse.class);
    }

    public static void sendToDest(Client client, Document batchDoc) {
        try {
            StringWriter writer = new StringWriter();
            String shost = updateUrl();
            WebResource r = client.resource(shost);
            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream entityInputStream = resp.getEntityInputStream();
                IOUtils.copyStreams(entityInputStream, bos);
            }
        } catch (UniformInterfaceException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (ClientHandlerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static String updateUrl() {
        String shost = KConfiguration.getInstance().getSolrHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "update";
        return shost;
    }

}
