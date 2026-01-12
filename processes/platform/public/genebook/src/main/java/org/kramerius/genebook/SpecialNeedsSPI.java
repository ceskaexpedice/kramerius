package org.kramerius.genebook;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class SpecialNeedsSPI extends AbstractPluginSpi {
    @Override
    public String getMainClass() {
        return SpecialNeedsEBookProcess.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
