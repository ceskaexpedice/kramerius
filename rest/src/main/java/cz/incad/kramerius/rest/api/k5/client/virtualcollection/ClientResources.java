package cz.incad.kramerius.rest.api.k5.client.virtualcollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import org.json.JSONObject;
import org.json.JSONArray;


import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.replication.exceptions.ObjectNotFound;
import cz.incad.kramerius.virtualcollections.Collection;
import cz.incad.kramerius.virtualcollections.Collection.Description;
import cz.incad.kramerius.virtualcollections.CollectionsManager;
import cz.incad.kramerius.virtualcollections.impl.CDKResourcesFilter;
import cz.incad.kramerius.virtualcollections.impl.CDKVirtualCollectionsGetImpl;

@Path("/v5.0/sources")
public class ClientResources {

    public static Logger LOGGER = Logger.getLogger(ClientResources.class.getName());

    @Inject
    @Named("fedora")
    CollectionsManager colManager;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    private CDKResourcesFilter cdkResFilter = new CDKResourcesFilter();

    
    @GET
    @Path("{pid}")
    @Consumes
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response oneVirtualCollection(@PathParam("pid") String pid) {
        try {
            Collection vc = this.colManager.getCollection(pid);
            if (vc != null && this.cdkResFilter.isResource(vc.getPid())) {
                if (!this.cdkResFilter.isHidden(vc.getPid())) {
                    return Response
                            .ok()
                            .entity(resourceTOJSON(this.fedoraAccess, vc)).build();
                } else {
                    throw new ObjectNotFound("cannot find vc '" + pid + "'");
                }
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

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response get() {
        try {
            JSONArray jsonArr = new JSONArray();
            List<String> resources = this.cdkResFilter.getResources();
            for (String pid : resources) {
                if (!this.cdkResFilter.isHidden(pid)) {
                    Collection col = this.colManager.getCollection(pid);
                    if (col != null) {
                        jsonArr.put(resourceTOJSON(this.fedoraAccess, col));
                    }
                }
            }
            return Response.ok().entity(jsonArr.toString()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new GenericApplicationException(e.getMessage());
        }
    }
    
    public static JSONObject resourceTOJSON(FedoraAccess fa, Collection vc) throws XPathExpressionException, IOException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("pid", vc.getPid());
        jsonObj.put("label", vc.getLabel());
        
        Document dc = fa.getDC(vc.getPid());
        String url = CDKVirtualCollectionsGetImpl.disectURL(dc);
        if (url != null) {
            jsonObj.put("url", url);
        }

        JSONObject jsonMap = new JSONObject();
        List<Description> descriptions = vc.getDescriptions();
        for (Description description : descriptions) {
            jsonMap.put(description.getLangCode(), description.getText());
        }
        jsonObj.put("descs", jsonMap);
        
        return jsonObj;
    }

}
