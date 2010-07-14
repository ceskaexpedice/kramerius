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

	public List<LRProcess> getPlannedProcess(int howMany);
	
	public List<LRProcess> getLongRunningProcesses(LRProcessOrdering ordering,TypeOfOrdering typeOfOrdering, LRProcessOffset offset);

	public int getNumberOfLongRunningProcesses();
	
	public void updateLongRunningProcessState(LRProcess lrProcess);
	public void updateLongRunningProcessName(LRProcess lrProcess);
	public void updateLongRunningProcessPID(LRProcess lrProcess);
	public void updateLongRunningProcessStartedDate(LRProcess lrProcess);

}
