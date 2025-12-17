package org.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class FlagToLicenseSPI extends AbstractPluginSpi {
    @Override
    public String getMainClass() {
        return FlagToLicenseStarter.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
