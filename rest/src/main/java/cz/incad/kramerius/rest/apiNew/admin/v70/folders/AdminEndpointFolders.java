package cz.incad.kramerius.rest.apiNew.admin.v70.folders;

import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.security.SecuredActions;
import cz.inovatika.folders.db.FolderDatabase;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Read-only overview of user folders for administrators. */
@Path("/admin/v7.0/folders")
public class AdminEndpointFolders extends AdminApiResource {

    @Inject
    private FolderDatabase folderDatabase;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getFolders(
            @QueryParam("q") String query,
            @QueryParam("userId") String userId,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("50") int requestedLimit) {
        checkCurrentUserByJsessionidIsAllowedToPerformGlobalSecuredAction(SecuredActions.A_ADMIN_READ);
        if (offset < 0 || requestedLimit < 1) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new JSONObject().put("error", "offset must be non-negative and limit must be positive").toString())
                    .type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                    .build();
        }

        List<FolderDatabase.Folder> allFolders = folderDatabase.getAllFolders();
        List<FolderDatabase.Folder> filtered = allFolders.stream()
                .filter(folder -> matches(folder, query, userId))
                .collect(Collectors.toList());
        int limit = Math.min(requestedLimit, 200);
        int from = Math.min(offset, filtered.size());
        int to = Math.min(from + limit, filtered.size());

        JSONArray folders = new JSONArray();
        for (FolderDatabase.Folder folder : filtered.subList(from, to)) {
            JSONObject json = new JSONObject();
            json.put("uuid", folder.uuid);
            json.put("name", folder.name);
            json.put("itemsCount", folder.itemsCount);
            json.put("updatedAt", folder.updatedAt);
            JSONArray users = new JSONArray();
            for (FolderDatabase.FolderUser folderUser : folder.users) {
                users.put(new JSONObject()
                        .put("userId", folderUser.userId)
                        .put("userRole", folderUser.userRole)
                        .put("createdAt", folderUser.createdAt));
            }
            json.put("users", users);
            folders.put(json);
        }

        JSONObject result = new JSONObject();
        result.put("totalCount", allFolders.size());
        result.put("filteredCount", filtered.size());
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("folders", folders);
        return Response.ok(result.toString()).type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
    }

    boolean matches(FolderDatabase.Folder folder, String query, String userId) {
        if (query != null && !query.trim().isEmpty()) {
            String needle = query.trim().toLowerCase(Locale.ROOT);
            boolean matchesText = contains(folder.name, needle) || contains(folder.uuid, needle)
                    || folder.users.stream().anyMatch(user -> contains(user.userId, needle));
            if (!matchesText) return false;
        }
        return userId == null || userId.trim().isEmpty()
                || folder.users.stream().anyMatch(user -> user.userId.equalsIgnoreCase(userId.trim()));
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
