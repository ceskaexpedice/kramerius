package cz.incad.kramerius.rest.api.processes;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * jen docasne takto, bude potreba refactoring
 *
 * @see cz.incad.Kramerius.views.ProcessLogsViewObject
 */
public class ProcessLogsHelper {

    public static Logger LOGGER = Logger.getLogger(ProcessLogsHelper.class.getName());

    private final LRProcess process;
    private LRProcessDefinition definition;

    //offsety
    //TODO: presunout do metod a nema to byt string;
    private String stdFrom = "0";
    private String errFrom = "0";

    //limit
    //TODO: presunout do metod a nema to byt string;
    private String count = "50";

    public ProcessLogsHelper(LRProcess process, LRProcessDefinition definition) {
        this.process = process;
        this.definition = definition;
    }

    public long getErrorFileSize() throws IOException {
        RandomAccessFile errorProcessRAFile = null;
        try {
            errorProcessRAFile = this.process.getErrorProcessRAFile();
            return errorProcessRAFile.length();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, ex.getMessage(), ex);
            return 0;
        } finally {
            if (errorProcessRAFile != null) errorProcessRAFile.close();
        }
    }

    public long getStdFileSize() throws IOException {
        RandomAccessFile stdProcessRAFile = null;
        try {
            stdProcessRAFile = this.process.getStandardProcessRAFile();
            return stdProcessRAFile.length();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, ex.getMessage(), ex);
            return 0;
        } finally {
            if (stdProcessRAFile != null) stdProcessRAFile.close();
        }
    }

    public File getStdOutDirectory() {
        return new File(getProcessWorkingDirectory().getAbsolutePath() + File.separator + this.definition.getStandardStreamFolder() + File.separator + "stout.out");
    }

    public File getErrOutDirectory() {
        return new File(getProcessWorkingDirectory().getAbsolutePath() + File.separator + this.definition.getErrStreamFolder() + File.separator + "sterr.err");
    }

    public String getErrOutData() {
        RandomAccessFile raf = null;
        try {
            raf = this.process.getErrorProcessRAFile();
            return bufferFromRAF(raf, this.errFrom);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
        } finally {
            try {
                if (raf != null) raf.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        }
        return "";
    }

    public String getProcessUUID() {
        return process.getUUID();
    }

    public File getProcessWorkingDirectory() {
        return process.processWorkingDirectory();
    }

    public String getStdOutData() {
        RandomAccessFile raf = null;
        try {
            raf = this.process.getStandardProcessRAFile();
            return bufferFromRAF(raf, this.stdFrom);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
        return "";
    }

    private String bufferFromRAF(RandomAccessFile raf, String from) throws IOException {
        long fromL = Long.parseLong(from);
        long countL = Long.parseLong(this.count);

        byte[] buffer = new byte[(int) countL];
        raf.seek(fromL);
        int read = raf.read(buffer);
        if (read >= 0) {
            byte[] nbuffer = new byte[read];
            System.arraycopy(buffer, 0, nbuffer, 0, read);
            return new String(nbuffer);
        } else return "";
    }

}
