package org.kramerius;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.qbizm.kramerius.imp.jaxb.*;
//import com.qbizm.kramerius.imptool.poc.valueobj.RelsExt;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.FedoraAccess;
//import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.processes.new_api.ProcessScheduler;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.service.FOXMLAppendLicenseService;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

import org.apache.commons.io.IOUtils;
//import cz.incad.kramerius.utils.IOUtils;

import cz.incad.kramerius.utils.conf.KConfiguration;
//import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;
import cz.incad.kramerius.utils.pid.LexerException;
//import cz.incad.kramerius.utils.pid.PIDParser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.solr.client.solrj.SolrServerException;
//import org.fcrepo.common.rdf.FedoraNamespace;
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
//import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

//import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;

import cz.incad.kramerius.utils.*;

import static cz.incad.kramerius.fedora.om.impl.AkubraUtils.getCurrentXMLGregorianCalendar;
import static cz.incad.kramerius.utils.XMLUtils.*;
import static cz.incad.kramerius.FedoraNamespaces.*;

/**
 * Import
 */
public class Import {

    public static final String NON_KEYWORD = "-none-";

    //static ObjectFactory of;
    static int counter = 0;
    private static final Logger log = Logger.getLogger(Import.class.getName());

    // only syncronization object
    private final static Object marshallingLock = new Object();

    private static Unmarshaller unmarshaller = null;
    //private static Marshaller datastreamMarshaller = null;

    private static List<String> classicRootModels = null; //top-level models, not including convolutes
    private static SortingService sortingService;
    private static Map<String, List<String>> updateMap = new HashMap<>();

    private static String imgTreePath = "";
    private static String imgTreeUrl = "";
    //private static DocumentBuilder docBuilder;

