package cz.incad.kramerius.keycloak.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.incad.kramerius.keycloak.KeycloakAdminService;
import cz.incad.kramerius.keycloak.KeycloakAdminServiceImpl;

/** Guice bindings for Keycloak integration services. */
public class KeycloakModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KeycloakAdminService.class)
                .to(KeycloakAdminServiceImpl.class)
                .in(Scopes.SINGLETON);
    }
}
