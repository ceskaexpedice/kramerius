/*
 * Copyright (C) Jan 10, 2024 Pavel Stastny
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
package cz.incad.kramerius.rest.oai;

import javax.inject.Inject;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Provider;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.rest.apiNew.client.v60.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.oai.exceptions.OAIException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import cz.incad.kramerius.SolrAccess;


import static cz.incad.kramerius.rest.oai.OAITools.*;


import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/harvest/v7.0")
public class OAIEndpoint extends ClientApiResource {
    
    public static Logger LOGGER = Logger.getLogger(OAIEndpoint.class.getName());
    

    @Inject
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    public OAIEndpoint() {
    }
    
    
    @GET
    @Path("oai")
    @Produces(MediaType.APPLICATION_XML)
    public Response oai(
            @QueryParam("verb") String verb, 
            @QueryParam("set") String set, 
            @QueryParam("metadataPrefix") String metadataPrefix
            ) throws OAIException {
        
        if (StringUtils.isAnyString(verb)) {
            try {
                 OAIVerb oaiVerb = OAIVerb.valueOf(verb);
                 Document oai = createOAIDocument();
                 Element oaiRoot = oai.getDocumentElement();
                 oaiVerb.perform(this.fedoraAccess, this.getSolrAccess(), this.requestProvider.get(), oai, oaiRoot);
                 StringWriter writer = new StringWriter();
                 XMLUtils.print(oai, writer);
                 return Response.ok(writer.toString()).build();
            } catch(OAIException e) {
                throw e;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(),e);
                throw new OAIException(ErrorCode.badVerb, null,null, ApplicationURL.applicationURL(requestProvider.get()),null);
            }
        } else {
            throw new OAIException(ErrorCode.badVerb, null,null, ApplicationURL.applicationURL(requestProvider.get()),null);
        }        
    }
}
