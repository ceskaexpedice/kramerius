package cz.incad.kramerius.resourceindex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.qbizm.kramerius.imp.jaxb.DatastreamType;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.impl.AkubraObject;
import cz.incad.kramerius.fedora.om.impl.AkubraUtils;
import cz.incad.kramerius.fedora.om.impl.RELSEXTSPARQLBuilder;
import cz.incad.kramerius.fedora.om.impl.RELSEXTSPARQLBuilderImpl;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.fcrepo.client.FcrepoOperationFailedException;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessingIndexRebuild {
    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexCheck.class.getName());

    private static Unmarshaller unmarshaller = null;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
    }
    private volatile static long counter = 0;


    public static void main(String[] args) throws IOException, SolrServerException, RepositoryException, FcrepoOperationFailedException {
        Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
        final FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
        final ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
        try {
            long start = System.currentTimeMillis();
            feeder.deleteProcessingIndex();
            Path objectStoreRoot = Paths.get(KConfiguration.getInstance().getProperty("objectStore.path"));
            Files.walk(objectStoreRoot).parallel().filter(Files::isRegularFile).forEach(path -> {
                try {
                    FileInputStream inputStream = new FileInputStream(path.toFile());
                    DigitalObject digitalObject = createDigitalObject(inputStream);
                    rebuildProcessingIndex(feeder, digitalObject);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
                }
            });
            LOGGER.info("Finished tree walk in "+ (System.currentTimeMillis() - start)+ " ms");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
        } finally {
            if (feeder != null) {
                feeder.commit();
                LOGGER.info("Feeder commited.");
            }
        }
        fa.shutdown();
    }

    private static DigitalObject createDigitalObject(InputStream inputStream) {
        DigitalObject obj = null;
        try {
            synchronized (unmarshaller) {
                obj = (DigitalObject) unmarshaller.unmarshal(inputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return obj;
    }


    public static void rebuildProcessingIndex(ProcessingIndexFeeder feeder, DigitalObject digitalObject) throws RepositoryException {
        try {
            List<DatastreamType> datastreamList = digitalObject.getDatastream();
            for (DatastreamType datastreamType : datastreamList) {
                if (FedoraUtils.RELS_EXT_STREAM.equals(datastreamType.getID())) {
                    InputStream streamContent = AkubraUtils.getStreamContent(AkubraUtils.getLastStreamVersion(datastreamType), null);
                    AkubraObject akubraObject = new AkubraObject(null, digitalObject.getPID(), digitalObject, feeder);
                    rebuildProcessingIndexImpl(akubraObject, streamContent);
                }
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    private static void rebuildProcessingIndexImpl(AkubraObject akubraObject, InputStream content) throws RepositoryException {
        try {
            String s = IOUtils.toString(content, "UTF-8");
            RELSEXTSPARQLBuilder sparqlBuilder = new RELSEXTSPARQLBuilderImpl();
            sparqlBuilder.sparqlProps(s.trim(), (object, localName) -> {
                akubraObject.processRELSEXTRelationAndFeedProcessingIndex(object, localName);
                return object;
            });
            LOGGER.info("Processed PID:"+ akubraObject.getPid()+ ",  count:"+ (++counter));
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        }
    }
}
