package cz.incad.kramerius.processes.impl;

import java.util.Timer;

import com.google.inject.Inject;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.NextSchedulerTask;
import cz.incad.kramerius.processes.ProcessScheduler;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ProcessSchedulerImpl implements ProcessScheduler {

	private LRProcessManager lrProcessManager;
	private DefinitionManager definitionManager;
	
	private int interval;
	private String applicationLib;
	private String[]jarFiles;
	
	private Timer timer;
	
	
	@Inject
	public ProcessSchedulerImpl(LRProcessManager lrProcessManager,
			DefinitionManager definitionManager) {
		super();
		this.lrProcessManager = lrProcessManager;
		this.definitionManager = definitionManager;
		this.timer = new Timer(ProcessSchedulerImpl.class.getName()+"-thread",true);
		
	}

	@Override
	public String getApplicationLib() {
		return this.applicationLib;
	}


	
	@Override
	public void init(String applicationLib, String... jarFiles) {
		// Jak to vyresit ??? 
		this.applicationLib = applicationLib;
		this.jarFiles = jarFiles;
		String sinterval  = KConfiguration.getInstance().getProperty("processQueue.checkInterval","10000");
		this.interval =  Integer.parseInt(sinterval);
		this.scheduleNextTask();
	}

	@Override
	public void scheduleNextTask() {
		this.timer.purge();
		NextSchedulerTask schedulerTsk = new NextSchedulerTask(this.lrProcessManager, this.definitionManager,this, this.interval);
		this.timer.schedule(schedulerTsk, this.interval);
	}

    @Override
    public String[] getAdditionalJarFiles() {
        return this.jarFiles;
    }
}
