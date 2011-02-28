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

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.io.IOException;
import java.util.List;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomFlagService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class DeepZoomFlagServiceImpl implements DeepZoomFlagService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeepZoomFlagServiceImpl.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    public void deleteFlagToUUID(final String uuid) throws IOException {
        KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
        if (krameriusModel.equals(KrameriusModels.PAGE)) {
            deleteFlagToUUIDInternal(uuid);
        } else {
 
            try {
                fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {
                    @Override
                    public void handle(Element elm, FedoraRelationship relation, int level) {
                        if (relation.equals(FedoraRelationship.hasPage)) {
                            try {
                         
                                String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
                                PIDParser pidParse = new PIDParser(pid);
                                pidParse.disseminationURI();
                                String pageUuid = pidParse.getObjectId();

                                deleteFlagToUUIDInternal(pageUuid);
                            } catch (LexerException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public boolean breakProcess() {
                        return false;
                    }

                    @Override
                    public boolean accept(FedoraRelationship relation) {
                        return relation.name().startsWith("has");
                    }
                });
            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException)e.getCause();
                } else throw new RuntimeException(e);
            }
        }
        
    }
    
    
    @Override
    public void setFlagToUUID(final String uuid, final String tilesUrl) throws IOException {
        KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
        if (krameriusModel.equals(KrameriusModels.PAGE)) {
            setFlagToUUIDInternal(uuid, tilesUrl);
        } else {
 
            try {
                fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {
                    @Override
                    public void handle(Element elm, FedoraRelationship relation, int level) {
                        if (relation.equals(FedoraRelationship.hasPage)) {
                            try {
                         
                                String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
                                PIDParser pidParse = new PIDParser(pid);
                                pidParse.disseminationURI();
                                String pageUuid = pidParse.getObjectId();

                                setFlagToUUIDInternal(pageUuid, tilesUrl);
                            } catch (LexerException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public boolean breakProcess() {
                        return false;
                    }

                    @Override
                    public boolean accept(FedoraRelationship relation) {
                        return relation.name().startsWith("has");
                    }
                });
            } catch (Exception e) {
                if ((e.getCause() != null) && (e.getCause() instanceof IOException)) {
                    throw (IOException)e.getCause();
                } else throw new RuntimeException(e);
            }
        }
    }


    public void deleteFlagToUUIDInternal(String uuid) {
        LOGGER.info("deleting uuid '"+uuid+"'");
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String pid = "uuid:"+uuid;
        String tilesUrlNS = FedoraNamespaces.KRAMERIUS_URI+"tiles-url";
        List<RelationshipTuple> relationships = apim.getRelationships(pid, tilesUrlNS);
        if (!relationships.isEmpty()) {
            apim.purgeRelationship(pid, tilesUrlNS,relationships.get(0).getObject(), relationships.get(0).isIsLiteral(), relationships.get(0).getDatatype());
        } else {
            LOGGER.warning("no relation found");
        }
    }
    
    public void setFlagToUUIDInternal(String uuid, String tilesUrl) {
        FedoraAPIM apim = fedoraAccess.getAPIM();
        String pid = "uuid:"+uuid;
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
