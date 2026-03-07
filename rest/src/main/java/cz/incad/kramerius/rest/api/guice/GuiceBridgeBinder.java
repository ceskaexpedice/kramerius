package cz.incad.kramerius.rest.api.guice;

import com.google.inject.Injector;
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
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.folders.jersey.EndpointFolders;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * GuiceBridgeBinder
 * @author ppodsednik
 */
public class GuiceBridgeBinder extends AbstractBinder {

    @Override
    protected void configure() {

        Injector injector = GuiceBootstrap.getInjector();

        boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
        boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);

        // API Remote
        bindFactory(new GuiceFactory<>(ReplicationsResource.class, injector)).to(ReplicationsResource.class);

        // Client user resource
        if (cdkServerMode) {
            bindFactory(new GuiceFactory<>(
                    cz.incad.kramerius.rest.apiNew.client.v70.cdk.ClientUserResource.class,
                    injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ClientUserResource.class);
        } else {
            bindFactory(new GuiceFactory<>(
                    cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource.class,
                    injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource.class);
        }

        // Admin resources
        bindFactory(new GuiceFactory<>(RightsResource.class, injector)).to(RightsResource.class);
        bindFactory(new GuiceFactory<>(UsersResource.class, injector)).to(UsersResource.class);
        bindFactory(new GuiceFactory<>(RolesResource.class, injector)).to(RolesResource.class);

        // Statistics / monitoring
        bindFactory(new GuiceFactory<>(StatisticsResource.class, injector)).to(StatisticsResource.class);
        bindFactory(new GuiceFactory<>(APIMonitorResource.class, injector)).to(APIMonitorResource.class);

        bindFactory(new GuiceFactory<>(LicensesResource.class, injector)).to(LicensesResource.class);

        // CDK secured channel
        if (channel) {
            bindFactory(new GuiceFactory<>(CDKForwardResource.class, injector)).to(CDKForwardResource.class);
            bindFactory(new GuiceFactory<>(CDKIIIFResource.class, injector)).to(CDKIIIFResource.class);
            bindFactory(new GuiceFactory<>(CDKItemResource.class, injector)).to(CDKItemResource.class);
            bindFactory(new GuiceFactory<>(CDKUsersResource.class, injector)).to(CDKUsersResource.class);
        }

        // Client API
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.InfoResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.InfoResource.class);

        if (cdkServerMode) {
            bindFactory(new GuiceFactory<>(
                    cz.incad.kramerius.rest.apiNew.client.v70.cdk.ItemsResource.class,
                    injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ItemsResource.class);
        } else {
            bindFactory(new GuiceFactory<>(
                    cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.class,
                    injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.class);
        }

        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.UsersRequestsResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.UsersRequestsResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.SearchResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.SearchResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.UIConfigResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.UIConfigResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.ConfigResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.ConfigResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.pdf.AsyncPDFResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.pdf.AsyncPDFResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.LocksResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.LocksResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.client.v70.res.EmbeddedFilesResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.client.v70.res.EmbeddedFilesResource.class);

        // Admin API
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.collections.CollectionsResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.collections.CollectionsResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.akubra.AkubraResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.akubra.AkubraResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.WorkModeResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.WorkModeResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig.UIConfigResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig.UIConfigResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.ConfigResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.ConfigResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.ItemsResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.ItemsResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.ServerFilesResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.ServerFilesResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.sync.SDNNTSyncResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.sync.SDNNTSyncResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.conf.Configurations.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.conf.Configurations.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.AdminLockResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.AdminLockResource.class);
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.admin.v70.index.IndexReflectionResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.admin.v70.index.IndexReflectionResource.class);

        // OAI endpoint
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.oai.OAIEndpoint.class,
                injector)).to(cz.incad.kramerius.rest.oai.OAIEndpoint.class);

        // EXTS
        bindFactory(new GuiceFactory<>(
                cz.incad.kramerius.rest.apiNew.exts.v70.ExtsTokensResource.class,
                injector)).to(cz.incad.kramerius.rest.apiNew.exts.v70.ExtsTokensResource.class);

        // config dependent
        if (cdkServerMode) {
            bindFactory(new GuiceFactory<>(ConnectedInfoResource.class, injector)).to(ConnectedInfoResource.class);
            bindFactory(new GuiceFactory<>(ReharvestResource.class, injector)).to(ReharvestResource.class);
        }

        bindFactory(new GuiceFactory<>(EndpointFolders.class, injector)).to(EndpointFolders.class);
        bindFactory(new GuiceFactory<>(KeycloakProxy.class, injector)).to(KeycloakProxy.class);

    }
}