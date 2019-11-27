package cz.cas.lib.knav.indexer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.indexer.Indexer;
import cz.incad.kramerius.indexer.ProgramArguments;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Support multiple 
 * @author pavels
 *
 */
public class BatchIndexerSupport {

    public static final String INDEXING_STAMP = "indexing.stamp";
    
    public static Logger LOGGER = Logger.getLogger(BatchIndexerSupport.class.getName());

    public static void main(String[] args) {
        if (args.length > 2) {
            
            // Create stamp file - restart posibility
            try {
                createIndexingStamp(args);
            } catch (IOException e1) {
                LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
            }
            String act = args[0];

            LOGGER.info("starting indexer utility for pid(s) "+pidsToIndex(args));
            try {
                ProgramArguments parsedIndexerArguments = new ProgramArguments();
                String[] indexerArgs = {act,pidsToIndex(args)};
                if (parsedIndexerArguments.parse(indexerArgs)) {
                    Indexer indexer = new Indexer(parsedIndexerArguments);
                    indexer.run();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

        }
    }

    public static String pidsToIndex(String[] args) {
        StringBuilder builder = new StringBuilder();
        String separatorString = KConfiguration.getInstance().getConfiguration().getString("indexer.pidSeparator", ";");
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                builder.append(separatorString);
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private static void createIndexingStamp(String[] args) throws IOException {
        PrintWriter stampWriter = null;
        try {
            File stampFile = new File(System.getProperty("user.dir")+File.separator+INDEXING_STAMP);
            FileWriter fwriter = new FileWriter(stampFile);
            stampWriter = new PrintWriter(fwriter);
            for (int i = 1; i < args.length; i++) {
                stampWriter.println(args[i]);
            }
        } finally {
            IOUtils.tryClose(stampWriter);
        }
    }
}
