package cz.incad.kramerius.rest.apiNew.cdk.v70;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKIIIFResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKItemResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKUsersResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKZoomifyResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.SOLRResource;
import cz.incad.kramerius.service.ReplicateException;

/**
 * CDK Forward resource
 * <p>
 *  Provides endpoints for secured channel between source and CDK instance; 
 *  The visibility of endpoints must be enabled and configuration by following <a href="https://github.com/ceskaexpedice/ceska-digitalni-knihovna/wiki/Zabezpe%C4%8Den%C3%A1-komunikace-chr%C3%A1n%C4%9Bn%C3%BD-kan%C3%A1l"> insructions </a>
 *</p>
 */
@Path("cdk/v7.0/forward")
public class CDKForwardResource {

    @Inject
    CDKUsersResource usersResource;

    @Inject
    CDKItemResource itemResource;

    @Inject
    CDKIIIFResource iiifResource;

    @Inject
    CDKZoomifyResource zoomifyResource;

    @Inject
    SOLRResource solrResource;
    
    // --------- User's endpoint --------------------
    @GET
    @Path("user")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response user() {
        return this.usersResource.user();
    }

    
    // --------------- Item's endpoint ---------------
    @GET
    @Path("item/{pid}/streams/{dsid}")
    public Response stream(@Context HttpHeaders headers, @PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        return itemResource.stream(pid, dsid);
    }

    @GET
    @Path("iiif/{pid}/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid) {
        return this.iiifResource.iiifManifest(pid);
    }

    @GET
    //@Produces("image/jpeg")
    @Path("iiif/{pid}/{region}/{size}/{rotation}/{qf}")
    public Response tile(@PathParam("pid") String pid, @PathParam("region") String region,
            @PathParam("size") String size, @PathParam("rotation") String rotation,@PathParam("qf") String qf) {
        try {
            return this.iiifResource.iiifTile(pid, region, size, rotation, qf);
        } catch (IOException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    
    
    
    @GET
    @Path("zoomify/{pid}/ImageProperties.xml")
    @Produces("application/xml")
    public Response zoomifyManifest(@PathParam("pid") String pid) {
        return this.zoomifyResource.zoomifyManifest(pid);
    }

    @GET
    @Path("zoomify/{pid}/TileGroup0/{level}-{x}-{y}.jpg")
    @Produces("image/jpeg")
    public Response zoomifyTile(@PathParam("pid") String pid, @PathParam("level") String level,
            @PathParam("x") String x, @PathParam("y") String y) {
        try {
            return this.zoomifyResource.renderZoomifyTile(pid, level, x, y, "jpg");
        } catch (IOException | XPathExpressionException | SQLException e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @GET
    @Path("providedBy/{pid}")
    @Produces("appliction/json")
    public Response providedBy(@PathParam("pid") String pid) {
        return this.itemResource.providedBy(pid);
    }

    
    
    // --------------- CDK Replication endpoint ---------------
    @GET
    @Path("sync/solr/select")
    @Produces({ MediaType.APPLICATION_XML + ";charset=utf-8" })
    public Response selectXML(@Context UriInfo uriInfo) throws IOException {
        return this.solrResource.selectXML(uriInfo);
    }

    @GET
    @Path("sync/solr/select")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response selectJSON(@Context UriInfo uriInfo) throws IOException {
        return this.solrResource.selectJSON(uriInfo);
    }
    
    @GET
    @Path("sync/batch/foxmls")
    @Produces("application/zip")
    public Response batchedFOXL(@QueryParam("pids") String stringPids, @QueryParam("collection") String collection)  throws ReplicateException, IOException {
        return this.solrResource.batchedFOXL(stringPids, collection);
    }
    
    @GET
    @Path("sync/{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getExportedFOXML(@PathParam("pid") String pid,
        @QueryParam("collection") String collection)
        throws ReplicateException, UnsupportedEncodingException {
        return this.solrResource.getExportedFOXML(pid, collection);
    }

}
