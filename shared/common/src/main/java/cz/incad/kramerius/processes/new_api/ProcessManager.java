package cz.incad.kramerius.processes.new_api;

import java.util.List;

public interface ProcessManager {

    public Integer getBatchesCount(Filter filter);

    public List<ProcessInBatch> getProcessesInBatches(Filter filter, int offset, int limit);

    public List<ProcessOwner> getProcessesOwners();

}
