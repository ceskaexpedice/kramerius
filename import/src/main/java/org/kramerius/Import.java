package org.kramerius;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;
import org.fedora.api.ObjectFactory;
import org.fedora.api.RelationshipTuple;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class Import {
    static FedoraAPIMService service;
    static FedoraAPIM port;
    static ObjectFactory of;
    static int counter = 0;

    private static final Logger log = Logger.getLogger(Import.class.getName());

    /**
     * @param args
     */
    public static void main(String[] args) {
        Import.ingest(KConfiguration.getInstance().getProperty("ingest.url"), KConfiguration.getInstance().getProperty("ingest.user"), KConfiguration.getInstance().getProperty("ingest.password"), KConfiguration.getInstance().getProperty("import.directory"));
    }

    public static void ingest(final String url, final String user, final String pwd, String importRoot) {
        log.info("INGEST:"+url+user+pwd+importRoot);
        if (KConfiguration.getInstance().getConfiguration().getBoolean("ingest.skip",false)){
            log.info("INGEST CONFIGURED TO BE SKIPPED, RETURNING");
            return;
        }
        long start = System.currentTimeMillis();

        File importFile = new File(importRoot);
        if (!importFile.exists()) {
            log.severe("Import root folder doesn't exist: " + importFile.getAbsolutePath());
            return;
        }



        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
               return new PasswordAuthentication(user, pwd.toCharArray());
             }
           });

        FedoraAccess fedoraAccess = null;
        try {
            fedoraAccess = new FedoraAccessImpl(null);
            log.info("Instantiated FedoraAccess");
        } catch (IOException e) {
            log.log(Level.SEVERE,"Cannot instantiate FedoraAccess",e);
            throw new RuntimeException(e);
        }
        port = fedoraAccess.getAPIM();


        of = new ObjectFactory();

        visitAllDirsAndFiles(importFile);
        log.info("FINISHED INGESTION IN "+((System.currentTimeMillis()-start)/1000.0)+"s, processed "+counter+" files");
    }

    private static void visitAllDirsAndFiles(File importFile) {
        if (importFile.isDirectory()) {

            File[] children = importFile.listFiles();
            if (children.length>1 && children[0].isDirectory()){//Issue 36
                Arrays.sort(children);
            }
            for (int i = 0; i < children.length; i++) {
                visitAllDirsAndFiles(children[i]);
            }
        } else {
            ingest(importFile);
        }
    }

    private static void ingest(File file) {
        if (!file.getName().toLowerCase().endsWith(".xml")){
            return;
        }
        try {
            long start = System.currentTimeMillis();
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
            log.info("Ingested:" + pid + " in " + (System.currentTimeMillis() - start) + "ms, count:"+counter);

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
}

class RDFTuple {
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
