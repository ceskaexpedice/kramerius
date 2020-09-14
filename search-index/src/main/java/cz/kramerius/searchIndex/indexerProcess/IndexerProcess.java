package cz.kramerius.searchIndex.indexerProcess;


import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexer;
import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.Foxml2SolrInputConverter;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNode;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNodeManager;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IndexerProcess {

    private static final Logger logger = Logger.getLogger(IndexerProcess.class.getName());

    private final SolrConfig solrConfig;
    //status info
    private boolean stopped = false;
    private ProgressListener progressListener;
    private long initTime;
    //helpers
    private final ReportLogger reportLogger;
    private final KrameriusRepositoryAccessAdapter repositoryConnector;
    private final RepositoryNodeManager nodeManager;

    private final Foxml2SolrInputConverter foxml2SolrInputConverter;
    private SolrIndexer solrIndexer = null;


    public IndexerProcess(KrameriusRepositoryAccessAdapter repositoryConnector, SolrConfig solrConfig, OutputStream reportLoggerStream) {
        long start = System.currentTimeMillis();
        this.repositoryConnector = repositoryConnector;
        this.nodeManager = new RepositoryNodeManager(repositoryConnector);
        this.foxml2SolrInputConverter = new Foxml2SolrInputConverter();
        this.solrConfig = solrConfig;
        this.reportLogger = new ReportLogger(reportLoggerStream);
        init();
        this.initTime = System.currentTimeMillis() - start;
    }

    private void report(String message) {
        reportLogger.report(message);
    }

    private void report(String message, Throwable e) {
        reportLogger.report(message, e);
    }

    private void init() {
        report("Parameters");
        report("==============================");
        reportParams();

        report("Initialization");
        report("==============================");

        try {
            solrIndexer = new SolrIndexer(solrConfig);
            report("- SOLR API connector initialized");
        } catch (Exception e) {
            report("Initialization error: TemplateException: " + e.getMessage());
            logger.log(Level.SEVERE, "Initialization error", e);
        }
        report(" ");
    }


    public void indexByObjectPid(String pid, IndexationType type) {
        long start = System.currentTimeMillis();
        Counters counters = new Counters();
        report("Processing " + pid + " (indexation type: " + type + ")");
        //int limit = 3;
        report("==============================");

        indexObjectWithCounters(pid, counters);
        processChildren(nodeManager.getKrameriusNode(pid), true, type, counters);
        commitAfterLastIndexation(counters);

        report(" ");

        if (stopped) {
            report("Process has been stopped prematurely");
        }

        report("Summary");
        report("=====================================================");
        report(" objects found    : " + counters.getFound());
        report(" objects processed: " + counters.getProcessed());
        report(" objects indexed  : " + counters.getIndexed());
        report(" objects erroneous: " + counters.getErrors());
        report(" initialization duration: " + formatTime(initTime));
        report(" records processing duration: " + formatTime(System.currentTimeMillis() - start));
        if (progressListener != null) {
            progressListener.onFinished(counters.getProcessed(), counters.getFound());
        }
    }

    private void processChildren(String parentPid, IndexationType type, Counters counters) {
        RepositoryNode parentNode = nodeManager.getKrameriusNode(parentPid);
        if (parentNode != null) {
            processChildren(parentNode, false, type, counters);
        } else {
            report(" Object node not found: " + parentPid);
        }
    }

    private void indexObjectWithCounters(String pid, Counters counters) {
        try {
            counters.incrementFound();
            boolean objectAvailable = repositoryConnector.isObjectAvailable(pid);
            if (!objectAvailable) {
                counters.incrementErrors();
                report(" Object not available in storage: " + pid);
            } else {
                report("Indexing " + pid);
                Document foxmlDoc = repositoryConnector.getObjectFoxml(pid, true);
                RepositoryNode repositoryNode = nodeManager.getKrameriusNode(pid);
                report("model: " + repositoryNode.getModel());
                report("title: " + repositoryNode.getTitle());
                //the isOcrTextAvailable method (and for other datastreams) is inefficient for implementation through http stack (because of HEAD requests)
                //String ocrText = repositoryConnector.isOcrTextAvailable(pid) ? repositoryConnector.getOcrText(pid) : null;
                String ocrText = repositoryConnector.getOcrText(pid);
                //System.out.println("ocr: " + ocrText);
                SolrInput solrInput = foxml2SolrInputConverter.convert(foxmlDoc, ocrText, repositoryNode, nodeManager);
                String solrInputStr = solrInput.getDocument().asXML();
                solrIndexer.indexFromXmlString(solrInputStr, false);
                counters.incrementIndexed();
                report("");
            }
        } catch (DocumentException e) {
            counters.incrementErrors();
            report(" Document error", e);
        } catch (IOException e) {
            counters.incrementErrors();
            report(" I/O error", e);
        } catch (SolrServerException e) {
            counters.incrementErrors();
            report(" Solr server error", e);
        }
    }

    private void processChildren(RepositoryNode parent, boolean isIndexationRoot, IndexationType type, Counters counters) {
        //System.out.println("processChildren (" + parent.getPid() + ")");
        switch (type) {
            case OBJECT: {
                //nothing
            }
            break;
            case OBJECT_AND_CHILDREN: {
                if (isIndexationRoot) {
                    for (String childPid : parent.getPidsOfOwnChildren()) { //index own children
                        indexObjectWithCounters(childPid, counters);
                    }
                    for (String childPid : parent.getPidsOfFosterChildren()) { //index foster children
                        indexObjectWithCounters(childPid, counters);
                    }
                }
            }
            break;
            case TREE: {
                for (String childPid : parent.getPidsOfOwnChildren()) {
                    indexObjectWithCounters(childPid, counters);//index own children
                    processChildren(childPid, type, counters); //process own childrens' trees
                }
            }
            break;
            case TREE_INDEX_ONLY_NEWER: {
                for (String childPid : parent.getPidsOfOwnChildren()) {
                    boolean isNewer = true; //TODO: detect
                    if (isNewer) {
                        indexObjectWithCounters(childPid, counters);//index own children
                    }
                    processChildren(childPid, type, counters); //process own childrens' trees
                }
            }
            break;
            case TREE_PROCESS_ONLY_NEWER: {
                for (String childPid : parent.getPidsOfOwnChildren()) {
                    boolean isNewer = true; //TODO: detect
                    if (isNewer) {
                        indexObjectWithCounters(childPid, counters);//index own children
                        processChildren(childPid, type, counters); //process own childrens' trees
                    }
                }
            }
            break;
            case TREE_INDEX_ONLY_PAGES: {
                for (String childPid : parent.getPidsOfOwnChildren()) {
                    boolean isPage = false; //TODO: detect
                    if (isPage) {
                        indexObjectWithCounters(childPid, counters);//index own children
                    } else {
                        processChildren(childPid, type, counters); //process own childrens' trees
                    }
                }
            }
            break;
            case TREE_INDEX_ONLY_NONPAGES: {
                for (String childPid : parent.getPidsOfOwnChildren()) {
                    boolean isPage = false; //TODO: detect
                    if (!isPage) {
                        indexObjectWithCounters(childPid, counters);//index own children
                        processChildren(childPid, type, counters); //process own childrens' trees
                    }
                }
            }
            break;
            case TREE_AND_FOSTER_TREES: {
                for (String childPid : parent.getPidsOfOwnChildren()) {
                    indexObjectWithCounters(childPid, counters);//index own children
                    processChildren(childPid, type, counters); //process own childrens' trees
                }
                for (String childPid : parent.getPidsOfFosterChildren()) {
                    indexObjectWithCounters(childPid, counters);//index foster children
                    processChildren(childPid, type, counters); //process foster childrens' trees
                }
            }
        }
    }


    private void commitAfterLastIndexation(Counters counters) {
        try {
            solrIndexer.commit();
        } catch (IOException e) {
            counters.incrementErrors();
            report(" I/O error", e);
        } catch (SolrServerException e) {
            counters.incrementErrors();
            report(" Solr server error", e);
        }
    }


    @Deprecated
    public void indexDoc(Document solrDoc) {
        List<Document> singleItemList = new ArrayList<>();
        singleItemList.add(solrDoc);
        indexDocBatch(singleItemList);
    }

    @Deprecated
    public void indexDocBatch(List<Document> solrDocs) {
        long start = System.currentTimeMillis();
        Counters counters = new Counters();
        report("Processing " + counters.getFound() + " records");
        //int limit = 3;
        report("==============================");
        for (Document doc : solrDocs) {
            if (stopped) {
                report(" stopped ");
                break;
            }
            index(doc, counters, false);
        }
        report(" ");

        report("Summary");
        report("=====================================================");
        report(" records found    : " + counters.getFound());
        report(" records processed: " + counters.getProcessed());
        report(" records indexed  : " + counters.getIndexed());
        report(" records erroneous: " + counters.getErrors());
        report(" initialization duration: " + formatTime(initTime));
        report(" records processing duration: " + formatTime(System.currentTimeMillis() - start));
        if (progressListener != null) {
            progressListener.onFinished(counters.getProcessed(), counters.getFound());
        }
    }

    private void index(Document solrDoc, Counters counters, boolean explicitCommit) {
        try {
            counters.incrementFound();
            report(" indexing");
            solrIndexer.indexFromXmlString(solrDoc.asXML(), explicitCommit);
            report(" indexed");
            counters.incrementIndexed();
        } catch (IOException e) {
            counters.incrementErrors();
            report(" I/O error", e);
        } catch (SolrServerException e) {
            counters.incrementErrors();
            report(" Solr server error", e);
        } catch (SolrException e) {
            counters.incrementErrors();
            report(" Solr error", e);
        } catch (DocumentException e) {
            counters.incrementErrors();
            report(" Document error", e);
        }
        if (progressListener != null) {
            progressListener.onProgress(counters.getProcessed(), counters.getFound());
        }
    }

    private String formatTime(long millis) {
        long hours = millis / (60 * 60 * 1000);
        long minutes = millis / (60 * 1000) - hours * 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%d:%02d:%02d", hours, minutes, seconds);
    }

    private void reportParams() {
        report(" SOLR API");
        report(" -----------------");
        report("  Base url: " + solrConfig.baseUrl);
        report("  Collection: " + solrConfig.collection);
        report("  Https: " + solrConfig.useHttps);
        report("  Login: " + solrConfig.login);
        report(" ");
    }

    public void stop() {
        stopped = true;
    }

    public void close() {
        reportLogger.close();
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }
}