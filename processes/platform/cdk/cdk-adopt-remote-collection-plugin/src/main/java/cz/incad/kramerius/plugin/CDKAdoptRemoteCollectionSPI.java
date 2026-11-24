package cz.incad.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;
import java.util.Set;

public final class CDKAdoptRemoteCollectionSPI extends AbstractPluginSpi {
    @Override public String getMainClass() { return CDKAdoptRemoteCollection.class.getName(); }
    @Override public Set<String> getScheduledProfiles() { return Set.of(); }
}
