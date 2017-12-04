package cz.incad.kramerius.processes.os.impl.unix;

import cz.incad.kramerius.processes.utils.PIDList;
import cz.incad.kramerius.utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UnixPIDList extends PIDList {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(UnixPIDList.class.getName());

	@Override
	public List<String> getProcessesPIDS() throws IOException, InterruptedException {
		List<String> data = new ArrayList<String>();
		List<String> command = new ArrayList<String>();
		command.add("ps");
		command.add("-A");
		command.add("-o");
		command.add("pid");
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		Process psProcess = processBuilder.start();
		InputStream inputStream = psProcess.getInputStream();
		int exitValue = psProcess.waitFor();
		if (exitValue != 0) {
			LOGGER.warning("ps exiting with value '" + exitValue + "'");
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copyStreams(inputStream, bos);
		BufferedReader reader = new BufferedReader(new StringReader(new String(
				bos.toByteArray())));
		String line = null;
		boolean firstLine = false;
		while ((line = reader.readLine()) != null) {
			if (!firstLine)
				firstLine = true;
			else {
				data.add(line.trim());
			}
		}

		IOUtils.tryClose(inputStream);
		return data;
	}
}
