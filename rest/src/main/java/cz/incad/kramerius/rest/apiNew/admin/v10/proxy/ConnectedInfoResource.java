package cz.incad.kramerius.rest.apiNew.admin.v10.proxy;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;

import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.OneInstance.TypeOfChangedStatus;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.timestamps.Timestamp;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.timestamps.impl.SolrTimestamp;

@Path("/admin/v7.0/connected")
public class ConnectedInfoResource {

    public static final Logger LOGGER = Logger.getLogger(ConnectedInfoResource.class.getName());

    @Inject
    private Instances libraries;

    @Inject
    private TimestampStore timestampStore;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllConnected() {
        JSONObject retval = new JSONObject();
        this.libraries.allInstances().forEach(library -> {
            JSONObject json = libraryJSON(library);
            retval.put(library.getName(), json);
        });
        return Response.ok(retval).build();
    }

    private JSONObject libraryJSON(OneInstance found) {
        JSONObject retval = new JSONObject();
        retval.put("status", found.isConnected());
        retval.put("type", found.getType().name());
        return retval;
    }

    @GET
    @Path("{library}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response library(@PathParam("library") String library) {
        OneInstance find = this.libraries.find(library);
        if (find != null) {
            return Response.ok(libraryJSON(find)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @PUT
    @Path("{library}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response library(@PathParam("library") String library, @QueryParam("status") String status) {
        OneInstance find = this.libraries.find(library);
        if (find != null) {
            find.setConnected(Boolean.parseBoolean(status), TypeOfChangedStatus.user);
            return Response.ok(libraryJSON(find)).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @GET
    @Path("{library}/timestamp")
    @Produces(MediaType.APPLICATION_JSON)
    public Response timestamp(@PathParam("library") String library) {
        try {
            Timestamp latest = this.timestampStore.findLatest(library);
            if (latest != null) {
                return Response.ok(latest.toJSONObject().toString()).build();
            } else
                return Response.status(Response.Status.NOT_FOUND).build();
        } catch (SolrServerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @GET
    @Path("refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refresh() {
        try {
            this.libraries.cronRefresh();
            return Response.ok(new JSONObject()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("{library}/timestamps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response timestamps(@PathParam("library") String library) {
        try {
            JSONArray array = new JSONArray();
            List<Timestamp> retrieveTimestamp = this.timestampStore.retrieveTimestamps(library);
            retrieveTimestamp.forEach(t -> {
                array.put(t.toJSONObject());
            });
            return Response.ok(array.toString()).build();
        } catch (SolrServerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Path("{library}/timestamp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response timestamp(@PathParam("library") String library, JSONObject jsonObject) {
        try {
            Timestamp timestamp = SolrTimestamp.fromJSONDoc(library, jsonObject);
            if (timestamp.getDate() == null) {
                timestamp.updateDate(new Date());
            }
            timestamp.updateName(library);
            this.timestampStore.storeTimestamp(timestamp);
            return Response.ok(timestamp.toJSONObject().toString()).build();
        } catch (SolrServerException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
