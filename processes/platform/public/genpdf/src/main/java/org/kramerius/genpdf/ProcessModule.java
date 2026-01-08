package org.kramerius.genpdf;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import org.kramerius.genpdf.impl.SpecialNeedsServiceImpl;

import java.io.File;
import java.util.Locale;

public class ProcessModule extends AbstractModule {

    protected void configure() {
        bind(SpecialNeedsService.class).to(SpecialNeedsServiceImpl.class);
        bind(SecuredAkubraRepository.class).to(SpecialNeedsSecuredAkubraRepositoryImpl.class);
    }

    @Provides
    @Named("fontsDir")
    public File getProcessFontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        return new File(dirName);
    }

    @Provides
    public Locale getProcessPdfsFolder() {
        Locale locale = Locale.getDefault();
        return locale;
    }

}
