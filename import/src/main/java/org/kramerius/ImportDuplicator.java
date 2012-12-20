package org.kramerius;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPFaultException;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;
import org.fedora.api.ObjectFactory;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import com.qbizm.kramerius.imp.jaxb.DatastreamVersionType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.XmlContentType;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ImportDuplicator {
    static FedoraAPIMService service;
    static FedoraAPIM port;
    static ObjectFactory of;
    static int counter = 0;

    private static final Logger log = Logger.getLogger(ImportDuplicator.class.getName());

    private static Unmarshaller unmarshaller = null;

    private static List<String> rootModels = null;


    static{
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance( DigitalObject.class);

            unmarshaller = jaxbContext.createUnmarshaller();


        } catch (Exception e) {
            log.log(Level.SEVERE,"Cannot init JAXB", e);
            throw new RuntimeException(e);
        }

        rootModels = Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.topLevelModels"));
        if (rootModels==null){
            rootModels = new ArrayList<String>();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length!=2){
            log.severe("USAGE: ImportDuplicator startPrefix repeats");
            throw new IllegalArgumentException("USAGE: ImportDuplicator startPrefix repeats");
        }
        ImportDuplicator.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), KConfiguration.getInstance().getProperty("import.directory"),Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    }

    public static void ingest(final String url, final String user, final String pwd, String importRoot, int startPrefix, int repeats ) {
        log.info("IMPORT DUPLICATOR:"+url+" "+user+" "+pwd+" "+importRoot+" "+startPrefix+" "+repeats);
        if (KConfiguration.getInstance().getConfiguration().getBoolean("ingest.skip",false)){
            log.info("INGEST CONFIGURED TO BE SKIPPED, RETURNING");
            return;
        }
        long start = System.currentTimeMillis();

        File importFile = new File(importRoot);
        if (!importFile.exists()) {
            log.severe("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
            throw new RuntimeException("Import root folder or control file doesn't exist: " + importFile.getAbsolutePath());
        }



        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(user, pwd.toCharArray());
             }
           });

        FedoraAccess fedoraAccess = null;
        try {
            fedoraAccess = new FedoraAccessImpl(null,null);
            log.info("Instantiated FedoraAccess");
        } catch (IOException e) {
            log.log(Level.SEVERE,"Cannot instantiate FedoraAccess",e);
            throw new RuntimeException(e);
        }
        port = fedoraAccess.getAPIM();


        of = new ObjectFactory();

        List<TitlePidTuple> roots = new ArrayList<TitlePidTuple>();
        if (importFile.isDirectory()){
            visitAllDirsAndFiles(importFile, roots);
        }else{
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(importFile));
            } catch (FileNotFoundException e) {
                log.severe("Import file list "+importFile+" not found: "+e);
                throw new RuntimeException(e);
            }
            try {
                for (String line; (line = reader.readLine()) != null;) {
                    if ("".equals(line)) continue;
                    File importItem = new File(line);
                    if (!importItem.exists()) {
                        log.severe("Import folder doesn't exist: " + importItem.getAbsolutePath());
                        continue;
                    }
                    if (!importItem.isDirectory()){
                        log.severe("Import item is not a folder: " + importItem.getAbsolutePath());
                        continue;
                    }
                    log.info("Importing "+importItem.getAbsolutePath());
                    visitAllDirsAndFiles(importItem, roots);
                }
                reader.close();
            } catch (IOException e) {
                log.severe("Exception reading import list file: " + e);
                throw new RuntimeException(e);
            }
        }
        log.info("LOADED ALL MASTER FILES IN "+((System.currentTimeMillis()-start)/1000.0)+"s, processed "+originals.size()+" files");
        for (int i = startPrefix; i<startPrefix+repeats; i++){
            long batchStart = System.currentTimeMillis();
            for (String original: originals){
                ingest(prefixPIDS(original,i));
            }
            log.info("FINISHED INGESTION OF BATCH "+i+" IN "+((System.currentTimeMillis()-batchStart)/1000.0)+"s, processed "+counter+" files");
            /*for (TitlePidTuple tpt :roots){
                IndexerProcessStarter.spawnIndexer(true, tpt.title+"-"+i, prefixPIDS(tpt.pid,i));
            }
            log.info("ALL ROOT OBJECTS  OF BATCH "+i+" SCHEDULED FOR INDEXING.");
            */
        }
        log.info("ALL BATCHES PROCESSED IN "+((System.currentTimeMillis()-start)/1000.0)+"s. INGESTED "+originals.size()*repeats +" OBJECTS. LAST PREFIX: "+(startPrefix+repeats-1));
    }


    private static String prefixPIDS (String original, int prefix){
        return original.replace("uuid:", "uuid:"+prefix+"-");
    }

    private static void visitAllDirsAndFiles(File importFile, List<TitlePidTuple> roots) {
        if (importFile.isDirectory()) {

            File[] children = importFile.listFiles();
            if (children.length>1 && children[0].isDirectory()){//Issue 36
                Arrays.sort(children);
            }
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(children[i], roots);
            }
        } else {
            collect(importFile);
            checkRoot(importFile, roots);
        }
    }

    private static List<String> originals = new LinkedList<String>();

    private static void collect(File file) {
        if (!file.getName().toLowerCase().endsWith(".xml")){
            return;
        }
        try {
            //System.out.println("Processing:"+file.getAbsolutePath());
            FileInputStream is = new FileInputStream(file);
            // Get the size of the file
            long length = file.length();

            if (length > Integer.MAX_VALUE) {
                throw new RuntimeException("File is too large: " + file.getName());
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new RuntimeException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();

            originals.add(new String(bytes,Charset.forName("UTF-8")));

        } catch (Exception ex) {
            log.log(Level.SEVERE,"Ingestion error ",ex);
            throw new RuntimeException(ex);
        }
    }


    private static void ingest(String  content) {

        try {
            long start = System.currentTimeMillis();

            // Create the byte array to hold the data
            byte[] bytes = content.getBytes("UTF-8");


            String pid = "";
            try {
                pid = port.ingest(bytes, "info:fedora/fedora-system:FOXML-1.1", "Initial ingest");
            } catch (SOAPFaultException sfex) {

                if (sfex.getMessage().contains("ObjectExistsException")) {
                    merge(bytes);
                }else{
                    log.severe("Ingest SOAP fault:"+sfex);
                    throw new RuntimeException(sfex);
                }
            }
            counter++;
            //log.info("Ingested:" + pid + " in " + (System.currentTimeMillis() - start) + "ms, count:"+counter);

        } catch (Exception ex) {
            log.log(Level.SEVERE,"Ingestion error ",ex);
            throw new RuntimeException(ex);
        }
    }



    private static void merge(byte[] bytes) {
        List<RDFTuple> ingested = readRDF(bytes);
        if (ingested.isEmpty()) {
            return;
        }
        String pid = ingested.get(0).subject.substring("info:fedora/".length());
        List<RelationshipTuple> existingWS = port.getRelationships(pid, null);
        List<RDFTuple> existing = new ArrayList<RDFTuple>(existingWS.size());
        for (RelationshipTuple t : existingWS) {
            existing.add(new RDFTuple(t.getSubject(), t.getPredicate(), t.getObject()));
        }
        ingested.removeAll(existing);
        for (RDFTuple t : ingested) {
            if (t.object != null){
                try{
                    port.addRelationship(t.subject.substring("info:fedora/".length()), t.predicate, t.object, false, null);
                }catch (Exception ex){
                    log.severe("WARNING- could not add relationship:"+t+"("+ex+")");
                }
            }
        }
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
                        retval.add(new RDFTuple(subject, predicate, object));
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


    /**
     * Parse FOXML file and if it has model in fedora.topLevelModels, add its PID to roots list. Objects in the roots list then will be submitted to indexer
     */
    private static void checkRoot(File importFile, List<TitlePidTuple> roots){
        try{
            if (importFile == null) return;
            if (!importFile.getName().toLowerCase().endsWith(".xml")){
                return;
            }
            Object obj = unmarshaller.unmarshal(importFile);
            if (obj instanceof DigitalObject){
                DigitalObject dobj = (DigitalObject)obj;
                for (DatastreamType ds : dobj.getDatastream()){
                    if("DC".equals(ds.getID())){
                        List<DatastreamVersionType> versions = ds.getDatastreamVersion();
                        if (versions!= null){
                            DatastreamVersionType v = versions.get(versions.size()-1);
                            XmlContentType dcxml = v.getXmlContent();
                            List<Element> elements = dcxml.getAny();
                            for (Element el:elements){
                                NodeList types = el.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "type");
                                for (int i= 0; i<types.getLength();i++){
                                    String type = types.item(i).getTextContent();
                                    if (type.startsWith("model:")){
                                        String model = type.substring(6);

                                        if (rootModels.contains(model)){
                                            NodeList titles = el.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
                                            String title = "";
                                            if (titles.getLength()>0){
                                                title = titles.item(0).getTextContent();
                                            }
                                            TitlePidTuple npt = new TitlePidTuple(title, dobj.getPID());
                                            roots.add(npt);
                                            log.info("Found object for indexing - "+npt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }catch (Exception ex){
            log.log(Level.WARNING,"Error in Ingest.checkRoot for file "+importFile.getName()+", file cannot be checked for auto-indexing : " + ex);
        }
    }


    static class RDFTuple {
        String subject;
        String predicate;
        String object;

        public RDFTuple(String subject, String predicate, String object) {
            super();
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((object == null) ? 0 : object.hashCode());
            result = prime * result + ((predicate == null) ? 0 : predicate.hashCode());
            result = prime * result + ((subject == null) ? 0 : subject.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof RDFTuple))
                return false;
            RDFTuple other = (RDFTuple) obj;
            if (object == null) {
                if (other.object != null)
                    return false;
            } else if (!object.equals(other.object))
                return false;
            if (predicate == null) {
                if (other.predicate != null)
                    return false;
            } else if (!predicate.equals(other.predicate))
                return false;
            if (subject == null) {
                if (other.subject != null)
                    return false;
            } else if (!subject.equals(other.subject))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "RDFTuple [subject=" + subject + ", predicate=" + predicate + ", object=" + object + "]";
        }

    }

    static class TitlePidTuple {
        public String title;
        public String pid;

        public TitlePidTuple(String name, String pid){
            this.title = name;
            this.pid=pid;
        }

        @Override
        public String toString() {
            return "Title:"+title+" PID:"+pid;
        }

    }



}


