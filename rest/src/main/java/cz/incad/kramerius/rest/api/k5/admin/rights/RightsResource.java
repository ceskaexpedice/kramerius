/*
 * Copyright (C) 2013 Pavel Stastny
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
package cz.incad.kramerius.rest.api.k5.admin.rights;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;


@Path("/k5/admin/rights")
public class RightsResource {

    @Inject
    ResourceBundleService bundleService;
    
    @Inject
    Provider<Locale> localesProvider;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsManager rightsManager;
    
    
    /*
    public SecuredActionWrapper[] getWrappers()  {
        try {
            Locale locale = this.localesProvider.get();
            ResourceBundle resbundle = bundleService.getResourceBundle("labels", locale);
            return SecuredActionWrapper.wrap(resbundle, SecuredActions.values());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }*/
    
    
    
	@GET
    @Path("{pid}")
	@Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
	public Response rightsForObject(@PathParam("pid") String pid) {
		//this.rightsManager.findAllRights(pids, action);
		
		return null;
	}    

	
	@GET
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
	public Response rights() {
		JSONObject jsonObj = new JSONObject();
		SecuredActions[] values =  SecuredActions.values();
		for (SecuredActions act : values) {
			
			jsonObj.put(act.name(), new JSONObject());
		}
		return Response.ok().entity(jsonObj.toString()).build();
	}
}
