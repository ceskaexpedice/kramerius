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
import com.google.inject.name.Names;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import cz.incad.kramerius.keycloak.KeycloakProxy;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
//import cz.incad.kramerius.rest.api.k5.admin.rights.RightsResource;
import cz.incad.kramerius.rest.api.k5.admin.statistics.StatisticsResource;
//import cz.incad.kramerius.rest.api.k5.admin.users.RolesResource;
//import cz.incad.kramerius.rest.api.k5.admin.users.UsersResource;

//import cz.incad.kramerius.keycloak.KeycloakProxy;
import cz.incad.kramerius.rest.apiNew.admin.v10.license.LicensesResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.proxy.ConnectedInfoResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.rights.RightsResource;
import cz.incad.kramerius.rest.api.k5.admin.statistics.StatisticsResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.rights.RolesResource;
import cz.incad.kramerius.rest.apiNew.admin.v10.rights.UsersResource;

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
import cz.incad.kramerius.rest.apiNew.client.v60.ClientProvider;
import cz.incad.kramerius.rest.apiNew.client.v60.ClientUserResource;
import cz.incad.kramerius.rest.apiNew.client.v60.filter.DefaultFilter;
import cz.incad.kramerius.rest.apiNew.client.v60.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.properties.DefaultPropertiesInstances;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.timestamps.impl.SolrTimestampStore;
import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyReader;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API module
 *
 * @author pavels
 * Remove old
 */
public class ApiServletModule extends JerseyServletModule {

    // public static String VERSION = "v4.6";

    @Override
    protected void configureServlets() {
        // API Remote 4.6 Resources
        bind(LRResource.class);

        // API Client 5.0 Resources
        bind(ClientUserResource.class);
        bind(FeederResource.class);

        bind(SearchResource.class);
        bind(FeedbackResource.class);
        bind(ClientRightsResource.class);
        bind(PDFResource.class);
        bind(AsyncPDFResource.class);
        bind(RightsResource.class);
        bind(UsersResource.class);
        bind(RolesResource.class);

        //        bind(VirtualCollectionsResource.class);
        bind(StatisticsResource.class);

        //bind(KrameriusRepositoryApi.class).to(KrameriusRepositoryApiImpl.class);

        bind(LicensesResource.class);

        // API Client 6.0 Resources
        bind(cz.incad.kramerius.rest.apiNew.client.v60.InfoResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v60.SearchResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v60.ConfigResource.class);
        bind(cz.incad.kramerius.rest.apiNew.client.v70.InfoResource.class);
        
        bind(Client.class).annotatedWith(Names.named("forward-client")).toProvider(ClientProvider.class).asEagerSingleton();
        bind(cz.incad.kramerius.rest.apiNew.client.v60.ItemsResource.class);
        
        

        // API Admin 1.0 Resources
        bind(cz.incad.kramerius.rest.apiNew.admin.v10.processes.ProcessResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v10.collections.CollectionsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v10.ConfigResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v10.ItemsResource.class);
        bind(cz.incad.kramerius.rest.apiNew.admin.v10.ServerFilesResource.class);

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


        // cdk
        bind(TimestampStore.class).to(SolrTimestampStore.class).asEagerSingleton();
        bind(Instances.class).to(DefaultPropertiesInstances.class).asEagerSingleton();
        bind(ProxyFilter.class).to(DefaultFilter.class);
        bind(ConnectedInfoResource.class);
        
        
        // api
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        parameters.put("com.sun.jersey.config.property.packages",
                "cz.incad.kramerius.rest.api.processes.messages");

        serve("/api/*").with(GuiceContainer.class, parameters);
        // serve("/api/"+VERSION+"/*").with(GuiceContainer.class, parameters);
    }

    public static void decoratorsBindings(Multibinder<JSONDecorator> decs) {
        // feeder
        decs.addBinding().to(SolrISSNDecorate.class);
        decs.addBinding().to(SolrDateDecorate.class);
        decs.addBinding().to(SolrLanguageDecorate.class);
        decs.addBinding().to(FeederSolrRootModelDecorate.class);
        decs.addBinding().to(FeederSolrRootPidDecorate.class);
        decs.addBinding().to(FeederSolrAuthorDecorate.class);
        decs.addBinding().to(FeederSolrPolicyDecorate.class);
        decs.addBinding().to(FeederSolrMimeDecorate.class);

        // item
    }

    private void decorators() {
        Multibinder<JSONDecorator> decs = Multibinder.newSetBinder(binder(),
                JSONDecorator.class);
        decoratorsBindings(decs);
    }

}
