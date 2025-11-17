package cz.inovatika.kramerius.services.workers.copy;

import com.sun.jersey.api.client.*;
import cz.inovatika.kramerius.services.config.ProcessConfig;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.Worker;
import cz.inovatika.kramerius.services.workers.WorkerFinisher;

import java.util.*;

public class SimpleCopyWorker extends Worker {

    public SimpleCopyWorker(ProcessConfig processConfig, Client client, List<IterationItem> items, WorkerFinisher finisher) {
        super(processConfig, client, items, finisher);
    }
}
