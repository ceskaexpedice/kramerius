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

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.relsext.TreeNodeProcessor;
import org.w3c.dom.DOMException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.imaging.paths.DirPath;
import cz.incad.kramerius.imaging.paths.Path;
import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Delete generated deep zoom cache
 * @author pavels
 */
public class DeleteGeneratedDeepZoomCache {


    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateThumbnail.class.getName());

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new Fedora3Module(), new NullStatisticsModule());
            // TODO AK_NEW FedoraAccess fa = injector.getInstance(Key.get(FedoraAccess.class, Names.named("rawFedoraAccess")));
            AkubraRepository akubraRepository = injector.getInstance(Key.get(AkubraRepository.class));
            DiscStrucutreForStore discStruct = injector.getInstance(DiscStrucutreForStore.class);
            deleteCacheForPID(args[0], akubraRepository, discStruct);
            
            
            boolean spawnFlag = Boolean.getBoolean(GenerateDeepZoomFlag.class.getName());
            if (spawnFlag) {
                String[] processArgs = {GenerateDeepZoomFlag.Action.DELETE.name(),args[0]};
                ProcessUtils.startProcess("generateDeepZoomFlag", processArgs);
            }
        }
    }
    
    /**
     * Recursive delete 
     * @param pid Master PID
     * @param discStruct DiscStructure instance
     * @throws IOException IO error has been occurred
     */
    public static void deleteCacheForPID(String pid, final AkubraRepository akubraRepository, final DiscStrucutreForStore discStruct) throws IOException {
        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
            try {
                deleteFolder(pid, discStruct);
            } catch (XPathExpressionException e) {
                LOGGER.severe(e.getMessage());
            }
        } else {
            akubraRepository.re().processInTree(pid, new TreeNodeProcessor() {

                @Override
                public void process(String pid, int level) {
                    try {
                        if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
                            //LOGGER.info("Deleting " + (pageIndex++) +" uuid = "+uuid);
                            deleteFolder(pid, discStruct);
                        }
                    } catch (DOMException e) {
                        LOGGER.severe(e.getMessage());
                    } catch (XPathExpressionException e) {
                        LOGGER.severe(e.getMessage());
                    } catch (IOException e) {
                        LOGGER.severe(e.getMessage());
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

            });
            
        }

    }

    private static void deleteFolder(String pid,DiscStrucutreForStore discStruct) throws IOException, XPathExpressionException {
        try {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            Path uuidFolder = discStruct.getUUIDFile(pidParser.getObjectId(), KConfiguration.getInstance().getDeepZoomCacheDir());
            if (uuidFolder.exists()) {
                DirPath dp = (DirPath) uuidFolder;
                Path[] list = dp.list();
                for (Path p : list) { dp.deleteChild(p.getName()); }
            }
            //FileUtils.deleteDirectory(uuidFolder);
        } catch (LexerException e) {
            LOGGER.severe(e.getMessage());
        }
    }

}
