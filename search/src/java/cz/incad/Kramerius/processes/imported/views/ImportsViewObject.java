package cz.incad.Kramerius.processes.imported.views;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcess;

public class ImportsViewObject {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ImportsViewObject.class.getName());
	
	private File successLog;
	private File failureLog;
	
	private List<SuccessfulImportViewObject> imported = new ArrayList<SuccessfulImportViewObject>();
	private List<FailedImportViewObject> failed = new ArrayList<FailedImportViewObject>();
	
	public ImportsViewObject(LRProcess lrProcess) {
		super();
		File processWorkingDirectory = lrProcess.processWorkingDirectory();
		this.successLog = new File(processWorkingDirectory, "replication-success.txt");
		this.failureLog = new File(processWorkingDirectory, "replication-failed.txt");
		try {
			readFiles();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public ImportsViewObject(File processWrokingDir) {
        this.successLog = new File(processWrokingDir, "replication-success.txt");
        this.failureLog = new File(processWrokingDir, "replication-failed.txt");
        try {
            readFiles();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
	}
	
	
	
	private void readFiles() throws IOException {
		FileReader reader = new FileReader(this.successLog);
		BufferedReader bufReader = null;
		try  {
			bufReader = new BufferedReader(reader);
			String line = null;
			while((line=bufReader.readLine()) != null) {
				SuccessfulImportViewObject itm =  readSucceedItem(line);
				if (itm != null) {
					this.imported.add(itm);
				}
			}
		} finally {
			try {
				bufReader.close();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			try{
				reader.close();
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		reader = new FileReader(this.failureLog);
		try  {
			bufReader = new BufferedReader(reader);
			String line = null;
			while((line=bufReader.readLine()) != null) {
				FailedImportViewObject itm =  readFailedItem(line);
				if (itm != null) {
					this.failed.add(itm);
				}
			}
		} finally {
			try {
				bufReader.close();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			try{
				reader.close();
			}catch(Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	private SuccessfulImportViewObject readSucceedItem(String line) {
	    int indexOf = line.indexOf('\t');
	    
	    StringTokenizer tokenizer = new StringTokenizer(line,"\t");
		String data = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String name = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String href = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		SuccessfulImportViewObject viewObject = new SuccessfulImportViewObject(data,name, href);
		return viewObject;
	}
	
	private FailedImportViewObject readFailedItem(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line,"\t");
		String data = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String excp = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		FailedImportViewObject fviewObj = new FailedImportViewObject(data,excp);
		return fviewObj;
	}

	public List<SuccessfulImportViewObject> getItems() {
		return imported;
	}
	
	public List<FailedImportViewObject> getFails() {
		return this.failed;
	}
}
