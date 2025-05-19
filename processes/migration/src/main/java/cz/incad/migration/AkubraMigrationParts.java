package cz.incad.migration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.processingindex.ProcessingIndex;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.resourceindex.ProcessingIndexRebuild.rebuildProcessingIndex;
import static cz.incad.migration.LegacyMigrationParts.LOG_MESSAGE_ITERATION;

public enum AkubraMigrationParts {


    OBJECT_AND_STREAMS {
        @Override
        public void doMigrationPart(String[] args) throws SQLException, IOException, SAXException {
            long start = System.currentTimeMillis();
            AkubraRepository akubraRepository = null;
            try {
                String objectSource = KConfiguration.getInstance().getProperty("objectStore.migrationsource");
                String objectPaths = KConfiguration.getInstance().getProperty("objectStore.path");
                String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");


                String datastreamSource = KConfiguration.getInstance().getProperty("datastreamStore.migrationsource");
                String datastreamPaths = KConfiguration.getInstance().getProperty("datastreamStore.path");
                String datastreamPattern = KConfiguration.getInstance().getProperty("datastreamStore.pattern");

                Injector injector = Guice.createInjector(new SolrModule(), new RepoModule(), new NullStatisticsModule());
                akubraRepository = injector.getInstance(AkubraRepository.class);
                final boolean rebuildProcessingIndex = "true".equalsIgnoreCase(args[1]);
                processRoot(akubraRepository, datastreamSource, datastreamPaths, datastreamPattern, false);
                processRoot(akubraRepository, objectSource, objectPaths, objectPattern, rebuildProcessingIndex);

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            } finally {
                akubraRepository.shutdown();
                long stop = System.currentTimeMillis();
                LOGGER.info("AKubra repository restructured in " + (stop - start) + " ms");
            }
        }

    };

    private static void processRoot(AkubraRepository akubraRepository, String datastreamSource, String datastreamPaths, String datastreamPattern, boolean rebuildProcessingIndex) throws IOException, SolrServerException {
        try {

            if (rebuildProcessingIndex) {
                akubraRepository.pi().deleteProcessingIndex();
            }
            Path objectStoreRoot = Paths.get(datastreamSource);
            HashPathIdMapper idMapper = new HashPathIdMapper(datastreamPattern);
            final AtomicInteger currentIteration = new AtomicInteger(0);
            Files.walk(objectStoreRoot).parallel().filter(Files::isRegularFile).forEach(path -> {
                try {
                    if ((currentIteration.incrementAndGet() % LOG_MESSAGE_ITERATION) == 0) {
                        LOGGER.info("Migrated " + currentIteration + " items.");
                    }
                    String filename = "";
                    try {
                        filename = java.net.URLDecoder.decode(path.getFileName().toString(), StandardCharsets.UTF_8.name());
                        filename = filename.replace("info:fedora/", "");
                        filename = filename.replace("/", "+");
                    } catch (UnsupportedEncodingException e) {
                        // not going to happen - value came from JDK's own StandardCharsets
                    }
                    String internalId = idMapper.getInternalId(LegacyMigrationParts.getBlobId(filename)).toString();
                    String subdirPath = internalId.substring(internalId.indexOf(":") + 1, internalId.lastIndexOf("/"));
                    String targetFileName = internalId.substring(internalId.lastIndexOf("/") + 1);
                    File directory = new File(datastreamPaths, subdirPath);
                    directory.mkdirs();

                    File targetFile = new File(directory, targetFileName);
                    boolean renamed = path.toFile().renameTo(targetFile);
                    if (!renamed) {
                        throw new RuntimeException("Cannot rename file " + path + " to " + targetFile.getAbsolutePath());
                    }

                    if (rebuildProcessingIndex) {
                        FileInputStream inputStream = new FileInputStream(targetFile);
                        DigitalObject digitalObject = LegacyMigrationParts.createDigitalObject(inputStream);
                        rebuildProcessingIndex(akubraRepository, digitalObject, false);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
        } finally {
            akubraRepository.pi().commit();
            LOGGER.info("Feeder commited.");
        }
    }


    abstract void doMigrationPart(String[] args) throws SQLException, IOException, SAXException;


    static Logger LOGGER = Logger.getLogger(AkubraMigrationParts.class.getName());


}
