package cz.incad.kramerius.processes;

import java.util.List;

/**
 * Implemenations manages LR processes
 * @author pavels
 */
public interface LRProcessManager {

	/**
	 * Register new lr process
	 * @param lp
	 */
	public void registerLongRunningProcess(LRProcess lp);
	
	/**
	 * Returns lr process with given uuid
	 * @param uuid
	 * @return
	 */
	public LRProcess getLongRunningProcess(String uuid);
	
	public List<LRProcess> getLongRunningProcesses();

	public void updateLongRunningProcessState(LRProcess lrProcess);
}
