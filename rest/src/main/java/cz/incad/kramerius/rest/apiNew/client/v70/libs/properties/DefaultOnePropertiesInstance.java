package cz.incad.kramerius.rest.apiNew.client.v70.libs.properties;

import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V5ForwardHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V5RedirectHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V7ForwardHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.V7RedirectHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.V5ForwardUserHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.user.V7ForwardUserHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class DefaultOnePropertiesInstance implements OneInstance {
    
    private Instances instances;
    private ReharvestManager reharvestManager;
    private String instanceAcronym;
    private ConfigManager configManager;

    private Map<String, String> info = new HashMap<>();

    private boolean connectedState = true;
    private TypeOfChangedStatus typeOfChangedStatus;

    public DefaultOnePropertiesInstance( ConfigManager configManager, ReharvestManager reharvestManager,
                                        Instances instances,
                                        String instanceAcronym,
                                        boolean connectedState,
                                        TypeOfChangedStatus typeOfChangedStatus) {
        super();
        this.reharvestManager = reharvestManager;
        this.instanceAcronym = instanceAcronym;
        this.instances = instances;
        this.connectedState = connectedState;
        this.typeOfChangedStatus = typeOfChangedStatus;
        this.configManager = configManager;
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
    public ProxyItemHandler createProxyItemHandler(User user, Client client, SolrAccess solrAccess, String source,
            String pid, String remoteAddr) {
        if (hasFullAccess()) {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5ForwardHandler(this.reharvestManager, this.instances, user, client, solrAccess, source, pid, remoteAddr);
            default:
                return new V7ForwardHandler(this.reharvestManager,this.instances, user, client, solrAccess, source, pid, remoteAddr);
            }
        } else {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5RedirectHandler(this.reharvestManager,this.instances, user, client, solrAccess, source, pid, remoteAddr);
            case V7:
                return new V7RedirectHandler(this.reharvestManager,this.instances, user, client, solrAccess, source, pid, remoteAddr);
            default:
                return new V5RedirectHandler(this.reharvestManager,this.instances, user, client, solrAccess, source, pid, remoteAddr);
            }

        }
    }

    @Override
    public ProxyUserHandler createProxyUserHandler(User user, Client client, SolrAccess solrAccess, String source,
            String remoteAddr) {
        if (hasFullAccess()) {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5ForwardUserHandler(this.reharvestManager,this.instances, user, client, solrAccess, source, remoteAddr);
            default:
                return new V7ForwardUserHandler(this.reharvestManager,this.instances, user, client, solrAccess, source, remoteAddr);
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
