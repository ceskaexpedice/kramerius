package org.kramerius;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.qbizm.kramerius.imptool.poc.convertor.BaseConvertor;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.new_api.ProcessScheduler;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.service.FOXMLAppendLicenseService;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.PathEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.RepositoryNamespaces;
import org.ceskaexpedice.akubra.pid.LexerException;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.utils.XMLUtils.*;
import static org.ceskaexpedice.akubra.RepositoryNamespaces.DC_NAMESPACE_URI;
import org.w3c.dom.DOMException;


/**
 * Import
 */
public class Import {

    public static final String NON_KEYWORD = "-none-";


    static int counter = 0;
    private static final Logger log = Logger.getLogger(Import.class.getName());

    // only syncronization object
    private final static Object marshallingLock = new Object();

    private static Unmarshaller unmarshaller = null;
    private static List<String> classicRootModels = null; //top-level models, not including convolutes
    private static SortingService sortingService;
    private static Map<String, List<String>> updateMap = new HashMap<>();

    private static String imgTreePath = "";
    private static String imgTreeUrl = "";
    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
            unmarshaller = jaxbContext.createUnmarshaller();
            JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            jaxbdatastreamContext.createMarshaller();
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.newDocumentBuilder();
        } catch (JAXBException | ParserConfigurationException e) {
            log.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
        List<String> crm = Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.topLevelModels"));
        classicRootModels = (crm!=null) ? crm : new ArrayList<>();
        
        if (useImageServer()) {
            setImgTree(); //set imgTreePath and imgTreeUrl for export to imageserver
        }

    }

    /**
     * args[0] - authToken
     * args[1] - import dir, optional
     * args[2] - start indexer, optional
     */
    public static void main(String[] args) throws IOException, RepositoryException, SolrServerException {
        /*for (int i = 0; i < args.length; i++) {
            System.out.println("arg " + i + ": " + args[i]);
        }*/
        if (args.length < 1) {
            throw new RuntimeException("Not enough arguments.");
        }
        int argsIndex = 0;
        //token for keeping possible following processes in same batch
        String authToken = args[argsIndex++]; //auth token always second, but still suboptimal solution, best would be if it was outside the scope of this as if ProcessHelper.scheduleProcess() similarly to changing name (ProcessStarter)
        //process params
        String importDirFromArgs = args.length > argsIndex ? args[argsIndex++] : null;
        log.info(String.format("Import directory %s", importDirFromArgs));


        Boolean startIndexerFromArgs = args.length > argsIndex ? Boolean.valueOf(args[argsIndex++]) : null;

        String license = null;
        String addCollection = null;
        if (startIndexerFromArgs != null && startIndexerFromArgs) {
            license = args.length > argsIndex ? args[argsIndex++] : null;
            addCollection = args.length > argsIndex ? args[argsIndex++] : null;
        }


        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule(), new ImportModule());
        AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
        SortingService sortingServiceLocal = injector.getInstance(SortingService.class);
        FOXMLAppendLicenseService foxmlService = injector.getInstance(FOXMLAppendLicenseService.class);


        //priority: 1. args, 2. System property, 3. KConfiguration, 4. explicit defalut value
        String importDirectory = KConfiguration.getInstance().getProperty("import.directory");
        if (importDirFromArgs != null) {
            importDirectory = importDirFromArgs;
        } else if (System.getProperties().containsKey("import.directory")) {
            importDirectory = System.getProperty("import.directory");
        }

        Boolean startIndexer = Boolean.valueOf(KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer", "true"));
        if (startIndexerFromArgs != null) {
            startIndexer = startIndexerFromArgs;
        } else if (System.getProperties().containsKey("ingest.startIndexer")) {
            startIndexer = Boolean.valueOf(System.getProperty("ingest.startIndexer"));
        }


        try {
            if (license != null && !license.equals(NON_KEYWORD)) {

                File importFolder = new File(importDirectory);
                File importParentFolder = importFolder.getParentFile();
                File licensesImportFile = new File(importParentFolder, importFolder.getName() + "-" + license);
                licensesImportFile.mkdirs();
                log.info(String.format("Copy data from  %s to %s", importFolder.getAbsolutePath(), licensesImportFile.getAbsolutePath()));
                FileUtils.copyDirectory(importFolder, licensesImportFile);


                log.info(String.format("Applying license %s", license));
                try {
                    foxmlService.appendLicense(licensesImportFile.getAbsolutePath(), license);
                } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException |
                         LexerException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }

                ProcessStarter.updateName(String.format("Import FOXML z %s ", importDirectory));
                log.log(Level.INFO, "import dir: {0}", licensesImportFile);
                log.log(Level.INFO, "start indexer: {0}", startIndexer);
                log.log(Level.INFO, "license : {0}", license);

                Import.run(akubraRepository, akubraRepository.pi(), sortingServiceLocal, KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), licensesImportFile.getAbsolutePath(), startIndexer, authToken, addCollection);

                log.info(String.format("Deleting import folder %s", licensesImportFile));
                FileUtils.deleteDirectory(licensesImportFile);

            } else {

                ProcessStarter.updateName(String.format("Import FOXML z %s ", importDirectory));
                log.log(Level.INFO, "import dir: {0}", importDirectory);
                log.log(Level.INFO, "start indexer: {0}", startIndexer);

                Import.run(akubraRepository, akubraRepository.pi(), sortingServiceLocal, KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), importDirectory, startIndexer, authToken, addCollection);
            }
        }finally {
            akubraRepository.shutdown();
        }
    }

    public static void run(AkubraRepository akubraRepository, ProcessingIndex feeder, SortingService sortingServiceParam, final String url, final String user, final String pwd, String importRoot) throws IOException, SolrServerException {
        run(akubraRepository, feeder, sortingServiceParam, url, user, pwd, importRoot, true, null, null);
    }

    public static void run(AkubraRepository akubraRepository, ProcessingIndex feeder, SortingService sortingServiceParam, final String url, final String user, final String pwd, String importRoot, boolean startIndexer, String authToken, String addcollections) throws IOException, SolrServerException {
        log.log(Level.INFO, "INGEST - url:{0} user:{1} importRoot:{2}", new Object[]{url, user, importRoot});
        sortingService = sortingServiceParam;
        // system property 
        try {
            String skipIngest = System.getProperties().containsKey("ingest.skip") ? System.getProperty("ingest.skip") : KConfiguration.getInstance().getConfiguration().getString("ingest.skip", "false");
            if (Boolean.parseBoolean(skipIngest)) {
                log.info("INGEST CONFIGURED TO BE SKIPPED, RETURNING");
                return;
            }

            boolean updateExisting = Boolean.parseBoolean(System.getProperties().containsKey("ingest.updateExisting") ? System.getProperty("ingest.updateExisting") : KConfiguration.getInstance().getConfiguration().getString("ingest.updateExisting", "false"));
            log.log(Level.INFO, "INGEST updateExisting: {0}", updateExisting);


            long start = System.currentTimeMillis();

            File importFile = new File(importRoot);
            if (!importFile.exists()) {
                log.log(Level.SEVERE, "Import root folder or control file doesn''t exist: {0}", importFile.getAbsolutePath());
                throw new RuntimeException("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
            }

            initialize(user, pwd);

            ClassicRootMap classicRoots = new ClassicRootMap();
            Set<TitlePidTuple> convolutes = new HashSet<>();
            Set<TitlePidTuple> collections = new HashSet<>();

            Set<String> sortRelations = new HashSet<>();
            if (importFile.isDirectory()) {
                visitAllDirsAndFiles(akubraRepository, importFile, classicRoots, convolutes, collections, sortRelations, updateExisting);
            } else {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(importFile));
                } catch (FileNotFoundException e) {
                    log.log(Level.SEVERE, "Import file list {0} not found: {1}", new Object[]{importFile, e});
                    throw new RuntimeException(e);
                }
                try {
                    for (String line; (line = reader.readLine()) != null; ) {
                        if ("".equals(line)) {
                            continue;
                        }
                        File importItem = new File(line);
                        if (!importItem.exists()) {
                            log.log(Level.SEVERE, "Import folder doesn''t exist: {0}", importItem.getAbsolutePath());
                            continue;
                        }
                        if (!importItem.isDirectory()) {
                            log.log(Level.SEVERE, "Import item is not a folder: {0}", importItem.getAbsolutePath());
                            continue;
                        }
                        log.log(Level.INFO, "Importing {0}", importItem.getAbsolutePath());
                        visitAllDirsAndFiles(akubraRepository, importItem, classicRoots, convolutes, collections, sortRelations, updateExisting);
                    }
                    reader.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Exception reading import list file: {0}", e);
                    throw new RuntimeException(e);
                }
            }
            log.log(Level.INFO, "FINISHED INGESTION IN {0}s, processed {1} files", new Object[]{(System.currentTimeMillis() - start) / 1000.0, counter});

            String startSortProperty = System.getProperties().containsKey("ingest.sortRelations") ? System.getProperty("ingest.sortRelations") : KConfiguration.getInstance().getConfiguration().getString("ingest.sortRelations", "true");
            if (Boolean.parseBoolean(startSortProperty)) {


                if (sortRelations.isEmpty()) {
                    log.info("NO MERGED OBJECTS FOR RELATIONS SORTING FOUND.");
                } else {
                    for (String sortPid : sortRelations) {
                        sortingService.sortRelations(sortPid, false);
                    }
                    log.info("ALL MERGED OBJECTS RELATIONS SORTED.");
                }
            } else {
                log.info("RELATIONS SORTING DISABLED.");
            }

            if (startIndexer) {

                List<String> addCollectionList = new ArrayList<>();
                if (StringUtils.isAnyString(addcollections)) {
                    Arrays.stream(addcollections.split(";")).forEach(addCollectionList::add);
                    for (String pid : addCollectionList) {
                        if (!pid.trim().equals(NON_KEYWORD)) {
                            addCollection(akubraRepository, pid, classicRoots, collections, authToken);
                        }
                    }
                }


                if (collections.isEmpty()) {
                    log.info("NO COLLECTIONS FOR INDEXING FOUND.");
                } else {
                    try {
                        String waitIndexerProperty = System.getProperties().containsKey("ingest.startIndexer.wait") ? System.getProperty("ingest.startIndexer.wait") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer.wait", "1000");
                        // should wait
                        log.log(Level.INFO, "Waiting for soft commit :{0} s", waitIndexerProperty);
                        Thread.sleep(Integer.parseInt(waitIndexerProperty));

                        if (authToken != null) {
                            for (TitlePidTuple col : collections) {

                                ProcessScheduler.scheduleIndexation(col.pid, col.title, false, authToken);
                            }
                            log.info("ALL COLLECTIONS SCHEDULED FOR INDEXING.");
                        } else {
                            log.warning("cannot schedule indexation due to missing process credentials");
                        }
                    } catch (InterruptedException | NumberFormatException e) {
                        log.log(Level.WARNING, e.getMessage(), e);
                    }
                }


                if (convolutes.isEmpty()) {
                    log.info("NO CONVOLUTES FOR INDEXING FOUND.");
                } else {
                    try {
                        String waitIndexerProperty = System.getProperties().containsKey("ingest.startIndexer.wait") ? System.getProperty("ingest.startIndexer.wait") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer.wait", "1000");
                        // should wait
                        log.log(Level.INFO, "Waiting for soft commit :{0} s", waitIndexerProperty);
                        Thread.sleep(Integer.parseInt(waitIndexerProperty));

                        if (authToken != null) {
                            for (TitlePidTuple convolute : convolutes) {
                                ProcessScheduler.scheduleIndexation(convolute.pid, convolute.title, false, authToken);
                            }
                            log.info("ALL CONVOLUTES SCHEDULED FOR INDEXING.");
                        } else {
                            log.warning("cannot schedule indexation due to missing process credentials");
                        }
                    } catch (InterruptedException | NumberFormatException e) {
                        log.log(Level.WARNING, e.getMessage(), e);
                    }
                }

                if (classicRoots.isEmpty()) {
                    log.info("NO ROOT OBJECTS FOR INDEXING FOUND.");
                } else {
                    try {
                        String waitIndexerProperty = System.getProperties().containsKey("ingest.startIndexer.wait") ? System.getProperty("ingest.startIndexer.wait") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer.wait", "1000");
                        // should wait
                        log.log(Level.INFO, "Waiting for soft commit :{0} s", waitIndexerProperty);
                        Thread.sleep(Integer.parseInt(waitIndexerProperty));

                        if (authToken != null) {
                            for (TitlePidTuple root : classicRoots) {

                                if (akubraRepository.exists(root.pid)) {//Here could be check if we skip the indexation.
                                    ProcessScheduler.scheduleIndexation(root.pid, root.title, true, authToken);
                                } else {
                                    LOGGER.warning(String.format("Object '%s' does not exist in the repository. ", root.pid));
                                }

                            }
                            log.info("ALL ROOT OBJECTS SCHEDULED FOR INDEXING.");
                        } else {
                            log.warning("cannot schedule indexation due to missing process credentials");
                        }
                    } catch (InterruptedException | NumberFormatException e) {
                        log.log(Level.WARNING, e.getMessage(), e);
                    }
                }

            } else {
                log.info("AUTO INDEXING DISABLED.");
            }
        } finally {
            if (feeder != null) {
                feeder.commit();
            }
        }

    }

    public static void initialize(final String user, final String pwd) {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pwd.toCharArray());
            }
        });
    }

    private static void visitAllDirsAndFiles(AkubraRepository akubraRepository, File importFile, ClassicRootMap classicRoots,
                                             Set<TitlePidTuple> convolutes,
                                             Set<TitlePidTuple> collections,
                                             Set<String> sortRelations, boolean updateExisting) {
        if (importFile == null) {
            return;
        }
        if (importFile.isDirectory()) {

            File[] children = importFile.listFiles();

            for (File f : children) {
                if ("update.list".equalsIgnoreCase(f.getName())) {
                    log.log(Level.INFO, "File update.list detected in folder {0}", importFile);
                    parseUpdateList(f);
                }
            }

            if (children.length > 1 && children[0].isDirectory()) {//Issue 36
                Arrays.sort(children);
            }
            for (File child : children) {
                visitAllDirsAndFiles(akubraRepository, child, classicRoots, convolutes, collections, sortRelations, updateExisting);
            }
        } else {
            DigitalObject dobj;
            try {
                if (!importFile.getName().toLowerCase().endsWith(".xml")) {
                    return;
                }
                // must be syncrhonized
                synchronized (marshallingLock) {
                    Object obj = unmarshaller.unmarshal(importFile);
                    dobj = (DigitalObject) obj;
                }
            } catch (JAXBException e) {
                log.log(Level.WARNING, "Skipping file {0} - not an FOXML object. ({1})", new Object[]{importFile.getName(), e});
                log.log(Level.WARNING, "Underlying error was:", e);
                return;
            }
            try {
                if (updateMap.containsKey(dobj.getPID())) {
                    log.log(Level.INFO, "Updating datastreams {0} in object {1}", new Object[]{updateMap.get(dobj.getPID()), dobj.getPID()});
                    List<DatastreamType> importedDatastreams = dobj.getDatastream();
                    List<String> datastreamsToUpdate = updateMap.get(dobj.getPID());
                    for (String dsName : datastreamsToUpdate) {
                        for (DatastreamType ds : importedDatastreams) {
                            if (dsName.equalsIgnoreCase(ds.getID())) {
                                log.log(Level.INFO, "Updating datastream {0}", ds.getID());
                                DatastreamVersionType dsversion = ds.getDatastreamVersion().get(0);
                                if (dsversion.getXmlContent() != null) {
                                    Element element = dsversion.getXmlContent().getAny().get(0);
                                    if (dsName.equals(FedoraUtils.DC_STREAM)) {
                                        String rights = DCUtils.rightsFromDC(element);
                                        if (rights != null) {
                                            Element elm = findElement(element, "rights", DC_NAMESPACE_URI);
                                            if (elm == null) {
                                                elm = element.getOwnerDocument().createElementNS(DC_NAMESPACE_URI, "rights");
                                                element.appendChild(elm);
                                            }
                                            elm.setTextContent(rights);
                                        }
                                    }


                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                    try {
                                        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(element), new StreamResult(outputStream));
                                    } catch (TransformerException e) {
                                        throw new RuntimeException(e);
                                    }

                                    final DigitalObject transactionDigitalObject = dobj;


                                    String mimeType = akubraRepository.getDatastreamMetadata(transactionDigitalObject.getPID(), ds.getID()).getMimetype();
                                    akubraRepository.updateXMLDatastream(transactionDigitalObject.getPID(), ds.getID(), mimeType, new ByteArrayInputStream(outputStream.toByteArray()));
                                } else if (dsversion.getBinaryContent() != null) {
                                    throw new RuntimeException("Update of managed binary datastream content is not supported.");
                                } else if (dsversion.getContentLocation() != null) {

                                    final DigitalObject transactionDigitalObject = dobj;
                                    String mimeType = akubraRepository.getDatastreamMetadata(transactionDigitalObject.getPID(), ds.getID()).getMimetype();
                                    akubraRepository.updateRedirectedDatastream(transactionDigitalObject.getPID(), ds.getID(), dsversion.getContentLocation().getREF(), mimeType);
                                }
                            }
                        }
                    }
                    if (classicRoots != null) {
                        classicRoots.add(new TitlePidTuple("", dobj.getPID()));
                        log.log(Level.INFO, "Added updated object for indexing:{0}", dobj.getPID());
                        //NOTE: inefficient for updated convolutes, everyting new/changed inside it will be indexed twice
                    }
                } else {
                    final DigitalObject transactionDigitalObject = dobj;

                    ingest(akubraRepository, importFile, sortRelations, classicRoots, updateExisting);
                    checkModelIsClassicRoot(transactionDigitalObject, classicRoots);
                    checkModelIsConvoluteOrCollection(transactionDigitalObject, convolutes, collections, classicRoots);
                }
            } catch (RuntimeException | TransformerFactoryConfigurationError t) {
                log.log(Level.SEVERE, "Error when ingesting PID: {0}, {1}", new Object[]{dobj.getPID(), t.getMessage()});
                throw new RuntimeException(t);
            }
        }
    }

    private static void parseUpdateList(File listFile) {
        try (BufferedReader reader= new BufferedReader(new FileReader(listFile));) {
            for (String line; (line = reader.readLine()) != null; ) {
                if ("".equals(line.trim()) || line.trim().startsWith("#")) {
                    continue;
                }
                String[] lineItems = line.split(" ");
                if (lineItems.length < 2) {
                    continue;
                }
                List<String> streams = new ArrayList<>(lineItems.length - 1);
                for (int i = 0; i < lineItems.length - 1; i++) {
                    if (!"".equals(lineItems[i + 1])) {
                        streams.add(lineItems[i + 1]);
                    }
                }
                updateMap.put(lineItems[0], streams);
            }
        } catch (FileNotFoundException e) {
            log.log(Level.SEVERE, "update.list file {0} not found: {1}", new Object[]{listFile, e});
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception reading update.list file: {0}", e);
            throw new RuntimeException(e);
        }
    }

    public static void ingest(AkubraRepository akubraRepository, InputStream is, String filename, Set<String> sortRelations, ClassicRootMap roots, boolean updateExisting) throws IOException, RepositoryException, JAXBException, LexerException, TransformerException {
        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(is, bos);
        byte[] bytes = bos.toByteArray();
        DigitalObject obj;
        try {
            synchronized (marshallingLock) {
                obj = (DigitalObject) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
            }
        } catch (JAXBException e) {
            log.log(Level.INFO, "Skipping file {0} - not an FOXML object.", filename);
            log.log(Level.INFO, "Underlying error was:", e);
            return;
        }
        if (useImageServer()) {
            convertForImageServer(obj);
        }
        String pid = obj.getPID();
        DigitalObject finalObj = obj;
        akubraRepository.doWithWriteLock(pid, () -> {
            try {
                akubraRepository.ingest(finalObj);
            } catch (RepositoryException sfex) {
                if (objectExists(akubraRepository, pid)) {
                    if (updateExisting) {
                        log.log(Level.INFO, "Replacing existing object {0}", pid);
                        try {
                            akubraRepository.delete(pid, true, false);
                            log.log(Level.INFO, "purged old object {0}", pid);
                        } catch (Exception ex) {
                            log.log(Level.SEVERE, "Cannot purge object {0}, skipping: {1}", new Object[]{pid, ex});
                            throw new RuntimeException(ex);
                        }
                        try {
                            if (finalObj != null) {
                                akubraRepository.ingest(finalObj);
                            }
                            log.log(Level.INFO, "Ingested new object {0}", pid);
                        } catch (RepositoryException rsfex) {
                            log.log(Level.SEVERE, "Replace ingest SOAP fault:{0}", rsfex);
                            throw new RuntimeException(rsfex);
                        }
                        if (roots != null) {
                            roots.add(new TitlePidTuple("", pid));
                            log.log(Level.INFO, "Added replaced object for indexing:{0}", pid);
                        }
                    } else {
                        log.log(Level.INFO, "Merging with existing object {0}", pid);
                        if (merge(akubraRepository, bytes)) {
                            if (sortRelations != null) {
                                sortRelations.add(pid);
                                log.log(Level.INFO, "Added merged object for sorting relations:{0}", pid);
                            }
                            if (roots != null) {
                                roots.add(new TitlePidTuple("", pid));
                                log.log(Level.INFO, "Added merged object for indexing:{0}", pid);
                            }
                        }
                    }
                } else {
                    log.log(Level.SEVERE, "Ingest fault:{0}", sfex);
                    throw new RuntimeException(sfex);
                }
            }
            return null;
        });

        counter++;
        log.log(Level.INFO, "Ingested:{0} in {1}ms, count:{2}", new Object[]{pid, System.currentTimeMillis() - start, counter});
    }

    public static void ingest(AkubraRepository repo, File file, Set<String> sortRelations, ClassicRootMap roots, boolean updateExisting) {
        try (FileInputStream is = new FileInputStream(file)) {
            ingest(repo, is, file.getName(), sortRelations, roots, updateExisting);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Ingestion error ", ex);
            throw new RuntimeException(ex);
        }
    }

    private static boolean merge(AkubraRepository repo, byte[] ingestedBytes) throws RepositoryException {
        List<RDFTuple> ingested = readRDF(ingestedBytes);
        if (ingested.isEmpty()) {
            return false;
        }
        String pid = ingested.get(0).subject.substring("info:fedora/".length());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            DigitalObject existingObject = repo.get(pid).asDigitalObject();
            if (existingObject == null) {
                throw new IllegalStateException("Cannot merge object: " + pid + " - object not in repository");
            }
            if (!repo.re().exists(pid)) {
                throw new IllegalStateException("Cannot merge object: " + pid + " - object does not have RELS-EXT stream");
            }
            IOUtils.copy(repo.re().get(pid).asInputStream(), bos);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot copy streams in merge", e);
            throw new RuntimeException(e);
        }
        byte[] existingBytes = bos.toByteArray();
        List<RDFTuple> existing = readRDF(existingBytes);
        ingested.removeAll(existing);

        boolean touched = false;
        for (RDFTuple t : ingested) {
            if (t.object != null) {
                try {
                    if (t.literal) {
                        repo.re().addLiteral(pid, t.predicate, t.namespace, t.object);
                    } else {
                        repo.re().addRelation(pid, t.predicate, t.namespace, t.object);
                    }
                    //port.addRelationship(t.subject.substring("info:fedora/".length()), t.predicate, t.object, t.literal, null);
                    touched = true;
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "WARNING - could not add relationship:" + t + "(" + ex + ")", ex);
                }
            }
        }
        return touched;
    }

    public static boolean useImageServer() {
        return System.getProperties().containsKey("convert.useImageServer")
                ? Boolean.parseBoolean(System.getProperty("convert.useImageServer")) :
                KConfiguration.getInstance().getConfiguration().getBoolean("convert.useImageServer", false);
    }

    public static void setImgTree() {
        Calendar now = Calendar.getInstance();
        String year = String.format("%04d", now.get(Calendar.YEAR));
        String month = String.format("%02d", now.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", now.get(Calendar.DAY_OF_MONTH));
        imgTreePath = System.getProperty("file.separator") + year + System.getProperty("file.separator") + month + System.getProperty("file.separator") + day;
        imgTreeUrl = "/" + year + "/" + month + "/" + day;

    }

    public static String getImgTreePath() {
        return imgTreePath;
    }

    public static String getImgTreeUrl() {
        return imgTreeUrl;
    }

    private static void convertForImageServer(DigitalObject obj) {
        for (DatastreamType ds : obj.getDatastream()) {
            if ("IMG_FULL".equals(ds.getID()) && "M".equals(ds.getCONTROLGROUP())) {
                List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                if (versions != null) {
                    DatastreamVersionType ver = versions.get(versions.size() - 1);
                    if ("image/jp2".equals(ver.getMIMETYPE())) {
                        byte[] binaryContent = ver.getBinaryContent();
                        String filename = obj.getPID().replace("uuid:", "") + ".jp2";
                        convertFullStream(binaryContent, filename, obj, ds, ver);
                    }
                }
            }
        }
    }

    private static void convertFullStream(byte[] img, String filename, DigitalObject obj, DatastreamType stream, DatastreamVersionType version) {
        try {
            stream.setCONTROLGROUP("E");
            stream.setVERSIONABLE(false);
            stream.setSTATE(StateType.A);
            version.setCREATED(BaseConvertor.getCurrentXMLGregorianCalendar());

            String externalImagesDirectory = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerDirectory");
            String binaryDirectory = externalImagesDirectory + getImgTreePath();
            File dir = cz.incad.kramerius.utils.IOUtils.checkDirectory(binaryDirectory);
            // Write file to imageserver directory
            File target = new File(dir, filename);
            Files.write(target.toPath(), img);
            ContentLocationType cl = new ContentLocationType();
            String tilesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerTilesURLPrefix") + getImgTreeUrl();
            String imagesPrefix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerImagesURLPrefix") + getImgTreeUrl();
            String suffix = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.big");

            if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.imageServerSuffix.removeFilenameExtensions", false)) {
                String pageFileNameWithoutExtension = FilenameUtils.removeExtension(filename);
                cl.setREF(imagesPrefix + "/" + PathEncoder.encPath(pageFileNameWithoutExtension) + suffix);

                //Adjust RELS-EXT
                String suffixTiles = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.tiles");
                adjustRELSEXT(obj, tilesPrefix + "/" + PathEncoder.encPath(pageFileNameWithoutExtension) + suffixTiles);
            } else {
                cl.setREF(imagesPrefix + "/" + PathEncoder.encPath(filename) + suffix);

                //Adjust RELS-EXT
                String suffixTiles = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerSuffix.tiles");
                adjustRELSEXT(obj, tilesPrefix + "/" + PathEncoder.encPath(filename) + suffixTiles);
            }
            cl.setTYPE("URL");
            version.setContentLocation(cl);
            version.setBinaryContent(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void adjustRELSEXT(DigitalObject obj, String tilesUrlValue) {
        for (DatastreamType ds : obj.getDatastream()) {
            if ("RELS-EXT".equals(ds.getID())) {
                List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                if (versions != null) {
                    DatastreamVersionType ver = versions.get(versions.size() - 1);
                    XmlContentType xmlContent = ver.getXmlContent();
                    Node element = xmlContent.getAny().get(0).getFirstChild();
                    // printElement(element);
                    Document document = element.getOwnerDocument();
                    appendChildNS(document, element, NS_KRAMERIUS, "kramerius:tiles-url", tilesUrlValue);
                }
            }
        }
    }

    private static final String NS_KRAMERIUS = "http://www.nsdl.org/ontologies/relationships#";

    private static Element appendChildNS(Document d, Node parent, String prefix, String name, String value) {
        Element e = d.createElementNS(prefix, name);
        e.setTextContent(value);
        parent.appendChild(e);
        return e;
    }

    public static void printElement(Node element) {
        try {
            // Set up a transformer to convert the element to a string
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            // Create a DOMSource for the Element
            DOMSource source = new DOMSource(element);

            // Print to console
            StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            log.log(Level.WARNING, "Error printing element", e);
        }
    }


    private static List<RDFTuple> readRDF(byte[] bytes) {
        XMLInputFactory f = XMLInputFactory.newInstance();

        List<RDFTuple> retval = new ArrayList<>();
        String subject = null;
        boolean inRdf = false;
        try {
            XMLStreamReader r = f.createXMLStreamReader(new ByteArrayInputStream(bytes));
            while (r.hasNext()) {
                r.next();
                if (r.isStartElement()) {
                    if ("rdf".equals(r.getName().getPrefix()) && "Description".equals(r.getName().getLocalPart())) {
                        subject = r.getAttributeValue(r.getNamespaceURI("rdf"), "about");
                        inRdf = true;
                        continue;
                    }
                    if (inRdf) {
                        String namespace = r.getName().getNamespaceURI();
                        String predicate = r.getName().getLocalPart();
                        String object = r.getAttributeValue(r.getNamespaceURI("rdf"), "resource");
                        boolean literal = false;
                        if (object == null) {
                            object = r.getElementText();
                            if (object != null) {
                                literal = true;
                            }
                        }
                        retval.add(new RDFTuple(subject, namespace, predicate, object, literal));
                    }
                }
                if (r.isEndElement()) {
                    if ("rdf".equals(r.getName().getPrefix()) && "Description".equals(r.getName().getLocalPart())) {
                        inRdf = false;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            log.log(Level.WARNING, "Error reading RDF from bytes", ex);
        }
        return retval;
    }


    /**
     * Parse FOXML file and if it has model in fedora.topLevelModels, add its
     * PID to roots list. Objects in the roots list then will be submitted to
     * Indexer (whole-tree indexation).
     * Note that object might not be actual root, when it is part of a convolute,
     * but here it is still considered a root.
     */
    private static void checkModelIsClassicRoot(DigitalObject dobj, ClassicRootMap roots) {
        try {
            boolean isRootObject = false;
            String title = "";
            for (DatastreamType ds : dobj.getDatastream()) {
                if ("DC".equals(ds.getID())) {//obtain title from DC stream
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType v = versions.get(versions.size() - 1);
                        XmlContentType dcxml = v.getXmlContent();
                        List<Element> elements = dcxml.getAny();
                        for (Element el : elements) {
                            NodeList titles = el.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
                            if (titles.getLength() > 0) {
                                title = titles.item(0).getTextContent();
                            }
                        }
                    }
                }
                if ("RELS-EXT".equals(ds.getID())) { //check for root model in RELS-EXT
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType v = versions.get(versions.size() - 1);
                        XmlContentType dcxml = v.getXmlContent();
                        List<Element> elements = dcxml.getAny();
                        for (Element el : elements) {
                            NodeList types = el.getElementsByTagNameNS("info:fedora/fedora-system:def/model#", "hasModel");
                            for (int i = 0; i < types.getLength(); i++) {
                                String type = types.item(i).getAttributes().getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getNodeValue();
                                if (type.startsWith("info:fedora/model:")) {
                                    String model = type.substring(18);//get the string after info:fedora/model:
                                    isRootObject = classicRootModels.contains(model);
                                }
                            }
                        }
                    }
                }

            }
            if (isRootObject) {
                TitlePidTuple npt = new TitlePidTuple(title, dobj.getPID());
                if (roots != null) {
                    roots.add(npt);
                    log.log(Level.INFO, "Found (root) object for indexing - {0}", npt);
                }
            }

        } catch (DOMException ex) {
            log.log(Level.WARNING, "Error in Ingest.checkRoot for file {0}, file cannot be checked for auto-indexing : {1}", new Object[]{dobj.getPID(), ex});
        }
    }

    private static void addCollection(AkubraRepository akubraRepository, String collectionPid, ClassicRootMap classicRoots, Set<TitlePidTuple> collectionsToReindex, String authToken) {
        Client c = Client.create();

        List<String> rootPids = new ArrayList<>();
        classicRoots.forEach(clRoot -> rootPids.add(clRoot.pid));

        List<String> pidsToCollection = new ArrayList<>();


        String adminPoint = KConfiguration.getInstance().getConfiguration().getString("api.admin.v7.point");
        if (!adminPoint.endsWith("/")) adminPoint = adminPoint + "/";
        String collectionDescUrl = adminPoint + String.format("collections/%s", collectionPid);
        WebResource collectionResource = c.resource(collectionDescUrl);
        String collectionJSON = collectionResource.header("parent-process-auth-token", authToken).accept(MediaType.APPLICATION_JSON).get(String.class);

        JSONArray alreadyInCollection = new JSONObject(collectionJSON).getJSONArray("items");
        List<String> alreadyInCollectionList = new ArrayList<>();
        for (int i = 0; i < alreadyInCollection.length(); i++) {
            alreadyInCollectionList.add(alreadyInCollection.getString(i));
        }

        for (String pidToAdd : rootPids) {
            if (!alreadyInCollectionList.contains(pidToAdd)) {
                pidsToCollection.add(pidToAdd);
            } else {
                LOGGER.info(String.format("Pid %s has been already added to %s", pidToAdd, collectionPid));
            }
        }

        String collectionsUrl = String.format("%scollections/%s/items?indexation=false",adminPoint, collectionPid);
        WebResource r = c.resource(collectionsUrl);
        for (String pidToCollection : pidsToCollection) {
            LOGGER.info(String.format("Adding %s  to collection %s", pidToCollection, collectionPid));
            ClientResponse clientResponse = r.accept(MediaType.TEXT_PLAIN_TYPE).header("parent-process-auth-token", authToken).entity(pidToCollection, MediaType.TEXT_PLAIN_TYPE).post(ClientResponse.class);
            if (clientResponse.getStatus() != 200 && clientResponse.getStatus() != 201) {
                String responseBody = clientResponse.getEntity(String.class);
                throw new RuntimeException(String.format("Status code %d, %s", clientResponse.getStatus(), responseBody));
            }
        }
        collectionsToReindex.add(new TitlePidTuple("Sbírka", collectionPid));
    }

    /**
     * Parse FOXML file and if it has model "convolute", add its
     * PID to convolutes list. Objects in the convolutes list then will be submitted to
     * Indexer (object-only indexation)
     */
    private static void checkModelIsConvoluteOrCollection(DigitalObject dobj, Set<TitlePidTuple> convolutes, Set<TitlePidTuple> collections, ClassicRootMap roots) {
        try {
            boolean isConvolute = false;
            boolean isCollection = false;
            String title = "";
            for (DatastreamType ds : dobj.getDatastream()) {
                if ("DC".equals(ds.getID())) {//obtain title from DC stream
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType v = versions.get(versions.size() - 1);
                        for (Element el : v.getXmlContent().getAny()) {
                            NodeList titles = el.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
                            if (titles.getLength() > 0) {
                                title = titles.item(0).getTextContent();
                            }
                        }
                    }
                }
                if ("RELS-EXT".equals(ds.getID())) { //check for root model in RELS-EXT
                    List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                    if (versions != null) {
                        DatastreamVersionType v = versions.get(versions.size() - 1);
                        for (Element el : v.getXmlContent().getAny()) {
                            NodeList types = el.getElementsByTagNameNS("info:fedora/fedora-system:def/model#", "hasModel");
                            for (int i = 0; i < types.getLength(); i++) {
                                String type = types.item(i).getAttributes().getNamedItemNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource").getNodeValue();
                                if (type.startsWith("info:fedora/model:")) {
                                    String model = type.substring(18);//get the string after info:fedora/model:
                                    isConvolute = "convolute".equals(model);
                                    isCollection = "collection".equals(model);
                                }
                            }
                        }
                        //<rel:contains xmlns:rel="http://www.nsdl.org/ontologies/relationships#" rdf:resource="info:fedora/uuid:be0aa9c4-cbbb-4d3d-b552-e9533c9b4fed"/>
                        for (Element elm : v.getXmlContent().getAny()) {
                            List<Element> pids = XMLUtils.getElementsRecursive(elm, (Element element) -> element.getLocalName().equals("contains"));
                            for (int i = 0; i < pids.size(); i++) {
                                String attributeNS = pids.get(i).getAttributeNS(RepositoryNamespaces.RDF_NAMESPACE_URI, "resource");
                                if (attributeNS.contains("info:fedora/")) {
                                    String rootPid = attributeNS.substring("info:fedora/".length());
                                    roots.add(new TitlePidTuple(rootPid, rootPid));
                                    LOGGER.info(String.format("Adding contains relation from collection %s", rootPid));
                                }
                            }
                        }
                    }
                }
            }
            if (isConvolute) {
                TitlePidTuple npt = new TitlePidTuple(title, dobj.getPID());
                if (convolutes != null) {
                    convolutes.add(npt);
                    log.log(Level.INFO, "Found (convolute) object for indexing - {0}", npt);
                }
            } else if (isCollection) {
                TitlePidTuple npt = new TitlePidTuple(title, dobj.getPID());
                if (collections != null) {
                    collections.add(npt);
                    log.log(Level.INFO, "Found (collection) object for indexing - {0}", npt);
                }

            }

        } catch (DOMException ex) {
            log.log(Level.WARNING, "Error in Ingest.checkRoot for file {0}, file cannot be checked for auto-indexing : {1}", new Object[]{dobj.getPID(), ex});
        }
    }


    /**
     * Checks if fedora contains object with given PID
     *
     * @param pid requested PID
     * @return true if given object exists
     */
    public static boolean objectExists(AkubraRepository repo, String pid) throws RepositoryException {
        return repo.exists(pid);
    }
}

class RDFTuple {

    String subject;
    String namespace;
    String predicate;
    String object;
    boolean literal;

    public RDFTuple(String subject, String namespace, String predicate, String object, boolean literal) {
        super();
        this.subject = subject;
        this.namespace = namespace;
        this.predicate = predicate;
        this.object = object;
        this.literal = literal;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RDFTuple rdfTuple = (RDFTuple) o;

        if (literal != rdfTuple.literal) return false;
        if (object != null ? !object.equals(rdfTuple.object) : rdfTuple.object != null) return false;
        if (namespace != null ? !namespace.equals(rdfTuple.namespace) : rdfTuple.namespace != null) return false;
        if (predicate != null ? !predicate.equals(rdfTuple.predicate) : rdfTuple.predicate != null) return false;
        return !(subject != null ? !subject.equals(rdfTuple.subject) : rdfTuple.subject != null);
    }

    @Override
    public int hashCode() {
        int result = subject != null ? subject.hashCode() : 0;
        result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (literal ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RDFTuple{" +
                "subject=" + subject +
                ", namespace=" + namespace +
                ", predicate=" + predicate +
                ", object=" + object +
                ", literal=" + literal +
                '}';
    }
}

class TitlePidTuple {

    public String title;
    public String pid;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TitlePidTuple that = (TitlePidTuple) o;

        return !(pid != null ? !pid.equals(that.pid) : that.pid != null);
    }

    @Override
    public int hashCode() {
        return pid != null ? pid.hashCode() : 0;
    }

    public TitlePidTuple(String name, String pid) {
        this.title = name;
        this.pid = pid;
    }

    @Override
    public String toString() {
        return "Title:" + title + " PID:" + pid;
    }
}
//class ImportModule extends AbstractModule {
//
//    @Override
//    protected void configure() {
//        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
//
//        bind(StatisticsAccessLog.class).annotatedWith(Names.named("database")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
//        bind(StatisticsAccessLog.class).annotatedWith(Names.named("dnnt")).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
//
//
//        bind(AggregatedAccessLogs.class).to(GenerateDeepZoomCacheModule.NoStatistics.class).in(Scopes.SINGLETON);
//        bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
//        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
//        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);
//    }
//}
//
