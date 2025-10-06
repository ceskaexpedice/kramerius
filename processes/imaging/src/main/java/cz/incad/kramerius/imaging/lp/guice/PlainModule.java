package cz.incad.kramerius.imaging.lp.guice;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.imaging.impl.Fedora3StreamsDiscStructure;
import cz.incad.kramerius.imaging.impl.PlainDiscStructure;

public class PlainModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DiscStrucutreForStore.class).to(PlainDiscStructure.class);
    }
}
