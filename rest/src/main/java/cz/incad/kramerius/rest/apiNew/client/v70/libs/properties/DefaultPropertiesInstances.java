package cz.incad.kramerius.rest.apiNew.client.v70.libs.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ProcessingException;

import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;

import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DefaultPropertiesInstances implements Instances {

    public static final String INFO_URL =
            "https://api.registr.digitalniknihovna.cz/api/libraries";
    private static final String STATUS_URL =
            "https://api.registr.digitalniknihovna.cz/api/libraries?detail=status";

    public static final Logger LOGGER =
            Logger.getLogger(DefaultPropertiesInstances.class.getName());

    private final List<OneInstance> instances = new ArrayList<>();
    private final Set<String> names = new HashSet<>();

    private final ReharvestManager reharvestManager;
    private final ConfigManager configManager;
    private final CDKRequestCacheSupport cacheSupport;
    private final ExecutorService executor;
    private final DeleteTriggerSupport deleteTriggerSupport;

    @Inject
    public DefaultPropertiesInstances(CDKRequestCacheSupport cacheSupport,
                                      ReharvestManager reharvestManager,
                                      DeleteTriggerSupport deleteTriggerSupport,
                                      ConfigManager configManager) {
        super();
        this.cacheSupport = cacheSupport;
        this.reharvestManager = reharvestManager;
        this.configManager = configManager;
        this.deleteTriggerSupport = deleteTriggerSupport;

        int executors = KConfiguration.getInstance().getConfiguration().getInt("cdk.forward.executor", 40);
        this.executor = Executors.newFixedThreadPool(executors);

        LOGGER.info("Refreshing configuration with reharvestManager "+this.reharvestManager);
        //refresh();
    }

    @Override
    public void refresh() {
        this.instances.clear();
        this.names.clear();

        Map<String, String> properties = new HashMap<>();
        this.configManager
                .getPropertiesByRegularExpression("cdk.collections.sources.*")
                .forEach(it -> properties.put(it.getKey(), it.getValue()));

        Configuration configuration = KConfiguration.getInstance().getConfiguration();
        Iterator<String> keys = configuration.getKeys("cdk.collections.sources");
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.contains("cdk.collections.sources.")) {
                String rest = key.substring("cdk.collections.sources.".length());
                if (rest.lastIndexOf('.') > 0) {
                    String acronym = rest.substring(0, rest.lastIndexOf('.'));
                    if (!names.contains(acronym)) {
                        addOneInstance(acronym, properties);
                    }
                }
            }
        }
    }

    private void addOneInstance(String acronym, Map<String, String> properties) {
        LOGGER.fine(String.format("Adding library %s with reharvestManager %s",
                acronym, reharvestManager));

        String keyConnected =
                String.format("cdk.collections.sources.%s.enabled", acronym);
        String keyTypeofStatus =
                String.format("cdk.collections.sources.%s.status", acronym);

        boolean connected = true;
        TypeOfChangedStatus typeOfChange = TypeOfChangedStatus.automat;

        if (properties.containsKey(keyConnected)) {
            connected = Boolean.parseBoolean(properties.get(keyConnected));
        }
        if (properties.containsKey(keyTypeofStatus)) {
            typeOfChange = TypeOfChangedStatus.valueOf(properties.get(keyTypeofStatus));
        }

        names.add(acronym);


        DefaultOnePropertiesInstance di = new DefaultOnePropertiesInstance(
                this, this.cacheSupport,
                this.configManager,
                this.reharvestManager,
                this.deleteTriggerSupport,
                this.executor,
                acronym,
                connected,
                typeOfChange);
        instances.add(di);
    }

    @Override
    public List<OneInstance> allInstances() {
        this.refresh();
        List<OneInstance> snapshot = new ArrayList<>(this.instances);
        return snapshot;
    }

    @Override
    public List<OneInstance> enabledInstances() {
        this.refresh();
        List<OneInstance> snapshot = new ArrayList<>(this.instances);
        return snapshot.stream().filter(it-> {
            return it.isConnected();
        }).collect(Collectors.toList());
    }

    @Override
    public List<OneInstance> disabledInstances() {
        this.refresh();
        List<OneInstance> snapshot = new ArrayList<>(this.instances);
        return snapshot.stream().filter(it -> {
            return !it.isConnected();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isAnyDisabled() {
        this.refresh();
        List<OneInstance> eInsts = this.enabledInstances();
        return this.allInstances().size() != eInsts.size();
    }

    @Override
    public OneInstance find(String acronym) {
        this.refresh();
        List<OneInstance> snapshot = new ArrayList<>(this.instances);
        List<OneInstance> collect = snapshot.stream().filter(it -> {
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
        this.refresh();
        List<OneInstance> snapshot = new ArrayList<>(this.instances);
        Optional<OneInstance> found = snapshot.stream().filter(inst -> {
            boolean retval = inst.getName() != null;
            if (!retval) {
                LOGGER.warning("Null instance is "+inst.toString());
            }
            return retval;
        }).filter(instance -> instance.getName().equals(acronym)).findFirst();
        if (found.isPresent()) {
            return found.get().isConnected();
        } else {
            return false;
        }
    }

    @Override
    public void cronRefresh() {
        refresh();

        Client client = ClientBuilder.newClient();
        try {
            Map<String, Boolean> statuses = new HashMap<>();
            String statusString = registerData(client, STATUS_URL);
            JSONArray jsonArray = new JSONArray(statusString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                statuses.put(obj.optString("code"), obj.optBoolean("alive"));
            }

            Map<String, Map<String, String>> info = new HashMap<>();
            String infoString = registerData(client, INFO_URL);
            JSONArray infoArray = new JSONArray(infoString);

            for (int i = 0; i < infoArray.length(); i++) {
                JSONObject obj = infoArray.getJSONObject(i);
                String code = obj.optString("code");
                if (code != null) {
                    info.computeIfAbsent(code, k -> new HashMap<>());
                    info.get(code).put(OneInstance.NAME_CZE, obj.optString("name"));
                    info.get(code).put(OneInstance.NAME_ENG, obj.optString("name_en"));
                }
            }

            for (OneInstance oneInstance : instances) {
                boolean isConnected = oneInstance.isConnected();
                TypeOfChangedStatus type = oneInstance.getType();

                if (!(type == TypeOfChangedStatus.user && !isConnected)) {
                    Boolean registrStatus = statuses.get(oneInstance.getName());
                    if (registrStatus != null && registrStatus != isConnected) {
                        oneInstance.setConnected(registrStatus, TypeOfChangedStatus.automat);
                    }
                }

                if (info.containsKey(oneInstance.getName())) {
                    info.get(oneInstance.getName())
                            .forEach(oneInstance::setRegistrInfo);
                }
            }

        } catch (ProcessingException | JSONException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            client.close();
        }
    }

    protected String registerData(Client client, String url) throws IOException {
        WebTarget target = client.target(url);
        try (InputStream inputStream =
                     target.request(MediaType.APPLICATION_JSON).get(InputStream.class)) {
            return org.apache.commons.io.IOUtils.toString(inputStream, "UTF-8");
        }
    }
}