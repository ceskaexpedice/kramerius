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
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.oai.exceptions.OAIException;
import cz.incad.kramerius.rest.oai.exceptions.OAIInfoException;
import cz.incad.kramerius.rest.apiNew.client.v70.filter.ProxyFilter;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.oai.exceptions.OAIException;
import cz.incad.kramerius.rest.oai.exceptions.OAIInfoException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.execution.Indexer;

import static cz.incad.kramerius.rest.oai.OAITools.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/harvest/v7.0")
public class OAIEndpoint extends ClientApiResource {
    
    public static Logger LOGGER = Logger.getLogger(OAIEndpoint.class.getName());
    

    @Inject
    @Named("cachedFedoraAccess")
    private transient FedoraAccess fedoraAccess;

//    @Inject
//    @Named("cachedFedoraAccess")
//    private transient FedoraAccess fedoraAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;
    
    @Inject
    ConfigManager configManager;

    @Inject
    @Named("forward-client")
    Provider<Client> clientProvider;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    Instances instances;

    @Inject
    ProxyFilter proxyFilter;

    
    public OAIEndpoint() {
    }

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response info(
            @QueryParam("set") String set
            ) throws OAIException {
        

//        private String host;
//        private String setSpec;
//        private String setName;
//        private String setDescription;
//        private String filterQuery;

        
        OAISets sets = new OAISets(configManager,null);
        OAISet found = sets.findBySet(set);
        if (found != null) {
            try {

				boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
                int ndocs = cdkServerMode ? found.numberOfDocOnCDKSide(proxyFilter, solrAccess) : found.numberOfDocOnLocal(solrAccess);


                JSONObject object = new JSONObject();
                object.put("setSpec", found.getSetSpec());
                object.put("setName", found.getSetName());
                object.put("setDescription", found.getSetDescription());
                object.put("filterQuery", found.getFilterQuery());
                object.put("numberDocs", ndocs);
                
                return Response.ok(object.toString()).type(MediaType.APPLICATION_JSON.toString()).build();
            } catch (IOException e) {
                throw new OAIInfoException(e.getMessage());
            } catch (ParserConfigurationException e) {
                throw new OAIInfoException(e.getMessage());
            } catch (SAXException e) {
                throw new OAIInfoException(e.getMessage());
            }
        } else {
            if (set != null) {
                throw new OAIInfoException(String.format("Set %s not found", set));
            } else  {
                throw new OAIInfoException("No default set found ");
            }
        }
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

				 boolean cdkServerMode = KConfiguration.getInstance().getConfiguration().getBoolean("cdk.server.mode");
				 if (cdkServerMode) {
					// cdk
					oaiVerb.performOnCDKSide(this.userProvider, clientProvider,  instances, configManager, this.proxyFilter, this.solrAccess, this.requestProvider.get(), oai, oaiRoot);
				 } else {
					// local	
					oaiVerb.performOnLocal(configManager, this.fedoraAccess, solrAccess, this.requestProvider.get(), oai, oaiRoot);
				 }

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
