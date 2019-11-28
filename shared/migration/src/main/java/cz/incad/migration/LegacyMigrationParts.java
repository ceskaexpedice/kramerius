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
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSOutput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Stack;
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
            dbSelect(db,"datastreampaths", new File(datastreamPaths), "select * from datastreampaths where tokendbid > "+args[1]+" order by tokendbid", (f) -> {
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
            dbSelect(db,"objectpaths", new File(objectPaths), "select * from objectpaths where tokendbid > "+args[2]+" order by tokendbid", (f) -> {
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

    private static void dbSelect(Connection db, String tablename, File targetDir, String sqlCommand, Consumer<File> consumer) throws SQLException {
        final long start = System.currentTimeMillis();
        Stack<Integer> stack = new Stack<>();
        stack.push(new Integer(0));
        int counter = 0;
        List<Pair<String, String>> ids = new JDBCQueryTemplate<Pair<String, String>>(db, false) {

            public boolean handleRow(ResultSet rs, List<Pair<String, String>> returnsList) throws SQLException {
                Integer currentIteration = stack.pop();
                if ((currentIteration % LOG_MESSAGE_ITERATION) == 0) {
                    long stop = System.currentTimeMillis();
                    LOGGER.info("Current iteration " + currentIteration + " and took " + (stop - start) + " ms ");
                    LOGGER.info("Last tokendbid for  " + tablename + ": " + rs.getLong("tokendbid"));
                }
                stack.push(new Integer(currentIteration.intValue() + 1));

                String token = rs.getString("token");
                String path = rs.getString("path");

                token = token.replaceAll("\\+", "/");
                String hex = Utils.asHex(MD5.digest(("info:fedora/" + token).getBytes(Charset.forName("UTF-8"))));

                File objectFile = new File(path);
                if (objectFile.exists()) {

                    File directory = Utils.directory(targetDir, hex, 2, 2);
                    directory.mkdirs();

                    long start = System.currentTimeMillis();
                    File targetFile = new File(directory, Utils.encode("info:fedora/" + token));
                    boolean renamed = objectFile.renameTo(targetFile);
                    long stop = System.currentTimeMillis();
                    LOGGER.info("\t--> objectFile.renameTo(targetFile):" + (stop - start) + " ms ");
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

}
