package cz.inovatika.cdk.sync;

import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import cz.incad.kramerius.services.utils.kubernetes.KubernetesEnvSupport;


public class KubernetesSyncProcess {

    public static final Logger LOGGER = Logger.getLogger(KubernetesSyncProcess.class.getName());

    public static final String ONLY_SHOW_CONFIGURATION = "ONLY_SHOW_CONFIGURATION";


    
    protected static Client buildClient() {
        // Client client = Client.create();
        ClientConfig cc = new DefaultClientConfig();
        cc.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        return Client.create(cc);
    }

    public static void main(String[] args)  {
        
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"));

        Map<String, String> env = System.getenv();
        Map<String, String> iterationMap = KubernetesEnvSupport.iterationMap(env);
        if (!iterationMap.containsKey("batch")) {
            iterationMap.put("batch", "45");
        }
        
        
    }
}
