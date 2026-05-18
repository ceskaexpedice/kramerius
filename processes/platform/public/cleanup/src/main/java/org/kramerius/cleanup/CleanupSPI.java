package org.kramerius.cleanup;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

/**
 * CleanupSPI
 * @author ppodsednik
 */
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
