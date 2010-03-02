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
}
