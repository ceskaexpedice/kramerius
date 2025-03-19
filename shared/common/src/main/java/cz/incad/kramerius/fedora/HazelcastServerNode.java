/*
 * Copyright (C) 2025 Inovatika
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
package cz.incad.kramerius.fedora;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.config.HazelcastConfiguration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HazelcastServerNode implements ServletContextListener {


    //private static final ILogger LOGGER = Logger.getLogger(HazelcastServerNode.class);
    //private static HazelcastInstance hzInstance;

    /* TODO AK_NEW
    public static synchronized void ensureHazelcastNode() {
        if (hzInstance == null) {
            Config config = null;
            File configFile = KConfiguration.getInstance().findConfigFile("hazelcast.config");
            if (configFile != null) {
                try (FileInputStream configStream = new FileInputStream(configFile)) {
                    config = new XmlConfigBuilder(configStream).build();
                } catch (IOException ex) {
                    LOGGER.warning("Could not load Hazelcast config file " + configFile, ex);
                }
            }
            if (config == null) {
                config = new Config(KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance"));
                GroupConfig groupConfig = config.getGroupConfig();
                groupConfig.setName(KConfiguration.getInstance().getConfiguration().getString("hazelcast.user"));
            }
            hzInstance = Hazelcast.getOrCreateHazelcastInstance(config);
        }
    }*/

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastInstance("akubrasync")
                .hazelcastUser("dev")
                .build();
        org.ceskaexpedice.akubra.HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);
        //ensureHazelcastNode();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        org.ceskaexpedice.akubra.HazelcastServerNode.shutdown();
        /*
        AkubraDOManager.shutdown();
        if (hzInstance != null) {
            hzInstance.shutdown();
        }*/
    }
}