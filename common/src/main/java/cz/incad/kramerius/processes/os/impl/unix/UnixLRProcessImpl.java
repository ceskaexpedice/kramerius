package cz.incad.kramerius.processes.os.impl.unix;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.impl.AbstractLRProcessImpl;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class UnixLRProcessImpl extends AbstractLRProcessImpl {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(UnixLRProcessImpl.class.getName());
	
	public UnixLRProcessImpl(LRProcessDefinition definition,
			LRProcessManager manager, KConfiguration configuration) {
		super(definition, manager, configuration);
		this.setStartTime(System.currentTimeMillis());
	}

	@Override
	protected void stopMeOsDependent() {
		try {
			LOGGER.info("Killing process "+getPid());
			// kill -9 <pid>
			List<String> command = new ArrayList<String>();
			command.add("kill");
			command.add("-9");
			command.add(getPid());
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.start();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}
