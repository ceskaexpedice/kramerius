package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.MigrationUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class DNNTWorker implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(DNNTWorker.class.getName());

    protected FedoraAccess fedoraAccess;
    protected Client client;
    protected CyclicBarrier barrier;
    protected String parentPid;
    protected boolean addRemoveFlag;

    public DNNTWorker(FedoraAccess fedoraAccess, Client client, String parentPid, boolean addRemoveFlag) {
        this.fedoraAccess = fedoraAccess;
        this.client = client;
        this.addRemoveFlag = addRemoveFlag;
        this.parentPid = parentPid;
    }

    protected static String selectUrl() {
        String shost = KConfiguration.getInstance().getSolrHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "select";
        return shost;
    }

    protected static String updateUrl() {
        String shost = KConfiguration.getInstance().getSolrHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "update";
        return shost;
    }

    protected static boolean configuredUseCursor() {
        boolean useCursor = KConfiguration.getInstance().getConfiguration().getBoolean("dnnt.usecursor", true);
        DNNTLabeledWrokerFlag.LOGGER.info("Use cursor "+useCursor);
        return useCursor;
    }


    protected void sendToDest(Client client, Document batchDoc) {
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
                DNNTLabeledWrokerFlag.LOGGER.log(Level.SEVERE, new String(bos.toByteArray()));
            }
        } catch (UniformInterfaceException | ClientHandlerException | IOException | TransformerException e) {
            DNNTLabeledWrokerFlag.LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    protected Set<String> fetchAllPids(String q) throws UnsupportedEncodingException {
        String masterQuery = URLEncoder.encode(q,"UTF-8");
        Set<String> allSet = new HashSet<>();
        if (configuredUseCursor()) {
            try {
                IterationUtils.cursorIteration(client, KConfiguration.getInstance().getSolrHost() ,masterQuery,(em, i) -> {
                    List<String> pp = MigrationUtils.findAllPids(em);
                    allSet.addAll(pp);
                }, ()->{});
            } catch (ParserConfigurationException | SAXException | IOException | InterruptedException | MigrateSolrIndexException | BrokenBarrierException e  ) {
                DNNTWorkerFlag.LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }


        } else try {
            IterationUtils.queryFilterIteration(client, MigrationUtils.configuredSourceServer(), masterQuery, (em, i) -> {
                List<String> pp = MigrationUtils.findAllPids(em);
                allSet.addAll(pp);
            }, () -> {
            });
        } catch (MigrateSolrIndexException | IOException | SAXException | ParserConfigurationException | BrokenBarrierException | InterruptedException e) {
            DNNTWorkerFlag.LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return allSet;
    }


    protected List<String> solrPidPaths(String pid)  {
        // &fl=pid_path&wt=xml
        try {
            List<String> paths = new ArrayList<>();
            Element element = IterationUtils.executeQuery(this.client, selectUrl(), "?q="+URLEncoder.encode("PID:\"" + pid + "\"", "UTF-8")+"&fl=pid_path&wt=xml");
            Element pidPath = XMLUtils.findElement(element, (e) -> {
                if (e.getNodeName().equals("arr") && e.getAttribute("name").equals("pid_path")) {
                    return true;
                } else return false;
            });
            if (pidPath != null) {
                NodeList childNodes = pidPath.getChildNodes();
                for(int i=0, ll=childNodes.getLength();i<ll;i++) {
                    paths.add(childNodes.item(i).getTextContent().trim());
                }
            }
            return paths;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public void run() {
        try {
            LOGGER.info("DNNT Flag thread "+Thread.currentThread().getName()+" "+this.parentPid);

            List<String> paths = solrPidPaths(this.parentPid);
            if (!paths.isEmpty()) {

                String q = solrChildrenQuery(paths);
                boolean changedFoxmlFlag = changeFOXML(this.parentPid);

                Set<String> allSet = fetchAllPids(q);
                List<String> all = new ArrayList<>(allSet);
                if (this.addRemoveFlag) {
                    LOGGER.info("Setting flag for all children for "+this.parentPid+" and number of children are "+all.size());
                } else {
                    LOGGER.info("UnSetting flag for all children for "+this.parentPid+" and number of children are "+all.size());
                }
                int batchSize = KConfiguration.getInstance().getConfiguration().getInt(".dnnt.solr.batchsize", 100);
                int numberOfBatches = all.size() / batchSize;
                if (all.size() % batchSize > 0) {
                    numberOfBatches += 1;
                }
                for(int i=0;i<numberOfBatches;i++) {
                    int start = i * batchSize;
                    List<String> sublist = all.subList(start, Math.min(start + batchSize, all.size()));
                    Document batch = createBatch(sublist, changedFoxmlFlag);
                    try {
                        StringWriter writer = new StringWriter();
                        XMLUtils.print(batch, writer);
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }

                    sendToDest(client, batch);
                }
                LOGGER.info("DNNT Flag for  "+this.parentPid+" has been set");
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.severe("DNNT Flag for  "+this.parentPid+" hasn't been set");
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            //commit(client);
            try {
                this.barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
              LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }

    }

    public void setBarrier(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    protected abstract  Document createBatch(List<String> sublist, boolean changedFoxmlFlag);
    protected  abstract String solrChildrenQuery(List<String> pidPaths);
    protected abstract boolean changeFOXML(String pid);

    protected List<String> changeDNNTLabelInFOXML(String pid, String label) {
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String dnntLabel = FedoraNamespaces.KRAMERIUS_URI+"dnnt-label";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, dnntLabel);
        Optional<RelationshipTuple> any = relationships.stream().filter(tuple -> {
            return tuple.getObject().equals(label);
        }).findAny();
        if (!any.isPresent()) {
            if (addRemoveFlag) apim.addRelationship(pid, dnntLabel,label, true, null);
        } else {
            apim.purgeRelationship(pid, dnntLabel, any.get().getObject(), any.get().isIsLiteral(), any.get().getDatatype());
            if (addRemoveFlag) apim.addRelationship(pid, dnntLabel,label, true, null);
        }

        relationships = apim.getRelationships(pid, dnntLabel);
        return relationships.stream().map(RelationshipTuple::getObject).collect(Collectors.toList());
    }

    protected boolean changeDNNTInFOXML(String pid, boolean flag) {
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String dnntFlag = FedoraNamespaces.KRAMERIUS_URI+"dnnt";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, dnntFlag);
        if (relationships.isEmpty()) {
            if (flag)  apim.addRelationship(pid, dnntFlag,"true", true, null);
        } else {
            apim.purgeRelationship(pid, dnntFlag, relationships.get(0).getObject(), relationships.get(0).isIsLiteral(), relationships.get(0).getDatatype());
            if (flag) apim.addRelationship(pid, dnntFlag,"true", true, null);
        }

        relationships = apim.getRelationships(pid, dnntFlag);
        return !relationships.isEmpty();
    }

    protected boolean changeDNNTInFOXML(String pid) {
        return changeDNNTInFOXML(pid, this.addRemoveFlag);
    }
}
