package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class CDKDeleteLibrarySPI extends AbstractPluginSpi {

    @Override
    public String getMainClass() {
        return CDKDeleteLibraryProcess.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
