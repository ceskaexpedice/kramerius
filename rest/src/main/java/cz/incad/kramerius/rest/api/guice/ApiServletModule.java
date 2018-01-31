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

import java.util.HashMap;
import java.util.Map;

import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import cz.incad.kramerius.rest.api.k5.admin.rights.RightsResource;
import cz.incad.kramerius.rest.api.k5.admin.statistics.StatisticsResource;
import cz.incad.kramerius.rest.api.k5.admin.users.RolesResource;
import cz.incad.kramerius.rest.api.k5.admin.users.UsersResource;
import cz.incad.kramerius.rest.api.k5.admin.vc.VirtualCollectionsResource;
import cz.incad.kramerius.rest.api.k5.client.JSONDecorator;
import cz.incad.kramerius.rest.api.k5.client.SolrMemoization;
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
import cz.incad.kramerius.rest.api.k5.client.item.ItemResource;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.CollectionsDecorator;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.DonatorDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.HandleDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.ItemSolrRootModelDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.ItemSolrRootPidDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.ItemSolrTitleDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.ReplicatedFromDecorator;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.SolrContextDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.SolrDataNode;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.SolrRightsFlag;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.details.*;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.display.PDFDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.display.ZoomDecorate;
import cz.incad.kramerius.rest.api.k5.client.pdf.AsyncPDFResource;
import cz.incad.kramerius.rest.api.k5.client.pdf.PDFResource;
import cz.incad.kramerius.rest.api.k5.client.rights.ClientRightsResource;
import cz.incad.kramerius.rest.api.k5.client.search.SearchResource;
import cz.incad.kramerius.rest.api.k5.client.user.ClientUserResource;
import cz.incad.kramerius.rest.api.k5.client.virtualcollection.ClientVirtualCollections;
import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.rest.api.replication.CDKReplicationsResource;
import cz.incad.kramerius.rest.api.replication.ReplicationsResource;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyReader;
import cz.incad.kramerius.rest.api.serialization.SimpleJSONMessageBodyWriter;

/**
 * REST API module
 * 
 * @author pavels
 */
public class ApiServletModule extends JerseyServletModule {

    // public static String VERSION = "v4.6";

    @Override
    protected void configureServlets() {
        // API Resources
        bind(ReplicationsResource.class);
        bind(CDKReplicationsResource.class);
        bind(LRResource.class);

        // k5 - znovu...
        bind(ClientUserResource.class);
        bind(ItemResource.class);
        
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
        bind(VirtualCollectionsResource.class);

        bind(StatisticsResource.class);

        bind(SolrMemoization.class).to(SolrMemoizationImpl.class)
                .asEagerSingleton();

        // simple reader & writrr
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

    public static void decoratorsBindings(Multibinder<JSONDecorator> decs) {
        // feeder
        decs.addBinding().to(SolrISSNDecorate.class);
        decs.addBinding().to(SolrDateDecorate.class);
        decs.addBinding().to(SolrLanguageDecorate.class);
        decs.addBinding().to(FeederSolrRootModelDecorate.class);
        decs.addBinding().to(FeederSolrRootPidDecorate.class);
        decs.addBinding().to(FeederSolrTitleDecorate.class);
        decs.addBinding().to(FeederSolrAuthorDecorate.class);
        decs.addBinding().to(FeederSolrPolicyDecorate.class);
        decs.addBinding().to(FeederSolrMimeDecorate.class);

        // item
        decs.addBinding().to(HandleDecorate.class);
        decs.addBinding().to(ItemSolrTitleDecorate.class);
        decs.addBinding().to(ItemSolrRootModelDecorate.class);
        decs.addBinding().to(ItemSolrRootPidDecorate.class);
        decs.addBinding().to(SolrContextDecorate.class);
        decs.addBinding().to(SolrDataNode.class);
        decs.addBinding().to(CollectionsDecorator.class);
        decs.addBinding().to(ReplicatedFromDecorator.class);
        decs.addBinding().to(SolrRightsFlag.class);
        decs.addBinding().to(DonatorDecorate.class);

        // item, display
        decs.addBinding().to(ZoomDecorate.class);
        decs.addBinding().to(PDFDecorate.class);

        // item, details
        decs.addBinding().to(MonographUnitDecorate.class);
        decs.addBinding().to(PageDetailDecorate.class);
        decs.addBinding().to(PeriodicalItemDecorate.class);
        decs.addBinding().to(PeriodicalVolumeDecorator.class);
        decs.addBinding().to(InternalPartDecorate.class);
        decs.addBinding().to(InternalPartDecorate.class);
        decs.addBinding().to(SupplementDecorator.class);
    }

    private void decorators() {
        Multibinder<JSONDecorator> decs = Multibinder.newSetBinder(binder(),
                JSONDecorator.class);
        decoratorsBindings(decs);
    }

}