    static {
        try {
            unmarshaller = JAXBContext.newInstance(DigitalObject.class).createUnmarshaller();
            //datastreamMarshaller = JAXBContext.newInstance(DatastreamType.class).createMarshaller();
            //docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (JAXBException/*  | ParserConfigurationException*/ e) {
            log.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
        String[] rootModelsArr = KConfiguration.getInstance().getPropertyList("fedora.topLevelModels");
        classicRootModels = (rootModelsArr!=null && rootModelsArr.length>0)?Arrays.asList(rootModelsArr):new ArrayList<>();
        if (useImageServer()) setImgTree(); //set imgTreePath and imgTreeUrl for export to imageserver
    }

    /**
     * args[0] - authToken args[1] - import dir, optional args[2] - start
     * indexer, optional
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
        Boolean sortRelationsFromArgs = args.length > argsIndex ? Boolean.valueOf(args[argsIndex++]) : null;
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule(), new ImportModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        SortingService sortingServiceLocal = injector.getInstance(SortingService.class);
        FOXMLAppendLicenseService foxmlService = injector.getInstance(FOXMLAppendLicenseService.class);

        //priority: 1. args, 2. System property, 3. KConfiguration, 4. explicit defalut value
        String importDirectory = (importDirFromArgs != null) ? importDirFromArgs : TryGetKey("import.directory", "");

        Boolean startIndexer = (startIndexerFromArgs != null) ? startIndexerFromArgs : Boolean.valueOf(TryGetKey("ingest.startIndexer", "true"));

        Boolean sortRelations = (sortRelationsFromArgs != null) ? sortRelationsFromArgs : Boolean.valueOf(TryGetKey("ingest.sortRelations", "true"));

        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);

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
            } catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException
                    | LexerException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

            ProcessStarter.updateName(String.format("Import FOXML z %s ", importDirectory));
            log.log(Level.INFO, "import dir: {0}", licensesImportFile);
            log.log(Level.INFO, "start indexer: {0}", startIndexer);
            log.log(Level.INFO, "license : {0}", license);

            Import.run(fa, feeder, sortingServiceLocal, KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), licensesImportFile.getAbsolutePath(), startIndexer, authToken, addCollection,sortRelations);

            log.info(String.format("Deleting import folder %s", licensesImportFile));
            FileUtils.deleteDirectory(licensesImportFile);

        } else {

            ProcessStarter.updateName(String.format("Import FOXML z %s ", importDirectory));
            log.log(Level.INFO, "import dir: {0}", importDirectory);
            log.log(Level.INFO, "start indexer: {0}", startIndexer);
            Import.run(fa, feeder, sortingServiceLocal, KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), importDirectory, startIndexer, authToken, addCollection,sortRelations);
        }
    }

    /**
     * @param key
     * @param defaultval
     * @return value of key if it is in System.getProperties otherwise from KConfiguration and lastly defaultval
     */
    public static String TryGetKey(String key, String defaultval) {
        return System.getProperties().containsKey(key) ? System.getProperty(key) : KConfiguration.getInstance().getConfiguration().getString(key, defaultval);
    }

    public static void run(FedoraAccess fa, ProcessingIndexFeeder feeder, SortingService sortingServiceParam, final String url, final String user, final String pwd, String importRoot) throws IOException, SolrServerException {
        run(fa, feeder, sortingServiceParam, url, user, pwd, importRoot, true, null, null);
    }
    public static void run(FedoraAccess fa, ProcessingIndexFeeder feeder, SortingService sortingServiceParam, final String url, final String user, final String pwd, String importRoot, boolean startIndexer, String authToken, String addcollections)throws IOException, SolrServerException{
        run(fa,feeder,sortingServiceParam,url,user,pwd,importRoot,startIndexer,authToken,addcollections,Boolean.valueOf(TryGetKey("ingest.sortRelations", "true")));
    }
    public static void run(FedoraAccess fa, ProcessingIndexFeeder feeder, SortingService sortingServiceParam, final String url, final String user, final String pwd, String importRoot, boolean startIndexer, String authToken, String addcollections,Boolean startSortProperty) throws IOException, SolrServerException {
        log.log(Level.INFO, "INGEST - url:{0} user:{1} importRoot:{2}", new Object[]{url, user, importRoot});
        sortingService = sortingServiceParam;
        // system property 
        try {
            Boolean skipIngest = Boolean.valueOf(TryGetKey("ingest.skip", "false"));
            if (skipIngest) {
                log.info("INGEST CONFIGURED TO BE SKIPPED, RETURNING");
                return;
            }

            boolean updateExisting = Boolean.parseBoolean(TryGetKey("ingest.updateExisting", "false"));
            log.log(Level.INFO, "INGEST updateExisting: {0}", updateExisting);

            long start = System.currentTimeMillis();

            File importFile = new File(importRoot);
            if (!importFile.exists()) {
                String message = "Import root folder or control file doesn't exist: " + importFile.getAbsolutePath();
                log.log(Level.SEVERE, message);
                throw new RuntimeException(message);
            }

            initialize(user, pwd);

            Set<TitlePidTuple> classicRoots = new HashSet<>();
            Set<TitlePidTuple> convolutes = new HashSet<>();
            Set<TitlePidTuple> collections = new HashSet<>();

            Set<String> sortRelations = new HashSet<>();
            if (importFile.isDirectory()) {
                visitAllDirsAndFiles(fa, importFile, classicRoots, convolutes, collections, sortRelations, updateExisting);
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
                        visitAllDirsAndFiles(fa, importItem, classicRoots, convolutes, collections, sortRelations, updateExisting);
                    }
                    reader.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "Exception reading import list file: {0}", e);
                    throw new RuntimeException(e);
                }
            }
            log.log(Level.INFO, "FINISHED INGESTION IN {0}s, processed {1} files", new Object[]{(System.currentTimeMillis() - start) / 1000.0, counter});

