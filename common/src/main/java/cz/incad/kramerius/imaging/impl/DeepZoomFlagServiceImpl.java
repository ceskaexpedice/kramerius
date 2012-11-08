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
import java.util.List;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.imaging.DeepZoomFlagService;
import cz.incad.kramerius.impl.AbstractTreeNodeProcessorAdapter;

public class DeepZoomFlagServiceImpl implements DeepZoomFlagService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeepZoomFlagServiceImpl.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    public void deleteFlagToPID(final String pid) throws IOException {
        if (fedoraAccess.isImageFULLAvailable(pid)) {
            deleteFlagToPIDInternal(pid);
        } else {
 
            try {

                fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
                    
                    @Override
                    public void process(String pid, int level) throws ProcessSubtreeException {
                        deleteFlagToPIDInternal(pid);
                        
                    }
                    
                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        return false;
                    }
                });

                
//                fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {
//                    @Override
//                    public void handle(Element elm, FedoraRelationship relation, String relationshipName, int level) {
//                        if (relation.name().startsWith("has")) {
//                            try {
//                         
//                                String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
//                                PIDParser pidParse = new PIDParser(pid);
//                                pidParse.disseminationURI();
//                                String pageUuid = pidParse.getObjectId();
//
//                                deleteFlagToUUIDInternal(pageUuid);
//                            } catch (LexerException e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public boolean breakProcess() {
//                        return false;
//                    }
//
//                    @Override
//                    public boolean accept(FedoraRelationship relation, String relationShipName) {
//                        return relation.name().startsWith("has");
//                    }
//                });
            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException)e.getCause();
                } else throw new RuntimeException(e);
            }
        }
        
    }
    
    
    @Override
    public void setFlagToPID(final String pid, final String tilesUrl) throws IOException {
        if (fedoraAccess.isImageFULLAvailable(pid)) {
            setFlagToPIDInternal(pid, tilesUrl);
        } else {
 
            try {
                
//                
//                fedoraAccess.processSubtree(pid, new AbstractTreeNodeProcessorAdapter() {
//
//                    
//                    @Override
//                    public void processUuid(String pageUuid, int level) {
//                        setFlagToUUIDInternal(pageUuid, tilesUrl);
//                    }
//
//                });
                fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
                    
                    @Override
                    public void process(String pid, int level) throws ProcessSubtreeException {
                        setFlagToPIDInternal(pid, tilesUrl);
                        
                    }
                    
                    @Override
                    public boolean skipBranch(String pid, int level) {
                        return false;
                    }


                    @Override
                    public boolean breakProcessing(String pid, int level) {
                        return false;
                    }
                });
                
            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException)e.getCause();
                } else throw new RuntimeException(e);
            }
        }
    }


    void deleteFlagToPIDInternal(String pid) {
        LOGGER.info("deleting uuid '"+pid+"'");
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String tilesUrlNS = FedoraNamespaces.KRAMERIUS_URI+"tiles-url";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, tilesUrlNS);
        if (!relationships.isEmpty()) {
            apim.purgeRelationship(pid, tilesUrlNS,relationships.get(0).getObject(), relationships.get(0).isIsLiteral(), relationships.get(0).getDatatype());
        } else {
            LOGGER.warning("no relation found");
        }
    }
    
    void setFlagToPIDInternal(String pid, String tilesUrl) {
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String tilesUrlNS = FedoraNamespaces.KRAMERIUS_URI+"tiles-url";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, tilesUrlNS);
        if (relationships.isEmpty()) {
            apim.addRelationship(pid, tilesUrlNS,tilesUrl, true, null);
        } else {
            if (!relationships.get(0).getObject().equals(tilesUrl)) {
                apim.purgeRelationship(pid, tilesUrlNS, relationships.get(0).getObject(), relationships.get(0).isIsLiteral(), relationships.get(0).getDatatype());
                apim.addRelationship(pid, tilesUrlNS,tilesUrl, true, null);
            }
        }
    }
}
