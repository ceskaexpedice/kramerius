package cz.incad.kramerius.processes.os.impl.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.processes.os.impl.windows.csv.CSVLexer;
import cz.incad.kramerius.processes.os.impl.windows.csv.CSVParser;

public class WindowsPIDListProcessOutput {
	
	public static List<String> pids(BufferedReader reader) throws IOException {
		List<String> pids = new ArrayList<String>();
		try {
			CSVParser parser = new CSVParser(new CSVLexer(reader));
			List file = parser.file();
			for (Object object : file) {
				List record = (List) object;
				if (record.size() > 1) {
					String pid = (String) record.get(1);
					if (pid.startsWith("\"")) {
						pid = pid.substring(1);
					} 
					if (pid.endsWith("\"")) {
						pid = pid.substring(0,pid.length()-1);
					}
					pids.add(pid);
				}
			}
			return pids;
		} catch (RecognitionException e) {
			throw new IOException(e);
		} catch (TokenStreamException e) {
			throw new IOException(e);
		}
	}
	
}
