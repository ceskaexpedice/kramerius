package cz.inovatika.folders.jersey;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.inovatika.folders.db.FolderDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

//@Path("/folders")
@Path("/client/v7.0/folders")
public class EndpointFolders {

    @Inject
    Provider<User> userProvider;
    
    @Inject
    FolderDatabase db;
    //Authenticator auth = new Authenticator();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFoldersRelatedToMe() {
        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;
        if (authenticated) {
            String userId = luser.getLoginname();
            List<FolderDatabase.Folder> folders = db.getFolders(userId);
            JSONArray result = new JSONArray();
            for (FolderDatabase.Folder folder : folders) {
                JSONObject folderJson = new JSONObject();
                folderJson.put("uuid", folder.uuid);
                folderJson.put("name", folder.name);
                folderJson.put("itemsCount", folder.itemsCount);
                folderJson.put("updatedAt", folder.updatedAt);
                JSONArray users = new JSONArray();
                for (FolderDatabase.FolderUser user : filterFolderUsers(folder.users, userId)) {
                    JSONObject userJson = new JSONObject();
                    userJson.put("userId", user.userId);
                    userJson.put("userRole", user.userRole);
                    userJson.put("createdAt", user.createdAt);
                    users.put(userJson);
                }
                folderJson.append("users", users);

                result.put(folderJson);
            }
            return Response.ok(result.toString()).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Path("/{folderUuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolder(@PathParam("folderUuid") String folderUuid) {
        User luser = this.userProvider.get();

        FolderDatabase.Folder folder = db.getFolderByUuid(folderUuid);
        if (folder == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            JSONObject folderJson = new JSONObject();
            folderJson.put("uuid", folder.uuid);
            folderJson.put("name", folder.name);
            folderJson.put("itemsCount", folder.itemsCount);
            folderJson.put("updatedAt", folder.updatedAt);

            JSONArray usersJson = new JSONArray();
            for (FolderDatabase.FolderUser folderUser : filterFolderUsers(folder.users, luser != null ? luser.getLoginname() : null)) {
                JSONObject userJson = new JSONObject();
                userJson.put("userId", folderUser.userId);
                userJson.put("userRole", folderUser.userRole);
                userJson.put("createdAt", folderUser.createdAt);
                usersJson.put(userJson);
            }
            folderJson.append("users", usersJson);

            JSONArray itemsJson = new JSONArray();
            db.getItems(folderUuid).stream().forEach(folderItem -> {
                JSONObject itemJson = new JSONObject();
                itemJson.put("id", folderItem.id);
                itemJson.put("createdAt", folderItem.createdAt);
                itemsJson.put(itemJson);
            });
            folderJson.append("items", itemsJson);

            return Response.ok(folderJson.toString()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFolder(String folderDefinitionStr) {
        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;
        if (authenticated) {
            //check inputs
            //String userId = auth.extractUserIdFromAuthHeader(authorizationHeader);
            JSONObject definition = new JSONObject(folderDefinitionStr);

            //System.out.println(folderDefinitionStr);
            if (!definition.has("name")) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("missing 'name'")).build();
            }
            String name = definition.getString("name").trim();
            if (name.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("empty 'name'")).build();
            }

            //create database records
            String folderUuid = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            FolderDatabase.Folder folder = db.createFolder(name, folderUuid, now);
            FolderDatabase.FolderUser folderOwner = db.createFolderUser(folderUuid, luser.getLoginname(), "owner", now);

            //return results
            JSONObject folderJson = new JSONObject();
            folderJson.put("uuid", folder.uuid);
            folderJson.put("name", folder.name);
            folderJson.put("itemsCount", folder.itemsCount);
            folderJson.put("updatedAt", folder.updatedAt);

            JSONArray usersJson = new JSONArray();
            JSONObject userJson = new JSONObject();
            userJson.put("userId", folderOwner.userId);
            userJson.put("userRole", folderOwner.userRole);
            userJson.put("createdAt", folderOwner.createdAt);
            usersJson.put(userJson);
            folderJson.append("users", usersJson);
            return Response.status(Response.Status.CREATED).entity(folderJson.toString()).build();

        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
            
        }
    }

    @PUT
    @Path("/{folderUuid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFolder(String folderDefinitionStr, @PathParam("folderUuid") String folderUuid) {
        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;
        if (authenticated) {

            JSONObject definition = new JSONObject(folderDefinitionStr);
            //System.out.println(folderDefinitionStr);
            if (!definition.has("name")) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("missing 'name'")).build();
            }
            String name = definition.getString("name").trim();
            if (name.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("empty 'name'")).build();
            }

