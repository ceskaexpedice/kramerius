package cz.incad.kramerius.fedora.om.impl;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class HazelcastServerNode implements ServletContextListener {



        private static HazelcastInstance hzInstance;

        public static synchronized  void ensureHazelcastNode(){
            if (hzInstance == null) {
                Config config = new Config(KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance"));
                GroupConfig groupConfig = config.getGroupConfig();
                groupConfig.setName(KConfiguration.getInstance().getConfiguration().getString("hazelcast.user"));

                hzInstance = Hazelcast.getOrCreateHazelcastInstance(config);
            }
        }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ensureHazelcastNode();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AkubraDOManager.shutdown();
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
