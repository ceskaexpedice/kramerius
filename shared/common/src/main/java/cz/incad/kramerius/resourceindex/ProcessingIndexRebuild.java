package cz.incad.kramerius.resourceindex;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.DatastreamType;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Deklarace procesu je v shared/common/src/main/java/cz/incad/kramerius/processes/res/lp.st (processing_rebuild)
 */
public class ProcessingIndexRebuild {
    // Could be any number between 100 and 500,000. Lower the number, lower memory usage.
    // If it was too low, parallelization would be less effective.
    // If it was too large, memory usage would slower overall execution, due to memory management.
    private static final int MAX_QUEUED_SUBMITTED_TASKS = 10000;

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexCheck.class.getName());

    private static final Unmarshaller unmarshaller = initUnmarshaller();

    private volatile static long counter = 0;


    public static void main(String[] args) throws IOException, SolrServerException {
        if (args.length>=1 && "REBUILDPROCESSING".equalsIgnoreCase(args[0])){
            LOGGER.info("Přebudování Processing indexu");
        } else {
            ProcessStarter.updateName("Přebudování Processing indexu");
        }
        Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
        final AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));

        long start = System.currentTimeMillis();
        akubraRepository.pi().deleteProcessingIndex();
        Path objectStoreRoot = null;
        if (KConfiguration.getInstance().getConfiguration().getBoolean("legacyfs")) {
            objectStoreRoot = Paths.get(KConfiguration.getInstance().getProperty("object_store_base"));
        } else {
            objectStoreRoot = Paths.get(KConfiguration.getInstance().getProperty("objectStore.path"));
        }
        
        boolean exclusiveCommit = KConfiguration.getInstance().getConfiguration().getBoolean("processingIndex.commit", false);
        

        // ForkJoinPool is used to preserve parallelization.
        // The default constructor of ForkJoinPool creates a pool with parallelism
        // equal to Runtime.availableProcessors(), same as parallel streams.
        ForkJoinPool forkJoinPool = new ForkJoinPool();

        // Files.walkFileTree() is used because it does not store any Paths in memory,
        // which makes it a more efficient solution to the problem compared to Files.walk().
        Files.walkFileTree(objectStoreRoot,
                Collections.singleton(FileVisitOption.FOLLOW_LINKS),
                Integer.MAX_VALUE,
                new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!Files.isRegularFile(file)) {
                    return FileVisitResult.CONTINUE;
                }

                if (forkJoinPool.getQueuedSubmissionCount() < MAX_QUEUED_SUBMITTED_TASKS) {
                    forkJoinPool.execute(() -> {
                        String filename = file.toString();
                        try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
                            DigitalObject digitalObject = createDigitalObject(inputStream);
                            rebuildProcessingIndex(akubraRepository, digitalObject, exclusiveCommit);
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Error processing file: " + filename, ex);
                        }
                    });
                } else {
                    String filename = file.toString();
                    try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
                        DigitalObject digitalObject = createDigitalObject(inputStream);
                        rebuildProcessingIndex(akubraRepository, digitalObject, exclusiveCommit);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error processing file: " + filename, ex);
                    }
                }

                return FileVisitResult.CONTINUE;
            }
            

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                LOGGER.log(Level.SEVERE, "Error processing file: " + file.toString(), exc);

                // This will allow the execution to continue uninterrupted,
                // even in the event of encountering permission errors.
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    LOGGER.log(Level.SEVERE, "Error searching directory : " + dir.toString(), exc);
                }

                // This will allow the execution to continue uninterrupted,
                // even in the event of encountering permission errors.
                return FileVisitResult.CONTINUE;
            }
        });

//        Files.walk(objectStoreRoot, FileVisitOption.FOLLOW_LINKS).parallel().filter(Files::isRegularFile).forEach(path -> {
//            String filename = path.toString();
//            try {
//                FileInputStream inputStream = new FileInputStream(path.toFile());
//                DigitalObject digitalObject = createDigitalObject(inputStream);
//                rebuildProcessingIndex(feeder, digitalObject);
//            } catch (Exception ex) {
//                LOGGER.log(Level.SEVERE, "Error processing file: " + filename, ex);
//            }
//        });

        // Wait for all tasks to finish
        forkJoinPool.shutdown();
        try {
            if (!forkJoinPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                LOGGER.severe("ForkJoinPool did not terminate.");
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Interrupted while waiting for ForkJoinPool to terminate", e);
            Thread.currentThread().interrupt();
        }

        LOGGER.info("Finished tree walk in " + (System.currentTimeMillis() - start) + " ms");

        akubraRepository.pi().commit();
        akubraRepository.shutdown();
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


    public static void rebuildProcessingIndex(AkubraRepository akubraRepository, DigitalObject digitalObject, boolean commitAfteringest ) {
        akubraRepository.pi().rebuildProcessingIndex(digitalObject.getPID());
    }

    public static void rebuildProcessingIndex(AkubraRepository akubraRepository, String pid, boolean commitAfteringest ) {
        akubraRepository.pi().rebuildProcessingIndex(pid);
    }

    private static Unmarshaller initUnmarshaller() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);
            return jaxbContext.createUnmarshaller();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
    }
}
