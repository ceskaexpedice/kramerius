package cz.incad.kramerius.processes.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.impl.io.FollowStreamThread;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class AbstractLRProcessImpl implements LRProcess {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(AbstractLRProcessImpl.class.getName());
	
	private LRProcessDefinition definition;
	private LRProcessManager manager;
	private KConfiguration configuration;

	private String pid;
	private long startTime;
	private String uuid;
	private States state = States.NOT_RUNNING;
	
	
	public AbstractLRProcessImpl(
			LRProcessDefinition definition,
			LRProcessManager manager,
			KConfiguration configuration) {
		super();
		this.definition = definition;
		this.manager = manager;
		this.configuration = configuration;
		this.uuid = UUID.randomUUID().toString();
	}

	
	@Override
	public String getDescription() {
		return this.definition.getDescription();
	}

	@Override
	public String getUUID() {
		return this.uuid;
	}


	@Override
	public boolean canBeStopped() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public long getStart() {
		return this.startTime;
	}

	@Override
	public void startMe(boolean wait) {
		try {
			//"java -D"+ProcessStarter.MAIN_CLASS_KEY+"="+mainClass
			// create command
			List<String> command = new ArrayList<String>();
			command.add("java");
			command.add("-D"+ProcessStarter.MAIN_CLASS_KEY+"="+this.definition.getMainClass());
			command.add("-D"+ProcessStarter.UUID_KEY+"="+this.uuid);
			command.add("-D"+ProcessStarter.JDBC_URL+"="+configuration.getJdbcUrl());
			command.add("-D"+ProcessStarter.JDBC_USER_NAME+"="+configuration.getJdbcUserName());
			command.add("-D"+ProcessStarter.JDBC_USER_PASS+"="+configuration.getJdbcUserPass());
			command.add(ProcessStarter.class.getName());
			List<String> params = this.definition.getParameters();
			for (String par : params) {
				command.add(par);
			}
			
			//create CLASSPATH
			StringBuffer buffer = new StringBuffer();
			String libsDirPath = this.definition.getLibsDir();
			if (libsDirPath != null) {
				File libsDir = new File(libsDirPath);
				File[] listFiles = libsDir.listFiles();
				if (listFiles !=  null) {
					for (File file : listFiles) {
						buffer.append(file.getAbsolutePath());
						buffer.append(File.pathSeparator);
					}
				}
			}

			
			
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.environment().put(ProcessStarter.CLASSPATH_NAME, buffer.toString());
			Process process = processBuilder.start();
			File errStreamFile = new File(this.definition.getErrStreamFile());
			LOGGER.info("errorStream > "+errStreamFile.getAbsolutePath());
			new FollowStreamThread(process.getErrorStream(), new FileOutputStream(errStreamFile)).start();
			File standardStreamFile = new File(this.definition.getStandardStreamFile());
			LOGGER.info("standardStream > "+standardStreamFile.getAbsolutePath());
			new FollowStreamThread(process.getInputStream(), new FileOutputStream(standardStreamFile)).start();
			//TODO: Synchronizace ?? Jak na to ?
			this.state = States.RUNNING;
			manager.registerLongRunningProcess(this);
			if (wait) {
				int val = process.waitFor();
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void stopMe() {
		if (this.pid == null) {
			throw new IllegalStateException("cannot stop this process! No PID associated");
		}
		this.stopMeOsDependent();
	}

	protected abstract void stopMeOsDependent();

	
	public String getPid() {
		return pid;
	}


	public void setPid(String pid) {
		this.pid = pid;
	}


	@Override
	public String getDefinitionId() {
		return this.definition.getId();
	}


	public LRProcessDefinition getDefinition() {
		return definition;
	}


	public void setDefinition(LRProcessDefinition definition) {
		this.definition = definition;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}


	public String getUuid() {
		return uuid;
	}


	public void setUuid(String uuid) {
		this.uuid = uuid;
	}


	public void setProcessState(States state) {
		this.state =  state;
	}

	@Override
	public States getProcessState() {
		return this.state;
	}
}
