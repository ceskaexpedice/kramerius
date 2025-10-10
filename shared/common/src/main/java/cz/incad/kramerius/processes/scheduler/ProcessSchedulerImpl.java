/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.processes.scheduler;

import java.util.Timer;
import java.util.logging.Logger;

import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import javax.inject.Inject;
import javax.inject.Named;

public class ProcessSchedulerImpl implements ProcessScheduler {

	public static final Logger LOGGER = Logger.getLogger(ProcessScheduler.class.getName());
	
	private ProcessDefinitionManager definitionManager;
	
	private int interval;

	private Timer timer;

    // TODO pepo
    @Inject
    @Named("forward-client")
    private CloseableHttpClient apacheClient;

	@Inject
	public ProcessSchedulerImpl(ProcessDefinitionManager definitionManager) {
		super();
		this.definitionManager = definitionManager;
		this.timer = new Timer(ProcessSchedulerImpl.class.getName()+"-thread",true);
		init();
	}

	@Override
	public void init() {
		String sinterval  = KConfiguration.getInstance().getProperty("processQueue.checkInterval","10000");
		this.interval =  Integer.parseInt(sinterval);
	}

	@Override
	public void scheduleNextTask() {
		this.timer.purge();
		NextSchedulerTask schedulerTsk = new NextSchedulerTask(this.definitionManager,this, this.interval, apacheClient);
		this.timer.schedule(schedulerTsk, this.interval);
	}

	@Override
	public void shutdown() {
		LOGGER.info("Canceling process scheduler");
		this.timer.cancel();
	}

}
