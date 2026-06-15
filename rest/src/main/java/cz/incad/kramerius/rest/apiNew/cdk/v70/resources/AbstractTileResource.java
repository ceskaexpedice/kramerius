package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractTileResource {

    private final Client client;

    public static final Logger LOGGER =
            Logger.getLogger(AbstractTileResource.class.getName());

    protected AbstractTileResource() {
        this.client = ClientBuilder.newClient();
    }

    protected void reportAccess(AggregatedAccessLogs accessLogs, String pid) {
        try {
            accessLogs.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Can't write statistic records for " + pid, e);
        }
    }

    protected Client getClient() {
        return client;
    }
}