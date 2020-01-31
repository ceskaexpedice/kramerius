package cz.incad.kramerius.processes.new_api;

import java.util.List;

public interface ProcessManager {

    public Integer getBatchesCount(Filter filter);

    public List<Batch> getBatches(Filter filter, int offset, int limit);

}
