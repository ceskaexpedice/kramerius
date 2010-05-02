package cz.incad.Kramerius.views;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcess;

public class ProcessLogsViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProcessLogsViewObject.class.getName());
	
	private String stdFrom;
	private String errFrom;
	private String count;
	private LRProcess process;
	
	public ProcessLogsViewObject(String stdFrom, String errFrom, String count, LRProcess process) {
		super();
		this.stdFrom = stdFrom != null ? stdFrom : "0";
		this.errFrom = errFrom != null ? errFrom : "0";
		this.count = count != null ? count : "20";
		this.process = process;
	}
	public String getErrOutData() {
		try {
			return  linesFromIS(this.process.getErrorProcessOutputStream(), this.errFrom);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return "";
	}
	public String getStdOutData() {
		try {
			return  linesFromIS(this.process.getStandardProcessOutputStream(), this.stdFrom);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return "";
		
	}

	private String linesFromIS(InputStream is, String from) throws IOException {
		int fromI = Integer.parseInt(from);
		int countI = Integer.parseInt(this.count);
		int pocitadlo = 0;
		StringBuffer buffer =  new StringBuffer();
		try {
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while((line = bufReader.readLine()) != null) {
				if (pocitadlo> (fromI + countI)) return buffer.toString();

				if (pocitadlo >= fromI) {
					buffer.append(line).append('\n');
				}

				pocitadlo += 1;
			}
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return buffer.toString();
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

}
