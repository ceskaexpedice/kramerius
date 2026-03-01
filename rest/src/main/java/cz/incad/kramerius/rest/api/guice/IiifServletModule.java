package cz.incad.kramerius.rest.api.guice;


import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import cz.incad.kramerius.rest.api.iiif.IiifAPI;

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

        PackagesResourceConfig resourceConfig = new PackagesResourceConfig("jersey.resources.package");
        for (Class<?> resource : resourceConfig.getClasses()) {
            bind(resource);
        }

        // TODO migration serve("/iiif-presentation/*").with(GuiceContainer.class);
    }

}
