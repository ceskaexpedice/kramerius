package cz.incad.kramerius.rest.apiNew.client.v60.libs.properties;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.V5ForwardHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.V5RedirectHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.V7ForwardHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.item.V7RedirectHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.user.ProxyUserHandler;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.user.V5ForwardUserHandler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class DefaultOnePropertiesInstance implements OneInstance {

    private Instances instances;
    private String instanceAcronym;
    private boolean connected = true;
    private TypeOfChangedStatus typeOfChangedStatus = TypeOfChangedStatus.automat;

    public DefaultOnePropertiesInstance(Instances instances, String instanceAcronym) {
        super();
        this.instanceAcronym = instanceAcronym;
        this.instances = instances;
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
                return new V5ForwardHandler(this.instances, user, client, solrAccess, source, pid, remoteAddr);
            default:
                return new V7ForwardHandler(this.instances, user, client, solrAccess, source, pid, remoteAddr);
            }
        } else {
            InstanceType instanceType = getInstanceType();
            switch (instanceType) {
            case V5:
                return new V5RedirectHandler(this.instances, user, client, solrAccess, source, pid, remoteAddr);
            case V7:
                return new V7RedirectHandler(this.instances, user, client, solrAccess, source, pid, remoteAddr);
            default:
                return new V5RedirectHandler(this.instances, user, client, solrAccess, source, pid, remoteAddr);
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
                return new V5ForwardUserHandler(this.instances, user, client, solrAccess, source, remoteAddr);
            default:
                throw new UnsupportedOperationException("licence access is not supported");
            }

        } else {
            throw new UnsupportedOperationException("supported only for full access");
        }
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public void setConnected(boolean connected, TypeOfChangedStatus type) {
        this.connected = connected;
        this.typeOfChangedStatus = type;
    }

    @Override
    public TypeOfChangedStatus getType() {
        return this.typeOfChangedStatus;
    }

    @Override
    public String toString() {
        return "DefaultOnePropertiesInstance [instanceAcronym=" + instanceAcronym + ", getName()=" + getName()
                + ", hasFullAccess()=" + hasFullAccess() + ", getInstanceType()=" + getInstanceType() + "]";
    }

}
