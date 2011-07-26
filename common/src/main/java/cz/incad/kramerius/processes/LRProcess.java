package cz.incad.kramerius.processes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

import cz.incad.kramerius.security.User;

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
	 * Plan process to start
	 */
	public void planMe();

	/**
	 * This method starts underlaying os process and change state from PLANNED to RUNNING;<br>
	 * @see States
	 * @param wait
	 * @param additionalJarFiles TODO
	 */
	public void startMe(boolean wait, String krameriusAppLib, String... additionalJarFiles);
	
	/**
	 * Stops underlaying os process
	 */
	public void stopMe();
	
	/**
	 * Returns timestamp start of process
	 * @return
	 */
	public long getStartTime();
	
	/**
	 * Set time of the start of the process 
	 * @param start
	 */
	public void setStartTime(long start);
	
	/**
	 * Return time of 
	 * @return
	 */
	public long getPlannedTime();
	
	/**
	 * Sets time of 
	 * @param ptime
	 */
	public void setPlannedTime(long ptime);

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

	/**
	 * Returns process name
	 * @return
	 */
	public String getProcessName();
	
	/**
	 * Sets process name
	 * @param nm
	 */
	public void setProcessName(String nm);
	
	/**
	 * Returns stdout as stream
	 * @return
	 * @throws FileNotFoundException
	 */
	public InputStream getStandardProcessOutputStream() throws FileNotFoundException;

	/**
	 * Returns errout as stream
	 * @return
	 * @throws FileNotFoundException
	 */
	public InputStream getErrorProcessOutputStream() throws FileNotFoundException;
	
	/**
	 * Retunrs stdout as RandomAccessFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public RandomAccessFile getStandardProcessRAFile() throws FileNotFoundException;
	
	/**
	 * Returns errout as RandomAccessFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public RandomAccessFile getErrorProcessRAFile() throws FileNotFoundException;
	
	
	public File processWorkingDirectory();
	

    public User getUser();
    
    public void setUser(User user);
    
    public String getToken();
    
    public void setToken(String token);

    public String getLoginname();
    
    public void setLoginname(String lname);
    
    public String getSurname();
    
    public void setSurname(String sname);
    
    public String getFirstname();
    
    public void setFirstname(String fname);
    
    
    public String getLoggedUserKey();
    
    public void setLoggedUserKey(String loggedUserKey);
    
    public boolean isMasterProcess();
    
    public void setMasterProcess(boolean flag);
}
