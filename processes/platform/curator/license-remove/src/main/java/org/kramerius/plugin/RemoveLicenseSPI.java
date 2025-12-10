package org.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class RemoveLicenseSPI extends AbstractPluginSpi {
    @Override
    public String getMainClass() {
        return RemoveLicenseStarter.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
