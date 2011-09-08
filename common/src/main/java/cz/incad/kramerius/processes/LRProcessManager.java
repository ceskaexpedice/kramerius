package cz.incad.kramerius.processes;

import java.util.List;
import java.util.concurrent.locks.Lock;

import cz.incad.kramerius.security.User;


/**
 * This class can manage LR processes
 * @author pavels
 */
public interface LRProcessManager {

	/**
	 * Register new lr process
	 * @param lp
	 * @param loggedUserKey TODO
	 */
	public void registerLongRunningProcess(LRProcess lp, String loggedUserKey);
	
	/**
	 * Returns lr process with given uuid
	 * @param uuid
	 * @return
	 */
	public LRProcess getLongRunningProcess(String uuid);

	/**
	 * Return all lr processes
	 * @return
	 */
	public List<LRProcess> getLongRunningProcesses();

	/**
	 * Retrun planned processes 
	 * @param howMany How many processes should be returned
	 * @return
	 */
	public List<LRProcess> getPlannedProcess(int howMany);
	
	/**
	 * Return planned processes for given rules
	 * @param ordering Ordering column
	 * @param typeOfOrdering Type of ordering (ASC, DESC)
	 * @param offset offset
	 * @return
	 */
	public List<LRProcess> getLongRunningProcesses(LRProcessOrdering ordering,TypeOfOrdering typeOfOrdering, LRProcessOffset offset);
	
	/**
	 * Returns all processes for given state
	 * @param state
	 * @return
	 */
	public List<LRProcess> getLongRunningProcesses(States state);
	
	
	/**
	 * Returns all process by given token
	 * @param token
	 * @return
	 */
    public List<LRProcess> getLongRunningProcessesByToken(String token);
	
	/**
	 * Returns number of running processes
	 * @return
	 */
	public int getNumberOfLongRunningProcesses();
	
	/**
	 * Update state 
	 * @param lrProcess
	 */
	public void updateLongRunningProcessState(LRProcess lrProcess);
	
	/**
	 * Update name
	 * @param lrProcess
	 */
	public void updateLongRunningProcessName(LRProcess lrProcess);
	
	/**
	 * Update PID
	 * @param lrProcess
	 */
	public void updateLongRunningProcessPID(LRProcess lrProcess);
	
	
	/**
	 * Update started date
	 * @param lrProcess
	 */
	public void updateLongRunningProcessStartedDate(LRProcess lrProcess);

//	public void updateRolesProcess(LRProcess lrProcess, User user);

	public void updateTokenMapping(LRProcess lrProcess, String sessionKey);
	
	public String getSessionKey(String token);
	
	/**
	 * Delete process
	 * @param uuid
	 */
	public void deleteLongRunningProcess(LRProcess lrProcess);
	
	
	
	
	public Lock getSynchronizingLock();
}
