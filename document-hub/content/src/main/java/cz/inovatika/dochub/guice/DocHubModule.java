package cz.inovatika.dochub.guice;

import com.google.inject.AbstractModule;
import cz.inovatika.dochub.PermanentContentSpace;
import cz.inovatika.dochub.UserContentSpace;
import cz.inovatika.dochub.impl.FilePermanentContentSpaceImpl;
import cz.inovatika.dochub.impl.FileUserContentSpaceImpl;

public class DocHubModule extends AbstractModule {

    protected void configure() {
        bind(PermanentContentSpace.class).to(FilePermanentContentSpaceImpl.class);
        bind(UserContentSpace.class).to(FileUserContentSpaceImpl.class);
    }
}
