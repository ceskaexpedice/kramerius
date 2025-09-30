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
package cz.incad.kramerius.processes.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import cz.incad.kramerius.processes.BatchStates;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.States;
import cz.incad.kramerius.processes.starter.ProcessStarter;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;

public abstract class AbstractLRProcessImpl implements LRProcess {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(AbstractLRProcessImpl.class.getName());

    private LRProcessDefinition definition;
    private KConfiguration configuration = KConfiguration.getInstance();

    private String pid;
    private long startTime;
    private long plannedTime;
    private long finishedTime;
    private String uuid;

    private States state = States.NOT_RUNNING;
    private BatchStates batchState = BatchStates.NO_BATCH;

    private String name;
    // private int userId;

    private String loginname;
    private String firstname;
    private String surname;

    private User user;
    private String loggedUserKey;

    private String groupToken;
    private String authToken;
    private boolean masterProcess;

    private List<String> parameters = new ArrayList<String>();
    private Properties parametersMapping = new Properties();

    private String ipAddress;

    private String ownerId;
    private String ownerName;

    
    public AbstractLRProcessImpl(LRProcessDefinition definition) {
        super();
        this.definition = definition;
        this.uuid = UUID.randomUUID().toString();
    }

    @Override
    public String getDescription() {
        return this.definition.getDescription();
    }

    @Override
    public String getUUID() {
        return this.uuid;
    }

    @Override
    public List<String> getParameters() {
        return this.parameters;
    }

    @Override
    public void setParameters(List<String> params) {
        this.parameters = new ArrayList<String>(params);
    }

    public Properties getParametersMapping() {
        return parametersMapping;
    }

    public void setParametersMapping(Properties parametersMapping) {
        this.parametersMapping = parametersMapping;
    }

    public File errorOutFile(File processWorkingDir) {
        return new File(createFolderIfNotExists(processWorkingDir
                + File.separator + this.definition.getErrStreamFolder()),
                "sterr.err");
    }

    public File standardOutFile(File processWorkingDir) {
        return new File(createFolderIfNotExists(processWorkingDir
                + File.separator + this.definition.getStandardStreamFolder()),
                "stout.out");
    }

    public File processWorkingDirectory() {
        String key = LRProcess.class.getName() + ".workingdir";
        String value = System.getProperty(key, DefinitionManager.DEFAULT_LP_WORKDIR
                + File.separator + uuid);
        File processWorkingDir = new File(value);
        if (!processWorkingDir.exists()) {
            boolean mkdirs = processWorkingDir.mkdirs();
            if (!mkdirs)
                throw new RuntimeException("cannot create directory '"
                        + processWorkingDir.getAbsolutePath() + "'");
        }
        return processWorkingDir;
    }

    private File createFolderIfNotExists(String folder) {
        File fldr = new File(folder);
        if (!fldr.exists()) {
            boolean mkdirs = fldr.mkdirs();
            if (!mkdirs)
                throw new RuntimeException("cannot create directory '"
                        + fldr.getAbsolutePath() + "'");
        }
        return fldr;
    }

    protected abstract void stopMeOsDependent();

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    @Override
    public String getDefinitionId() {
        return this.definition.getId();
    }

    public LRProcessDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(LRProcessDefinition definition) {
        this.definition = definition;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setProcessState(States state) {
        this.state = state;
    }

    @Override
    public String getProcessName() {
        return this.name;
    }

    @Override
    public void setProcessName(String nm) {
        this.name = nm;
    }

    // public File processWorkingDirectory() {

    public long getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(long plannedTime) {
        this.plannedTime = plannedTime;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getGroupToken() {
        return this.groupToken;
    }

    @Override
    public void setGroupToken(String token) {
        this.groupToken = token;
    }

    @Override
    public String getAuthToken() {
        return this.authToken;
    }

    @Override
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getLoginname() {
        return loginname;
    }

    public void setLoginname(String loginname) {
        this.loginname = loginname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @Override
    public String getLoggedUserKey() {
        return this.loggedUserKey;
    }

    @Override
    public void setLoggedUserKey(String loggedUserKey) {
        this.loggedUserKey = loggedUserKey;
    }

    public boolean isMasterProcess() {
        return this.masterProcess;
    }

    @Override
    public void setMasterProcess(boolean flag) {
        this.masterProcess = flag;
    }

    @Override
    public String getPlannedIPAddress() {
        return this.ipAddress;
    }

    @Override
    public void setPlannedIPAddress(String ipAddr) {
        this.ipAddress = ipAddr;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
