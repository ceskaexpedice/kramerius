package cz.incad.migration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.fcrepo.common.Constants;
import org.fcrepo.common.FaultException;
import org.fcrepo.common.PID;
import org.fcrepo.server.errors.MalformedPidException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSOutput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.resourceindex.ProcessingIndexRebuild.rebuildProcessingIndex;
import static cz.incad.migration.Utils.DOMIMPL;
import static cz.incad.migration.Utils.SERIALIZER;

/**
 * Set of parts dedicated for moving data from MZK to CDK
 */
public enum LegacyMigrationParts {


    /**
     * move and rename streams
     */
    STREAMS {
        @Override
        public void doMigrationPart(Connection db, String[] args) throws SQLException {
            String datastreamPaths = KConfiguration.getInstance().getProperty("datastreamStore.path");
            String datastreamPattern = KConfiguration.getInstance().getProperty("datastreamStore.pattern");
            dbSelect(db, "datastreampaths", new File(datastreamPaths), datastreamPattern, "select * from datastreampaths where tokendbid >= " + args[1] + " and tokendbid < " + args[2] + " order by tokendbid", args, (f) -> {
            });
        }
    },

    /**
     * move reaname and change mzk objects
     */
    OBJECTS {
        @Override
        public void doMigrationPart(Connection db, String[] args) throws SQLException {
            Injector injector = Guice.createInjector(new SolrModule(), new RepoModule());
            final AkubraRepository akubraRepository = injector.getInstance(AkubraRepository.class);

            String objectPaths = KConfiguration.getInstance().getProperty("objectStore.path");
            String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");
            Consumer<File> consumer = null;
            if (args.length >= 6) { // kdyz mame vice jak 6 parametru, prevadime po kouskach z LEGACY do Akubry.
                // pri prevodu po kouskach nemuzeme poustet create processing index.
                args[5] = "false";
            }
            if ("true".equalsIgnoreCase(args[5])) {
                try {
                    akubraRepository.pi().deleteProcessingIndex();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error in deleteProcessingIndex: ", e);
                }
                consumer = (f) -> {
                    try {
                        FileInputStream inputStream = new FileInputStream(f);
                        DigitalObject digitalObject = createDigitalObject(inputStream);
                        rebuildProcessingIndex(akubraRepository, digitalObject, false);
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
                    }
                };
            } else {
                consumer = f -> {
                };
            }
            dbSelect(db, "objectpaths", new File(objectPaths), objectPattern, "select * from objectpaths where tokendbid >= " + args[3] + " and tokendbid < " + args[4] + " order by tokendbid", args, consumer);
        }


    };

    private static void print(Document parsed, FileOutputStream fos) {
        LSOutput lsOutput = DOMIMPL.createLSOutput();
        lsOutput.setByteStream(fos);
        SERIALIZER.write(parsed, lsOutput);
    }

    private static void dbSelect(Connection db, String tablename, File targetDir, String directoryPattern, String sqlCommand, String[] args, Consumer<File> consumer) throws SQLException {
        HashPathIdMapper idMapper = new HashPathIdMapper(directoryPattern);
        final long start = System.currentTimeMillis();
        final AtomicInteger currentIteration = new AtomicInteger(0);
        List<Pair<String, String>> ids = new JDBCQueryTemplate<Pair<String, String>>(db, false) {

            public boolean handleRow(ResultSet rs, List<Pair<String, String>> returnsList) throws SQLException {
                if ((currentIteration.incrementAndGet() % LOG_MESSAGE_ITERATION) == 0) {
                    long stop = System.currentTimeMillis();
                    LOGGER.info("Iteration " + currentIteration + " finished after " + (stop - start) + " ms ");
                    LOGGER.info("Last processed tokendbid in  " + tablename + ": " + rs.getLong("tokendbid"));
                }


                String token = rs.getString("token");
                String path = rs.getString("path");


                File objectFile = new File(path);
                if (objectFile.exists()) {
                    String internalId = idMapper.getInternalId(getBlobId(token)).toString();
                    String subdirPath = internalId.substring(internalId.indexOf(":") + 1, internalId.lastIndexOf("/"));
                    String targetFileName = internalId.substring(internalId.lastIndexOf("/") + 1);
                    File directory = new File(targetDir, subdirPath);
                    directory.mkdirs();

                    long start = System.currentTimeMillis();
                    File targetFile = new File(directory, targetFileName);
                    if (args.length == 7) {
                        if ("-m".equalsIgnoreCase(args[6])) {
                            boolean renamed = objectFile.renameTo(targetFile);
                            //long stop2 = System.currentTimeMillis();
                            //LOGGER.info("\t--> objectFile.renameTo(targetFile):" + (stop2 - start) + " ms ");
                            if (!renamed) {
                                throw new RuntimeException("Cannot rename file " + objectFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                            }
                        }

                        if ("-c".equalsIgnoreCase(args[6])) {
                            try {
                                FileUtils.copyFile(objectFile, targetFile, true); // preserve file date = true
                                File targetFileForControl = new File(directory, targetFileName);
                                boolean contentEquals = FileUtils.contentEquals(objectFile, targetFileForControl);
                                //long stop2 = System.currentTimeMillis();
                                //LOGGER.info("\t--> FileUtils.copyFile("+objectFile+", "+targetFile+"):" + (stop2 - start) + " ms ");
                                if (!contentEquals) {
                                    throw new RuntimeException("Bad copy file " + objectFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                                }
                            } catch (IOException ioe) {
                                LOGGER.info("IOException " + objectFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath() + " - " + ioe);
                            }
                        }

                        consumer.accept(new File(directory, Utils.encode("info:fedora/" + token)));
                    }
                    return true;
                } else {
                    return true;
                }
            }
        }.executeQuery(sqlCommand);
    }

    static DigitalObject createDigitalObject(InputStream inputStream) {
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

    abstract void doMigrationPart(Connection connection, String[] args) throws SQLException;

    private static Logger LOGGER = Logger.getLogger(LegacyMigrationParts.class.getName());

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


    // Message after 50 iterations
    static int LOG_MESSAGE_ITERATION = KConfiguration.getInstance().getConfiguration().getInt("akubra.migration.logfrequency", 10000);

    static URI getBlobId(String token) {
        try {
            int i = token.indexOf('+');
            if (i == -1) {
                return new URI(new PID(token).toURI());
            } else {
                String[] dsParts = token.substring(i + 1).split("\\+");
                if (dsParts.length != 2) {
                    throw new IllegalArgumentException(
                            "Malformed datastream token: " + token);
                }
                return new URI(Constants.FEDORA.uri
                        + token.substring(0, i) + "/"
                        + uriEncode(dsParts[0]) + "/"
                        + uriEncode(dsParts[1]));
            }
        } catch (MalformedPidException e) {
            throw new IllegalArgumentException(
                    "Malformed object token: " + token, e);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Malformed object or datastream token: " + token, e);
        }
    }

    private static String uriEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException wontHappen) {
            throw new FaultException(wontHappen);
        }
    }

}


