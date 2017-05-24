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
package cz.incad.kramerius.rest.api.k5.client.virtualcollection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.impl.fedora.FedoraStreamUtils;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.k5.client.item.exceptions.PIDNotFound;
import cz.incad.kramerius.rest.api.k5.client.utils.PIDSupport;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.CollectionUtils;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.CollectionsManager.SortOrder;
import cz.incad.kramerius.virtualcollections.CollectionsManager.SortType;

@Path("/v5.0/vc")
public class ClientVirtualCollections {

    public static final Logger LOGGER = Logger
            .getLogger(ClientVirtualCollections.class.getName());

    @Inject
    @Named("solr")
    CollectionsManager manager;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> req;
    
    @GET
    @Path("{pid}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response oneVirtualCollection(@PathParam("pid") String pid) {
        try {
            List<Collection> collections = this.manager.getCollections();
            Collection vc = this.manager.getCollection(pid);
            if (vc != null) {
                return Response
                        .ok()
                        .entity(CollectionUtils
                                .virtualCollectionTOJSON(vc)).build();
            } else {
                throw new ObjectNotFound("cannot find vc '" + pid + "'");
            }
        } catch (ObjectNotFound e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }
    
    private void checkPid(String pid) throws PIDNotFound {
        try {
            if (!this.fedoraAccess.isObjectAvailable(pid)) {
                throw new PIDNotFound("pid not found");
            }
        } catch (IOException e) {
            throw new PIDNotFound("pid not found");
        } catch(Exception e) {
            throw new PIDNotFound("error while parsing pid ("+pid+")");
        }
    }


    @GET
    @Path("{pid}/thumb")
    public Response thumb(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
                String suri = ApplicationURL
                        .applicationURL(this.req.get())
                        + "/api/v5.0/item/" + pid + "/streams/"+ImageStreams.IMG_THUMB;
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }


    @GET
    @Path("{pid}/full")
    public Response full(@PathParam("pid") String pid) {
        try {
            checkPid(pid);
                String suri = ApplicationURL
                        .applicationURL(this.req.get())
                        + "/api/v5.0/item/" + pid + "/streams/"+ImageStreams.IMG_FULL;
                URI uri = new URI(suri);
                return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new PIDNotFound("pid not found '" + pid + "'");
        }
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response get(
    		@QueryParam("sort") String ordering,
    		@QueryParam("sortType") String sType,
    		@QueryParam("langCode") String langCode) {
        try {
        	SortOrder order = sortOrdering(ordering);
        	SortType sortType = sortType(sType);
        	List<Collection> collections = null;
            if (order != null) {
            	if (sortType == null) {
            		sortType = SortType.ALPHABET;
            	}
                Locale locale = null;
                if (langCode != null) {
                    locale = Locale.forLanguageTag(langCode);
                } else {
                    locale = this.req.get().getLocale();
                }
                collections = this.manager.getSortedCollections(locale, order, sortType);
            }  else {
                collections = this.manager.getCollections();
            }
            JSONArray jsonArr = new JSONArray();
            for (Collection vc : collections) {
                jsonArr.put(CollectionUtils
                        .virtualCollectionTOJSON(vc));
            }
            return Response.ok().entity(jsonArr.toString()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private SortType sortType(String sType) {
    	if (sType != null) {
    		SortType selVal = null;
    		SortType[] values = CollectionsManager.SortType.values();
    		for (SortType sortType : values) {
                if (sortType.name().equals(sType)) {
                	selVal = sortType;
                    break;
                }
				
			}
            return selVal;
    	}
    	return null;
	}

	private SortOrder sortOrdering(String sortOrdering) {
        if (sortOrdering!=null) {
            SortOrder selectedVal = null;
            for (SortOrder v : CollectionsManager.SortOrder.values()) {
                if (sortOrdering.equals(v.name())) {
                    selectedVal = v;
                    break;
                }
            }
            return selectedVal;
        } else return null;
    }

}
