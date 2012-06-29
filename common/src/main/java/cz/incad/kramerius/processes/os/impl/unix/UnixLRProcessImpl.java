package cz.incad.kramerius.processes.os.impl.unix;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.impl.AbstractLRProcessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.processes.utils.PIDList;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;


public class UnixLRProcessImpl extends AbstractLRProcessImpl {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(UnixLRProcessImpl.class.getName());
	
	public UnixLRProcessImpl(LRProcessDefinition definition,
			LRProcessManager manager, KConfiguration configuration) {
		super(definition, manager, configuration);
		//this.setStartTime(System.currentTimeMillis());
	}

	@Override
	protected void stopMeOsDependent() {
		try {
			LOGGER.fine("Killing process "+getPid());
			// kill -9 <pid>
			List<String> command = new ArrayList<String>();
			command.add("kill");
			command.add("-9");
			command.add(getPid());
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			Process startedProcess = processBuilder.start();
			LOGGER.fine("killing command '"+command+"' and exit command "/*+startedProcess.exitValue()*/);
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public boolean isLiveProcess() {
		try {
			LOGGER.info("is alive");
			if (getPid() != null) {
				List<String> command = new ArrayList<String>();
				command.add("ps");
				command.add("-p");
				command.add(getPid());
				command.add("-o");
				command.add("pid,time,cmd");
				ProcessBuilder processBuilder = new ProcessBuilder(command);
				Process psProcess = processBuilder.start();
				InputStream inputStream = psProcess.getInputStream();
				// pockam az bude konec
				int exitValue = psProcess.waitFor();
				if (exitValue != 0) {
					LOGGER.warning("ps exiting with value '"+exitValue+"'");
				}
				List<String[]> data = new ArrayList<String[]>();
				// pak ctu vypis procesu
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				IOUtils.copyStreams(inputStream, bos);
				//System.out.println(new String(bos.toByteArray()));
				BufferedReader reader = new BufferedReader(new StringReader(new String(bos.toByteArray())));
				String line = null;
				boolean firstLine = false;
				while((line = reader.readLine()) != null) {
					if (!firstLine) firstLine = true;
					else {
						String[] array = line.split(" ");
						LOGGER.info("ps data == "+Arrays.asList(array));
						data.add(array);
					}
				}
				
				IOUtils.tryClose(inputStream);
				
				return data.size() == 1;
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		return false;
	}


	
	public static void main(String[] args) throws IOException, InterruptedException {
	    for (int i = 0; i < 2222; i++) {
            PIDList createPIDList = UnixPIDList.createPIDList();
            Thread.sleep(2500);
            System.out.println(createPIDList);
	    }
	}
	
	private static void test() throws IOException, InterruptedException {
//		List<String> command = new ArrayList<String>();
//		command.add("ps");
//		command.add("-p");
//		command.add(getPid());
//		command.add("-o");
//		command.add("pid,time,cmd");
//		ProcessBuilder processBuilder = new ProcessBuilder(command);
//		Process psProcess = processBuilder.start();
//		InputStream inputStream = psProcess.getInputStream();
//		// pockam az bude konec
//		int exitValue = psProcess.waitFor();
//		// pak ctu vypis procesu
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		IOUtils.copyStreams(inputStream, System.out);
//		System.out.println(new String(bos.toByteArray()));
	}
}
