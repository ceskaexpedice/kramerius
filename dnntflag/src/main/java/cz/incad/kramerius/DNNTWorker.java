package cz.incad.kramerius;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.service.MigrateSolrIndexException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DNNTWorker implements Runnable {
    public static Logger LOGGER = Logger.getLogger(DNNTWorker.class.getName());

    private static final String DNNT_QUERY = "dnnt.solr.query";
    private static final String DNNT_QUERY_UNSET = "dnnt.solr.unsetquery";



    private String parentPid;
    private FedoraAccess fedoraAccess;
    private Client client;

    private CyclicBarrier barrier;
    private boolean flag;

    DNNTWorker(String parentPid, FedoraAccess fedoraAccess, Client client, boolean flag) {
        this.parentPid = parentPid;
        this.fedoraAccess = fedoraAccess;
        this.client = client;
        this.flag = flag;
        LOGGER.info("Constructing   worker for "+this.parentPid);
    }


    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }


    @Override
    public void run() {
        try {
            LOGGER.info("DNNT Flag thread "+Thread.currentThread().getName()+" "+this.parentPid);
            String q = null;
            String rParentPid = "root_pid:\""+this.parentPid+"\"";
            if (this.flag) {
                //(root_pid: -dnnt:[ * TO * ]) || (* +dnnt:false)
                q = KConfiguration.getInstance().getConfiguration().getString( DNNT_QUERY,"("+rParentPid+" -dnnt:[* TO *]) || ("+rParentPid+" +dnnt:false)");
            } else {
                q = KConfiguration.getInstance().getConfiguration().getString( DNNT_QUERY_UNSET,"("+rParentPid+" dnnt:[* TO *]) || ("+rParentPid+" +dnnt:true)");
            }
            String masterQuery = URLEncoder.encode(q,"UTF-8");
            changeDNNTFlag(fedoraAccess, this.parentPid, this.flag);
            Set<String> allSet = new HashSet<>();
            if (configuredUseCursor()) {
                try {
                    IterationUtils.cursorIteration(client,KConfiguration.getInstance().getSolrHost() ,masterQuery,(em, i) -> {
                        List<String> pp = MigrationUtils.findAllPids(em);
                        allSet.addAll(pp);
                    }, ()->{});
                } catch (ParserConfigurationException  | SAXException | IOException | InterruptedException | MigrateSolrIndexException | BrokenBarrierException e  ) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }


            } else try {
                IterationUtils.queryFilterIteration(client, MigrationUtils.configuredSourceServer(), masterQuery, (em, i) -> {
                    List<String> pp = MigrationUtils.findAllPids(em);
                    allSet.addAll(pp);
                }, () -> {
                });
            } catch (MigrateSolrIndexException | IOException | SAXException | ParserConfigurationException | BrokenBarrierException | InterruptedException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

            List<String> all = new ArrayList<>(allSet);
            LOGGER.info("Setting flag for all children for "+this.parentPid+" and number of children are "+all.size());
            int batchSize = KConfiguration.getInstance().getConfiguration().getInt(".dnnt.solr.batchsize", 100);
            int numberOfBatches = all.size() / batchSize;
            if (all.size() % batchSize > 0) {
                numberOfBatches += 1;
            }
            for(int i=0;i<numberOfBatches;i++) {
                int start = i * batchSize;
                List<String> sublist = all.subList(start, Math.min(start + batchSize, all.size()));
                try {
                    Document batch = DNNTBatchUtils.createBatch(sublist, this.flag);
                    sendToDest(client, batch);
                } catch (ParserConfigurationException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }
            LOGGER.info("DNNT Flag for  "+this.parentPid+" has been set");
        } catch (UnsupportedEncodingException e) {
            LOGGER.info("DNNT Flag for  "+this.parentPid+" hasn't been set");
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            commit(client);

            try {
                this.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }

    }

    private static boolean configuredUseCursor() {
        boolean useCursor = KConfiguration.getInstance().getConfiguration().getBoolean("dnnt.usecursor", false);
        LOGGER.info("Use cursor "+useCursor);
        return useCursor;
    }

    private static void changeDNNTFlag(FedoraAccess fedoraAccess, String pid, boolean set) {
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String dnntFlag = FedoraNamespaces.KRAMERIUS_URI+"dnnt";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, dnntFlag);
        if (relationships.isEmpty()) {
            if (set)  apim.addRelationship(pid, dnntFlag,"true", true, null);
        } else {
            apim.purgeRelationship(pid, dnntFlag, relationships.get(0).getObject(), relationships.get(0).isIsLiteral(), relationships.get(0).getDatatype());
            if (set) apim.addRelationship(pid, dnntFlag,"true", true, null);
        }
    }


    public static void commit(Client client) {
        String shost = updateUrl()+"?commit=true";
        WebResource r = client.resource(shost);
        r.accept(MediaType.TEXT_XML).entity("<commit/>").type(MediaType.TEXT_XML).post(ClientResponse.class);
    }

    public static void sendToDest(Client client, Document batchDoc) {
        try {
            StringWriter writer = new StringWriter();
            XMLUtils.print(batchDoc, writer);
            String shost = updateUrl();
            WebResource r = client.resource(shost);
            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream entityInputStream = resp.getEntityInputStream();
                IOUtils.copyStreams(entityInputStream, bos);
                LOGGER.log(Level.SEVERE, new String(bos.toByteArray()));
            }
        } catch (UniformInterfaceException | ClientHandlerException | IOException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static String updateUrl() {
        String shost = KConfiguration.getInstance().getSolrHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "update";
        return shost;
    }

}
