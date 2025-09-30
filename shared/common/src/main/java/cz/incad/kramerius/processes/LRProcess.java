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

import cz.incad.kramerius.security.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Properties;

/**
 * Represents one running process
 *
 * @author pavels
 */
public interface LRProcess {

    /**
     * Parameters to process
     *
     * @return parameters
     */
    public List<String> getParameters();

    /**
     * Runtime parameters of this process
     *
     * @param params new params
     */
    public void setParameters(List<String> params);


    /**
     * Return unique identifier of LRPprocess
     *
     * @return UUID of process
     */
    public String getUUID();

    /**
     * Return process pid
     *
     * @return System PID of process
     */
    public String getPid();

    /**
     * Method for setting proceses's pid
     *
     * @param pid new system PID
     */
    public void setPid(String pid);

    /**
     * Process definintion id
     *
     * @return returns definition identification
     * @see LRProcessDefinition#getId()
     */
    public String getDefinitionId();

    //TODO: Vyhodit

    /**
     * Returns process description
     */
    @Deprecated
    public String getDescription();

    /**
     * Returns process name
     *
     * @return name of process
     */
    public String getProcessName();

    /**
     * Sets process name
     *
     * @param nm new process name
     */
    public void setProcessName(String nm);

    /**
     * Returns user associated with process
     *
     * @return associated user
     */
    @Deprecated
    public User getUser();

    /**
     * Associate user with this process
     *
     * @param user new user
     */
    @Deprecated
    public void setUser(User user);

    /**
     * Returns token associated with this process
     *
     * @return Grouping token
     */
    public String getGroupToken();

    /**
     * Associate grouping token with this process
     *
     * @param token Grouping token
     */
    public void setGroupToken(String token);

    /**
     * Returns authentication token
     *
     * @return Authentication token
     */
    //TODO: Auth token is not necessary now
    public String getAuthToken();

    /**
     * Sets new authentication token
     *
     * @param authToken authentication token
     */
    //TODO: Auth token is not necessary now
    public void setAuthToken(String authToken);


    /**
     * Returns login name of the user (who has started this process)
     *
     * @return login name
     */
    public String getLoginname();

    /**
     * Sets login name
     *
     * @return login name
     */
    public void setLoginname(String lname);

    /**
     * Returns surname of the user (who has started this process)
     *
     * @return surname
     */
    public String getSurname();

    /**
     * Sets surname
     *
     * @param sname new surname
     */
    public void setSurname(String sname);

    /**
     * Returns firstname of the user (who has started this process)
     *
     * @return firstname
     */
    public String getFirstname();

    /**
     * Sets firstname
     *
     * @param fname firstname
     */
    public void setFirstname(String fname);

    /**
     * Returns logged user key
     *
     * @return logged user key
     */
    @Deprecated
    public String getLoggedUserKey();

    /**
     * Sets logged user key
     *
     * @param loggedUserKey sets loggeduserkey
     */
    public void setLoggedUserKey(String loggedUserKey);

    /**
     * Returns true, if this process is mater process
     *
     * @return true if this process is master process
     */
    public boolean isMasterProcess();

    /**
     * Sets flag for master process
     *
     * @param flag master process flag
     */
    public void setMasterProcess(boolean flag);

    /**
     * Returns parameters mapping
     *
     * @return Parameters mapping
     */
    public Properties getParametersMapping();

    /**
     * Sets the parameters mapping
     *
     * @param parametersMapping new parameters mapping
     */
    public void setParametersMapping(Properties parametersMapping);

    /**
     * Returns IP address associated with HTTP request
     *
     * @return
     */
    public String getPlannedIPAddress();

    /**
     * Sets IP address
     *
     * @param ipAddr
     */
    public void setPlannedIPAddress(String ipAddr);


    /**
     * Returns owner's id
     *
     * @return
     */
    public String getOwnerId();

    /**
     * Sets owner's id
     *
     * @param ownerId
     */
    public void setOwnerId(String ownerId);

    /**
     * Returns owner's whole name
     *
     * @return
     */
    public String getOwnerName();

    /**
     * Sets owner's name
     *
     * @param ownerName
     * @return
     */
    public void setOwnerName(String ownerName);

}
