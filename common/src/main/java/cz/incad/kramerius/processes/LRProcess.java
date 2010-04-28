package cz.incad.kramerius.processes;

import java.util.List;

/**
 * Represents one running process
 * @author pavels
 */
public interface LRProcess {
	
	
	/**
	 * Parameters to process
	 * @return
	 */
	public List<String> getParameters();

	/**
	 * Runtime parameters of this process
	 * @param params
	 */
	public void setParameters(List<String> params);
	
	
	/**
	 * Return unique identifier of LRPprocess
	 * @return
	 */
	public String getUUID();
	
	/**
	 * Return process pid
	 * @return
	 */
	public String getPid();

	/**
	 * Method for setting proceses's pid
	 * @param pid
	 */
	public void setPid(String pid);
	
	/**
	 * Process definintion id
	 * @see LRProcessDefinition
	 * @return
	 */
	public String getDefinitionId();

	//TODO: Vyhodit
	public String getDescription();
	
	/**
	 * This method starts underlaying os process and change state from NOT_RUNNING to RUNNING;<br>
	 * @see States
	 * @param wait
	 */
	public void startMe(boolean wait);
	
	/**
	 * Stops underlaying os process
	 */
	public void stopMe();
	
	/**
	 * Returns timestamp start of process
	 * @return
	 */
	public long getStart();

	//TODO: Vyhodit
	public boolean canBeStopped();
	
	/**
	 * Returns current processes state
	 * @return
	 */
	public States getProcessState();
	
	/**
	 * Setting process's state
	 * @param st
	 */
	public void setProcessState(States st);

	/**
	 * Returns true, if the process is alive
	 * @return
	 */
	public boolean isLiveProcess();

	
	public String getProcessName();
	
	public void setProcessName(String nm);
}
