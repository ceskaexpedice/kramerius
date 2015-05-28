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
            for (int i = 1; i < args.length; i++) {
                LOGGER.info("starting indexer utility for pid "+args[i]);
                try {
                    ProgramArguments parsedIndexerArguments = new ProgramArguments();
                    String[] indexerArgs = {act,args[i]};
                    if (parsedIndexerArguments.parse(indexerArgs)) {
                        Indexer indexer = new Indexer(parsedIndexerArguments);
                        indexer.run();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
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
