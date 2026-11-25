package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.statistics.ReportedAction;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTileResource {
    public static final Logger LOGGER = Logger.getLogger(AbstractTileResource.class.getName());

    @Inject
    @Named("forward-client")
    private Provider<Client> clientProvider;

    public AbstractTileResource() {
        super();
    }

    protected void reportAccess(AggregatedAccessLogs accessLogs, String pid) {
        try {
            accessLogs.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM, ReportedAction.READ.name());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
        }
    }


    public Client getClient() {
        return clientProvider.get();
    }

}
