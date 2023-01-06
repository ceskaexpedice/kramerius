package cz.incad.kramerius.rest.apiNew.cdk.v70;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKIIIFResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKItemResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKUsersResource;
import cz.incad.kramerius.rest.apiNew.cdk.v70.resources.CDKZoomifyResource;


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

    @GET
    @Path("user")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response user() {
        return this.usersResource.user();
    }

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
    @Produces("image/jpeg")
    @Path("iiif/{pid}/{region}/{size}/{rotation}/default.jpg")
    public Response tile(@PathParam("pid") String pid, @PathParam("region") String region,
            @PathParam("size") String size, @PathParam("rotation") String rotation) {
        try {
            return this.iiifResource.iiifTile(pid, region, size, rotation);
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

}
