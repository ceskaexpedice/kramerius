package cz.incad.kramerius.processes;

import java.util.List;

public interface GCScheduler {

	public void init();

	void scheduleFindGCCandidates();
	
	void scheduleCheckFoundGCCandidates(List<String> procUuids);
}
