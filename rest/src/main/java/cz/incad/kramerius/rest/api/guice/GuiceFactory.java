package cz.incad.kramerius.rest.api.guice;

import com.google.inject.Injector;
import org.glassfish.hk2.api.Factory;

/**
 * GuiceFactory
 * @author ppodsednik
 * @param <T>*
 */
public class GuiceFactory<T> implements Factory<T> {

    private final Class<T> clazz;
    private final Injector injector;

    public GuiceFactory(Class<T> clazz, Injector injector) {
        this.clazz = clazz;
        this.injector = injector;
    }

    @Override
    public T provide() {
        return injector.getInstance(clazz);
    }

    @Override
    public void dispose(T instance) {
    }
}