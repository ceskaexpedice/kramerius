package cz.incad.kramerius.imaging.lp.guice;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.impl.Fedora3StreamsDiscStructure;

public class Fedora3Module extends AbstractModule {

    @Override
    protected void configure() {
        bind(Connection.class).annotatedWith(Names.named("fedora3")).toProvider(RAWFedoraDatabaseConnectionProvider.class);
        bind(DiscStrucutreForStore.class).to(Fedora3StreamsDiscStructure.class);
    }

    
}
