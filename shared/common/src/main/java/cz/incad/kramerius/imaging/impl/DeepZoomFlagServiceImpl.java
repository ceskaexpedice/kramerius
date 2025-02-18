/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.imaging.impl;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.om.impl.AkubraDOManager;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;

import com.google.inject.Inject;
import com.google.inject.name.Named;


import cz.incad.kramerius.imaging.DeepZoomFlagService;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.core.repository.KnownDatastreams;
import org.ceskaexpedice.akubra.core.repository.RepositoryNamespaces;
import org.ceskaexpedice.akubra.utils.ProcessSubtreeException;
import org.ceskaexpedice.akubra.utils.ProcessingIndexUtils;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.ceskaexpedice.akubra.utils.TreeNodeProcessor;

public class DeepZoomFlagServiceImpl implements DeepZoomFlagService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeepZoomFlagServiceImpl.class.getName());
    
    /* TODO AK_NEW
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

     */
    @Inject
    AkubraRepository akubraRepository;

    public void deleteFlagToPID(final String pid) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString())) {
            try {
                deleteFlagToPIDInternal(pid);
            } catch (RepositoryException e) {
                throw new IOException(e);
            }
        } else {
 
            try {

                RelsExtUtils.processSubtree(pid, new TreeNodeProcessor() {
                    
                    @Override
                    public void process(String pid, int level) throws ProcessSubtreeException {
                        try {
                            deleteFlagToPIDInternal(pid);
                        } catch (RepositoryException e) {
                            throw new ProcessSubtreeException(e);
                        }
                    }
                    
                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        return false;
                    }
                }, akubraRepository);

                
            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException)e.getCause();
                } else throw new RuntimeException(e);
            }
        }
        
    }
    
    
    @Override
    public void setFlagToPID(final String pid, final String tilesUrl) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL.toString())) {
            try {
                setFlagToPIDInternal(pid, tilesUrl);
            } catch (RepositoryException e) {
                throw new IOException(e);
            }
        } else {
            try {
                RelsExtUtils.processSubtree(pid, new TreeNodeProcessor() {
                    @Override
                    public void process(String pid, int level) throws ProcessSubtreeException {
                        try {
                            setFlagToPIDInternal(pid, tilesUrl);
                        } catch (RepositoryException e) {
                            throw new ProcessSubtreeException(e);
                        }
                    }
                    
                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        return false;
                    }
                }, akubraRepository);
                
            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException)e.getCause();
                } else throw new RuntimeException(e);
            }
        }
    }


    void deleteFlagToPIDInternal(String pid) throws RepositoryException {
        LOGGER.info("deleting deep zoom url for '"+pid+"'");
        Fedora4Utils.doWithProcessingIndexCommit(akubraRepository, (repo)->{
            if (repo.objectExists(pid)) {
                Lock writeLock = AkubraDOManager.getWriteLock(pid);
                try {
                    boolean flag = akubraRepository.relsExtRelationExists(pid,"tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    if (flag) {
                        akubraRepository.relsExtRemoveRelationsByNameAndNamespace(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    }
                }finally{
                    writeLock.unlock();
                }
            }

        });
   }
    
    void setFlagToPIDInternal(String pid, String tilesUrl) throws RepositoryException {
        Fedora4Utils.doWithProcessingIndexCommit(akubraRepository, (repo)->{
            if (repo.objectExists(pid)) {
                Lock writeLock = AkubraDOManager.getWriteLock(pid);
                try {
                    boolean flag = akubraRepository.relsExtRelationExists(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    if (flag) {
                        akubraRepository.relsExtRemoveRelationsByNameAndNamespace(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI);
                    }
                    akubraRepository.relsExtAddLiteral(pid, "tiles-url", RepositoryNamespaces.KRAMERIUS_URI, tilesUrl);
                }finally{
                    writeLock.unlock();
                }
            }
        });
    }


}
