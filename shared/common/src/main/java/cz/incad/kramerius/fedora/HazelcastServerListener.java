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
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastServerNode;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.File;
import java.util.logging.Logger;

public class HazelcastServerListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(HazelcastServerListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        File hazelcastConfigFile = KConfiguration.getInstance().findConfigFile("hazelcast.config");
        String hazelcastConfigFileS = (hazelcastConfigFile != null && hazelcastConfigFile.exists()) ? hazelcastConfigFile.getAbsolutePath() : null;
        String hazelcastInstance = KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance");
        String hazelcastUser = KConfiguration.getInstance().getConfiguration().getString("hazelcast.user");

        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastConfigFile(hazelcastConfigFileS)
                .hazelcastInstance(hazelcastInstance)
                .hazelcastUser(hazelcastUser)
                .build();
        HazelcastServerNode.ensureHazelcastNode(hazelcastConfig);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HazelcastServerNode.shutdown();
    }
}