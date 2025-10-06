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
package cz.incad.kramerius.imaging.lp;

import java.io.IOException;
import java.util.Arrays;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.imaging.DeepZoomFlagService;
import cz.incad.kramerius.imaging.lp.guice.Fedora3Module;
import cz.incad.kramerius.imaging.lp.guice.GenerateDeepZoomCacheModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

public class GenerateDeepZoomFlag {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GenerateDeepZoomFlag.class.getName());
    
    public static void main(String[] args) throws IOException {
        System.out.println("Generate deepzoom flag :" + Arrays.asList(args));
        if (args.length >= 2) {
            Action action = Action.valueOf(args[0]);
            action.doAction(args);
        } else {
            LOGGER.severe("bad number of arguments ");
        }

    }
    
    static enum Action {
        SET {
            @Override
            void doAction(String[] args) throws IOException {
                if (args.length >= 3) {
                    LOGGER.info("setting flag ...");
                    Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new RepoModule(), new Fedora3Module(), new NullStatisticsModule());
                    DeepZoomFlagService service = injector.getInstance(DeepZoomFlagService.class);
                    service.setFlagToPID(args[1],args[2]);
                    LOGGER.info("Process finished");
                }
            }
        },
        
        DELETE {
            @Override
            void doAction(String[] args) throws IOException {
                if (args.length >= 2) {
                    LOGGER.info("deleting flag ...");
                    Injector injector = Guice.createInjector(new GenerateDeepZoomCacheModule(), new RepoModule(), new Fedora3Module());
                    DeepZoomFlagService service = injector.getInstance(DeepZoomFlagService.class);
                    service.deleteFlagToPID(args[1]);
                    LOGGER.info("Process finished");
                }
                
            }
        };
    
        abstract void doAction(String[] args) throws IOException;
    }
}
