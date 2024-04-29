package cz.incad.kramerius.rest.apiNew.admin.v10.reharvest;

import java.text.ParseException;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;

@Path("/admin/v7.0/reharvest")
public class ReharvestResource {

    public Logger LOGGER = Logger.getLogger(ReharvestResource.class.getName());

    @Inject
    private ReharvestManager reharvestManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarvests() {
        JSONArray jsonArray = new JSONArray();
        this.reharvestManager.getItems().forEach(ri -> {
            jsonArray.put(ri.toJSON());
        });
        return Response.ok(jsonArray.toString()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHarvest(@PathParam("id") String id) {
        ReharvestItem item = this.reharvestManager.getItemById(id);
        if (item != null) {
            return Response.ok(item.toJSON().toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        ReharvestItem item = this.reharvestManager.getItemById(id);
        if (item != null) {
            this.reharvestManager.deregister(id);
            return Response.ok(item.toJSON().toString()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(String json) {
        try {
            JSONObject jsonObj = new JSONObject(json);
            if (!jsonObj.has("id")) {
                jsonObj.put("id", UUID.randomUUID().toString());
            }
            ReharvestItem item = ReharvestItem.fromJSON(jsonObj);
            if (item != null) {
                item.setTimestamp(Instant.now());
                this.reharvestManager.register(item);
                return Response.ok(item.toJSON().toString()).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (JSONException | ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
  

  
  @GET
  @Path("top")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTopHarvest(@QueryParam("state")String state) {
      ReharvestItem topItem = this.reharvestManager.getTopItem(state);
      if (topItem != null) {
          return Response.ok(topItem.toJSON().toString()).build();
      } else {
          return Response.status(Response.Status.NOT_FOUND).build();
      }
  }

  
  
  @PUT
  @Path("{id}/state")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTopHarvest(@PathParam("id") String id, @QueryParam("state") String state) {
      ReharvestItem itemById = reharvestManager.getItemById(id);
      if (itemById != null) {
          itemById.setState(state);
          return Response.ok(itemById.toJSON().toString()).build();
      } else {
          return Response.status(Response.Status.NOT_FOUND).build();
      }
  }

}
