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
package cz.incad.kramerius.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.replication.ReplicationServiceFoxmlFilter;

public class ReplicationServiceImpl implements ReplicationService{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ReplicationServiceImpl.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    private ReplicationServiceFoxmlFilter foxmlFilter;

    
    @Override
    public List<String> prepareExport(String pid) throws ReplicateException {
        final List<String> pids = new ArrayList<String>();
        try {
            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    pids.add(pid);
                }

                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }
            });
            return pids;
        } catch (ProcessSubtreeException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new ReplicateException(e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new ReplicateException(e);
        }
    }

    @Override
    public byte[] getExportedFOXML(String pid) throws ReplicateException {
        byte[] exported = fedoraAccess.getAPIM().export(pid, "info:fedora/fedora-system:FOXML-1.1", "archive");
        if (this.foxmlFilter != null) {
            return this.foxmlFilter.filterFoxmlLData(exported);
        } else return exported;
    }

    @Override
    public ReplicationServiceFoxmlFilter getReplicationServiceFoxmlFilter() {
        return this.foxmlFilter;
    }

    @Override
    public void setReplicationServiceFoxmlFilter(ReplicationServiceFoxmlFilter filter) {
        this.foxmlFilter = filter;
    }
}

