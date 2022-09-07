package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.logging.Logger;


@Path("/admin/v7.0/files")
public class ServerFilesResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ServerFilesResource.class.getName());

    //TODO: prejmenovat role podle spravy uctu
    //private static final String ROLE_LIST_DIRS_ON_SERVER = "kramerius_admin";


    @javax.inject.Inject
    Provider<User> userProvider;

    @GET
    @Path("/input-data-dir-for_import-foxml{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInInputDataDirFor_importFoxml(@PathParam("path") String path) {
        //authentication
        //AuthenticatedUser user = getAuthenticatedUserByOauth();

        User user1 = this.userProvider.get();
        List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

        if (!permit(user1)) {
            throw new ForbiddenException("user '%s' is not allowed to list files on server (missing action '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
        }
        //return data
        return listFilesInDir("import.directory", path);
    }

    @GET
    @Path("/input-data-dir-for_convert-and-import-ndk{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInInputDataDirFor_convertAndImportNdk(@PathParam("path") String path) {
        //authentication
        User user1 = this.userProvider.get();
        List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
        if (!permit(user1)) {
            throw new ForbiddenException("user '%s' is not allowed to list files on server (missing action '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
        }
        //return data
        return listFilesInDir("convert.directory", path);
    }

    @GET
    @Path("/pidlist-dir{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInPidlistDir(@PathParam("path") String path) {
        //authentication
        User user1 = this.userProvider.get();
        List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
        if (!permit(user1)) {
            throw new ForbiddenException("user '%s' is not allowed to list files on server (missing role '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
        }
        //return data
        return listFilesInDir("pidlist.directory", path);
    }

    private Response listFilesInDir(String rootDirNamePropKey, String path) {
        try {
            if (path == null) {
                path = "";
            }
            String rootDirName = KConfiguration.getInstance().getProperty(rootDirNamePropKey);
            if (rootDirName == null) {
                throw new BadRequestException("no property found with name '" + rootDirNamePropKey + "'");
            }
            File dir = new File(rootDirName, path);
            if (!dir.exists()) {
                throw new BadRequestException("directory doesn't exist: " + dir.getAbsolutePath());
            } else if (!dir.isDirectory()) {
                throw new BadRequestException("file, not directory: " + dir.getAbsolutePath());
            }

            List<File> files = Arrays.stream(dir.listFiles()).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
            JSONArray filesJson = new JSONArray();
            for (File file : files) {
                JSONObject fileJson = new JSONObject();
                fileJson.put("name", file.getName());
                fileJson.put("isDir", file.isDirectory());
                filesJson.put(fileJson);
            }

            JSONObject json = new JSONObject();
            json.put("rootDir", dir.getAbsolutePath());
            json.put("files", filesJson);
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    boolean permit(User user) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.A_IMPORT.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }

}
