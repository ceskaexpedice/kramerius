package cz.incad.kramerius.workers;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.services.IterationUtils;
import cz.incad.kramerius.services.MigrationUtils;
import cz.incad.kramerius.solr.SolrFieldsMapping;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

/**
 * Default dnnt flag, label worker
 */
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
        String shost = KConfiguration.getInstance().getSolrSearchHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "select";
        return shost;
    }

    protected static String updateUrl() {
        String shost = KConfiguration.getInstance().getSolrSearchHost();
        shost = shost  + (shost.endsWith("/") ? ""  : "/") + "update";
        return shost;
    }

    protected static boolean configuredUseCursor() {
        boolean useCursor = KConfiguration.getInstance().getConfiguration().getBoolean("dnnt.usecursor", true);
        LOGGER.info("Use cursor "+useCursor);
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
                LOGGER.log(Level.SEVERE, new String(bos.toByteArray()));
            }
        } catch (UniformInterfaceException | ClientHandlerException | IOException | TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    protected Set<String> fetchAllPids(String q) throws UnsupportedEncodingException {
        //String masterQuery = URLEncoder.encode(q,"UTF-8");
        Set<String> allSet = new HashSet<>();
        if (configuredUseCursor()) {
            try {
                IterationUtils.cursorIteration(client, KConfiguration.getInstance().getSolrSearchHost() ,q,(em, i) -> {
                    List<String> pp = MigrationUtils.findAllPids(em);
                    allSet.addAll(pp);
                }, ()->{}, null);
            } catch (ParserConfigurationException | SAXException | IOException | InterruptedException | MigrateSolrIndexException | BrokenBarrierException e  ) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }


        } else try {
            IterationUtils.queryFilterIteration(client, MigrationUtils.configuredSourceServer(), q, (em, i) -> {
                List<String> pp = MigrationUtils.findAllPids(em);
                allSet.addAll(pp);
            }, () -> {
            }, null);
        } catch (MigrateSolrIndexException | IOException | SAXException | ParserConfigurationException | BrokenBarrierException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return allSet;
    }


    protected List<String> solrPidPaths(String pid)  {
        // &fl=pid_path&wt=xml
        try {
            List<String> paths = new ArrayList<>();
            Element element = IterationUtils.executeQuery(this.client, selectUrl(), "?q="+URLEncoder.encode(SolrFieldsMapping.getInstance().getPidField() + ":\"" + pid + "\"", "UTF-8")+"&fl="+ SolrFieldsMapping.getInstance().getPidPathField()+"&wt=xml");
            Element pidPath = XMLUtils.findElement(element, (e) -> {
                if (e.getNodeName().equals("str") && e.getAttribute("name").equals(SolrFieldsMapping.getInstance().getPidPathField())) {
                    return true;
                } else return false;
            });
            if (pidPath != null) {
                String textContent = pidPath.getTextContent();
                paths.add(textContent.trim());
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
            LOGGER.info("Setting label thread "+Thread.currentThread().getName()+" "+this.parentPid);

            List<String> paths = solrPidPaths(this.parentPid);
            if (!paths.isEmpty()) {

                String q = URLEncoder.encode(solrChildrenQuery(paths), "UTF-8");
                // change literal propagating down
                boolean changedFoxmlFlag = changeFOXMLDown(this.parentPid);

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
                    Document batch = createSOLRBatchForChildren(sublist, changedFoxmlFlag);
                    sendToDest(client, batch);
                }

                LOGGER.info("Settings all contains-licenses for parents");
                List<String> parentPids = solrPidParents(this.parentPid, paths);
                if (!parentPids.isEmpty()) {
                    if (this.addRemoveFlag) {
                        parentPids.stream().forEach(this::changeFOXMLUp);

                        // zrusit odeberiani smerem nahoru
                        Document batchForParents = createSOLRBatchForParents(parentPids, changedFoxmlFlag);
                        if (batchForParents != null) {
                            sendToDest(client, batchForParents);
                        }
                    } else {
                        LOGGER.info("Removing contains-licences is not supported; must be done by separate process");
                    }
                }
                LOGGER.info("Label for  "+this.parentPid+" has been set");
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

    /** Creating batch for setting (flag,label) for children */
    protected abstract  Document createSOLRBatchForChildren(List<String> sublist, boolean changedFoxmlFlag);

    /** Creating batch for setting (flag,label) for parents */
    protected abstract  Document createSOLRBatchForParents(List<String> sublist, boolean changedFoxmlFlag);

    /** Construct Children query */
    protected  abstract String solrChildrenQuery(List<String> pidPaths);

    /** Change foxml downwards */
    protected abstract boolean changeFOXMLDown(String pid);

    /** Change foxml upwards */
    protected abstract boolean changeFOXMLUp(String pid);

    List<String> solrPidParents(String pid, List<String> pidPaths) {
        List<String> parents = new ArrayList<>();
        pidPaths.stream().forEach(onePath-> {
            List<String> list = Arrays.stream(onePath.split("/")).collect(Collectors.toList());
            int indexOf = list.indexOf(pid);
            if (indexOf > -1) {
                parents.addAll(list.subList(0, indexOf));
            }
        });
        return parents;
    }



    protected boolean changeDNNTInFOXML(String pid, boolean flag) {
        try {
            Repository repo = fedoraAccess.getInternalAPI();
            if (repo.getObject(pid).relationsExists("dnnt", FedoraNamespaces.KRAMERIUS_URI)) {
                repo.getObject(pid).removeRelationsByNameAndNamespace("dnnt",FedoraNamespaces.KRAMERIUS_URI);
            }
            repo.getObject(pid).addLiteral("dnnt",FedoraNamespaces.KRAMERIUS_URI, flag+"");
            Optional<Triple<String, String, String>> dnnt = repo.getObject(pid).getRelations(FedoraNamespaces.KRAMERIUS_URI).stream().filter(tr -> {
                return tr.getLeft().equals("dnnt");
            }).findAny();

            if (dnnt.isPresent()) {
                return Boolean.parseBoolean(dnnt.get().getRight());
            } else {
                return false;
            }
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return false;
        }
    }

    protected boolean changeDNNTInFOXML(String pid) {
        return changeDNNTInFOXML(pid, this.addRemoveFlag);
    }
}
