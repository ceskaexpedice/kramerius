package org.kramerius.cleanup;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import cz.inovatika.dochub.CleanableSpace;
import cz.inovatika.dochub.impl.FilePermanentContentSpaceImpl;
import cz.inovatika.dochub.impl.FileUserContentSpaceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

public class CleanupModule extends AbstractModule {
    @Override
    protected void configure() {
        // Mapování ENUMU na konkrétní implementaci
        MapBinder<SpaceType, CleanableSpace> spaceBinder =
                MapBinder.newMapBinder(binder(), SpaceType.class, CleanableSpace.class);

        spaceBinder.addBinding(SpaceType.PERMANENT).to(FilePermanentContentSpaceImpl.class);
        spaceBinder.addBinding(SpaceType.USER_CONTEXT).to(FileUserContentSpaceImpl.class);
    }
}