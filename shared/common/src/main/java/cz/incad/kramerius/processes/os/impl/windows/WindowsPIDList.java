package cz.incad.kramerius.processes.os.impl.windows;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import cz.incad.kramerius.processes.utils.PIDList;
import cz.incad.kramerius.utils.IOUtils;

public class WindowsPIDList extends PIDList {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(WindowsPIDList.class.getName());
	
	@Override
	public List<String> getProcessesPIDS() throws IOException, InterruptedException {
		List<String> command = new ArrayList<String>();
		command.add("tasklist");
		command.add("/FO");
		command.add("CSV");
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process psProcess = processBuilder.start();
		InputStream inputStream = psProcess.getInputStream();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copyStreams(inputStream, bos);
		int exitValue = psProcess.waitFor();
		if (exitValue != 0) {
			LOGGER.warning("ps exiting with value '" + exitValue + "'");
		}
		BufferedReader reader = new BufferedReader(new StringReader(new String(bos.toByteArray(),"Windows-1250")));
		return WindowsPIDListProcessOutput.pids(reader);
	}
}
