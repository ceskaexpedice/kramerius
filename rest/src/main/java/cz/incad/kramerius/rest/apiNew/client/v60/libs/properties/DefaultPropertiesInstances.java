package cz.incad.kramerius.rest.apiNew.client.v60.libs.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    public static final Logger LOGGER = Logger.getLogger(DefaultPropertiesInstances.class.getName());

    private List<OneInstance> instances = new ArrayList<>();

    public DefaultPropertiesInstances() {
        super();
        Set<String> names = new HashSet<>();
        Configuration configuration = KConfiguration.getInstance().getConfiguration();
        Iterator<String> keys = configuration.getKeys("cdk.collections.sources");
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.contains("cdk.collections.sources.")) {
                String rest = key.substring("cdk.collections.sources.".length());
                if (rest.lastIndexOf(".") > 0) {
                    String acronym = rest.substring(0, rest.lastIndexOf("."));
                    if (!names.contains(acronym)) {
                        names.add(acronym);
                        instances.add(new DefaultOnePropertiesInstance(this, acronym));
                    }
                }
            }
        }
    }

    @Override
    public List<OneInstance> allInstances() {
        return this.instances;
    }

    @Override
    public List<OneInstance> enabledInstances() {
        return this.instances.stream().filter(OneInstance::isConnected).collect(Collectors.toList());
    }

    @Override
    public List<OneInstance> disabledInstances() {
        return this.instances.stream().filter(it -> {
            return !it.isConnected();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isAnyDisabled() {
        List<OneInstance> eInsts = this.enabledInstances();
        return this.allInstances().size() != eInsts.size();
    }

    @Override
    public OneInstance find(String acronym) {
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
    public void cronRefresh() {
        try {
            Map<String, Boolean> statuses = new HashMap<>();
            Client client = Client.create();
            String string = registerData(client);
            JSONArray jsonArray = new JSONArray(string);
            for (int i = 0, ll = jsonArray.length(); i < ll; i++) {
                JSONObject oneObject = jsonArray.getJSONObject(i);
                String code = oneObject.optString("code");
                Boolean status = oneObject.optBoolean("alive");
                statuses.put(code, status);
            }

            for (OneInstance oneInstance : instances) {
                boolean isConnected = oneInstance.isConnected();
                TypeOfChangedStatus type = oneInstance.getType();
                if (type.equals(TypeOfChangedStatus.user) && !isConnected) {
                    // musi vynechat, vypnul uzivatel
                } else {
                    Boolean registrStatus = statuses.get(oneInstance.getName());
                    if (registrStatus.booleanValue() != isConnected) {
                        oneInstance.setConnected(registrStatus.booleanValue(), TypeOfChangedStatus.automat);
                    }
                }
            }
        } catch (UniformInterfaceException | ClientHandlerException | JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    protected String registerData(Client client) throws IOException {
        WebResource r = client.resource("https://api.registr.digitalniknihovna.cz/api/libraries?detail=status");
        InputStream inputStream = r.accept(MediaType.APPLICATION_XML).get(InputStream.class);
        String string = org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
        return string;
    }
}
