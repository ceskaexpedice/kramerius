package cz.incad.kramerius.processes.impl;

import cz.incad.kramerius.processes.ProcessScheduler;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

import cz.incad.kramerius.service.LifeCycleHook;

public class SchedulersLifeCycleHook implements LifeCycleHook {

    public static final Logger LOGGER = Logger.getLogger(SchedulersLifeCycleHook.class.getName());

    @Inject
    ProcessScheduler processScheduler;


    @Override
    public void shutdownNotification() {
        boolean enabled  = KConfiguration.getInstance().getConfiguration().getBoolean("processQueue.enabled",true);
        if (enabled) {
            LOGGER.info("shutting down process schedulers");
            this.processScheduler.shutdown();
        }
    }

    @Override
    public void startNotification() {
        boolean enabled  = KConfiguration.getInstance().getConfiguration().getBoolean("processQueue.enabled",true);
        if (enabled) {
            LOGGER.info("starting process schedulers");
            this.processScheduler.scheduleNextTask();
        }
    }

}
