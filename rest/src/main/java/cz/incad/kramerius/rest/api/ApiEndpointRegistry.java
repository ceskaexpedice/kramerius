package cz.incad.kramerius.rest.api;

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
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.folders.jersey.EndpointFolders;

import java.util.ArrayList;
import java.util.List;

/**
 * ApiEndpointRegistry
 * @author ppodsednik
 */
public class ApiEndpointRegistry {

    public static List<Class<?>> getResources() {
        boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
        boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);

        List<Class<?>> resources = new ArrayList<>();

        resources.add(ReplicationsResource.class);
        if (cdkServerMode) {
            resources.add(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ClientUserResource.class);
        } else {
            resources.add(cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource.class);
        }
        // Admin resources
        resources.add(RightsResource.class);
        resources.add(UsersResource.class);
        resources.add(RolesResource.class);
        // Statistics / monitoring
        resources.add(StatisticsResource.class);
        resources.add(APIMonitorResource.class);
        resources.add(LicensesResource.class);

        // CDK secured channel
        if (channel) {
            resources.add(CDKForwardResource.class);
            resources.add(CDKIIIFResource.class);
            resources.add(CDKItemResource.class);
            resources.add(CDKUsersResource.class);
        }
        // Client API
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.InfoResource.class);
        if (cdkServerMode) {
            resources.add(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ItemsResource.class);
        } else {
            resources.add(cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.class);
        }
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.UsersRequestsResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.SearchResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.UIConfigResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.ConfigResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.pdf.AsyncPDFResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.LocksResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.client.v70.res.EmbeddedFilesResource.class);

        // Admin API
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.collections.CollectionsResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.akubra.AkubraResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.WorkModeResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig.UIConfigResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.ConfigResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.ItemsResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.ServerFilesResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.sync.SDNNTSyncResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.conf.Configurations.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.AdminLockResource.class);
        resources.add(cz.incad.kramerius.rest.apiNew.admin.v70.index.IndexReflectionResource.class);

        // OAI endpoint
        resources.add(cz.incad.kramerius.rest.oai.OAIEndpoint.class);

        // EXTS
        resources.add(cz.incad.kramerius.rest.apiNew.exts.v70.ExtsTokensResource.class);

        // config dependent
        if (cdkServerMode) {
            resources.add(ConnectedInfoResource.class);
            resources.add(ReharvestResource.class);
        }

        resources.add(EndpointFolders.class);

        return resources;
    }
}