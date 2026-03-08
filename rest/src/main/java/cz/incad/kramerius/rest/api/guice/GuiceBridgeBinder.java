package cz.incad.kramerius.rest.api.guice;

import com.google.inject.Injector;
import cz.incad.kramerius.rest.api.ApiEndpointRegistry;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * GuiceBridgeBinder
 *
 * @author ppodsednik
 */
public class GuiceBridgeBinder extends AbstractBinder {

    @Override
    protected void configure() {
        Injector injector = GuiceBootstrap.getInjector();
        for (Class<?> r : ApiEndpointRegistry.getResources()) {
            bindFactory(new GuiceFactory<>(r, injector)).to(r);
        }
    }
}