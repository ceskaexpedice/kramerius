package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

/**
 * CDKUpdateMigrationSPI
 */
public class CDKUpdateMigrationSPI extends AbstractPluginSpi {

    @Override
    public String getMainClass() {
        return CDKUpdateMigration.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of();
    }
}
