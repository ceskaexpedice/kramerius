package cz.incad.kramerius.rest.api.processes;

import cz.incad.kramerius.processes.LRProcess;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see cz.incad.Kramerius.views.ProcessLogsViewObject
 */
public class ProcessLogsHelper {

    public enum LogType {
        OUT,
        ERR
    }

    public static Logger LOGGER = Logger.getLogger(ProcessLogsHelper.class.getName());

    private final LRProcess process;

    public ProcessLogsHelper(LRProcess process) {
        this.process = process;
    }

    /**
     * @param type type of log, either OUT for output log , or ERR for error log
     * @return size of the log file in bytes
     * @throws IOException
     */
    public long getLogsFileSize(LogType type) throws IOException {
        RandomAccessFile errorProcessRAFile = null;
        try {
            errorProcessRAFile = getProcessRAFile(type);
            return errorProcessRAFile.length();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, ex.getMessage(), ex);
            return 0;
        } finally {
            if (errorProcessRAFile != null) errorProcessRAFile.close();
        }
    }

    /**
     * @param type   type of log, either OUT for output log , or ERR for error log
     * @param offset
     * @param limit
     * @return logs by line
     */
    public List<String> getLogsFileData(LogType type, long offset, long limit) {
        String asString = getLogsFileDataAsString(type, offset, limit);
        String[] splitByNewLine = asString.split("\\r?\\n");
        List<String> result = Arrays.asList(splitByNewLine);
        return result;
    }

    private String getLogsFileDataAsString(LogType type, long offset, long limit) {
        RandomAccessFile raf = null;
        try {
            raf = getProcessRAFile(type);
            return bufferFromRAF(raf, offset, limit);
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return "";
        } catch (IOException e) {
            LOGGER.log(Level.FINE, e.getMessage(), e);
            return "";
        } finally {
            try {
                if (raf != null) raf.close();
            } catch (IOException e) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
            }
        }
    }


    private RandomAccessFile getProcessRAFile(LogType type) throws FileNotFoundException {
        switch (type) {
            case OUT:
                return process.getStandardProcessRAFile();
            case ERR:
                return process.getErrorProcessRAFile();
            default:
                throw new RuntimeException();//impossible
        }
    }

    private String bufferFromRAF(RandomAccessFile raf, long offset, long limit) throws IOException {
        byte[] buffer = new byte[(int) limit];
        raf.seek(offset);
        int read = raf.read(buffer);
        if (read >= 0) {
            byte[] nbuffer = new byte[read];
            System.arraycopy(buffer, 0, nbuffer, 0, read);
            return new String(nbuffer);
            //TODO: jeste docist do konce radku a to doplnit. Abysme neusekli log v polovine radku, i kdyz teda vysledek nebude presne limit bytu
            //na zacatku to bud neresit, nebo znovu nacist z na novy radek to pred nim vynechat
            //tim padem ve vysledku by mohl byt vysledek delsi nez limit (napr. pri offset 0 kdyz se limitem netrefim presne na konec radku), anebo kratsi (kdyz se offsetem netrefim na zacatek radku)
        } else {
            return "";
        }
    }

}
