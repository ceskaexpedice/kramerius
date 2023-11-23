package cz.kramerius.searchIndex.indexer.execution;


import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.SolrInputBuilder;
import cz.kramerius.searchIndex.indexer.conversions.extraction.AudioAnalyzer;
import cz.kramerius.krameriusRepositoryAccess.KrameriusRepositoryFascade;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNodeManager;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.dom4j.Document;
import org.dom4j.DocumentException;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Indexer {
    private static final Logger LOGGER = Logger.getLogger(Indexer.class.getName());

    public static final int INDEXER_VERSION = 19; //this should be updated after every change in logic, that affects full indexation

    private final SolrConfig solrConfig;
    //only state variable
    private boolean shutDown = false;
    //helpers
    private final ReportLogger reportLogger;
    private final KrameriusRepositoryFascade krameriusRepositoryFascade;
    private final RepositoryNodeManager nodeManager;

    private final SolrInputBuilder solrInputBuilder;
    private SolrIndexAccess solrIndexer = null;

    private boolean ignoreInconsistentObjects=true;

    public static boolean useCompositeId() {
        return KConfiguration.getInstance().getConfiguration().getBoolean("solrSearch.useCompositeId", false);
    }

    public static String getCompositeId(RepositoryNode repositoryNode, String pid) {
        String rootPid = (repositoryNode != null) ? repositoryNode.getRootPid() : "null";
        return rootPid + "!" + pid;
    }

    public static void ensureCompositeId(SolrInput solrInput, RepositoryNode repositoryNode, String pid) {
        if (useCompositeId()) {
            solrInput.addField("compositeId", getCompositeId(repositoryNode, pid));
        }
    }

    public static void ensureCompositeId(SolrInputDocument solrInput, RepositoryNode repositoryNode, String pid) {
        if (useCompositeId()) {
            solrInput.addField("compositeId", getCompositeId(repositoryNode, pid));
        }
    }

    public Indexer(KrameriusRepositoryFascade krameriusRepositoryFascade, SolrConfig solrConfig, OutputStream reportLoggerStream, boolean ignoreInconsistentObjects) {
        this.krameriusRepositoryFascade = krameriusRepositoryFascade;
        this.nodeManager = new RepositoryNodeManager(krameriusRepositoryFascade, ignoreInconsistentObjects);
        this.solrInputBuilder = new SolrInputBuilder();
        this.solrConfig = solrConfig;
        this.reportLogger = new ReportLogger(reportLoggerStream);
        this.ignoreInconsistentObjects = ignoreInconsistentObjects;
        init();
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
            solrIndexer = new SolrIndexAccess(solrConfig);
            report("SOLR API connector initialized");
        } catch (Throwable e) {
            report("Initialization error: TemplateException: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Initialization error", e);
            throw e;
        }
        report(" ");
    }


    public void indexByObjectPid(String pid, IndexationType type, ProgressListener progressListener) {
        if (shutDown) {
            report("Indexer has already been shut down");
        } else {
            long start = System.currentTimeMillis();
            Counters counters = new Counters();
            LOGGER.info("Processing " + pid + " (indexation type: " + type + ")");
            RepositoryNode node = nodeManager.getKrameriusNode(pid);
            boolean setFullIndexationInProgress = type == IndexationType.TREE_AND_FOSTER_TREES;
            if (node != null && setFullIndexationInProgress) {
                setFullIndexationInProgress(pid, node);
            }
            indexObjectWithCounters(pid, node, counters, setFullIndexationInProgress, progressListener);
            processChildren(pid, node, counters, type, true, progressListener);
            if (node != null && setFullIndexationInProgress) {
                clearFullIndexationInProgress(pid, node);
            }
            commitAfterLastIndexation(counters);

            report(" ");
            if (shutDown) {
                report("Indexer was shut down during execution");
            }
            report("Summary* for " + pid);
            report("=======================================");
            report(" objects processed: " + counters.getProcessed());
            report(" objects indexed:   " + counters.getIndexed());
            report(" objects ignored:   " + counters.getIgnored());
            report(" objects removed:   " + counters.getRemoved());
            report(" objects erroneous: " + counters.getErrors());
            report(" *counters include pages from pdf, i.e. not real objects in repository");
            report(" records processing duration: " + formatTime(System.currentTimeMillis() - start));
            report("=======================================");
            report("");
            if (progressListener != null) {
                progressListener.onFinished(counters.getProcessed());
            }
        }
    }

    private void setFullIndexationInProgress(String pid, RepositoryNode repositoryNode) {
        boolean alreadyInIndex = false;
        try {
            SolrDocument objectByPid = solrIndexer.getObjectByPid(pid);
            alreadyInIndex = objectByPid != null;
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

        if (alreadyInIndex) { // keep data from previous indexation in case this indexation fails during
            solrIndexer.setSingleFieldValue(pid, repositoryNode, "full_indexation_in_progress", Boolean.TRUE.toString(), false);
            solrIndexer.setSingleFieldValue(pid, repositoryNode, "indexer_version", String.valueOf(INDEXER_VERSION), false);
        } else {
            try {
                SolrInput solrInput = new SolrInput();
                solrInput.addField("pid", pid);
                solrInput.addField("full_indexation_in_progress", Boolean.TRUE.toString());
                solrInput.addField("indexer_version", String.valueOf(INDEXER_VERSION));
                ensureCompositeId(solrInput, repositoryNode, pid);
                String solrInputStr = solrInput.getDocument().asXML();
                solrIndexer.indexFromXmlString(solrInputStr, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void clearFullIndexationInProgress(String pid, RepositoryNode repositoryNode) {
        report("clearing field full_indexation_in_progress for " + pid);
        //will not work for objects that are not stored and not docValues
        //see https://github.com/ceskaexpedice/kramerius/issues/782
        solrIndexer.setSingleFieldValue(pid, repositoryNode, "full_indexation_in_progress", null, false);
    }

    private void indexObjectWithCounters(String pid, RepositoryNode repositoryNode, Counters counters, boolean setFullIndexationInProgress, ProgressListener progressListener) {
        try {
            counters.incrementProcessed();
            boolean objectAvailable = repositoryNode != null;
            if (!objectAvailable) {
                //TODO: hodit ignoreObjectsMissingFromRepository do konfigurace, nebo nastavit na false pri pouziti takove implementace KrameriusRepositoryAccessAdapter, ktera falesne neoznacuje existujici objekty v repozitari za chybejici
                //protoze vysledkem tehle zmeny (neexistujici/falesne neexistujici objekty budou v indexu ponechany v dosavadnim stavu, namisto smazani) muze byt skryta zastaralost v indexu, napr.:
                //monografie se muze jevit jako zaindexovana indexerem ve verzi třeba 15, ale obsahuje stranku, ktera preindexovana nebyla, nebyla ale ani zahozena a jeji zaznam v indexu je nenapadne zastaraly
                //podobne pro periodikum, pokud by nebyl dostupny zaznam cisla, nebude preindexovano a ani jeji stranky

                // Replaced ingoremissingFromRepository by ingore inconsistent object
                //boolean ignoreObjectsMissingFromRepository = true;
                if (this.ignoreInconsistentObjects) { //ignore missing objects
                    report("object not found in repository (or found in inconsistent state), ignoring: " + pid);
                    System.err.println("object not found in repository (or found in inconsistent state), ignoring: " + pid);
                    counters.incrementIgnored();
                } else { //remove missing objects from index
                    report("object not found in repository (or found in inconsistent state), removing from index: " + pid);
                    System.err.println("object not found in repository (or found in inconsistent state), removing from index: " + pid);
                    solrIndexer.deleteById(pid);
                    counters.incrementRemoved();
                }
                report("");
            } else {
                LOGGER.info("Indexing " + pid);
                Document foxmlDoc = krameriusRepositoryFascade.getObjectFoxml(pid, true);
                report("model: " + repositoryNode.getModel());
                report("title: " + repositoryNode.getTitle());
                //the isOcrTextAvailable method (and for other datastreams) is inefficient for implementation through http stack (because of HEAD requests)
                //String ocrText = repositoryConnector.isOcrTextAvailable(pid) ? repositoryConnector.getOcrText(pid) : null;
                String ocrText = normalizeWhitespacesForOcrText(krameriusRepositoryFascade.getOcrText(pid));
                //System.out.println("ocr: " + ocrText);
                //IMG_FULL mimetype
                String imgFullMime = krameriusRepositoryFascade.getImgFullMimetype(pid);

                Integer audioLength = "track".equals(repositoryNode.getModel()) ? detectAudioLength(repositoryNode.getPid()) : null;
                try {
                    SolrInput solrInput = solrInputBuilder.processObjectFromRepository(foxmlDoc, ocrText, repositoryNode, nodeManager, imgFullMime, audioLength, setFullIndexationInProgress);
                    String solrInputStr = solrInput.getDocument().asXML();
                    solrIndexer.indexFromXmlString(solrInputStr, false);
                } catch (DocumentException e) {  //try to reindex without ocr - TODO: hack, ocr should be properly escaped
                    //typical root cause: Caused by: org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 2302; Character reference "&#6" is an invalid XML character.
                    SolrInput solrInput = solrInputBuilder.processObjectFromRepository(foxmlDoc, "", repositoryNode, nodeManager, imgFullMime, audioLength, setFullIndexationInProgress);
                    String solrInputStr = solrInput.getDocument().asXML();
                    solrIndexer.indexFromXmlString(solrInputStr, false);
                }
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
        } catch (SolrException e) {
            counters.incrementErrors();
            report(" Solr error", e);
        } finally {
            if (progressListener != null) {
                progressListener.onProgress(counters.getProcessed());
            }
        }
    }

    private Integer detectAudioLength(String pid) {
        try {
            AudioAnalyzer analyzer = new AudioAnalyzer();
            if (krameriusRepositoryFascade.isAudioWavAvailable(pid)) {
                AudioAnalyzer.Result result = analyzer.analyze(krameriusRepositoryFascade.getAudioWav(pid), AudioAnalyzer.Format.WAV);
                return result.duration;
            }
            System.out.println("failed to detect audio length of " + pid);
            return null;
        } catch (IOException | UnsupportedAudioFileException e) {
            System.err.println("error extracting audio length from " + pid);
            e.printStackTrace();
            return null;
        }
    }

    private void indexPagesFromPdf(String pid, RepositoryNode repositoryNode, Counters counters) throws IOException, DocumentException, SolrServerException {
        report("object " + pid + " contains PDF, extracting pages");
        InputStream imgFull = krameriusRepositoryFascade.getImgFull(pid);
        PdfExtractor extractor = new PdfExtractor(pid, imgFull);
        int pages = extractor.getPagesCount();
        for (int i = 0; i < pages; i++) {
            int pageNumber = i + 1;
            counters.incrementProcessed();
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

    private void processChildren(String parentPid, RepositoryNode parentNode, Counters counters, IndexationType type, boolean isIndexationRoot, ProgressListener progressListener) {
        if (parentNode == null) {
            System.err.println("object not found in repository (or found in inconsistent state), ignoring it's children: " + parentPid);
            return;
        }
        switch (type) {
            case OBJECT: {
                //nothing
            }
            break;
            case OBJECT_AND_CHILDREN: {
                if (isIndexationRoot) {
                    for (String childPid : parentNode.getPidsOfOwnChildren()) {
                        RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                        indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                    }
                    for (String childPid : parentNode.getPidsOfFosterChildren()) {
                        RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                        indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index foster child
                    }
                }
            }
            break;
            case TREE: {
                for (String childPid : parentNode.getPidsOfOwnChildren()) { //index own children
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                    processChildren(childPid, childNode, counters, type, false, progressListener); //process own child's tree
                }
            }
            break;
            case TREE_INDEX_ONLY_NEWER: {
                for (String childPid : parentNode.getPidsOfOwnChildren()) { //index own children
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    boolean isNewer = true; //TODO: detect
                    if (isNewer) {
                        indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                    }
                    processChildren(childPid, childNode, counters, type, false, progressListener); //process own child's tree
                }
            }
            break;
            case TREE_PROCESS_ONLY_NEWER: {
                for (String childPid : parentNode.getPidsOfOwnChildren()) { //index own children
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    boolean isNewer = true; //TODO: detect
                    if (isNewer) {
                        indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                        processChildren(childPid, childNode, counters, type, false, progressListener); //process own child's tree
                    }
                }
            }
            break;
            case TREE_INDEX_ONLY_PAGES: {
                for (String childPid : parentNode.getPidsOfOwnChildren()) { //index own children
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    boolean isPage = true; //TODO: detect
                    if (isPage) {
                        indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                    } else {
                        processChildren(childPid, childNode, counters, type, false, progressListener); //process own child's tree
                    }
                }
            }
            break;
            case TREE_INDEX_ONLY_NONPAGES: {
                for (String childPid : parentNode.getPidsOfOwnChildren()) { //index own children
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    boolean isPage = false; //TODO: detect
                    if (!isPage) {
                        indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                        processChildren(childPid, childNode, counters, type, false, progressListener); //process own child's tree
                    }
                }
            }
            break;
            case TREE_AND_FOSTER_TREES: {
                for (String childPid : parentNode.getPidsOfOwnChildren()) {
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index own child
                    processChildren(childPid, childNode, counters, type, false, progressListener); //process own child's tree
                }
                for (String childPid : parentNode.getPidsOfFosterChildren()) {
                    RepositoryNode childNode = nodeManager.getKrameriusNode(childPid);
                    indexObjectWithCounters(childPid, childNode, counters, false, progressListener); //index foster child
                    processChildren(childPid, childNode, counters, type, false, progressListener); //process foster child's tree
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
    public void indexDoc(Document solrDoc, ProgressListener progressListener) {
        List<Document> singleItemList = new ArrayList<>();
        singleItemList.add(solrDoc);
        indexDocBatch(singleItemList, progressListener);
    }

    @Deprecated
    public void indexDocBatch(List<Document> solrDocs, ProgressListener progressListener) {
        long start = System.currentTimeMillis();
        Counters counters = new Counters();
        report("Processing " + solrDocs.size() + " records");
        //int limit = 3;
        report("==============================");
        for (Document doc : solrDocs) {
            if (shutDown) {
                report(" stopped ");
                break;
            }
            index(doc, counters, null, false);
        }
        report(" ");
        if (shutDown) {
            report("Indexer was shut down during execution");
        }
        report("Summary");
        report("=======================================");
        report(" records processed: " + counters.getProcessed());
        report(" records indexed  : " + counters.getIndexed());
        report(" records erroneous: " + counters.getErrors());
        report(" records processing duration: " + formatTime(System.currentTimeMillis() - start));
        report("=======================================");
        if (progressListener != null) {
            progressListener.onFinished(counters.getProcessed());
        }
    }

    private void index(Document solrDoc, Counters counters, ProgressListener progressListener, boolean explicitCommit) {
        try {
            counters.incrementProcessed();
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
            progressListener.onProgress(counters.getProcessed());
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
        //report("  Login: " + solrConfig.login);
        report(" ");
    }

    public void shutDown() {
        shutDown = true;
    }

    public void close() {
        reportLogger.close();
    }
}