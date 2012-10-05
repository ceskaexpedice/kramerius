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

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.rest.api.replication.ReplicationsResource;

/**
 * REST API module
 * @author pavels
 */
public class ApiServletModule extends JerseyServletModule {

    @Override
    protected void configureServlets() {
        // API Resources
        bind(ReplicationsResource.class);
        bind(LRResource.class);

        // api
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(JSONConfiguration.FEATURE_POJO_MAPPING, "true");
        parameters.put("com.sun.jersey.config.property.packages", "cz.incad.kramerius.rest.api.processes.messages");

        serve("/api/*").with(GuiceContainer.class, parameters);
    }
}
