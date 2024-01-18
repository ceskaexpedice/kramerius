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

import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import cz.incad.kramerius.keycloak.KeycloakProxy;
import cz.incad.kramerius.rest.apiNew.admin.v70.files.GenerateDownloadLinks;
import cz.incad.kramerius.rest.apiNew.admin.v70.license.LicensesResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.RightsResource;
import cz.incad.kramerius.rest.api.k5.admin.statistics.StatisticsResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.RolesResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.rights.UsersResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.CDKForwardResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKIIIFResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKItemResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKUsersResource;
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
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.FeederSolrTitleDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrDateDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrISSNDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrLanguageDecorate;
import cz.incad.kramerius.rest.api.k5.client.impl.SolrMemoizationImpl;
import cz.incad.kramerius.rest.api.k5.client.info.InfoResource;
import cz.incad.kramerius.rest.api.k5.client.pdf.AsyncPDFResource;
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResource;
import cz.incad.kramerius.rest.api.k5.client.rights.ClientRightsResource;
import cz.incad.kramerius.rest.api.k5.client.search.SearchResource;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientUserResource;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.folders.db.FolderDatabase;
import cz.inovatika.folders.jersey.EndpointFolders;
import cz.incad.kramerius.rest.api.k5.client.virtualcollection.ClientVirtualCollections;
import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.rest.api.replication.CDKReplicationsResource;
import cz.incad.kramerius.rest.api.replication.ReplicationsResource;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyReader;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyWriter;

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
        bind(CDKReplicationsResource.class);
        bind(LRResource.class);

        // API Client 5.0 Resources - TODO: disable
        bind(ClientUserResource.class);
        //bind(ItemResource.class);
        bind(FeederResource.class);
        bind(ClientVirtualCollections.class);
        bind(SearchResource.class);
        bind(FeedbackResource.class);
        bind(ClientRightsResource.class);
        bind(PDFResource.class);
        bind(AsyncPDFResource.class);
        bind(InfoResource.class);
        bind(RightsResource.class);
        bind(UsersResource.class);
        bind(RolesResource.class);

        bind(StatisticsResource.class);
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
        bind(cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.SearchResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.ConfigResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.pdf.PDFResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.pdf.AsyncPDFResource.class);

        // API Admin 7.0 Resources
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.processes.ProcessResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.collections.CollectionsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.ConfigResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.ItemsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.ServerFilesResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.sync.SDNNTSyncResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.conf.Configurations.class);
        
        // OAI endpoint
        bind(cz.incad.kramerius.rest.oai.OAIEndpoint.class);
        
        // Generated download links
        bind(cz.incad.kramerius.rest.apiNew.admin.v70.files.GenerateDownloadLinks.class).asEagerSingleton();
        
        // Kramerius folders
        bind(EndpointFolders.class);
        bind(FolderDatabase.class);
        
        bind(KeycloakProxy.class);

        // debug resource
        bind(HTTPHeaders.class);

        bind(SolrMemoization.class).to(SolrMemoizationImpl.class)
                .asEagerSingleton();

        // simple reader & writer
        bind(SimpleJSONMessageBodyReader.class);
        bind(SimpleJSONMessageBodyWriter.class);

        // decorators
        decorators();

        // api
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        parameters.put("com.sun.jersey.config.property.packages",
                "cz.incad.kramerius.rest.api.processes.messages");

        serve("/api/*").with(GuiceContainer.class, parameters);
        // serve("/api/"+VERSION+"/*").with(GuiceContainer.class, parameters);
    }

    // not necessary in k7
    public static void decoratorsBindings(Multibinder<JSONDecorator> decs) {
//        // feeder
//        decs.addBinding().to(SolrISSNDecorate.class);
//        decs.addBinding().to(SolrDateDecorate.class);
//        decs.addBinding().to(SolrLanguageDecorate.class);
//        decs.addBinding().to(FeederSolrRootModelDecorate.class);
//        decs.addBinding().to(FeederSolrRootPidDecorate.class);
//        decs.addBinding().to(FeederSolrTitleDecorate.class);
//        decs.addBinding().to(FeederSolrAuthorDecorate.class);
//        decs.addBinding().to(FeederSolrPolicyDecorate.class);
//        decs.addBinding().to(FeederSolrMimeDecorate.class);
//
//        // item
//        decs.addBinding().to(HandleDecorate.class);
//        decs.addBinding().to(ItemSolrTitleDecorate.class);
//        decs.addBinding().to(ItemSolrRootModelDecorate.class);
//        decs.addBinding().to(ItemSolrRootPidDecorate.class);
//        decs.addBinding().to(SolrContextDecorate.class);
//        //decs.addBinding().to(SolrDataNode.class);
//        //decs.addBinding().to(CollectionsDecorator.class);
//        decs.addBinding().to(ReplicatedFromDecorator.class);
//        //decs.addBinding().to(SolrRightsFlag.class);
//        //decs.addBinding().to(DonatorDecorate.class);
//        //decs.addBinding().to(DNNTDecorator.class);
//
//        // item, display
//        decs.addBinding().to(ZoomDecorate.class);
//        decs.addBinding().to(PDFDecorate.class);
//
//        // item, details
//        decs.addBinding().to(MonographUnitDecorate.class);
//        decs.addBinding().to(PageDetailDecorate.class);
//        decs.addBinding().to(PeriodicalItemDecorate.class);
//        decs.addBinding().to(PeriodicalVolumeDecorator.class);
//        decs.addBinding().to(InternalPartDecorate.class);
//        decs.addBinding().to(InternalPartDecorate.class);
//        decs.addBinding().to(SupplementDecorator.class);
    }

    private void decorators() {
        Multibinder<JSONDecorator> decs = Multibinder.newSetBinder(binder(),
                JSONDecorator.class);
        decoratorsBindings(decs);
    }

}
