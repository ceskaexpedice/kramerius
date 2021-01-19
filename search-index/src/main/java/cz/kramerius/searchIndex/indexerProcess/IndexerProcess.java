package cz.kramerius.searchIndex.indexerProcess;


import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexer;
import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.SolrInputBuilder;
import cz.kramerius.searchIndex.repositoryAccess.KrameriusRepositoryAccessAdapter;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNode;
import cz.kramerius.searchIndex.repositoryAccess.nodes.RepositoryNodeManager;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrException;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;
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

    private final SolrInputBuilder solrInputBuilder;
    private SolrIndexer solrIndexer = null;


    public IndexerProcess(KrameriusRepositoryAccessAdapter repositoryConnector, SolrConfig solrConfig, OutputStream reportLoggerStream) {
        long start = System.currentTimeMillis();
        this.repositoryConnector = repositoryConnector;
        this.nodeManager = new RepositoryNodeManager(repositoryConnector);
        this.solrInputBuilder = new SolrInputBuilder();
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

        RepositoryNode node = nodeManager.getKrameriusNode(pid);
        indexObjectWithCounters(pid, node, counters);
        if (node != null) {
            processChildren(node, true, type, counters);
        }
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
        report(" objects removed  : " + counters.getRemoved());
        report(" objects erroneous: " + counters.getErrors());
        report(" *counters include pages from pdf, i.e. not real objects in repository");
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
        indexObjectWithCounters(pid, nodeManager.getKrameriusNode(pid), counters);
    }

    private void indexObjectWithCounters(String pid, RepositoryNode repositoryNode, Counters counters) {
        try {
            counters.incrementFound();
            boolean objectAvailable = repositoryNode != null;
            if (!objectAvailable) {
                report("object not found in repository, removing from index as well");
                solrIndexer.deleteById(pid);
                counters.incrementRemoved();
                report("");
            } else {
                report("Indexing " + pid);
                Document foxmlDoc = repositoryConnector.getObjectFoxml(pid, true);
                report("model: " + repositoryNode.getModel());
                report("title: " + repositoryNode.getTitle());
                //the isOcrTextAvailable method (and for other datastreams) is inefficient for implementation through http stack (because of HEAD requests)
                //String ocrText = repositoryConnector.isOcrTextAvailable(pid) ? repositoryConnector.getOcrText(pid) : null;
                String ocrText = normalizeWhitespacesForOcrText(repositoryConnector.getOcrText(pid));
                //System.out.println("ocr: " + ocrText);
                //IMG_FULL mimetype
                String imgFullMime = repositoryConnector.getImgFullMimetype(pid);
                SolrInput solrInput = solrInputBuilder.processObjectFromRepository(foxmlDoc, ocrText, repositoryNode, nodeManager, imgFullMime);
                String solrInputStr = solrInput.getDocument().asXML();
                solrIndexer.indexFromXmlString(solrInputStr, false);
                counters.incrementIndexed();
                report("");
                if ("application/pdf".equals(imgFullMime)) {
                    indexPagesFromPdf(pid, repositoryNode, counters);
                }
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

    private void indexPagesFromPdf(String pid, RepositoryNode repositoryNode, Counters counters) throws IOException, DocumentException, SolrServerException {
        report("object " + pid + " contains PDF, extracting pages");
        InputStream imgFull = repositoryConnector.getImgFull(pid);
        PdfExtractor extractor = new PdfExtractor(pid, imgFull);
        int pages = extractor.getPagesCount();
        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            counters.incrementFound();
            report("extracting page " + pageNumber + "/" + pages);
            String ocrText = normalizeWhitespacesForOcrText(extractor.getPageText(i));
            SolrInput solrInput = solrInputBuilder.processPageFromPdf(nodeManager, repositoryNode, pageNumber, ocrText);
            String solrInputStr = solrInput.getDocument().asXML();
            solrIndexer.indexFromXmlString(solrInputStr, false);
            counters.incrementIndexed();
            report("");
        }
    }

    private String normalizeWhitespacesForOcrText(String ocrText) {
        return ocrText == null ? null : ocrText
                // ("MAR-\nTIN", "MAR-\r\nTIN", "MAR-\n   TIN", "MAR-\n\tTIN", etc.) -> MARTIN
                .replaceAll("-\\r?\\n\\s*", "")
                // groups of white spaces -> " "
                .replaceAll("\\s+", " ");
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
