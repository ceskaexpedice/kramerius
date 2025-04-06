package cz.incad.kramerius.rest.apiNew.admin.v70.akubra;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.*;
import org.ceskaexpedice.akubra.relsext.RelsExtLiteral;
import org.ceskaexpedice.akubra.relsext.RelsExtRelation;
import org.ceskaexpedice.fedoramodel.DigitalObject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


// TODO AK_NEW -             WorkingModeManager.setReadOnly - delat check a vracet 400, pokus o zaktivneni


/**
 * AkubraResource
 * @author ppodsednik
 */
@Path("/admin/v7.0/repository")
public class AkubraResource extends AdminApiResource {

    public static final Logger LOGGER = Logger.getLogger(AkubraResource.class.getName());

    private enum ExportType {archive, storage};

    @Inject
    Provider<User> userProvider;
    @Inject
    RightsResolver rightsResolver;

    @GET
    @Path("/export")
    @Produces(MediaType.APPLICATION_XML)
    public Response export(@QueryParam("pid") String pid, @QueryParam("format") String format) {
        try {
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to export (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            DigitalObjectWrapper export;
            if(ExportType.archive.toString().equals(format)) {
                export = akubraRepository.export(pid);
            }else if(ExportType.storage.toString().equals(format)){
                export = akubraRepository.get(pid);
            }else{
                throw new BadRequestException("Not supported format: " + format);
            }
            return Response.ok(export.asString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("/getMetadata")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetadata(@QueryParam("pid") String pid) {
        try {
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to get object metadata (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            DigitalObjectMetadata digitalObjectMetadata = akubraRepository.getMetadata(pid);
            JSONObject jsonObject = DigitalObjectMetadataConverter.toJSONObject(digitalObjectMetadata);
            return Response.ok(jsonObject.toString()).build();
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
    public Response getDatastreamContent(@QueryParam("pid") String pid, @QueryParam("dsId") String dsId) {
        try {
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to get datastream content (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, dsId);
            DatastreamContentWrapper datastreamContent = akubraRepository.getDatastreamContent(pid, dsId);
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
    public Response getDatastreamMetadata(@QueryParam("pid") String pid, @QueryParam("dsId") String dsId) {
        try {
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to get datastream metadata (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, dsId);
            DatastreamMetadata datastreamMetadata = akubraRepository.getDatastreamMetadata(pid, dsId);
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
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to get datastream names (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
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
    @Path("/getRelations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelations(@QueryParam("pid") String pid) {
        try {
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to get relationships (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KnownDatastreams.RELS_EXT.toString());
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

    @GET
    @Path("/getLiterals")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLiterals(@QueryParam("pid") String pid) {
        try {
            if (!permitAction(this.rightsResolver, true)) {
                throw new ForbiddenException("user '%s' is not allowed to get literals (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_READ);
            }
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KnownDatastreams.RELS_EXT.toString());
            List<RelsExtLiteral> literals = akubraRepository.re().getLiterals(pid, null);
            JSONObject jsonObject = RelsExtLiteralConverter.toJSONObject(literals);
            return Response.ok(jsonObject.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @POST
    @Path("/ingest")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response ingest(InputStream ingestStream) {
        try {
            if (!permitAction(this.rightsResolver, false)) {
                throw new ForbiddenException("user '%s' is not allowed to ingest (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_EDIT);
            }
            DigitalObject digitalObject = akubraRepository.unmarshall(ingestStream);
            akubraRepository.ingest(digitalObject);
            JSONObject retVal = new JSONObject();
            retVal.put("pid", digitalObject.getPID());
            return Response.ok(retVal.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            //WorkingModeManager.setReadOnly
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @DELETE
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@QueryParam("pid") String pid) {
        try {
            if (!permitAction(this.rightsResolver, false)) {
                throw new ForbiddenException("user '%s' is not allowed to delete (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_EDIT);
            }
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            akubraRepository.delete(pid, true, true);
            JSONObject retVal = new JSONObject();
            retVal.put("pid", pid);
            return Response.ok(retVal.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @POST
    @Path("/createXMLDatastream")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response createXMLDatastream(@QueryParam("pid") String pid, @QueryParam("dsId") String dsId, @QueryParam("mimeType") String mimeType,
                                        InputStream inputStream) {
        try {
            if (!permitAction(this.rightsResolver, false)) {
                throw new ForbiddenException("user '%s' is not allowed to create a stream (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_EDIT);
            }
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            akubraRepository.createXMLDatastream(pid, dsId, mimeType, inputStream);
            JSONObject retVal = new JSONObject();
            retVal.put("dsId", dsId);
            return Response.ok(retVal.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            //WorkingModeManager.setReadOnly
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @POST
    @Path("/createManagedDatastream")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response createManagedDatastream(@QueryParam("pid") String pid, @QueryParam("dsId") String dsId, @QueryParam("mimeType") String mimeType,
                                        InputStream inputStream) {
        try {
            if (!permitAction(this.rightsResolver, false)) {
                throw new ForbiddenException("user '%s' is not allowed to create a stream (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_EDIT);
            }
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            akubraRepository.createManagedDatastream(pid, dsId, mimeType, inputStream);
            JSONObject retVal = new JSONObject();
            retVal.put("dsId", dsId);
            return Response.ok(retVal.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            //WorkingModeManager.setReadOnly
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @DELETE
    @Path("/deleteDatastream")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDatastream(@QueryParam("pid") String pid, @QueryParam("dsId") String dsId) {
        try {
            if (!permitAction(this.rightsResolver, false)) {
                throw new ForbiddenException("user '%s' is not allowed to delete (action '%s')", this.userProvider.get(),
                        SecuredActions.A_AKUBRA_EDIT);
            }
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, dsId);
            akubraRepository.deleteDatastream(pid, dsId);
            JSONObject retVal = new JSONObject();
            retVal.put("dsId", dsId);
            return Response.ok(retVal.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (DistributedLocksException e) {
            // TODO AK_NEW
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    public class DigitalObjectMetadataConverter {

        public static JSONObject toJSONObject(DigitalObjectMetadata metadata) {
            if (metadata == null) {
                return new JSONObject();
            }

            JSONObject json = new JSONObject();
            json.put("propertyLabel", metadata.getPropertyLabel());
            json.put("propertyCreated", formatDate(metadata.getPropertyCreated()));
            json.put("propertyLastModified", formatDate(metadata.getPropertyLastModified()));
            json.put("objectStoragePath", metadata.getObjectStoragePath() != null
                    ? metadata.getObjectStoragePath().getAbsolutePath()
                    : null);

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

    private static class DatastreamMetadataConverter {

        private static JSONObject toJSONObject(DatastreamMetadata datastream) {
            if (datastream == null) {
                return new JSONObject();
            }

            JSONObject json = new JSONObject();
            json.put("id", datastream.getId());
            json.put("mimetype", datastream.getMimetype());
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

    private class RelsExtLiteralConverter {

        private static JSONObject toJSONObject(List<RelsExtLiteral> literals) {
            JSONArray jsonArray = new JSONArray();

            for (RelsExtLiteral relation : literals) {
                JSONObject relationJson = new JSONObject();
                relationJson.put("namespace", relation.getNamespace());
                relationJson.put("localName", relation.getLocalName());
                relationJson.put("content", relation.getContent());
                jsonArray.put(relationJson);
            }

            JSONObject result = new JSONObject();
            result.put("literals", jsonArray);
            return result;
        }
    }

    public boolean permitAction(RightsResolver rightsResolver, boolean read) {
        boolean permitted = this.userProvider.get() != null ? rightsResolver.isActionAllowed(this.userProvider.get(),
                read ? SecuredActions.A_AKUBRA_READ.getFormalName() : SecuredActions.A_AKUBRA_EDIT.getFormalName(),
                SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag() : false;
        return permitted;
    }

}
