package cz.incad.kramerius.rest.apiNew.client.v60.libs.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class DefaultPropertiesInstances implements Instances {

    public static final String INFO_URL = "https://api.registr.digitalniknihovna.cz/api/libraries";
    private static final String STATUS_URL = "https://api.registr.digitalniknihovna.cz/api/libraries?detail=status";

    
    public static final Logger LOGGER = Logger.getLogger(DefaultPropertiesInstances.class.getName());

    private List<OneInstance> instances = new ArrayList<>();
    private Set<String> names = new HashSet<>();
    
    public DefaultPropertiesInstances() {
        super();

        LOGGER.info("Refreshing configuration ");
        refreshingConfiguration();
    }

    protected void refreshingConfiguration() {
        Configuration configuration = KConfiguration.getInstance().getConfiguration();
        Iterator<String> keys = configuration.getKeys("cdk.collections.sources");
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.contains("cdk.collections.sources.")) {
                String rest = key.substring("cdk.collections.sources.".length());
                if (rest.lastIndexOf(".") > 0) {
                    String acronym = rest.substring(0, rest.lastIndexOf("."));
                    if (!names.contains(acronym)) {
                        addOneInstance(acronym);
                    }
                }
            }
        }
    }

    private void addOneInstance(String acronym) {
        LOGGER.info(String.format("Adding library %s", acronym));
        names.add(acronym);
        DefaultOnePropertiesInstance di = new DefaultOnePropertiesInstance(this, acronym);
        instances.add(di);
    }

    
    @Override
    public List<OneInstance> allInstances() {
        this.refreshingConfiguration();
        return this.instances;
    }

    @Override
    public List<OneInstance> enabledInstances() {
        this.refreshingConfiguration();
        return this.instances.stream().filter(OneInstance::isConnected).collect(Collectors.toList());
    }

    @Override
    public List<OneInstance> disabledInstances() {
        this.refreshingConfiguration();
        return this.instances.stream().filter(it -> {
            return !it.isConnected();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isAnyDisabled() {
        this.refreshingConfiguration();
        List<OneInstance> eInsts = this.enabledInstances();
        return this.allInstances().size() != eInsts.size();
    }

    @Override
    public OneInstance find(String acronym) {
        this.refreshingConfiguration();
        List<OneInstance> collect = this.instances.stream().filter(it -> {
            return it.getName().equals(acronym);
        }).collect(Collectors.toList());

        if (!collect.isEmpty()) {
            return collect.get(0);
        } else {
            return null;
        }
    }

    
    
    
    @Override
    public boolean isEnabledInstance(String acronym) {
        this.refreshingConfiguration();
        Optional<OneInstance> found = instances.stream().filter(instance -> instance.getName().equals(acronym)).findFirst();
        if (found.isPresent()) {
            return found.get().isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void cronRefresh() {
        this.refreshingConfiguration();
        try {
            
            
            Map<String, Boolean> statuses = new HashMap<>();
            Client client = Client.create();
            String string = registerData(client, STATUS_URL);
            JSONArray jsonArray = new JSONArray(string);
            for (int i = 0, ll = jsonArray.length(); i < ll; i++) {
                JSONObject oneObject = jsonArray.getJSONObject(i);
                String code = oneObject.optString("code");
                Boolean status = oneObject.optBoolean("alive");
                statuses.put(code, status);
            }
            
            Map<String,Map<String,String>> info = new HashMap<>();
            String infoString = registerData(client, INFO_URL);
            JSONArray infoArray = new JSONArray(infoString);
            for (int i = 0, ll = infoArray.length(); i < ll; i++) {
                JSONObject oneObject = infoArray.getJSONObject(i);
                String czeName = oneObject.optString("name");
                String engName = oneObject.optString("name_en");
                String code =  oneObject.optString("code");
                if (code != null) {
                    if (!info.containsKey(code)) {
                        info.put(code, new HashMap<>());
                    }
                    info.get(code).put(OneInstance.NAME_CZE, czeName);
                    info.get(code).put(OneInstance.NAME_ENG, engName);
                    
                }
            }            
            

            for (OneInstance oneInstance : instances) {
                // statuses
                boolean isConnected = oneInstance.isConnected();
                TypeOfChangedStatus type = oneInstance.getType();
                if (type.equals(TypeOfChangedStatus.user) && !isConnected) {
                    // musi vynechat, vypnul uzivatel
                } else {
                    Boolean registrStatus = statuses.get(oneInstance.getName());
                    if (registrStatus != null) {
                        if (registrStatus.booleanValue() != isConnected) {
                            oneInstance.setConnected(registrStatus.booleanValue(), TypeOfChangedStatus.automat);
                        }
                    }
                }
                
                if (info.containsKey(oneInstance.getName())) {
                    Map<String, String> instInfo = info.get(oneInstance.getName());
                    instInfo.keySet().forEach(key-> {
                        oneInstance.setRegistrInfo(key, instInfo.get(key));
                    });
                }
                
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    
    
    
    
    
    protected String registerData(Client client, String url) throws IOException {
        WebResource r = client.resource(url);
        InputStream inputStream = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        String string = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
        return string;
    }

    
    
}


