package cz.incad.kramerius.rest.apiNew.admin.v70.akubra;

import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.DatastreamContentWrapper;
import org.ceskaexpedice.akubra.DatastreamMetadata;
import org.ceskaexpedice.akubra.DigitalObjectWrapper;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * AkubraResource
 * @author ppodsednik
 */
@Path("/admin/v7.0/repository")
public class AkubraResource extends AdminApiResource {

    public static final Logger LOGGER = Logger.getLogger(AkubraResource.class.getName());

    @GET
    @Path("/export")
    @Produces(MediaType.APPLICATION_XML)
    public Response export(@QueryParam("pid") String pid, @QueryParam("format") String format) {
        try {
            // TODO AK_NEW - add security check; remove format parameter
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            DigitalObjectWrapper export = akubraRepository.export(pid);
            return Response.ok(export.asString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("/getDatastreamContent")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getDatastreamContent(@QueryParam("pid") String pid, @QueryParam("dsID") String dsID) {
        try {
            // TODO AK_NEW - add security check;
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, dsID);
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, dsID);
            if (datastreamContent != null) {
                StreamingOutput stream = output -> {
                    IOUtils.copy(datastreamContent.asInputStream(), output);
                    IOUtils.closeQuietly(datastreamContent.asInputStream());
                };
                return Response.ok().entity(stream).type("application/octet-stream").build();

            } else return Response.status(Response.Status.NOT_FOUND).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("/getDatastreamMetadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatastreamMetadata(@QueryParam("pid") String pid, @QueryParam("dsID") String dsID) {
        try {
            // TODO AK_NEW - add security check;
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, dsID);
            DatastreamMetadata datastreamMetadata = akubraRepository.getDatastreamMetadata(pid, dsID);
            JSONObject jsonObject = DatastreamMetadataConverter.toJSONObject(datastreamMetadata);
            return Response.ok(jsonObject.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("/getDatastreamNames")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatastreamNames(@QueryParam("pid") String pid) {
        try {
            // TODO AK_NEW - add security check;
            checkSupportedObjectPid(pid);
            List<String> datastreamNames = akubraRepository.getDatastreamNames(pid);
            JSONObject jsonObject = DatastreamNamesConverter.toJSONObject(datastreamNames);
            return Response.ok(jsonObject.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("/getRelationships")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationships(@QueryParam("pid") String pid) {
        try {
            // TODO AK_NEW - add security check;
            checkSupportedObjectPid(pid);
            List<RelsExtRelation> relations = akubraRepository.re().getRelations(pid, null);
            JSONObject jsonObject = RelsExtRelationConverter.toJSONObject(relations);
            return Response.ok(jsonObject.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private static class DatastreamMetadataConverter {

        private static JSONObject toJSONObject(DatastreamMetadata datastream) {
            if (datastream == null) {
                return new JSONObject();
            }

            JSONObject json = new JSONObject();
            json.put("id", datastream.getId());
            json.put("mimetype", datastream.getMimetype());
            json.put("size", datastream.getSize());
            json.put("controlGroup", datastream.getControlGroup());
            json.put("location", datastream.getLocation());
            json.put("createDate", formatDate(datastream.getCreateDate()));
            json.put("lastModified", formatDate(datastream.getLastModified()));

            return json;
        }

        private static String formatDate(Date date) {
            if (date == null) {
                return null;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(date);
        }
    }

    private class DatastreamNamesConverter {

        private static JSONObject toJSONObject(List<String> datastreamNames) {
            JSONObject json = new JSONObject();
            json.put("datastreamNames", new JSONArray(datastreamNames));
            return json;
        }
    }

    private class RelsExtRelationConverter {

        private static JSONObject toJSONObject(List<RelsExtRelation> relations) {
            JSONArray jsonArray = new JSONArray();

            for (RelsExtRelation relation : relations) {
                JSONObject relationJson = new JSONObject();
                relationJson.put("namespace", relation.getNamespace());
                relationJson.put("localName", relation.getLocalName());
                relationJson.put("resource", relation.getResource());
                jsonArray.put(relationJson);
            }

            JSONObject result = new JSONObject();
            result.put("relations", jsonArray);
            return result;
        }
    }
}
