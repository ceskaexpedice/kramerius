package cz.incad.kramerius.rest.api;

import cz.incad.kramerius.rest.api.guice.GuiceBridgeBinder;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JerseyApplication
 * @author ppodsednik
 */
public class JerseyApplication extends ResourceConfig {

    public JerseyApplication() {
        // packages("com.example.api.resources") - not used now;
        ApiEndpointRegistry.getResources().forEach(this::register);
        // Guice bridge
        register(new GuiceBridgeBinder());
    }
}