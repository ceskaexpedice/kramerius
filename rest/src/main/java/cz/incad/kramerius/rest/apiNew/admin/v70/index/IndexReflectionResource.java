/*
 * Copyright (C) Sep 9, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.admin.v70.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

//@Path("/admin/v7.0/items")
//public class ItemsResource extends AdminApiResource {

@Path("/admin/v7.0/indexreflection")
public class IndexReflectionResource extends AdminApiResource {
    
    public static final Logger LOGGER = Logger.getLogger(IndexReflectionResource.class.getName());
    
    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @javax.inject.Inject
    @javax.inject.Named("solr-client")
    javax.inject.Provider<CloseableHttpClient> provider;


    @GET
    @Path("/search/schema")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSearchSchema() {
        if (permit(this.userProvider.get(), SecuredActions.A_INDEX)) {
            try {
                String serchHost = KConfiguration.getInstance().getSolrSearchHost();
                InputStream schema = SolrUtils.schema(this. provider.get(), serchHost, null);
                String jsonVal = IOUtils.toString(schema, "UTF-8");
                return Response.ok(jsonVal).type(MediaType.APPLICATION_JSON).build();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }
    
    @GET
    @Path("/search/schema/fields")
    public Response getSearchFields() {
        if (permit(this.userProvider.get(), SecuredActions.A_INDEX)) {
            try {
                String serchHost = KConfiguration.getInstance().getSolrSearchHost();
                InputStream schema = SolrUtils.fields(this.provider.get(), serchHost, null);
                String jsonVal = IOUtils.toString(schema, "UTF-8");
                return Response.ok(jsonVal).type(MediaType.APPLICATION_JSON).build();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }        
    }
    
    
    @GET
    @Path("/logs/schema")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogsSchema() {
        if (permit(this.userProvider.get(), SecuredActions.A_INDEX)) {
            try {
                String loggerPoint = KConfiguration.getInstance().getProperty("k7.log.solr.point","http://localhost:8983/solr/logs");
                InputStream schema = SolrUtils.schema(this.provider.get(),loggerPoint, null);
                String jsonVal = IOUtils.toString(schema, "UTF-8");
                return Response.ok(jsonVal).type(MediaType.APPLICATION_JSON).build();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }
    }


    @GET
    @Path("/logs/schema/fields")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogsFields() {
        if (permit(this.userProvider.get(), SecuredActions.A_INDEX)) {
            try {
                String loggerPoint = KConfiguration.getInstance().getProperty("k7.log.solr.point","http://localhost:8983/solr/logs");
                InputStream schema = SolrUtils.fields(this.provider.get(), loggerPoint, null);
                String jsonVal = IOUtils.toString(schema, "UTF-8");
                return Response.ok(jsonVal).type(MediaType.APPLICATION_JSON).build();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("not allowed");
        }        
    }

    boolean permit(User user, SecuredActions action) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    action.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }


}
