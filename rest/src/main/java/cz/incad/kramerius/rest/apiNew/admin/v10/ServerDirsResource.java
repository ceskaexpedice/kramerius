package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.logging.Logger;


@Path("/admin/v1.0/dirs")
public class ServerDirsResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ServerDirsResource.class.getName());

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_LIST_DIRS_ON_SERVER = "kramerius_admin";

    @GET
    @Path("/import-foxml-input/subDirs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listSubdirsOfDir_importFoxmlInput(@PathParam("key") String key) {
        //authentication
        AuthenticatedUser user = getAuthenticatedUserByOauth();
        String role = ROLE_LIST_DIRS_ON_SERVER;
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("user '%s' is not allowed to list dirs on server (missing role '%s')", user.getName(), role); //403
        }
        //return data
        return listSubdirsOfDir("import.directory");
    }

    //TODO: podobně metody pro další adresáře, jako třeba in/out pro import dat v NDK formatu

    private Response listSubdirsOfDir(String rootDirNamePropKey) {
        try {
            String rootDirName = KConfiguration.getInstance().getProperty(rootDirNamePropKey);
            if (rootDirName == null) {
                throw new BadRequestException("no property found with name '" + rootDirNamePropKey + "'");
            }
            List<String> dirNames = listDirsInRootDir(rootDirName);
            JSONObject json = new JSONObject();
            json.put("rootDir", new File(rootDirName).getAbsolutePath());
            JSONArray dirs = new JSONArray();
            for (String dirName : dirNames) {
                dirs.put(dirName);
            }
            json.put("dirs", dirs);
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private List<String> listDirsInRootDir(String rootDirName) {
        File rootDir = new File(rootDirName);
        List<String> result = new ArrayList<>();
        result.add(".");
        addAllSubdirs(rootDir, ".", result, 2);
        return result;
    }

    private void addAllSubdirs(File rootDir, String path, List<String> result, int levels) {
        List<File> dirs = Arrays.stream(rootDir.listFiles(f -> f.isDirectory())).sorted(Comparator.comparing(File::getName)).collect(Collectors.toList());
        for (File dir : dirs) {
            String currentPath = path + '/' + dir.getName();
            result.add(currentPath);
            if (levels > 0) {
                addAllSubdirs(dir, currentPath, result, levels - 1);
            }
        }
    }

}
