package cz.incad.kramerius.rest.apiNew.cdk.v70.resources;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.fedora.utils.FedoraUtils;

public abstract class AbstractTileResource {

    private Client c;

    public AbstractTileResource() {
        super();
        this.c = Client.create();
    }

    public static final Logger LOGGER = Logger.getLogger(AbstractTileResource.class.getName());

    protected void reportAccess(AggregatedAccessLogs accessLogs, String pid) {
        try {
            accessLogs.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
        }
    }
    
    public Client getClient() {
        return c;
    }

}
