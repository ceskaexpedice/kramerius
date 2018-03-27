package org.kramerius;

import com.google.inject.*;
import com.google.inject.name.Names;
import com.qbizm.kramerius.imp.jaxb.*;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.service.impl.SortingServiceImpl;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Import {


    static ObjectFactory of;
    static int counter = 0;
    private static final Logger log = Logger.getLogger(Import.class.getName());

    // only syncronization object
    private static Object marshallingLock = new Object();

    private static Unmarshaller unmarshaller = null;
    private static Marshaller datastreamMarshaller = null;

    private static List<String> rootModels = null;
    private static SortingService sortingService;
    private static Map<String, List<String>> updateMap = new HashMap<String, List<String>>();

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);

            unmarshaller = jaxbContext.createUnmarshaller();


            JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            datastreamMarshaller = jaxbdatastreamContext.createMarshaller();


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }

        rootModels = Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.topLevelModels"));
        if (rootModels == null) {
            rootModels = new ArrayList<String>();
        }
    }

    /**
     * @param args
     * @throws UnsupportedEncodingException 
     */
    public static void main(String[] args) throws IOException, RepositoryException, SolrServerException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule(),new ImportModule());
        FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        sortingService = injector.getInstance(SortingService.class);
        String importDirectory = System.getProperties().containsKey("import.directory") ? System.getProperty("import.directory") : KConfiguration.getInstance().getProperty("import.directory");
        ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        Import.ingest(fa, feeder , KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), importDirectory);
    }
    

    public static void ingest(FedoraAccess fa, ProcessingIndexFeeder feeder, final String url, final String user, final String pwd, String importRoot) throws IOException, SolrServerException {
        log.finest("INGEST - url:" + url + " user:" + user + " pwd:" + pwd + " importRoot:" + importRoot);

        // system property 
        try {
            String skipIngest = System.getProperties().containsKey("ingest.skip") ? System.getProperty("ingest.skip") : KConfiguration.getInstance().getConfiguration().getString("ingest.skip", "false");
            if (new Boolean(skipIngest)) {
                log.info("INGEST CONFIGURED TO BE SKIPPED, RETURNING");
                return;
            }

            boolean updateExisting = Boolean.valueOf (System.getProperties().containsKey("ingest.updateExisting") ? System.getProperty("ingest.updateExisting") : KConfiguration.getInstance().getConfiguration().getString("ingest.updateExisting", "false"));
            log.info("INGEST updateExisting: "+updateExisting);


            long start = System.currentTimeMillis();

            File importFile = new File(importRoot);
            if (!importFile.exists()) {
                log.severe("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
                throw new RuntimeException("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
            }

            initialize(user, pwd);

            Set<TitlePidTuple> roots = new HashSet<TitlePidTuple>();
            Set<String> sortRelations = new HashSet<String>();
            if (importFile.isDirectory()) {
                visitAllDirsAndFiles(fa, importFile, roots, sortRelations, updateExisting);
            } else {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(importFile));
                } catch (FileNotFoundException e) {
                    log.severe("Import file list " + importFile + " not found: " + e);
                    throw new RuntimeException(e);
                }
                try {
                    for (String line; (line = reader.readLine()) != null;) {
                        if ("".equals(line)) {
                            continue;
                        }
                        File importItem = new File(line);
                        if (!importItem.exists()) {
                            log.severe("Import folder doesn't exist: " + importItem.getAbsolutePath());
                            continue;
                        }
                        if (!importItem.isDirectory()) {
                            log.severe("Import item is not a folder: " + importItem.getAbsolutePath());
                            continue;
                        }
                        log.info("Importing " + importItem.getAbsolutePath());
                        visitAllDirsAndFiles(fa, importItem, roots, sortRelations, updateExisting);
                    }
                    reader.close();
                } catch (IOException e) {
                    log.severe("Exception reading import list file: " + e);
                    throw new RuntimeException(e);
                }
            }
            log.info("FINISHED INGESTION IN " + ((System.currentTimeMillis() - start) / 1000.0) + "s, processed " + counter + " files");

            String startSortProperty = System.getProperties().containsKey("ingest.sortRelations") ? System.getProperty("ingest.sortRelations") : KConfiguration.getInstance().getConfiguration().getString("ingest.sortRelations", "true");
            if (new Boolean(startSortProperty)) {


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

            String startIndexerProperty = System.getProperties().containsKey("ingest.startIndexer") ? System.getProperty("ingest.startIndexer") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer", "true");
            if (new Boolean(startIndexerProperty)) {
                if (roots.isEmpty()) {
                    log.info("NO ROOT OBJECTS FOR INDEXING FOUND.");
                } else {
                    StringBuilder pids = new StringBuilder();
                    String pidSeparator = KConfiguration.getInstance().getConfiguration().getString("indexer.pidSeparator", ";");
                    for (TitlePidTuple tpt : roots) {
                        if (pids.length()>0){
                            pids.append(pidSeparator);
                        }
                        pids.append(tpt.pid);
                    }

                    try {
                        String waitIndexerProperty = System.getProperties().containsKey("ingest.startIndexer.wait") ? System.getProperty("ingest.startIndexer.wait") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer.wait", "1000");
                        // should wait
                        log.info("Waiting for soft commit :"+waitIndexerProperty+" s");
                        Thread.sleep(Integer.parseInt(waitIndexerProperty));

                        IndexerProcessStarter.spawnIndexer(true, importFile.getName(), pids.toString());
                        log.info("ALL ROOT OBJECTS SCHEDULED FOR INDEXING.");
                    } catch (Exception e) {
                        log.log(Level.WARNING, e.getMessage(),e);
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
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pwd.toCharArray());
            }
        });


        of = new ObjectFactory();
    }

    private static void visitAllDirsAndFiles(FedoraAccess fa, File importFile, Set<TitlePidTuple> roots, Set<String> sortRelations, boolean updateExisting) {
        if (importFile == null) {
            return;
        }
        if (importFile.isDirectory()) {

            File[] children = importFile.listFiles();

            for (File f : children){
                if ("update.list".equalsIgnoreCase(f.getName())){
                    log.info("File update.list detected in folder "+importFile);
                    parseUpdateList(f);
                }
            }

            if (children.length > 1 && children[0].isDirectory()) {//Issue 36
                Arrays.sort(children);
            }
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(fa, children[i], roots, sortRelations, updateExisting);
            }
        } else {
            DigitalObject dobj = null;
            try {
                if (!importFile.getName().toLowerCase().endsWith(".xml")) {
                    return;
                }
                // must be syncrhonized
                synchronized (marshallingLock) {
                    Object obj = unmarshaller.unmarshal(importFile);
                    dobj = (DigitalObject) obj;
                }
            } catch (Exception e) {
                log.warning("Skipping file " + importFile.getName() + " - not an FOXML object. ("+e+")");
                log.log(Level.WARNING, "Underlying error was:", e);
                return;
            }
            try {
                if (updateMap.containsKey(dobj.getPID())) {
                    log.info("Updating datastreams " + updateMap.get(dobj.getPID()) + " in object " + dobj.getPID());
                    List<DatastreamType> importedDatastreams = dobj.getDatastream();
                    List<String> datastreamsToUpdate = updateMap.get(dobj.getPID());
                    for (String dsName : datastreamsToUpdate) {
                        for (DatastreamType ds : importedDatastreams) {
                            if (dsName.equalsIgnoreCase(ds.getID())) {
                                log.info("Updating datastream " + ds.getID());
                                DatastreamVersionType dsversion = ds.getDatastreamVersion().get(0);
                                if (dsversion.getXmlContent() != null) {
                                    Element element = dsversion.getXmlContent().getAny().get(0);
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
                                    if (fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).streamExists(ds.getID())) {
                                        mimeType = fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).getStream(ds.getID()).getMimeType();
                                        fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).deleteStream(ds.getID());
                                    }
                                    fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).createStream(ds.getID(), mimeType, new ByteArrayInputStream(outputStream.toByteArray()));


                                } else if (dsversion.getBinaryContent() != null) {
                                    throw new RuntimeException("Update of managed binary datastream content is not supported.");
                                } else if (dsversion.getContentLocation() != null) {

                                    final DigitalObject transactionDigitalObject = dobj;

                                    String mimeType = fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).getStream(ds.getID()).getMimeType();
                                    fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).deleteStream(ds.getID());
                                    fa.getInternalAPI().getObject(transactionDigitalObject.getPID()).createRedirectedStream(ds.getID(),dsversion.getContentLocation().getREF());


                                }
                            }
                        }
                    }
                    if (roots != null) {
                        TitlePidTuple npt = new TitlePidTuple("", dobj.getPID());
                        roots.add(npt);
                        log.info("Added updated object for indexing:" + dobj.getPID());
                    }
                } else {
                    final DigitalObject transactionDigitalObject = dobj;

                    ingest(fa.getInternalAPI(), importFile, transactionDigitalObject.getPID(), sortRelations, roots, updateExisting);
                    checkRoot(transactionDigitalObject, roots);

                }
            }catch (Throwable t){
                log.severe("Error when ingesting PID: "+dobj.getPID()+", "+ t.getMessage());
                throw new RuntimeException(t);
            }
        }
    }

    private static void parseUpdateList(File listFile){
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(listFile));
        } catch (FileNotFoundException e) {
            log.severe("update.list file " + listFile + " not found: " + e);
            throw new RuntimeException(e);
        }
        try {
            for (String line; (line = reader.readLine()) != null;) {
                if ("".equals(line.trim()) || line.trim().startsWith("#")) {
                    continue;
                }
                String[] lineItems = line.split(" ");
                if (lineItems.length<2){
                    continue;
                }
                List<String> streams = new ArrayList<String>(lineItems.length-1);
                for (int i=0;i<lineItems.length-1;i++){
                    if (!"".equals(lineItems[i+1])) {
                        streams.add(lineItems[i + 1]);
                    }
                }
                updateMap.put(lineItems[0], streams);
            }
            reader.close();
        } catch (IOException e) {
            log.severe("Exception reading update.list file: " + e);
            throw new RuntimeException(e);
        }
    }

    public static void ingest(Repository repo, InputStream is, String pid, Set<String> sortRelations, Set<TitlePidTuple> roots, boolean updateExisting) throws IOException, RepositoryException, JAXBException, LexerException, TransformerException {
        
        long start = System.currentTimeMillis();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copyStreams(is, bos);
        byte[] bytes = bos.toByteArray();
        DigitalObject obj = null;
        try {
            synchronized (marshallingLock) {
                obj = (DigitalObject) unmarshaller.unmarshal(new ByteArrayInputStream(bytes));
            }
            pid = ((DigitalObject) obj).getPID();
            ingest(repo, obj, pid,updateExisting);
        } catch (cz.incad.kramerius.fedora.om.RepositoryException sfex) {

            //if (sfex.getMessage().contains("ObjectExistsException")) {
            if (objectExists(pid)) {
                if (updateExisting){
                    log.info("Replacing existing object " + pid);
                    try{
                        repo.deleteobject(pid);
                        log.info("purged old object "+pid);
                    }catch(Exception ex){
                        log.severe("Cannot purge object " + pid + ", skipping: " + ex);
                        throw new RuntimeException(ex);
                    }
                    try {
                        if (obj != null) {
                            ingest(repo, obj, pid,updateExisting);
                        }
                        log.info("Ingested new object "+pid);
                    } catch (cz.incad.kramerius.fedora.om.RepositoryException rsfex) {
                        log.severe("Replace ingest SOAP fault:" + rsfex);
                        throw new RuntimeException(rsfex);
                    }
                    if (roots != null) {
                        TitlePidTuple npt = new TitlePidTuple("", pid);
                        roots.add(npt);
                        log.info("Added replaced object for indexing:" + pid);
                    }
                }else {
                    log.info("Merging with existing object " + pid);
                    if (merge(repo, bytes)) {
                        if (sortRelations != null) {
                            sortRelations.add(pid);
                            log.info("Added merged object for sorting relations:" + pid);
                        }
                        if (roots != null) {
                            TitlePidTuple npt = new TitlePidTuple("", pid);
                            roots.add(npt);
                            log.info("Added merged object for indexing:" + pid);
                        }
                    }
                }
            } else {

                log.severe("Ingest SOAP fault:" + sfex);
                throw new RuntimeException(sfex);
            }

        }
    }

    public static void ingest(Repository repo, File file, String pid, Set<String> sortRelations, Set<TitlePidTuple> roots, boolean updateExisting) {
        if (pid == null) {
            try {
                synchronized (marshallingLock) {
                    Object obj = unmarshaller.unmarshal(file);
                    pid = ((DigitalObject) obj).getPID();
                }
            } catch (Exception e) {
                log.info("Skipping file " + file.getName() + " - not an FOXML object.");
                log.log(Level.INFO, "Underlying error was:", e);
                return;
            }
        }

        try {
            FileInputStream is = new FileInputStream(file);
            ingest(repo , is, pid, sortRelations, roots, updateExisting);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Ingestion error ", ex);
            throw new RuntimeException(ex);
        }
    }

    private static boolean merge(Repository repo, byte[] bytes) throws RepositoryException {
        List<RDFTuple> ingested = readRDF(bytes);
        if (ingested.isEmpty()) {
            return false;
        }
        String pid = ingested.get(0).subject.substring("info:fedora/".length());
        List<Triple<String, String, String>> relations = repo.getObject(pid).getRelations(null);
        List<Triple<String, String, String>> literals = repo.getObject(pid).getLiterals(null);

        List<RDFTuple> existing = new ArrayList<>(relations.size());
        for (Triple<String, String, String> t : relations) {
            existing.add(new RDFTuple(t.getLeft(), t.getMiddle(), t.getRight(), false));
        }
        for (Triple<String, String, String> t : literals) {
            existing.add(new RDFTuple(t.getLeft(), t.getMiddle(), t.getRight(), true));
        }
        ingested.removeAll(existing);


        boolean touched = false;
        for (RDFTuple t : ingested) {
            if (t.object != null) {
                try {
                    if (t.literal) {
                        repo.getObject(pid).addLiteral(t.predicate, t.subject, t.object);
                    } else {
                        repo.getObject(pid).addRelation(t.predicate, t.subject, t.object);
                    }
                    //port.addRelationship(t.subject.substring("info:fedora/".length()), t.predicate, t.object, t.literal, null);
                    touched = true;
                } catch (Exception ex) {
                    log.severe("WARNING- could not add relationship:" + t + "(" + ex + ")");
                }
            }
        }
        return touched;
    }

    private static List<RDFTuple> readRDF(byte[] bytes) {
        XMLInputFactory f = XMLInputFactory.newInstance();
        List<RDFTuple> retval = new ArrayList<RDFTuple>();
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
                        String predicate = r.getName().getNamespaceURI() + r.getName().getLocalPart();
                        String object = r.getAttributeValue(r.getNamespaceURI("rdf"), "resource");
                        boolean literal = false;
                        if (object == null){
                            object = r.getElementText();
                            if (object != null){
                                literal = true;
                            }
                        }
                        retval.add(new RDFTuple(subject, predicate, object, literal));
                    }
                }
                if (r.isEndElement()) {
                    if ("rdf".equals(r.getName().getPrefix()) && "Description".equals(r.getName().getLocalPart())) {
                        inRdf = false;
                    }
                }
            }
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        }
        return retval;
    }




    public static void ingest(Repository repo, DigitalObject dob, String pid, boolean updateExisting) throws IOException, LexerException, TransformerException, RepositoryException {
        long start = System.currentTimeMillis();

        List<PropertyType> properties = dob.getObjectProperties().getProperty();
        for (PropertyType pt :
                properties) {
            String name = pt.getNAME();
            String[] splitted = name.split("#");
            if (splitted.length == 2) {
                //FedoraNamespaces.
            } else {
                log.log(Level.SEVERE, "expecting value size "+splitted.length);
            }
            String value = pt.getVALUE();
        }

        PIDParser pidPArser = new PIDParser(pid);
        pidPArser.objectPid();
        String objId = pidPArser.getObjectPid();

        RepositoryObject obj = repo.createOrFindObject( objId/*+"?mixin=fedora:object"*/);

        List<DatastreamType> datastream = dob.getDatastream();

        for (DatastreamType ds : datastream) {
            String id = ds.getID();
            String controlgroup = ds.getCONTROLGROUP();
            DatastreamVersionType latestDs =  ds.getDatastreamVersion().isEmpty() ? null : ds.getDatastreamVersion().get(ds.getDatastreamVersion().size()-1);
            if (latestDs != null) {
                if (controlgroup.equals("X")) {
                    byte[] xmlContent = xmlContent(latestDs);
                    if (xmlContent != null) {
                        //String s = IOUtils.toString(xmlContent, "UTF-8");
                        createDataStream(repo,obj, id, latestDs,xmlContent, dob, updateExisting);
                    }
                } else if (controlgroup.equals("M")) {
                    ContentLocationType contentLocation = latestDs.getContentLocation();
                    if (contentLocation != null) {

                    } else {
                        byte[] binaryContent = latestDs.getBinaryContent();
                        if (binaryContent != null) {
                            createDataStream(repo, obj, id, latestDs, binaryContent, dob,updateExisting);
                        }

                    }
                } else if ((controlgroup.equals("E") || (controlgroup.equals("R")))) {
                    ContentLocationType contentLocation = latestDs.getContentLocation();
                    String ref = contentLocation.getREF();
                    createRelationDataStream(repo, obj, id, ref);
                }
            }
        }

        counter++;
        log.info("Ingested:" + pid + " in " + (System.currentTimeMillis() - start) + "ms, count:" + counter);
    }
    private static void createRelationDataStream(Repository repo, RepositoryObject obj, String id, String url) throws RepositoryException {
        obj.createRedirectedStream(id, url);
    }

    private static void createDataStream(Repository repo, RepositoryObject obj, String id,
                                         DatastreamVersionType versionType, byte[] binaryContent, DigitalObject dob, boolean updateExisting) throws RepositoryException {
        boolean relsExt = id.equals(FedoraUtils.RELS_EXT_STREAM);
        String mimeType = relsExt ? "text/xml" : versionType.getMIMETYPE();

        try {
            //TODO: do it better
            if (id.equals("POLICY")) return;

            obj.createStream(id, mimeType, new ByteArrayInputStream(binaryContent));

        } catch (RepositoryException e) {
            if (updateExisting && obj.streamExists(id)) {
                if (relsExt) {
                    obj.removeRelationsAndRelsExt();
                } else {
                    obj.deleteStream(id);
                }
                obj.createStream(id, mimeType, new ByteArrayInputStream(binaryContent));
            } else {
                throw e;
            }
        }
    }

    public static byte[] xmlContent(DatastreamVersionType dstype) throws TransformerException {
        XmlContentType xmlContent = dstype.getXmlContent();
        List<Element> any = xmlContent.getAny();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos);
        XMLUtils.print(any.get(0),ps);
        StringWriter writer = new StringWriter();
        XMLUtils.print(any.get(0),writer);
        return bos.toByteArray();

    }

    /**
     * Parse FOXML file and if it has model in fedora.topLevelModels, add its
     * PID to roots list. Objects in the roots list then will be submitted to
     * indexer
     */
    private static void checkRoot(DigitalObject dobj, Set<TitlePidTuple> roots) {
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
                                    isRootObject = rootModels.contains(model);
                                }
                            }
                        }
                    }
                }

            }
            if (isRootObject) {
                TitlePidTuple npt = new TitlePidTuple(title, dobj.getPID());
                if(roots!= null){
                    roots.add(npt);
                    log.info("Found object for indexing - " + npt);
                }
            }

        } catch (Exception ex) {
            log.log(Level.WARNING, "Error in Ingest.checkRoot for file " + dobj.getPID() + ", file cannot be checked for auto-indexing : " + ex);
        }
    }

    /**
     * Checks if fedora contains object with given PID
     *
     * @param pid requested PID
     * @return true if given object exists
     */
    public static boolean objectExists(String pid) {
        try {
            String fedoraObjectURL = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid;
            URLConnection urlcon = RESTHelper.openConnection(fedoraObjectURL, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            urlcon.connect();
            Object target = urlcon.getContent();
            if (target != null) {
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}

class RDFTuple {

    String subject;
    String predicate;
    String object;
    boolean literal;

    public RDFTuple(String subject, String predicate, String object, boolean literal) {
        super();
        this.subject = subject;
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
        if (predicate != null ? !predicate.equals(rdfTuple.predicate) : rdfTuple.predicate != null) return false;
        if (subject != null ? !subject.equals(rdfTuple.subject) : rdfTuple.subject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = subject != null ? subject.hashCode() : 0;
        result = 31 * result + (predicate != null ? predicate.hashCode() : 0);
        result = 31 * result + (object != null ? object.hashCode() : 0);
        result = 31 * result + (literal ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RDFTuple{" +
                "subject='" + subject + '\'' +
                ", predicate='" + predicate + '\'' +
                ", object='" + object + '\'' +
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

        if (pid != null ? !pid.equals(that.pid) : that.pid != null) return false;

        return true;
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

class ImportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(SortingService.class).to(SortingServiceImpl.class).in(Scopes.SINGLETON);
    }
}
