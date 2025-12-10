package cz.kramerius.searchIndex.indexer.execution;


import cz.incad.kramerius.utils.IterationUtils;
import cz.kramerius.searchIndex.indexer.SolrConfig;
import cz.kramerius.searchIndex.indexer.SolrIndexAccess;
import cz.kramerius.searchIndex.indexer.SolrInput;
import cz.kramerius.searchIndex.indexer.conversions.SolrInputBuilder;
import cz.kramerius.searchIndex.indexer.conversions.extraction.AudioAnalyzer;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNode;
import cz.kramerius.searchIndex.indexer.nodes.RepositoryNodeManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
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

    public static final int INDEXER_VERSION = 21; //this should be updated after every change in logic, that affects full indexation

    private final SolrConfig solrConfig;
    //only state variable
    private boolean shutDown = false;
    //helpers
    private final ReportLogger reportLogger;
    private final AkubraRepository akubraRepository;
    private final RepositoryNodeManager nodeManager;

    private final SolrInputBuilder solrInputBuilder;
    private SolrIndexAccess solrIndexer = null;

    private boolean ignoreInconsistentObjects=true;

    public static String getCompositeId(RepositoryNode repositoryNode, String pid) {
        String rootPid = (repositoryNode != null) ? repositoryNode.getRootPid() : "null";
        return rootPid + "!" + pid;
    }

    public static String getCompositeId(String rootPid, String pid) {
        return rootPid + "!" + pid;
    }

    public static void ensureCompositeId(SolrInput solrInput, String rootPid, String pid) {
        if (IterationUtils.useCompositeId()) {
            solrInput.addField("compositeId", getCompositeId(rootPid, pid));
        }
    }

    public static void ensureCompositeId(SolrInput solrInput, RepositoryNode repositoryNode, String pid) {
        if (IterationUtils.useCompositeId()) {
            solrInput.addField("compositeId", getCompositeId(repositoryNode, pid));
        }
    }

    public static void ensureCompositeId(SolrInputDocument solrInput, RepositoryNode repositoryNode, String pid) {
        if (IterationUtils.useCompositeId()) {
            solrInput.addField("compositeId", getCompositeId(repositoryNode, pid));
        }
    }

    public Indexer(AkubraRepository akubraRepository, SolrConfig solrConfig, OutputStream reportLoggerStream, boolean ignoreInconsistentObjects) {
        this.akubraRepository = akubraRepository;
        this.nodeManager = new RepositoryNodeManager(akubraRepository, ignoreInconsistentObjects);
        this.solrInputBuilder = new SolrInputBuilder();
        this.solrConfig = solrConfig;
        this.reportLogger = new ReportLogger(reportLoggerStream);
        this.ignoreInconsistentObjects = ignoreInconsistentObjects;
        init();
    }

    private void report(String message) {
        reportLogger.report(message);
        LOGGER.log(Level.INFO,message);
    }

    private void reportError(String message) {
        reportLogger.report(message);
        LOGGER.log(Level.SEVERE,message);
    }

    private void reportError(String message, Throwable e) {
        reportLogger.report(message, e);
        LOGGER.log(Level.SEVERE,e.getMessage(), e);
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
            reportError("Initialization error: TemplateException: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Initialization error", e);
            throw e;
        }
        report(" ");
    }



    public void indexByObjectPid(String pid, IndexationType type, Counters counters, boolean commitAfterPid, ProgressListener progressListener) {
        if (shutDown) {
            report("Indexer has already been shut down");
        } else {
            //Counters counters = new Counters();
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
            // commit only if necessary
            if (commitAfterPid) {
                commmit(counters);
            }

            report(" ");
            if (shutDown) {
                report("Indexer was shut down during execution");
            }
            //summary(pid, counters, start);
            if (progressListener != null) {
                progressListener.onFinished(counters.getProcessed());
            }
        }
    }

    public void summary(List<String> pids, Counters counters) {
        report("Summary* for " + pids);
        report("=======================================");
        report(" objects processed: " + counters.getProcessed());
        report(" objects indexed:   " + counters.getIndexed());
        report(" objects ignored:   " + counters.getIgnored());
        report(" objects removed:   " + counters.getRemoved());
        report(" objects erroneous: " + counters.getErrors());
        report(" *counters include pages from pdf, i.e. not real objects in repository");
        report(" records processing duration: " + formatTime(System.currentTimeMillis() - counters.getStartTimestamp()));
        report("=======================================");
        report("");

        if (counters.getErrors() > 0 || counters.getIgnored() >0) {
            throw new IllegalStateException("Indexation finished with errors; see error log");
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
            solrIndexer.setSingleFieldValue(pid, repositoryNode, "full_indexation_in_progress", Boolean.TRUE.toString(), true, false);
            solrIndexer.setSingleFieldValue(pid, repositoryNode, "indexer_version", String.valueOf(INDEXER_VERSION), true, false);
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
        solrIndexer.setSingleFieldValue(pid, repositoryNode, "full_indexation_in_progress", null, false, false);
    }


    /**
     * <para>Returns offset to the last word in the text and the last word, if it ends with a hyphen. Otherwise returns null.</para>
     * <para>Offset example: "This is a test text-" returns 0, "This is a test text-\n-32-" returns 5</para>
     * @param text
     * @return
     */
    private Pair<String,Integer> getLastWordAndOffsetIfHyphen(String text) {
        String[] words = text.split("\\s");
        int lenghtFromEnd = 0;
        for(int i = words.length - 1; i >= 0; i--) {
            String word = words[i].trim();
            //Is not empty, is at least 2 characters long, and all but the last character are letters. If the last one is hyphen, return the word, otherwise return null.
            if (!word.isEmpty() && word.length()>1 && word.substring(0,word.length()-1).chars().allMatch(Character::isLetter)) {
                if(word.endsWith("-")){
                    return Pair.of(word,lenghtFromEnd);
                }
                else return null;
            }
            lenghtFromEnd+=word.length()+1;
        }
        return null;
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

                if (this.ignoreInconsistentObjects) { //ignore missing objects
                    reportError("object not found in repository (or found in inconsistent state), ignoring: " + pid);
                    counters.incrementIgnored();
                } else { //remove missing objects from index
                    reportError("object not found in repository (or found in inconsistent state), removing from index: " + pid);
                    solrIndexer.deleteById(pid);
                    counters.incrementRemoved();
                }
                report("");
            } else {
                LOGGER.info("Indexing " + pid);
                Document foxmlDoc = akubraRepository.get(pid).asDom4j(true);
                report("model: " + repositoryNode.getModel());
                report("title: " + repositoryNode.getTitle());
                //the isOcrTextAvailable method (and for other datastreams) is inefficient for implementation through http stack (because of HEAD requests)
                //String ocrText = repositoryConnector.isOcrTextAvailable(pid) ? repositoryConnector.getOcrText(pid) : null;


                boolean ocrExists = akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_TEXT);
                String ocr = ocrExists ?  akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_TEXT).asString() : null;
                String ocrText = normalizeWhitespacesForOcrText(ocr);

                //Check if the last character of the last valid word in OCR text is a hyphen, and if so, check the next page for the first word
                if (ocrText != null) {
                    Pair<String,Integer> tuple = getLastWordAndOffsetIfHyphen(ocrText);
                    if(tuple != null) {
                        int pos = repositoryNode.getPositionInOwnParent();
                        List<String> syblings = nodeManager.getKrameriusNode(repositoryNode.getOwnParentPid()).getPidsOfOwnChildren();
                        if (syblings.size() > pos+1) {
                            String nextPid = syblings.get(pos + 1);
                            if (akubraRepository.datastreamExists(nextPid, KnownDatastreams.OCR_TEXT)) {
                                String nextOcrText = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_TEXT).asString();
                                if (nextOcrText != null) {
                                    String lastWord = tuple.getLeft();
                                    int offset = tuple.getRight();
                                    //Uncomment the following 2 lines if we normalise differently (Awaiting confirmation from the team)
                                    ocrText= ocrText.substring(0, ocrText.length()-1-offset)+nextOcrText.split("\\s+")[0]+" "+lastWord+ocrText.substring( ocrText.length()-offset);
                                }
                            }
                        }
                    }
                }

                ocrText = normalizeWhitespacesForOcrText(ocrText);
                //IMG_FULL mimetype
                String imgFullMime = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();

                Integer audioLength = "track".equals(repositoryNode.getModel()) ? detectAudioLength(repositoryNode.getPid()) : null;
                try {
                    SolrInput solrInput = solrInputBuilder.processObjectFromRepository(akubraRepository, foxmlDoc, ocrText, repositoryNode, nodeManager, imgFullMime, audioLength, setFullIndexationInProgress);
                    String solrInputStr = solrInput.getDocument().asXML();
                    solrIndexer.indexFromXmlString(solrInputStr, false);
                } catch (DocumentException e) {  //try to reindex without ocr - TODO: hack, ocr should be properly escaped
                    //typical root cause: Caused by: org.xml.sax.SAXParseException; lineNumber: 2; columnNumber: 2302; Character reference "&#6" is an invalid XML character.
                    SolrInput solrInput = solrInputBuilder.processObjectFromRepository(akubraRepository, foxmlDoc, "", repositoryNode, nodeManager, imgFullMime, audioLength, setFullIndexationInProgress);
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
            reportError(" Document error", e);
        } catch (IOException e) {
            counters.incrementErrors();
            reportError(" I/O error", e);
        } catch (SolrServerException e) {
            counters.incrementErrors();
            reportError(" Solr server error", e);
        } catch (SolrException e) {
            counters.incrementErrors();
            reportError(" Solr error", e);
        } catch (RuntimeException e) {
            counters.incrementErrors();
            reportError(" Runtime error", e);
        } finally {
            if (progressListener != null) {
                progressListener.onProgress(counters.getProcessed());
            }
        }
    }

    private Integer detectAudioLength(String pid) {
        try {
            AudioAnalyzer analyzer = new AudioAnalyzer();
            if (akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_WAV)) {
                InputStream inputStream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.AUDIO_WAV).asInputStream();
                AudioAnalyzer.Result result = analyzer.analyze(inputStream, AudioAnalyzer.Format.WAV);
                return result.duration;
            }
            System.out.println("failed to detect audio length of " + pid);
            return null;
        } catch (IOException | UnsupportedAudioFileException e) {
            LOGGER.log(Level.SEVERE,"error extracting audio length from " + pid, e);
            return null;
        }
    }

    private void indexPagesFromPdf(String pid, RepositoryNode repositoryNode, Counters counters) throws IOException, DocumentException, SolrServerException {
        report("object " + pid + " contains PDF, extracting pages");
        InputStream imgFull =akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL) ?  akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream() : null;
        PdfExtractor extractor = new PdfExtractor(pid.replaceAll(":","_"), imgFull);
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

    static String normalizeWhitespacesForOcrText(String ocrText) {
        return ocrText == null ? null : ocrText
                // ("MAR-\nTIN", "MAR-\r\nTIN", "MAR-\n   TIN", "MAR-\n\tTIN", etc.) -> MARTIN
                .replaceAll("-\\r?\\n\\s*", "")
                // groups of white spaces -> " "
                .replaceAll("\\s+", " ");
    }

    private void processChildren(String parentPid, RepositoryNode parentNode, Counters counters, IndexationType type, boolean isIndexationRoot, ProgressListener progressListener) {
        if (parentNode == null) {
            LOGGER.log(Level.SEVERE, "object not found in repository (or found in inconsistent state), ignoring it's children: " + parentPid);
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


    public void commmit(Counters counters) {
        try {
            solrIndexer.commit();
        } catch (IOException e) {
            counters.incrementErrors();
            reportError(" I/O error", e);
        } catch (SolrServerException e) {
            counters.incrementErrors();
            reportError(" Solr server error", e);
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
            reportError(" I/O error", e);
        } catch (SolrServerException e) {
            counters.incrementErrors();
            reportError(" Solr server error", e);
        } catch (SolrException e) {
            counters.incrementErrors();
            reportError(" Solr error", e);
        } catch (DocumentException e) {
            counters.incrementErrors();
            reportError(" Document error", e);
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
