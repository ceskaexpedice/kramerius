package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class CDKReharvestSPI extends AbstractPluginSpi {

    @Override
    public String getMainClass() {
        return CDKReharvest.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
