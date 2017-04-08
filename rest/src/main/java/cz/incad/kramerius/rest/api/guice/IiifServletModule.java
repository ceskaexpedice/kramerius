package cz.incad.kramerius.rest.api.guice;

import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import cz.incad.kramerius.audio.CacheLifeCycleHook;
import cz.incad.kramerius.rest.api.iiif.IiifAPI;
import cz.incad.kramerius.service.LifeCycleHook;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;


/**
 * IiifServletModule
 *
 * @author Martin Rumanek
 */
public class IiifServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        bind(GuiceContainer.class);
        bind(IiifAPI.class);
        bind(HttpAsyncClient.class).toProvider(HttpAsyncClientProvider.class).in(Scopes.SINGLETON);

        PackagesResourceConfig resourceConfig = new PackagesResourceConfig("jersey.resources.package");
        for (Class<?> resource : resourceConfig.getClasses()) {
            bind(resource);
        }

        Multibinder<LifeCycleHook> lfhooks = Multibinder.newSetBinder(binder(), LifeCycleHook.class);
        lfhooks.addBinding().to(HttpAsyncClientLifeCycleHook.class);

        serve("/iiif-presentation/*").with(GuiceContainer.class);
    }


}
