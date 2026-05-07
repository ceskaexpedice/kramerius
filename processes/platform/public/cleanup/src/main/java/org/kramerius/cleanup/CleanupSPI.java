package org.kramerius.cleanup;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;
import org.kramerius.genpdf.GenerateFullPDFProcess;

import java.util.Set;

public class CleanupSPI extends AbstractPluginSpi {
    @Override
    public String getMainClass() {
        return Cleanup.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