            if (startSortProperty) {
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
                            addCollection(pid, classicRoots, collections, authToken);
                        }
                    }
                }

                if (collections.isEmpty()) {
                    log.info("NO COLLECTIONS FOR INDEXING FOUND.");
                } else {
                    try {
                        String waitIndexerProperty = TryGetKey("ingest.startIndexer.wait", "1000");
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
                        String waitIndexerProperty = TryGetKey("ingest.startIndexer.wait", "1000");
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
                } else try {
                    String waitIndexerProperty = TryGetKey("ingest.startIndexer.wait", "1000");
                    // should wait
                    log.log(Level.INFO, "Waiting for soft commit :{0} s", waitIndexerProperty);
                    Thread.sleep(Integer.parseInt(waitIndexerProperty));

                    if (authToken != null) {
                        for (TitlePidTuple root : classicRoots) {
                            final String pid = root.pid;
                            if (fa.isObjectAvailable(pid)) {
                                ProcessScheduler.scheduleIndexation(pid, root.title, true, authToken);
                            } else {
                                LOGGER.warning(String.format("Object '%s' does not exist in the repository. ", pid));
                            }

                        }
                        log.info("ALL ROOT OBJECTS SCHEDULED FOR INDEXING.");
                    } else {
                        log.warning("cannot schedule indexation due to missing process credentials");
                    }
                } catch (IOException | InterruptedException | NumberFormatException e) {
                    log.log(Level.WARNING, e.getMessage(), e);
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
        //of = new ObjectFactory();
    }

    private static void visitAllDirsAndFiles(FedoraAccess fa, File importFile, Set<TitlePidTuple> classicRoots,
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
            for (File children1 : children) {
                visitAllDirsAndFiles(fa, children1, classicRoots, convolutes, collections, sortRelations, updateExisting);
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
                                    Source xmlSource = new DOMSource(element);
                                    Result outputTarget = new StreamResult(outputStream);
                                    try {
                                        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
                                    } catch (TransformerException e) {
                                        throw new RuntimeException(e);
                                    }

                                    final DigitalObject transactionDigitalObject = dobj;

                                    String mimeType = "text/xml";
                                    Lock writeLock = AkubraDOManager.getWriteLock(transactionDigitalObject.getPID());
                                    try {
                                        if (fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).streamExists(ds.getID())) {
                                            mimeType = fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).getStream(ds.getID()).getMimeType();
                                            fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).deleteStream(ds.getID());
                                        }
                                        fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).createStream(ds.getID(), mimeType, new ByteArrayInputStream(outputStream.toByteArray()));
                                    } finally {
                                        writeLock.unlock();
                                    }

                                } else if (dsversion.getBinaryContent() != null) {
                                    throw new RuntimeException("Update of managed binary datastream content is not supported.");
                                } else if (dsversion.getContentLocation() != null) {

                                    final DigitalObject transactionDigitalObject = dobj;
                                    Lock writeLock = AkubraDOManager.getWriteLock(transactionDigitalObject.getPID());
                                    try {
                                        String mimeType = fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).getStream(ds.getID()).getMimeType();
                                        fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).deleteStream(ds.getID());
                                        fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).createRedirectedStream(ds.getID(), dsversion.getContentLocation().getREF(), mimeType);
                                    } finally {
                                        writeLock.unlock();
                                    }

                                }
                            }
                        }
                    }
                    if (classicRoots != null) {
                        TitlePidTuple npt = new TitlePidTuple("", dobj.getPID());
                        classicRoots.add(npt);
                        log.log(Level.INFO, "Added updated object for indexing:{0}", dobj.getPID());
                        //NOTE: inefficient for updated convolutes, everyting new/changed inside it will be indexed twice
                    }
                } else {
                    final DigitalObject transactionDigitalObject = dobj;

                    ingest(fa.getInternalAPI(), importFile, sortRelations, classicRoots, updateExisting);
                    checkModelIsClassicRoot(transactionDigitalObject, classicRoots);
                    checkModelIsConvoluteOrCollection(transactionDigitalObject, convolutes, collections, classicRoots);
                }
            } catch (RepositoryException | RuntimeException | TransformerFactoryConfigurationError t) {
                log.log(Level.SEVERE, "Error when ingesting PID: {0}, {1}", new Object[]{dobj.getPID(), t.getMessage()});
                throw new RuntimeException(t);
            }
        }
    }

    private static void parseUpdateList(File listFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(listFile));
        } catch (FileNotFoundException e) {
            log.log(Level.SEVERE, "update.list file {0} not found: {1}", new Object[]{listFile, e});
            throw new RuntimeException(e);
        }
        try {
            for (String line; (line = reader.readLine()) != null;) {
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
            reader.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception reading update.list file: {0}", e);
            throw new RuntimeException(e);
        }
    }

    public static void ingest(Repository repo, InputStream is, String filename, Set<String> sortRelations, Set<TitlePidTuple> roots, boolean updateExisting) throws IOException, RepositoryException, JAXBException, LexerException, TransformerException {
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
        Lock writeLock = AkubraDOManager.getWriteLock(pid);
        try {
            repo.ingestObject(obj);
        } catch (RepositoryException sfex) {
            if (objectExists(repo, pid)) {
                if (updateExisting) {
                    log.log(Level.INFO, "Replacing existing object {0}", pid);
                    try {
                        repo.deleteObject(pid, true, false);
                        log.log(Level.INFO, "purged old object {0}", pid);
                    } catch (RepositoryException ex) {
                        log.log(Level.SEVERE, "Cannot purge object {0}, skipping: {1}", new Object[]{pid, ex});
                        throw new RuntimeException(ex);
                    }
                    try {
                        repo.ingestObject(obj);
                        log.log(Level.INFO, "Ingested new object {0}", pid);
                    } catch (cz.incad.kramerius.fedora.om.RepositoryException rsfex) {
                        log.log(Level.SEVERE, "Replace ingest SOAP fault:{0}", rsfex);
                        throw new RuntimeException(rsfex);
                    }
                    if (roots != null) {
                        roots.add(new TitlePidTuple("", pid));
                        log.log(Level.INFO, "Added replaced object for indexing:{0}", pid);
                    }
                } else {
                    log.log(Level.INFO, "Merging with existing object {0}", pid);
                    if (merge(repo, bytes)) {
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
        } finally {
            writeLock.unlock();
        }

        counter++;
        log.log(Level.INFO, "Ingested:{0} in {1}ms, count:{2}", new Object[]{pid, System.currentTimeMillis() - start, counter});
    }

    public static void ingest(Repository repo, File file, Set<String> sortRelations, Set<TitlePidTuple> roots, boolean updateExisting) {
        try (FileInputStream is = new FileInputStream(file)) {
            ingest(repo, is, file.getName(), sortRelations, roots, updateExisting);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Ingestion error ", ex);
            throw new RuntimeException(ex);
        }
    }

    private static boolean merge(Repository repo, byte[] ingestedBytes) throws RepositoryException {
        List<RDFTuple> ingested = readRDF(ingestedBytes);
        if (ingested.isEmpty()) {
            return false;
        }
        String pid = ingested.get(0).subject.substring("info:fedora/".length());
        RepositoryObject existingObject = repo.getObject(pid);
        if (existingObject == null) {
            throw new IllegalStateException("Cannot merge object: " + pid + " - object not in repository");
        }
        RepositoryDatastream existingRelsext = existingObject.getStream("RELS-EXT");
        if (existingRelsext == null) {
            throw new IllegalStateException("Cannot merge object: " + pid + " - object does not have RELS-EXT stream");
        }
        InputStream existingContent = existingRelsext.getContent();
        if (existingContent == null) {
            throw new IllegalStateException("Cannot merge object: " + pid + " - object has empty RELS-EXT stream");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(existingContent, bos);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot copy streams in merge", e);
            throw new RuntimeException(e);
        }
        ingested.removeAll(readRDF(bos.toByteArray()));
        boolean touched = false;
        for (RDFTuple t : ingested) {
            if (t.object != null) {
                try {
                    if (t.literal) {
                        repo.getObject(pid).addLiteral(t.predicate, t.namespace, t.object);
                    } else {
                        repo.getObject(pid).addRelation(t.predicate, t.namespace, t.object);
                    }
                    //port.addRelationship(t.subject.substring("info:fedora/".length()), t.predicate, t.object, t.literal, null);
                    touched = true;
                } catch (RepositoryException ex) {
                    log.log(Level.SEVERE, "WARNING - could not add relationship:" + t + "(" + ex + ")", ex);
                }
            }
        }
        return touched;
    }

    public static boolean useImageServer() {
        return System.getProperties().containsKey("convert.useImageServer")
                ? Boolean.parseBoolean(System.getProperty("convert.useImageServer"))
                : KConfiguration.getInstance().getConfiguration().getBoolean("convert.useImageServer", false);
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
            version.setCREATED(getCurrentXMLGregorianCalendar());

            String binaryDirectory = KConfiguration.getInstance().getConfiguration().getString("convert.imageServerDirectory") + getImgTreePath();

            File dir = cz.incad.kramerius.utils.IOUtils.checkDirectory(binaryDirectory);
            // Write file to imageserver directory
            Files.write(new File(dir, filename).toPath(), img);
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
                    appendChildNS(element.getOwnerDocument(), element, NS_KRAMERIUS, "kramerius:tiles-url", tilesUrlValue);
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
            //e.printStackTrace();
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
        } catch (XMLStreamException e) {
            //e.printStackTrace();
        }
        return retval;
    }

    /**
     * Parse FOXML file and if it has model in fedora.topLevelModels, add its
     * PID to roots list. Objects in the roots list then will be submitted to
     * Indexer (whole-tree indexation). Note that object might not be actual
     * root, when it is part of a convolute, but here it is still considered a
     * root.
     */
    private static void checkModelIsClassicRoot(DigitalObject dobj, Set<TitlePidTuple> roots) {
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

    private static void addCollection(String collectionPid, Set<TitlePidTuple> classicRoots, Set<TitlePidTuple> collectionsToReindex, String authToken) {
        Client c = Client.create();

        List<String> rootPids = new ArrayList<>();
        classicRoots.forEach(clRoot -> rootPids.add(clRoot.pid));

        List<String> pidsToCollection = new ArrayList<>();

        String adminPoint = KConfiguration.getInstance().getConfiguration().getString("api.admin.v7.point");
        if (!adminPoint.endsWith("/")) {
            adminPoint = adminPoint + "/";
        }
        String collectionDescUrl = adminPoint + String.format("collections/%s", collectionPid);
        WebResource collectionResource = c.resource(collectionDescUrl);
        String collectionJSON = collectionResource.header("parent-process-auth-token", authToken).accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONObject collectionObject = new JSONObject(collectionJSON);

        JSONArray alreadyInCollection = collectionObject.getJSONArray("items");
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

        String collectionsUrl = adminPoint + String.format("collections/%s/items?indexation=false", collectionPid);
        WebResource r = c.resource(collectionsUrl);
        for (String pidToCollection : pidsToCollection) {
            LOGGER.info(String.format("Adding %s  to collection %s", pidToCollection, collectionPid));
            ClientResponse clientResponse = r.accept(MediaType.TEXT_PLAIN_TYPE).header("parent-process-auth-token", authToken).entity(pidToCollection, MediaType.TEXT_PLAIN_TYPE).post(ClientResponse.class);
            if (clientResponse.getStatus() != 200 && clientResponse.getStatus() != 201) {
                String responseBody = clientResponse.getEntity(String.class);
                throw new RuntimeException(String.format("Status code %d, %s", clientResponse.getStatus(), responseBody));
            }
        }

        TitlePidTuple npt = new TitlePidTuple("Sbírka", collectionPid);
        collectionsToReindex.add(npt);
    }

    /**
     * Parse FOXML file and if it has model "convolute", add its PID to
     * convolutes list. Objects in the convolutes list then will be submitted to
     * Indexer (object-only indexation)
     */
    private static void checkModelIsConvoluteOrCollection(DigitalObject dobj, Set<TitlePidTuple> convolutes, Set<TitlePidTuple> collections, Set<TitlePidTuple> roots) {
        try {
            boolean isConvolute = false;
            boolean isCollection = false;
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
                                    isConvolute = "convolute".equals(model);
                                    isCollection = "collection".equals(model);
                                }
                            }
                        }
                        //<rel:contains xmlns:rel="http://www.nsdl.org/ontologies/relationships#" rdf:resource="info:fedora/uuid:be0aa9c4-cbbb-4d3d-b552-e9533c9b4fed"/>
                        XmlContentType xmlContent = v.getXmlContent();
                        List<Element> any = xmlContent.getAny();
                        for (Element elm : any) {
                            List<Element> pids = XMLUtils.getElementsRecursive(elm, (Element element) -> {
                                return element.getLocalName().equals("contains");
                            });
                            for (int i = 0; i < pids.size(); i++) {
                                String attributeNS = pids.get(i).getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                                if (attributeNS.contains("info:fedora/")) {
                                    String rootPid = attributeNS.substring("info:fedora/".length());
                                    TitlePidTuple npt = new TitlePidTuple(rootPid, rootPid);

                                    LOGGER.info(String.format("Adding contains relation from collection %s", rootPid));

                                    roots.add(npt);
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
    public static boolean objectExists(Repository repo, String pid) throws RepositoryException {
        return repo.objectExists(pid);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RDFTuple rdfTuple = (RDFTuple) o;

        if (literal != rdfTuple.literal
                || object != null ? !object.equals(rdfTuple.object) : rdfTuple.object != null
                        || namespace != null ? !namespace.equals(rdfTuple.namespace) : rdfTuple.namespace != null
                                || predicate != null ? !predicate.equals(rdfTuple.predicate) : rdfTuple.predicate != null) {
            return false;
        }
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
        return "RDFTuple{"
                + "subject=" + subject
                + ", namespace=" + namespace
                + ", predicate=" + predicate
                + ", object=" + object
                + ", literal=" + literal
                + '}';
    }
}

class TitlePidTuple {

    public String title;
    public String pid;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        String thatpid = ((TitlePidTuple) o).pid;

        return !(pid != null ? !pid.equals(thatpid) : thatpid != null);
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
