package cz.incad.kramerius.processes.impl;

import java.util.List;

public interface RepositoryItemsSupport {

    public List<String> findPidsByModel(String model);
}
