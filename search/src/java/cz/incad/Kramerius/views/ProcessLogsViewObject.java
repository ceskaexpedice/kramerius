package cz.incad.Kramerius.views;

import cz.incad.kramerius.processes.LRDefinitionAction;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.logging.Level;

public class ProcessLogsViewObject {

    public static final int BUFFER_SIZE = 1 << 9;
    
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessLogsViewObject.class.getName());
	
	private String stdFrom;
	private String errFrom;
	
	
	private String count;
	private LRProcess process;
	private LRProcessDefinition definition;
	private ResourceBundle resBundle;
	
	public ProcessLogsViewObject(String stdFrom, String errFrom,  String count, LRProcess process, LRProcessDefinition definition, ResourceBundle re) {
		super();
		this.stdFrom = stdFrom != null ? stdFrom : "0";
		this.errFrom = errFrom != null ? errFrom : "0";
		
		
		this.count = count != null ? count : "4096";
		this.process = process;
		this.definition = definition;
		this.resBundle = re;
	}
	
	public String getProcessUUID() {
	    return process.getUUID();
	}
	
	public File getProcessWorkingDirectory() {
	    return process.processWorkingDirectory();
	}
	
	public File getStdOutDirectory() {
	    return new File(getProcessWorkingDirectory().getAbsolutePath()+File.separator+ this.definition.getStandardStreamFolder()+File.separator+"stout.out");
	}

	public File getErrOutDirectory() {
        return new File(getProcessWorkingDirectory().getAbsolutePath()+File.separator+ this.definition.getErrStreamFolder()+File.separator+"sterr.err");
	}
	
	public long getErrorFileSize() throws IOException {
	    RandomAccessFile errorProcessRAFile = null;
	    try {
            errorProcessRAFile = this.process.getErrorProcessRAFile();
            return  errorProcessRAFile.length();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE,ex.getMessage(),ex);
            return 0;
        } finally {
            if (errorProcessRAFile !=  null) errorProcessRAFile.close();
        }
	}

	public long getStdFileSize() throws IOException  {
        RandomAccessFile stdProcessRAFile = null;
        try {
            stdProcessRAFile = this.process.getStandardProcessRAFile();
            return  stdProcessRAFile.length();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE,ex.getMessage(),ex);
            return 0;
        } finally {
            if (stdProcessRAFile !=  null) stdProcessRAFile.close();
        }
	}
	
	public String getErrOutData() {
	    RandomAccessFile raf = null;
	    try {
	        raf = this.process.getErrorProcessRAFile();
		    return  bufferFromRAF(raf, this.errFrom);
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

	
	
	public String getStdOutData() {
        RandomAccessFile raf = null;
		try {
            raf = this.process.getStandardProcessRAFile();
            return  bufferFromRAF(raf, this.stdFrom);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.FINE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.FINE, e.getMessage(), e);
		}
		return "";
	}


	
	

	public boolean isActionsDefined() {
	    return !this.definition.getActions().isEmpty();
	}
	
	public List<Map<String, String>> getActionURLs() {
	    List<Map<String, String>> lst = new ArrayList<Map<String, String>>();
        List<LRDefinitionAction> actions = this.definition.getActions();
        for (LRDefinitionAction defAct : actions) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", defAct.getName());
            map.put("url", defAct.getActionURL());
            map.put("i18nName", this.resBundle.getString(defAct.getResourceBundleKey()));
            lst.add(map);
        }
        return lst;
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
	

	public String getNextStandardOutputAHREF() {
		int fromI = Integer.parseInt(this.stdFrom);
		int countI = Integer.parseInt(this.count);
		int next = fromI + countI;
		return  "<a href=\"_processes_logs.jsp?uuid="+this.process.getUUID()+"&stdFrom="+next+"&stdErr="+this.errFrom+"&count="+this.count+"\"> next &gt; </a>";
	}


	public String getNextErrorOutputAHREF() {
		int fromI = Integer.parseInt(this.errFrom);
		int countI = Integer.parseInt(this.count);
		int next = fromI + countI;
		return  "<a href=\"_processes_logs.jsp?uuid="+this.process.getUUID()+"&stdFrom="+this.stdFrom+"&stdErr="+next+"&count="+this.count+"\"> next &gt; </a>";
	}

	public String getPrevStandardOutputAHREF() {
		int fromI = Integer.parseInt(this.stdFrom);
		int countI = Integer.parseInt(this.count);
		int next = fromI - countI;
		return  "<a href=\"_processes_logs.jsp?uuid="+this.process.getUUID()+"&stdFrom="+next+"&stdErr="+this.errFrom+"&count="+this.count+"\"> &lt; prev </a>";
	}


	public String getPrevErrorOutputAHREF() {
		int fromI = Integer.parseInt(this.errFrom);
		int countI = Integer.parseInt(this.count);
		int next = fromI - countI;
		return  "<a href=\"_processes_logs.jsp?uuid="+this.process.getUUID()+"&stdFrom="+this.stdFrom+"&stdErr="+next+"&count="+this.count+"\"> &lt; prev </a>";
	}

	
	
	public static void main(String[] args) {
        System.out.println(BUFFER_SIZE);
    }
	
	
}
