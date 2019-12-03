package cz.incad.migration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.database.JDBCQueryTemplate;
import org.akubraproject.map.IdMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.fcrepo.common.Constants;
import org.fcrepo.common.FaultException;
import org.fcrepo.common.PID;
import org.fcrepo.server.errors.MalformedPidException;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSOutput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static cz.incad.kramerius.resourceindex.ProcessingIndexRebuild.rebuildProcessingIndex;
import static cz.incad.migration.Utils.*;

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
            dbSelect(db,"datastreampaths", new File(datastreamPaths), datastreamPattern,"select * from datastreampaths where tokendbid > "+args[1]+" order by tokendbid", (f) -> {
            });
        }
    },

    /**
     * move reaname and change mzk objects
     */
    OBJECTS {
        @Override
        public void doMigrationPart(Connection db, String[] args) throws SQLException {
            Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule());
            final ProcessingIndexFeeder feeder = injector.getInstance(ProcessingIndexFeeder.class);

            String objectPaths = KConfiguration.getInstance().getProperty("objectStore.path");
            String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");
            dbSelect(db,"objectpaths", new File(objectPaths), objectPattern, "select * from objectpaths where tokendbid > "+args[2]+" order by tokendbid", (f) -> {
                try {
                    FileInputStream inputStream = new FileInputStream(f);
                    DigitalObject digitalObject = createDigitalObject(inputStream);
                    rebuildProcessingIndex(feeder, digitalObject);
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Error processing file: ", ex);
                }
            });
        }


    };

    private static void print(Document parsed, FileOutputStream fos) {
        LSOutput lsOutput = DOMIMPL.createLSOutput();
        lsOutput.setByteStream(fos);
        SERIALIZER.write(parsed, lsOutput);
    }

    private static void dbSelect(Connection db, String tablename, File targetDir, String directoryPattern, String sqlCommand, Consumer<File> consumer) throws SQLException {
        IdMapper idMapper = new HashPathIdMapper(directoryPattern);
        final long start = System.currentTimeMillis();
        //Stack<Integer> stack = new Stack<>();
        //stack.push(new Integer(0));
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
                    String subdirPath = internalId.substring(internalId.indexOf(":")+1, internalId.lastIndexOf("/"));
                    String targetFileName = internalId.substring( internalId.lastIndexOf("/")+1);
                    File directory = new File(targetDir, subdirPath);
                    directory.mkdirs();

                    long start = System.currentTimeMillis();
                    File targetFile = new File(directory, targetFileName);
                    boolean renamed = objectFile.renameTo(targetFile);
                    //long stop2 = System.currentTimeMillis();
                    //LOGGER.info("\t--> objectFile.renameTo(targetFile):" + (stop2 - start) + " ms ");
                    if (!renamed) {
                        throw new RuntimeException("Cannot rename file " + objectFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                    }

                    consumer.accept(new File(directory, Utils.encode("info:fedora/" + token)));

                    return true;
                } else {
                    return true;

                }
            }
        }.executeQuery(sqlCommand);
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


    // Message after 60 iterations
    static int LOG_MESSAGE_ITERATION = 10000;

    private static URI getBlobId(String token) {
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