            //check existence
            FolderDatabase.Folder folderByUuid = db.getFolderByUuid(folderUuid);
            if (folderByUuid == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Utils.buildErrorJsonStr("folder doesn't exist")).build();
            }
            //check rights
            AtomicBoolean canEdit = new AtomicBoolean(false);
            db.getFolderUsers(folderUuid).stream().filter(folderUser -> folderUser.userId.equals(luser.getLoginname())).forEach(folderUser -> {
                if (folderUser.userRole.equals("owner")) {
                    canEdit.set(true);
                }
            });
            if (!canEdit.get()) {
                return Response.status(Response.Status.FORBIDDEN).entity(Utils.buildErrorJsonStr("you are not owner of the folder")).build();
            }

            //update name
            db.updateFolderName(folderUuid, name);

            //return updated folder
            FolderDatabase.Folder folder = db.getFolderByUuid(folderUuid);
            JSONObject folderJson = new JSONObject();
            folderJson.put("uuid", folder.uuid);
            folderJson.put("name", folder.name);
            folderJson.put("itemsCount", folder.itemsCount);
            folderJson.put("updatedAt", folder.updatedAt);

            JSONArray usersJson = new JSONArray();
            for (FolderDatabase.FolderUser folderUser : filterFolderUsers(folder.users, luser != null ? luser.getLoginname() : null)) {
                JSONObject userJson = new JSONObject();
                userJson.put("userId", folderUser.userId);
                userJson.put("userRole", folderUser.userRole);
                userJson.put("createdAt", folderUser.createdAt);
                usersJson.put(userJson);
            }
            folderJson.append("users", usersJson);
            return Response.ok(folderJson.toString()).build();

        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
            
        }
    }

    /**
     * Filter folder users based on user role and user id
     * <p>
     * If I am owner, return all users; if I am not owner, return only owner and possibly me; If user id is null, return only owner
     */
    private List<FolderDatabase.FolderUser> filterFolderUsers(List<FolderDatabase.FolderUser> users, String userId) {
        String ownerId = null;
        for (FolderDatabase.FolderUser user : users) {
            if (user.userRole.equals("owner")) {
                ownerId = user.userId;
            }
        }
        final String finalOwnerId = ownerId;
        if (userId == null) { //no user id, return only owner
            return users.stream().filter(user -> user.userId.equals(finalOwnerId)).collect(Collectors.toList());
        }
        if (userId.equals(ownerId)) { //I am owner, return all users
            return users;
        } else { //I am not owner, return only owner and possibly me
            return users.stream().filter(user -> user.userId.equals(userId) || user.userId.equals(finalOwnerId)).collect(Collectors.toList());
        }
    }

    @DELETE
    @Path("/{folderUuid}")
    public Response deleteFolder(@PathParam("folderUuid") String folderUuid) {
        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;

        if (authenticated) {
            FolderDatabase.Folder folderByUuid = db.getFolderByUuid(folderUuid);
            if (folderByUuid == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Utils.buildErrorJsonStr("folder doesn't exist")).build();
            }

            //check rights
            AtomicBoolean canEdit = new AtomicBoolean(false);
            db.getFolderUsers(folderUuid).stream().filter(folderUser -> folderUser.userId.equals(luser.getLoginname())).forEach(folderUser -> {
                if (folderUser.userRole.equals("owner")) {
                    canEdit.set(true);
                }
            });
            if (!canEdit.get()) {
                return Response.status(Response.Status.FORBIDDEN).entity(Utils.buildErrorJsonStr("you are not owner of the folder")).build();
            }

            //delete folderUsers and folder
            db.deleteFolderUsers(folderUuid);
            db.deleteFolder(folderUuid);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }        
    }

    @PUT
    @Path("/{folderUuid}/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addFolderItems(String folderDefinitionStr, @PathParam("folderUuid") String folderUuid) {

        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;

        if (authenticated) {
            //check inputs
            //String userId = auth.extractUserIdFromAuthHeader(authorizationHeader);
            JSONObject definition = new JSONObject(folderDefinitionStr);
            //System.out.println(folderDefinitionStr);
            if (!definition.has("items")) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("missing 'items'")).build();
            }
            if (!Utils.isJsonArrayContainingStrings(definition.get("items"))) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("items must be array of strings")).build();
            }
            JSONArray itemsJson = definition.getJSONArray("items");
            //check existence
            FolderDatabase.Folder folderByUuid = db.getFolderByUuid(folderUuid);
            if (folderByUuid == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Utils.buildErrorJsonStr("folder doesn't exist")).build();
            }
            //check rights
            AtomicBoolean canEdit = new AtomicBoolean(false);
            db.getFolderUsers(folderUuid).stream().filter(folderUser -> folderUser.userId.equals(luser.getLoginname())).forEach(folderUser -> {
                if (folderUser.userRole.equals("owner")) {
                    canEdit.set(true);
                }
            });
            if (!canEdit.get()) {
                return Response.status(Response.Status.FORBIDDEN).entity(Utils.buildErrorJsonStr("you are not owner of the folder")).build();
            }

            //add items that are not already there
            Set<String> itemsPresent = db.getItems(folderUuid).stream().map(folderItem -> folderItem.id).collect(Collectors.toSet());
            List<String> itemsAddedPreviously = new ArrayList<>();
            List<String> itemsAddedNow = new ArrayList<>();
            //itemsJson.forEach(o -> System.out.println(o.toString()));
            for (int i = 0; i < itemsJson.length(); i++) {
                String itemId = itemsJson.get(i).toString();
                if (itemsPresent.contains(itemId)) {
                    itemsAddedPreviously.add(itemId);
                } else {
                    db.addFolderItem(folderUuid, itemId, System.currentTimeMillis());
                    itemsAddedNow.add(itemId);
                }
                
            }
            JSONArray foundAlready = new JSONArray();
            itemsAddedPreviously.forEach(item -> foundAlready.put(item));
            JSONArray addedNow = new JSONArray();
            itemsAddedNow.forEach(item -> addedNow.put(item));
            JSONObject resultJson = new JSONObject();
            resultJson.put("foundAlready", foundAlready);
            resultJson.put("addedNow", addedNow);

            return Response.ok(resultJson.toString()).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }        
    }

    @DELETE
    @Path("/{folderUuid}/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeFolderItems(String folderDefinitionStr, @PathParam("folderUuid") String folderUuid) {
        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;

        if (authenticated) {
            JSONObject definition = new JSONObject(folderDefinitionStr);
            if (!definition.has("items")) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("missing 'items'")).build();
            }
            if (!Utils.isJsonArrayContainingStrings(definition.get("items"))) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("items must be array of strings")).build();
            }
            JSONArray itemsJson = definition.getJSONArray("items");
            //check existence
            FolderDatabase.Folder folderByUuid = db.getFolderByUuid(folderUuid);
            if (folderByUuid == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Utils.buildErrorJsonStr("folder doesn't exist")).build();
            }
            //check rights
            AtomicBoolean canEdit = new AtomicBoolean(false);
            db.getFolderUsers(folderUuid).stream().filter(folderUser -> folderUser.userId.equals(luser.getLoginname())).forEach(folderUser -> {
                if (folderUser.userRole.equals("owner")) {
                    canEdit.set(true);
                }
            });
            if (!canEdit.get()) {
                return Response.status(Response.Status.FORBIDDEN).entity(Utils.buildErrorJsonStr("you are not owner of the folder")).build();
            }
            //remove items that are there
            Set<String> itemsPresent = db.getItems(folderUuid).stream().map(folderItem -> folderItem.id).collect(Collectors.toSet());
            List<String> itemsNotFound = new ArrayList<>();
            List<String> itemsRemovedNow = new ArrayList<>();
            //itemsJson.forEach(o -> System.out.println(o.toString()));
            for (int i = 0; i < itemsJson.length(); i++) {
                String itemId = itemsJson.get(i).toString();
                if (!itemsPresent.contains(itemId)) {
                    itemsNotFound.add(itemId);
                } else {
                    db.removeFolderItem(folderUuid, itemId);
                    itemsRemovedNow.add(itemId);
                }
            }
            JSONArray notFound = new JSONArray();
            itemsNotFound.forEach(item -> notFound.put(item));
            JSONArray removedNow = new JSONArray();
            itemsRemovedNow.forEach(item -> removedNow.put(item));
            JSONObject resultJson = new JSONObject();
            resultJson.put("notFound", notFound);
            resultJson.put("removedNow", removedNow);
            return Response.ok(resultJson.toString()).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @POST
    @Path("/{folderUuid}/follow")
    public Response followFolder(@PathParam("folderUuid") String folderUuid) {
            User luser = this.userProvider.get();
            boolean authenticated = luser.getId() != -1;

            if (authenticated) {
                //check existence
                FolderDatabase.Folder folderByUuid = db.getFolderByUuid(folderUuid);
                if (folderByUuid == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity(Utils.buildErrorJsonStr("folder doesn't exist")).build();
                }
                //check not owning or following already
                AtomicBoolean amOwner = new AtomicBoolean(false);
                AtomicBoolean amFollower = new AtomicBoolean(false);
                db.getFolderUsers(folderUuid).stream().filter(folderUser -> folderUser.userId.equals(luser.getLoginname())).forEach(folderUser -> {
                    if (folderUser.userRole.equals("owner")) {
                        amOwner.set(true);
                    } else if (folderUser.userRole.equals("follower")) {
                        amFollower.set(true);
                    }
                });
                if (amOwner.get()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("you are already owner of the folder")).build();
                }
                if (amFollower.get()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("you are already following the folder")).build();
                }
                //follow
                db.createFolderUser(folderUuid, luser.getLoginname(), "follower", System.currentTimeMillis());
                return Response.status(Response.Status.CREATED).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
    }

    @POST
    @Path("/{folderUuid}/unfollow")
    public Response unfollowFolder(@PathParam("folderUuid") String folderUuid) {
        User luser = this.userProvider.get();
        boolean authenticated = luser.getId() != -1;
        if (authenticated) {

            //check existence
            FolderDatabase.Folder folderByUuid = db.getFolderByUuid(folderUuid);
            if (folderByUuid == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(Utils.buildErrorJsonStr("folder doesn't exist")).build();
            }
            //check following, but not owning
            AtomicBoolean amOwner = new AtomicBoolean(false);
            AtomicBoolean amFollower = new AtomicBoolean(false);
            db.getFolderUsers(folderUuid).stream().filter(folderUser -> folderUser.userId.equals(luser.getLoginname())).forEach(folderUser -> {
                if (folderUser.userRole.equals("owner")) {
                    amOwner.set(true);
                } else if (folderUser.userRole.equals("follower")) {
                    amFollower.set(true);
                }
            });
            if (amOwner.get()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("you can't unfollow folder that you own")).build();
            }
            if (!amFollower.get()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Utils.buildErrorJsonStr("you are not following the folder")).build();
            }
            //unfollow
            db.removeFolderUser(folderUuid, luser.getLoginname());
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
    }

}



