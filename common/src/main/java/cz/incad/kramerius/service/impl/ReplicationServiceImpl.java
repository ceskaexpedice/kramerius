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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.xml.soap.Detail;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.service.ReplicateException;
import cz.incad.kramerius.service.ReplicationService;
import cz.incad.kramerius.service.replication.ExternalReferencesFormat;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.service.replication.ReplicationFormat;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ReplicationServiceImpl implements ReplicationService{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ReplicationServiceImpl.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrAccess solrAccess;
    
    @Inject
    ServletContext servletContext;
    
    FormatType formatType = FormatType.EXTERNALREFERENCES; 
    
    
    @Override
    public List<String> prepareExport(String pid) throws ReplicateException,IOException {
        final List<String> pids = new ArrayList<String>();
        try {
            ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
            for (ObjectPidsPath objPath : paths) {
                if (objPath.contains(SpecialObjects.REPOSITORY.getPid())) {
                    objPath = objPath.cutHead(1);
                }
                String[] pathAsArray = objPath.getPathFromRootToLeaf();
                for (String pidInArray : pathAsArray) {
                    if (!pids.contains(pidInArray)) {
                        pids.add(pidInArray);
                    }
                }
            }
            
            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    if (!pids.contains(pid)) {
                        pids.add(pid);
                    }
                }
                
                @Override
                public boolean skipBranch(String pid, int level) {
                    // TODO Auto-generated method stub
                    return false;
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
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            throw new ReplicateException(e);
        }
    }
    
    @Override
	public byte[] getExportedFOXML(String pid) throws ReplicateException,
			IOException {
    	return this.getExportedFOXML(pid,this.formatType);
    }

	@Override
    public byte[] getExportedFOXML(String pid, FormatType fType) throws ReplicateException,IOException {
        ReplicationFormat  format = formatInstantiate(fType.getClazz());
        try {
            byte[] exported = fedoraAccess.getAPIM().export(pid, "info:fedora/fedora-system:FOXML-1.1", "archive");
            if (format != null) {
                return format.formatFoxmlData(exported);
            } else return exported;
        } catch (SOAPFaultException e) {
            SOAPFault fault = e.getFault();
            String str = fault.getFaultString();
            if (str.startsWith("org.fcrepo.server.errors.ObjectNotInLowlevelStorageException")) {
                throw new FileNotFoundException(e.getMessage());
            } else throw new ReplicateException(e);
        }
    }

	
	
    private ReplicationFormat formatInstantiate(Class<?> clz) throws ReplicateException{
        try {
        	ReplicationFormat repFormat = (ReplicationFormat) clz.newInstance();
            Injector inj = (Injector) servletContext.getAttribute(Injector.class.getName());
            inj.injectMembers(repFormat);
        	return repFormat;
        } catch (InstantiationException e) {
            throw new ReplicateException(e);
        } catch (IllegalAccessException e) {
            throw new ReplicateException(e);
        }
    }
}

