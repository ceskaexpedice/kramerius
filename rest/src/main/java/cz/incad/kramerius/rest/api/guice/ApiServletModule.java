/*
 * Copyright (C) 2012 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.api.guice;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.CachedSolrAccessImpl;
import cz.incad.kramerius.keycloak.KeycloakProxy;
import cz.incad.kramerius.rest.apiNew.admin.v70.license.LicensesResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.monitor.APIMonitorResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.proxy.ConnectedInfoResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.impl.SolrReharvestManagerImpl;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.RightsResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.RolesResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.UsersResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.statistics.StatisticsResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.CDKForwardResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKIIIFResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKItemResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKUsersResource;
/*
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.debug.HTTPHeaders;
import cz.incad.kramerius.rest.api.k5.client.feedback.FeedbackResource;
import cz.incad.kramerius.rest.api.k5.client.feeder.FeederResource;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.FeederSolrAuthorDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.FeederSolrMimeDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.FeederSolrPolicyDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.FeederSolrRootModelDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.FeederSolrRootPidDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrDateDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrISSNDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrLanguageDecorate;
import cz.incad.kramerius.rest.api.k5.client.impl.SolrMemoizationImpl;
import cz.incad.kramerius.rest.api.k5.client.pdf.AsyncPDFResource;
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResource;
import cz.incad.kramerius.rest.api.k5.client.rights.ClientRightsResource;
import cz.incad.kramerius.rest.api.k5.client.search.SearchResource;
*/
import cz.incad.kramerius.rest.apiNew.client.v70.ApacheCDKForwardClientProvider;
import cz.incad.kramerius.rest.apiNew.client.v70.ApacheCDKForwardPoolManagerProvider;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientProvider;
import cz.incad.kramerius.rest.apiNew.client.v70.filter.DefaultFilter;
import cz.incad.kramerius.rest.apiNew.client.v70.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.timestamps.impl.SolrTimestampStore;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.folders.db.FolderDatabase;
import cz.inovatika.folders.jersey.EndpointFolders;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
import cz.incad.kramerius.rest.api.k5.client.impl.SolrMemoizationImpl;
import cz.incad.kramerius.rest.api.replication.ReplicationsResource;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyReader;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyWriter;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API module
 *
 * @author pavels
 */
public class ApiServletModule extends JerseyServletModule {

    // public static String VERSION = "v4.6";

    @Override
    protected void configureServlets() {
        // API Remote 4.6 Resources
        bind(ReplicationsResource.class);
        //bind(CDKReplicationsResource.class);

		boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
		if (cdkServerMode) {
			bind(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ClientUserResource.class);
		} else {
	        bind(cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource.class);
		}
        

        //bind(ItemResource.class);
        //bind(FeederResource.class);
        //bind(ClientVirtualCollections.class);
        //bind(SearchResource.class);
        //bind(FeedbackResource.class);
        //bind(ClientRightsResource.class);
        //bind(PDFResource.class);
        //bind(AsyncPDFResource.class);
		
		// Admin resources
		bind(RightsResource.class);
        bind(UsersResource.class);
        bind(RolesResource.class);

        // statistics
        bind(StatisticsResource.class);
        // api monitoring
        bind(APIMonitorResource.class);

        bind(LicensesResource.class);

        // CDK Client 7.0 Resources
        boolean channel = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.secured.channel", false);
        if (channel) {
            bind(CDKForwardResource.class);
            bind(CDKIIIFResource.class);
            bind(CDKItemResource.class);
            bind(CDKUsersResource.class);
        }

        
        // API Client 7.0 Resources
        bind(cz.incad.kramerius.rest.apiNew.client.v70.InfoResource.class);
        
        // cdk server mode
        if (cdkServerMode) {
            bind(cz.incad.kramerius.rest.apiNew.client.v70.cdk.ItemsResource.class);
        } else {
            bind(cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.class);
        }
        
        bind(cz.incad.kramerius.rest.apiNew.client.v70.SearchResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.ConfigResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.pdf.AsyncPDFResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.LocksResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.res.EmbeddedFilesResource.class);

        // cdk forward client
        bind(CloseableHttpClient.class).annotatedWith(Names.named("forward-client")).toProvider(ApacheCDKForwardClientProvider.class).asEagerSingleton();
        bind(PoolingHttpClientConnectionManager.class).annotatedWith(Names.named("forward-client")).toProvider(ApacheCDKForwardPoolManagerProvider.class).asEagerSingleton();
        bind(Client.class).annotatedWith(Names.named("forward-client")).toProvider(ClientProvider.class).asEagerSingleton();

        // solr apache client
        bind(SolrAccess.class).annotatedWith(Names.named("cachedSolrAccess")).to(CachedSolrAccessImpl.class).in(Scopes.SINGLETON);


        // API Admin 7.0 Resources
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.collections.CollectionsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.akubra.AkubraResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.WorkModeResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.ConfigResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.ItemsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.ServerFilesResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.sync.SDNNTSyncResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.conf.Configurations.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.AdminLockResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.index.IndexReflectionResource.class);

        // OAI endpoint
        bind(cz.incad.kramerius.rest.oai.OAIEndpoint.class);
        
        // EXTS endpoint
        bind(cz.incad.kramerius.rest.apiNew.exts.v70.ExtsTokensResource.class);
        
        // Generated download links
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.files.GenerateDownloadLinks.class).asEagerSingleton();
        
        // Kramerius folders
        bind(EndpointFolders.class);
        bind(FolderDatabase.class);
        
        bind(KeycloakProxy.class);

        // debug resource
        //bind(HTTPHeaders.class);

        bind(SolrMemoization.class).to(SolrMemoizationImpl.class)
					.asEagerSingleton();

        // simple reader & writer
        bind(SimpleJSONMessageBodyReader.class);
        bind(SimpleJSONMessageBodyWriter.class);

        // decorators
        //decorators();

        /** CDK Part */
        bind(TimestampStore.class).to(SolrTimestampStore.class).asEagerSingleton();
        bind(Instances.class).to(DefaultPropertiesInstances.class).asEagerSingleton();
        bind(ReharvestManager.class).to(SolrReharvestManagerImpl.class).asEagerSingleton();
        bind(ProxyFilter.class).to(DefaultFilter.class);

        // config 
        if (cdkServerMode) {
            bind(ConnectedInfoResource.class);
            bind(ReharvestResource.class);
        }

        
        // api
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        parameters.put("com.sun.jersey.config.property.packages",
                "cz.incad.kramerius.rest.api.processes.messages");

        serve("/api/*").with(GuiceContainer.class, parameters);
        // serve("/api/"+VERSION+"/*").with(GuiceContainer.class, parameters);
    }


}
