package cz.incad.kramerius.rest.apiNew.admin.v10.processes;

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
     */
    public long getLogsFileSize(LogType type) {
        RandomAccessFile errorProcessRAFile = null;
        try {
            errorProcessRAFile = getProcessRAFile(type);
            return errorProcessRAFile.length();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, ex.getMessage(), ex);
            return 0;
        } finally {
            try {
                if (errorProcessRAFile != null) errorProcessRAFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
            return readFromRAF(raf, offset, limit);
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

    private String readFromRAF(RandomAccessFile raf, long offset, long limit) throws IOException {
        //ignore data before new line unless it is the very beginning of the file
        //we don't want to start in the middle of the line, so we rather return less then limit by cutting the beginning
        if (offset != 0) {
            raf.seek(offset);
            String remainsOfPreviousLine = raf.readLine();
            offset = offset + remainsOfPreviousLine.length();
            limit = limit + remainsOfPreviousLine.length();
        }
        byte[] buffer = new byte[(int) limit];
        raf.seek(offset);
        int read = raf.read(buffer);
        if (read >= 0) {
            byte[] nbuffer = new byte[read];
            System.arraycopy(buffer, 0, nbuffer, 0, read);
            String dataRead = new String(nbuffer);

            //read to the end of line if there's still some data,
            //we don't want to break lines, so we rather return slightly more then limit by adding data until new line
            long newOffset = offset + limit;
            if (newOffset < raf.length()) {
                raf.seek(newOffset);
                String remainsOfCurrentLine = raf.readLine();
                return dataRead + remainsOfCurrentLine;
            } else {
                return dataRead;
            }
        } else {
            return "";
        }
    }

}
