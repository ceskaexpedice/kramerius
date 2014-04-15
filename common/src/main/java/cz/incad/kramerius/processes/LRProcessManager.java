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

import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.Lock;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.database.TypeOfOrdering;
import cz.incad.kramerius.utils.database.Offset;
import cz.incad.kramerius.utils.database.SQLFilter;

/**
 * Manages live processes
 * 
 * @author pavels
 */
public interface LRProcessManager {

    /**
     * Register new lr process
     * 
     * @param lp
     * @param sessionKey
     *            Session key (determine current user)
     */
    public void registerLongRunningProcess(LRProcess lp, String sessionKey,
            Properties parametersMapping);

    /**
     * Returns lr process with given uuid
     * 
     * @param uuid
     *            Process uuid
     * @return
     */
    public LRProcess getLongRunningProcess(String uuid);

    /**
     * Return all lr processes
     * 
     * @return
     */
    public List<LRProcess> getLongRunningProcesses();

    /**
     * Retrun planned processes
     * 
     * @param howMany
     *            How many processes should be returned
     * @return
     */
    public List<LRProcess> getPlannedProcess(int howMany);

    /**
     * Returns master processes filtered by given rules
     * 
     * @param ordering
     *            Ordering column
     * @param typeOfOrdering
     *            Type of ordering (ASC, DESC)
     * @param offset
     *            Offset (paging)
     * @return
     */
    public List<LRProcess> getLongRunningProcessesAsGrouped(
            LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering,
            Offset offset, SQLFilter filter);

    /**
     * Returns all process filtered by given rules
     * 
     * @param ordering
     *            Ordering column
     * @param typeOfOrdering
     *            Type of ordering (ASC,DESC)
     * @param offset
     *            Offset (paging)
     * @return
     */
    public List<LRProcess> getLongRunningProcessesAsFlat(
            LRProcessOrdering ordering, TypeOfOrdering typeOfOrdering,
            Offset offset);

    /**
     * Returns all processes for given state
     * 
     * @param state
     * @return
     */
    public List<LRProcess> getLongRunningProcesses(States state);

    /**
     * Returns all process by given token
     * 
     * @param grpToken
     * @return
     */
    public List<LRProcess> getLongRunningProcessesByGroupToken(String grpToken);

    /**
     * Returns number of running processes
     * 
     * @return
     */
    public int getNumberOfLongRunningProcesses(SQLFilter filter);

    /**
     * Update process state
     * 
     * @param lrProcess
     */
    public void updateLongRunningProcessState(LRProcess lrProcess);

    /**
     * Update batch state of the process
     * 
     * @param lrProcess
     */
    public void updateLongRunninngProcessBatchState(LRProcess lrProcess);

    /**
     * Update name
     * 
     * @param lrProcess
     */
    public void updateLongRunningProcessName(LRProcess lrProcess);

    /**
     * Update PID
     * 
     * @param lrProcess
     */
    public void updateLongRunningProcessPID(LRProcess lrProcess);

    /**
     * Update started date
     * 
     * @param lrProcess
     */
    public void updateLongRunningProcessStartedDate(LRProcess lrProcess);

    /**
     * Update finished date
     * 
     * @param lrProcess
     */
    public void updateLongRunningProcessFinishedDate(LRProcess lrProcess);

    /**
     * Update mappings between process and sessionKey
     * 
     * @param lrProcess
     *            Started processs
     * @param sessionKey
     *            key represents logged user
     */
    public void updateAuthTokenMapping(LRProcess lrProcess, String sessionKey);

    /**
     * Returns session key associated with process
     * 
     * @param authToken
     *            Token associated with process
     * @return
     */
    public String getSessionKey(String authToken);

    /**
     * Returns true if there is any association betweeen process and session key
     * 
     * @param sessionKey
     *            Session key -> logged user
     * @return
     */
    public boolean isSessionKeyAssociatedWithProcess(String sessionKey);

    /**
     * Sets given token as inactive
     * 
     * @param authToken
     */
    public void closeAuthToken(String authToken);

    /**
     * Returns true if given token is closed
     * 
     * @param authToken
     * @return
     */
    public boolean isAuthTokenClosed(String authToken);

    /**
     * Delete process
     * 
     * @param uuid
     */
    public void deleteLongRunningProcess(LRProcess lrProcess);

    /**
     * Delete batch process and all its subprocesses
     * 
     * @param longRunningProcess
     */
    public void deleteBatchLongRunningProcess(LRProcess longRunningProcess);

    /**
     * Load parameters mapping
     * 
     * @param lrProcess
     *            LRProcess associated with the mapping
     * @return
     */
    public Properties loadParametersMapping(LRProcess lrProcess);

    // /**
    // * Store parameters mapping into db
    // * @param props Properties
    // * @param lrProcess Long running process
    // */
    // public void storeParametersMapping(Properties props, LRProcess
    // lrProcess);

    /**
     * Only for internal use
     * 
     * @return
     */
    public Lock getSynchronizingLock();

}
