package cz.incad.kramerius.rest.apiNew.client.v70;

import javax.inject.Provider;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ClientProvider implements Provider<Client> {

    private Client client;

    @Override
    public Client get() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    DefaultClientConfig config = new DefaultClientConfig();
                    config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
                    client = Client.create(config);
                }
            }
        }
        return client;
    }

    public void destroy() {
        synchronized (this) {
            if (client != null) {
                client.destroy();
                client = null;
            }
        }
    }
}
