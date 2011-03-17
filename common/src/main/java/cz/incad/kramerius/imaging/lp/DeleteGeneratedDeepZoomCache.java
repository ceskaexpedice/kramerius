/*
 *  Copyright (C) 2010 Pavel Stastny
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

package cz.incad.kramerius.imaging.lp;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.imaging.lp.guice.PlainModule;
import cz.incad.kramerius.impl.AbstractTreeNodeProcessorAdapter;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Delete generated deep zoom cache
 * @author pavels
 */
public class DeleteGeneratedDeepZoomCache {


    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateThumbnail.class.getName());

    public static void main(String[] args) throws IOException {
        System.out.println("Delete deepZoomCache :" + Arrays.asList(args));
        if (args.length == 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new Fedora3Module());
            FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("securedFedoraAccess")));
            DiscStrucutreForStore discStruct = injector.getInstance(DiscStrucutreForStore.class);
            deleteCacheForUUID(args[0], fa, discStruct);
            
            
            boolean spawnFlag = Boolean.getBoolean(GenerateDeepZoomFlag.class.getName());
            if (spawnFlag) {
                String[] processArgs = {GenerateDeepZoomFlag.Action.DELETE.name(),args[0]};
                ProcessUtils.startProcess("generateDeepZoomFlag", processArgs);
            }
        }
    }

    public static void deleteCacheForUUID(String uuid, final FedoraAccess fedoraAccess, final DiscStrucutreForStore discStruct) throws IOException {
        if (fedoraAccess.isImageFULLAvailable(uuid)) {
            try {
                deleteFolder(uuid, discStruct);
            } catch (XPathExpressionException e) {
                LOGGER.severe(e.getMessage());
            }
        } else {
            
            fedoraAccess.processSubtree("uuid:"+uuid, new AbstractTreeNodeProcessorAdapter() {
                
                @Override
                public void processUuid(String uuid) {
                    try {
                        if (fedoraAccess.isImageFULLAvailable(uuid)) {
                            //LOGGER.info("Deleting " + (pageIndex++) +" uuid = "+uuid);
                            deleteFolder(uuid, discStruct);
                        }
                    } catch (DOMException e) {
                        LOGGER.severe(e.getMessage());
                    } catch (XPathExpressionException e) {
                        LOGGER.severe(e.getMessage());
                    } catch (IOException e) {
                        LOGGER.severe(e.getMessage());
                    }
                }
            });
            
        }

    }

    private static void deleteFolder(String uuid,DiscStrucutreForStore discStruct) throws IOException, XPathExpressionException {
        File uuidFolder = discStruct.getUUIDFile(uuid, KConfiguration.getInstance().getDeepZoomCacheDir());
        FileUtils.deleteDirectory(uuidFolder);
    }

}
