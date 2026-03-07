package cz.incad.kramerius.rest.api.guice;

import cz.incad.kramerius.keycloak.KeycloakProxy;
import cz.incad.kramerius.rest.api.replication.ReplicationsResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.license.LicensesResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.monitor.APIMonitorResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.proxy.ConnectedInfoResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.RightsResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.RolesResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.UsersResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.statistics.StatisticsResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.CDKForwardResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKIIIFResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKItemResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKUsersResource;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.folders.jersey.EndpointFolders;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JerseyApplication
 * @author ppodsednik
 */
public class JerseyApplication extends ResourceConfig {

    public JerseyApplication() {
        // replaces "packages(...)" init-param
        //packages("com.example.api.resources");
        boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
        boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);

        register(ReplicationsResource.class);
        if (cdkServerMode) {
            register(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ClientUserResource.class);
        } else {
            register(cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource.class);
        }
        // Admin resources
        register(RightsResource.class);
        register(UsersResource.class);
        register(RolesResource.class);
        // Statistics / monitoring
        register(StatisticsResource.class);
        register(APIMonitorResource.class);

        register(LicensesResource.class);

        // CDK secured channel
        if (channel) {
            register(CDKForwardResource.class);
            register(CDKIIIFResource.class);
            register(CDKItemResource.class);
            register(CDKUsersResource.class);
        }

        // Client API
        register(cz.incad.kramerius.rest.apiNew.client.v70.InfoResource.class);
        if (cdkServerMode) {
            register(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ItemsResource.class);
        } else {
            register(cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.class);
        }
        register(cz.incad.kramerius.rest.apiNew.client.v70.UsersRequestsResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.SearchResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.UIConfigResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.ConfigResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.pdf.AsyncPDFResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.LocksResource.class);
        register(cz.incad.kramerius.rest.apiNew.client.v70.res.EmbeddedFilesResource.class);

        // Admin API
        register(cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.collections.CollectionsResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.akubra.AkubraResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.WorkModeResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig.UIConfigResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.ConfigResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.ItemsResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.ServerFilesResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.sync.SDNNTSyncResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.conf.Configurations.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.AdminLockResource.class);
        register(cz.incad.kramerius.rest.apiNew.admin.v70.index.IndexReflectionResource.class);

        // OAI endpoint
        register(cz.incad.kramerius.rest.oai.OAIEndpoint.class);

        // EXTS
        register(cz.incad.kramerius.rest.apiNew.exts.v70.ExtsTokensResource.class);

        // config dependent
        if (cdkServerMode) {
            register(ConnectedInfoResource.class);
            register(ReharvestResource.class);
        }

        register(EndpointFolders.class);
        register(KeycloakProxy.class);

        // Guice bridge
        register(new GuiceBridgeBinder());
    }
}