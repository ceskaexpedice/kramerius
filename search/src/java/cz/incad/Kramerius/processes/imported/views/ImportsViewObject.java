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
	
	private List<ImportedItemViewObject> imports = new ArrayList<ImportedItemViewObject>();
	
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
	
	private void readFiles() throws IOException {
		FileReader reader = new FileReader(this.successLog);
		BufferedReader bufReader = new BufferedReader(reader);
		String line = null;
		while((line=bufReader.readLine()) != null) {
			ImportedItemViewObject itm =  readItemViewObject(line);
			if (itm != null) {
				this.imports.add(itm);
			}
		}
	}

	private ImportedItemViewObject readItemViewObject(String line) {
		StringTokenizer tokenizer = new StringTokenizer(line,"\t");
		String data = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String name = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		String href = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
		ImportedItemViewObject viewObject = new ImportedItemViewObject(data,name, href);
		return viewObject;
	}

	public List<ImportedItemViewObject> getItems() {
		return imports;
	}
	
	
	
}
