package cz.incad.kramerius.processes;

import java.util.List;

public interface RepositoryItemsSupport {

    public List<String> findPidsByModel(String model);
}
