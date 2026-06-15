package cz.incad.kramerius.rest.api.guice;

import com.google.inject.Injector;

/**
 * GuiceBootstrap
 * @author ppodsednik
 */
public final class GuiceBootstrap {

    private static Injector injector;

    private GuiceBootstrap() {
    }

    public static void setInjector(Injector injectorInstance) {
        injector = injectorInstance;
    }

    public static Injector getInjector() {
        if (injector == null) {
            throw new IllegalStateException("Guice injector not initialized");
        }
        return injector;
    }
}