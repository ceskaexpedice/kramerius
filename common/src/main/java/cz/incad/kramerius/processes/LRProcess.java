/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.processes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Properties;

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
	public void planMe(Properties paramsMapping);

	
	
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
     * Returns current batch state
     * @return
     */
    public BatchStates getBatchState();

    /**
     * Sets batch state
     * @param st
     */
    public void setBatchState(BatchStates st);
    
    
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
	
	
	/**
	 * Returns process working directory (property 'user.dir')
	 * @return
	 */
	public File processWorkingDirectory();
	

	/**
	 * Returns user associated with process
	 * @return
	 */
	@Deprecated
    public User getUser();
    
    /**
     * Associate user with this process
     * @param user
     */
	@Deprecated
    public void setUser(User user);
    
    /**
     * Returns token associated with this process
     * @return
     */
    public String getToken();
    
    /**
     * Associate token with this process
     * @param token
     */
    public void setToken(String token);

    /**
     * Returns login name of the user (who has started this process)
     * @return
     */
    public String getLoginname();
    
    /**
     * Sets login name
     * @return
     */
    public void setLoginname(String lname);
    
    /**
     * Returns surname of the user (who has started this process)
     * @return
     */
    public String getSurname();
    
    /**
     * Sets surname
     * @param sname
     */
    public void setSurname(String sname);
    
    /**
     * Returns firstname of the user (who has started this process)
     * @return
     */
    public String getFirstname();
    
    /**
     * Sets firstname 
     * @param fname
     */
    public void setFirstname(String fname);
    
    /**
     * Returns logged user key
     * @return
     */
    public String getLoggedUserKey();
    
    /**
     * Sets logged user key
     * @param loggedUserKey
     */
    public void setLoggedUserKey(String loggedUserKey);
    
    /**
     * Returns true, if this process is mater process
     * @return
     */
    public boolean isMasterProcess();
    
    /**
     * Sets flag for master process
     * @param flag
     */
    public void setMasterProcess(boolean flag);

    /**
     * Returns parameters mapping 
     * @return
     */
    public Properties getParametersMapping();

    /**
     * Sets the parameters mapping
     * @param parametersMapping
     */
    public void setParametersMapping(Properties parametersMapping);

}
