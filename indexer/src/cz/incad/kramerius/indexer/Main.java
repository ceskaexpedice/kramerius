/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.utils.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Administrator
 */
public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) throws Exception {
            LOG.log(Level.INFO, "process args: {0}", Arrays.toString(args));
            ProgramArguments arguments = new ProgramArguments();
            if (!arguments.parse(args)) {
                throw new Exception("Program arguments are invalid: " + Arrays.toString(args));
            }

            try{
                ProcessStarter.updateName("Indexace dokumentu: " + arguments.title);
            } catch (Exception ex) {
                System.out.println("Asi jsme v konzoli");
            }

            Indexer indexer = new Indexer(arguments);
            indexer.run();
    }

    private static void checkFileOrCreateNew(String log4jFile, String resPath) throws IOException {
        File file = new File(log4jFile);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            InputStream resStream = null;
            try {
                FileOutputStream fos = new FileOutputStream(file);
                resStream = Main.class.getResourceAsStream(resPath);
                IOUtils.copyStreams(resStream, fos);
            } finally {
                if (resStream != null) {
                    resStream.close();
                }
            }
        }
    }

    private static void createParentDirs(File file) {
        // TODO Auto-generated method stub
    }
}
