package cz.incad.kramerius.rest.apiNew.client.v70.libs.properties;

import java.util.HashMap;
import java.util.Map;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V5ForwardHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V5RedirectHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V7ForwardHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V7RedirectHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.V5ForwardUserHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.V7ForwardUserHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

public class DefaultOnePropertiesInstance implements OneInstance {

    private DeleteTriggerSupport triggerSupport;
    private Instances instances;
    private ReharvestManager reharvestManager;
    private String instanceAcronym;
    private ConfigManager configManager;
    private CDKRequestCacheSupport cacheSupport;
    private Map<String, String> info = new HashMap<>();

    private boolean connectedState = true;
    private TypeOfChangedStatus typeOfChangedStatus;

    public DefaultOnePropertiesInstance(
            Instances instances,
            CDKRequestCacheSupport cacheSupport,
            ConfigManager configManager,
            ReharvestManager reharvestManager,
            DeleteTriggerSupport triggerSupport,
            String instanceAcronym,
            boolean connectedState,
            TypeOfChangedStatus typeOfChangedStatus
    ) {
        super();
        this.reharvestManager = reharvestManager;
        this.instanceAcronym = instanceAcronym;
        this.instances = instances;
        this.connectedState = connectedState;
        this.typeOfChangedStatus = typeOfChangedStatus;
        this.configManager = configManager;
        this.cacheSupport = cacheSupport;
        this.triggerSupport = triggerSupport;
    }

    @Override
    public String getName() {
        return this.instanceAcronym;
    }

    @Override
    public boolean hasFullAccess() {
        boolean license = KConfiguration.getInstance().getConfiguration()
                .getBoolean("cdk.collections.sources." + this.instanceAcronym + ".licenses", false);
        return license;
    }

    @Override
    public InstanceType getInstanceType() {
        String instType = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + this.instanceAcronym + ".api", "v5");
        return InstanceType.load(instType);
    }

    @Override
    public ProxyItemHandler createProxyItemHandler(User user,  CloseableHttpClient closeableHttpClient, DeleteTriggerSupport triggerSupport, SolrAccess solrAccess, String source,
            String pid, String remoteAddr) {
        if (hasFullAccess()) {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5ForwardHandler(this.cacheSupport, this.reharvestManager, this.instances, user, closeableHttpClient, triggerSupport, solrAccess, source, pid, remoteAddr);
            default:
                return new V7ForwardHandler(this.cacheSupport, this.reharvestManager,this.instances, user, closeableHttpClient, triggerSupport, solrAccess, source, pid, remoteAddr);
            }
        } else {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5RedirectHandler(this.cacheSupport, this.reharvestManager,this.instances, user, closeableHttpClient, triggerSupport, solrAccess, source, pid, remoteAddr);
            case V7:
                return new V7RedirectHandler(this.cacheSupport, this.reharvestManager,this.instances, user, closeableHttpClient,triggerSupport, solrAccess, source, pid, remoteAddr);
            default:
                return new V5RedirectHandler(this.cacheSupport, this.reharvestManager,this.instances, user, closeableHttpClient, triggerSupport, solrAccess, source, pid, remoteAddr);
            }

        }
    }

    @Override
    public ProxyUserHandler createProxyUserHandler(User user,  CloseableHttpClient apacheClient, SolrAccess solrAccess, String source,
                                                   String remoteAddr) {
        if (hasFullAccess()) {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5ForwardUserHandler(this.cacheSupport, this.reharvestManager,this.instances, user,  apacheClient, solrAccess, source, remoteAddr);
            default:
                return new V7ForwardUserHandler(this.cacheSupport, this.reharvestManager,this.instances, user,  apacheClient, solrAccess, source, remoteAddr);
            }

        } else {
            throw new UnsupportedOperationException("supported only for full access");
        }
    }
    
    
    

    @Override
    public Map<String, String> getRegistrInfo() {
        return this.info;
    }

    @Override
    public void setRegistrInfo(String key, String value) {
        this.info.put(key, value);
    }

    @Override
    public void removeRegistrInfo(String key) {
        this.info.remove(key);
    }

    @Override
    public boolean isConnected() {
        return this.connectedState;
    }

    @Override
    public void setConnected(boolean connected, TypeOfChangedStatus type) {
        String keyConnected = String.format("cdk.collections.sources.%s.enabled", this.instanceAcronym);
        String keyTypeofStatus = String.format("cdk.collections.sources.%s.status", this.instanceAcronym );
        configManager.setProperty(keyConnected, ""+connected);
        configManager.setProperty(keyTypeofStatus, type.name());
        this.instances.refresh();
    }

    @Override
    public TypeOfChangedStatus getType() {
        return typeOfChangedStatus;
    }

    @Override
    public String toString() {
        return "DefaultOnePropertiesInstance [instanceAcronym=" + instanceAcronym + ", getName()=" + getName()
                + ", hasFullAccess()=" + hasFullAccess() + ", getInstanceType()=" + getInstanceType() + "]";
    }

}
