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

import cz.incad.kramerius.rest.api.k5.client.authentication.AuthenticationResource;
import cz.incad.kramerius.rest.api.k5.client.feeder.FeederResource;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrDateDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrISSNDecorate;
import cz.incad.kramerius.rest.api.k5.client.feeder.decorators.SolrLanguageDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.Decorator;
import cz.incad.kramerius.rest.api.k5.client.item.ItemResource;
import cz.incad.kramerius.rest.api.k5.client.item.context.DefaultTreeRenderer;
import cz.incad.kramerius.rest.api.k5.client.item.context.ItemTreeRender;
import cz.incad.kramerius.rest.api.k5.client.item.context.TreeAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.HandleDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.RightsDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.SmallImageDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.decorators.SolrTitleDecorate;
import cz.incad.kramerius.rest.api.k5.client.item.display.DeepZoomDisplayType;
import cz.incad.kramerius.rest.api.k5.client.item.display.DisplayType;
import cz.incad.kramerius.rest.api.k5.client.item.display.DisplayTypeAggregate;
import cz.incad.kramerius.rest.api.k5.client.item.display.PDFDisplayType;
import cz.incad.kramerius.rest.api.k5.client.item.display.PlainImageDisplayType;
import cz.incad.kramerius.rest.api.k5.client.item.display.ZoomifyDisplayType;
import cz.incad.kramerius.rest.api.k5.client.item.metadata.DefaultMetadataImpl;
import cz.incad.kramerius.rest.api.k5.client.item.metadata.Metadata;
import cz.incad.kramerius.rest.api.k5.client.item.metadata.MetadataAggregate;
import cz.incad.kramerius.rest.api.k5.client.search.SearchResource;
import cz.incad.kramerius.rest.api.k5.client.user.UsersResource;
import cz.incad.kramerius.rest.api.k5.client.virtualcollection.VirtualCollectionResource;
import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.rest.api.replication.CDKReplicationsResource;
import cz.incad.kramerius.rest.api.replication.ReplicationsResource;

/**
 * REST API module
 * @author pavels
 */
public class ApiServletModule extends JerseyServletModule {

    public static String VERSION = "v4.6";
    
    @Override
    protected void configureServlets() {
        // API Resources
        bind(ReplicationsResource.class);
        bind(CDKReplicationsResource.class);
        bind(LRResource.class);
        // k5 - znovu...
        bind(ItemResource.class);
        bind(FeederResource.class);
        bind(VirtualCollectionResource.class);
        bind(UsersResource.class);
        bind(SearchResource.class);
        bind(AuthenticationResource.class);
        
        //decorators
        decs();
        
        // matadata
        metadata();
        
        // displayTypes
        displayTypes();
        
        //trees
        trees();

        
        // api
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("com.sun.jersey.api.json.POJOMappingFeature", "true");
        parameters.put("com.sun.jersey.config.property.packages", "cz.incad.kramerius.rest.api.processes.messages");

        serve("/api/"+VERSION+"/*").with(GuiceContainer.class, parameters);
    }

    private void decs() {
		Multibinder<Decorator> decs
        = Multibinder.newSetBinder(binder(), Decorator.class);
		
		decs.addBinding().to(RightsDecorate.class);
		decs.addBinding().to(SmallImageDecorate.class);
		decs.addBinding().to(HandleDecorate.class);
		decs.addBinding().to(SolrTitleDecorate.class);

		decs.addBinding().to(SolrDateDecorate.class);
		decs.addBinding().to(SolrISSNDecorate.class);
		decs.addBinding().to(SolrLanguageDecorate.class);

		
    }

	//TODO: remove
	private void trees() {
		Multibinder<ItemTreeRender> tcollectors
        = Multibinder.newSetBinder(binder(), ItemTreeRender.class);
		tcollectors.addBinding().to(DefaultTreeRenderer.class);

		// tree aggregator
		bind(TreeAggregate.class);
		
	}

	private void displayTypes() {
		Multibinder<DisplayType> dcollectors
        = Multibinder.newSetBinder(binder(), DisplayType.class);
        dcollectors.addBinding().to(PlainImageDisplayType.class);
        dcollectors.addBinding().to(PDFDisplayType.class);
        dcollectors.addBinding().to(DeepZoomDisplayType.class);
        dcollectors.addBinding().to(ZoomifyDisplayType.class);
        // display aggregator
        bind(DisplayTypeAggregate.class);
		
	}

	//TODO: remove
	private void metadata() {
		Multibinder<Metadata> mcollectors
        = Multibinder.newSetBinder(binder(), Metadata.class);
        mcollectors.addBinding().to(DefaultMetadataImpl.class);
        // metadata aggregator
        bind(MetadataAggregate.class);
	}
}
