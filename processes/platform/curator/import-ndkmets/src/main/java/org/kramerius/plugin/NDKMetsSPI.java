package org.kramerius.plugin;

import org.ceskaexpedice.processplatform.api.AbstractPluginSpi;

import java.util.Set;

public class NDKMetsSPI extends AbstractPluginSpi  {

    @Override
    public String getMainClass() {
        return NDKMetsImportStarter.class.getName();
    }

    @Override
    public Set<String> getScheduledProfiles() {
        return Set.of(
                "new_indexer_index_object",
                "new_indexer_index_model"
        );
    }
}
