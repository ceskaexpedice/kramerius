package cz.incad.kramerius.processes.impl;

import java.util.List;
import java.util.Timer;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.GCCheckFoundCandidatesTask;
import cz.incad.kramerius.processes.GCFindCandiatesTask;
import cz.incad.kramerius.processes.GCScheduler;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GCSchedulerImpl implements GCScheduler {

	public static Logger LOGGER = Logger.getLogger(GCScheduler.class.getName());
	
	private LRProcessManager lrProcessManager;
	private DefinitionManager definitionManager;
	
	private int interval;
	private Timer timer;
	
	@Inject
	public GCSchedulerImpl(LRProcessManager lrProcessManager,
			DefinitionManager definitionManager) {
		super();
		this.lrProcessManager = lrProcessManager;
		this.definitionManager = definitionManager;
		this.timer = new Timer(GCSchedulerImpl.class.getName()+"-thread",/*true*/ false);
	}

	@Override
	public void init() {
		String sinterval  = KConfiguration.getInstance().getProperty("gcScheduler.checkInterval","10000");
		this.interval =  Integer.parseInt(sinterval);
		//this.scheduleFindGCCandidates();
	}

	
	@Override
	public void scheduleFindGCCandidates() {
		GCFindCandiatesTask findCandidates = new GCFindCandiatesTask(this.lrProcessManager, this.definitionManager, this, this.interval);
		this.timer.schedule(findCandidates, this.interval);
	}

	@Override
	public void scheduleCheckFoundGCCandidates(List<String> procUuids) {
		GCCheckFoundCandidatesTask checkCandidates = new GCCheckFoundCandidatesTask(lrProcessManager, this, procUuids, this.interval);
		this.timer.schedule(checkCandidates, this.interval);
	}
	
	public void shutdown() {
		LOGGER.info("canceling gcscheduler");
		this.timer.cancel();
	}
}
