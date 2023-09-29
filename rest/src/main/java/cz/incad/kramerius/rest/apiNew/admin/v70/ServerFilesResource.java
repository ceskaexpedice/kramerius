package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.admin.v70.files.GenerateDownloadLinks;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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
    
    @javax.inject.Inject
    GenerateDownloadLinks genDownloadLinks;

    @GET
    @Path("/output-data-dir-for_nkplogs{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInOutputDataDirFor_nkplogs(@PathParam("path") String path,@QueryParam("generatedownloads") Boolean downloadLinks) {
        try {
            User user1 = this.userProvider.get();
            List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
            if (!permitNKPLogsFolders(user1)) {
                throw new ForbiddenException("user '%s' is not allowed to list files on server (missing action '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
            }
            
            String nkpFolder = KConfiguration.getInstance().getConfiguration().getString("nkp.logs.folder");
            if (nkpFolder != null) {
                File f = new File(nkpFolder, path);
                if (f.isDirectory()) {
                    //max 20 newest files
                    return listFilesInDir("nkp.logs.folder", path, (file)->{
                        return file.isFile() && file.getName().endsWith(".zip");
                    },Comparator.comparing(File::lastModified).reversed(), 20);
                } else {
                    
                    JSONObject fileJson = new JSONObject();
                    fileJson.put("name", f.getName());
                    fileJson.put("isDir", f.isDirectory());
                    
                    java.nio.file.Path p = f.toPath();

                    BasicFileAttributes attributes = Files.readAttributes(p, BasicFileAttributes.class);
                    fileJson.put("lastModifiedTime", attributes.lastModifiedTime().toMillis());
                    fileJson.put("lastAccessTime", attributes.lastAccessTime().toMillis());
                    
                    if (downloadLinks) {
                        fileJson.put("downloadlink", genDownloadLinks.generateTmpLink(f));
                    }
                    
                    return Response.ok(fileJson).build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    
    @GET
    @Path("/input-data-dir-for_import-foxml{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInInputDataDirFor_importFoxml(@PathParam("path") String path) {
        //authentication
        //AuthenticatedUser user = getAuthenticatedUserByOauth();

        User user1 = this.userProvider.get();
        List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());

        if (!permitImportFolders(user1)) {
            throw new ForbiddenException("user '%s' is not allowed to list files on server (missing action '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
        }
        //return data
        return listFilesInDir("import.directory", path, null, Comparator.comparing(File::getName), -1);
    }

    @GET
    @Path("/input-data-dir-for_convert-and-import-ndk{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInInputDataDirFor_convertAndImportNdk(@PathParam("path") String path) {
        //authentication
        User user1 = this.userProvider.get();
        List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
        if (!permitImportFolders(user1)) {
            throw new ForbiddenException("user '%s' is not allowed to list files on server (missing action '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
        }
        //return data
        return listFilesInDir("convert.directory", path, null,Comparator.comparing(File::getName),-1);
    }

    @GET
    @Path("/pidlist-dir{path: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFilesInPidlistDir(@PathParam("path") String path) {
        //authentication
        User user1 = this.userProvider.get();
        List<String> roles = Arrays.stream(user1.getGroups()).map(Role::getName).collect(Collectors.toList());
        if (!permitImportFolders(user1)) {
            throw new ForbiddenException("user '%s' is not allowed to list files on server (missing role '%s')", user1.getLoginname(), SecuredActions.A_IMPORT.getFormalName()); //403
        }
        //return data
        return listFilesInDir("pidlist.directory", path, null,Comparator.comparing(File::getName),-1);
    }

    @GET
    @Path("/download-path{path: (.+)?}")
    public Response downloadTmplPath(@PathParam("path") String tmppath) {
        try {
            if (tmppath.startsWith("/")) {
                tmppath = tmppath.substring(1);
            }
            File generatedTmpFile = this.genDownloadLinks.getGeneratedTmpFile(tmppath);
            if (generatedTmpFile != null) {
                InputStream is = new FileInputStream(generatedTmpFile);
                StreamingOutput stream = output -> {
                    IOUtils.copy(is, output);
                    IOUtils.closeQuietly(is);
                };
                return Response.ok().entity(stream).type("application/octet-stream").build();
                
            } else return Response.status(Response.Status.NOT_FOUND).build();
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    
    
    private Response listFilesInDir(String rootDirNamePropKey, String path, FileFilter filter, Comparator<File> comparator, int maxFiles) {
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

            List<File> files = Arrays.stream(filter != null ?  dir.listFiles(filter) : dir.listFiles()).sorted(comparator).collect(Collectors.toList());
            if (maxFiles > 0) {
                files = files.subList(0, Math.min(maxFiles, files.size()));
            }
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

    boolean permitImportFolders(User user) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.A_IMPORT.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }

    boolean permitNKPLogsFolders(User user) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.A_GENERATE_NKPLOGS.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }

}
