package cz.incad.kramerius.fedora.om.impl;

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class HazelcastServerNode implements ServletContextListener {


    private static final ILogger LOGGER = Logger.getLogger(HazelcastServerNode.class);
    private static HazelcastInstance hzInstance;

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
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ensureHazelcastNode();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO AK_NEW AkubraDOManager.shutdown();
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
