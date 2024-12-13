package cz.incad.migration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.akubraproject.map.IdMapper;
import org.apache.solr.client.solrj.SolrServerException;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
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
            try {
                String objectSource = KConfiguration.getInstance().getProperty("objectStore.migrationsource");
                String objectPaths = KConfiguration.getInstance().getProperty("objectStore.path");
                String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");


                String datastreamSource = KConfiguration.getInstance().getProperty("datastreamStore.migrationsource");
                String datastreamPaths = KConfiguration.getInstance().getProperty("datastreamStore.path");
                String datastreamPattern = KConfiguration.getInstance().getProperty("datastreamStore.pattern");

                Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule());
                final ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);
                final boolean rebuildProcessingIndex = "true".equalsIgnoreCase(args[1]);
                processRoot( feeder,  datastreamSource,  datastreamPaths,  datastreamPattern,  false);
                processRoot( feeder,  objectSource,  objectPaths,  objectPattern,  rebuildProcessingIndex);

            }catch(Exception ex) {
                throw  new RuntimeException(ex);
            } finally {
                long stop = System.currentTimeMillis();
                LOGGER.info("AKubra repository restructured in "+(stop - start )+ " ms");
            }
        }

    };

    private static void processRoot(ProcessingIndexFeeder feeder, String datastreamSource, String datastreamPaths, String datastreamPattern, boolean rebuildProcessingIndex) throws IOException, SolrServerException {
        try {

            if (rebuildProcessingIndex) {
                feeder.deleteProcessingIndex();
            }
            Path objectStoreRoot = Paths.get(datastreamSource);
            IdMapper idMapper = new HashPathIdMapper(datastreamPattern);
            final AtomicInteger currentIteration = new AtomicInteger(0);
            Files.walk(objectStoreRoot).parallel().filter(Files::isRegularFile).forEach(path -> {
                try {
                    if ((currentIteration.incrementAndGet() % LOG_MESSAGE_ITERATION) == 0) {
                        LOGGER.info("Migrated " + currentIteration + " items.");
                    }
                    String filename = "";try {
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
                        rebuildProcessingIndex(feeder, digitalObject, false);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
                }
            });

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
        } finally {
            if (feeder != null) {
                feeder.commit();
                LOGGER.info("Feeder commited.");
            }
        }
    }


    abstract  void doMigrationPart(String[] args) throws SQLException, IOException, SAXException;



    static Logger LOGGER = Logger.getLogger(AkubraMigrationParts.class.getName());



}
