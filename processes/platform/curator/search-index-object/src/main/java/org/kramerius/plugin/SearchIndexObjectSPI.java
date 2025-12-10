package org.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class SearchIndexObjectSPI extends AbstractPluginSpi  {
    @Override
    public String getMainClass() {
        return SearchIndexObjectPlatformStarter.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
