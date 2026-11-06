package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class CDKCompareLibrarySPI extends AbstractPluginSpi {
    @Override
    public String getMainClass() {
        return CDKCompareLibraryProcess.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
