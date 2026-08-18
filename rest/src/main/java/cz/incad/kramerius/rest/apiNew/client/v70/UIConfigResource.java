package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.uiconfig.*;
import cz.incad.kramerius.utils.ApplicationURL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/client/v7.0/ui-config")
public class UIConfigResource extends AdminApiResource {

    private static final Logger LOGGER = Logger.getLogger(UIConfigResource.class.getName());
    private final UIConfigReferencePatternProvider referencePatternProvider = new UIConfigReferencePatternProvider();

    @Inject
    RightsResolver rightsResolver;

    @Inject
    javax.inject.Provider<User> userProvider;

    @javax.inject.Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;

    @GET
    @Path("general")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralConfig() {

        return getConfig(UIConfigType.GENERAL);
    }

    @GET
    @Path("licenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicensesConfig() {
        return getConfig(UIConfigType.LICENSES);
    }

    @GET
    @Path("curator-lists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCuratorListsConfig() {
        return getConfig(UIConfigType.CURATOR_LISTS);
    }

    private Response getConfig(UIConfigType type) {
        try {
            DbUIConfigService dbUIConfigService = new DbUIConfigService(connectionProvider, new JsonValidator());
            InputStream in = dbUIConfigService.load(type);
            if (in == null) {
                throw new cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException("No such config");
            }

            Object json = new JSONTokener(in).nextValue();
            replaceResourceReferencesByPatterns(json, referencePatternProvider.getResourceReferencePatterns(type));

            return Response.ok(json.toString(), MediaType.APPLICATION_JSON_TYPE)
                    .header("Cache-Control", "no-cache")
                    .build();
        } catch (NotFoundException e) {
            throw e;
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to load UI config " + type, e);
            throw new InternalServerErrorException("Failed to load UI config");
        }
    }


    private void replaceResourceReferencesByPatterns(
            Object node,
            List<String> resourceReferencePatterns
    ) {
        List<List<String>> patterns = resourceReferencePatterns.stream()
                .map(this::normalizeJsonPathPattern)
                .toList();

        replaceResourceReferences(node, patterns, List.of());
    }

    private void replaceResourceReferences(
            Object node,
            List<List<String>> patterns,
            List<String> path
    ) {
        if (node == null || node == JSONObject.NULL) {
            return;
        }

        if (node instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) {
                Object item = array.opt(i);

                if (item instanceof String text && isResourceReferencePath(path, patterns)) {
                    array.put(i, toAbsoluteResourcePath(text));
                } else {
                    replaceResourceReferences(item, patterns, path);
                }
            }
            return;
        }

        if (node instanceof JSONObject object) {
            for (Object k : object.keySet()) {
                String key = k.toString();
                Object item = object.opt(key);

                List<String> childPath = new ArrayList<>(path);
                childPath.add(key);

                if (item instanceof String text && isResourceReferencePath(childPath, patterns)) {
                    object.put(key, toAbsoluteResourcePath(text));
                } else {
                    replaceResourceReferences(item, patterns, childPath);
                }
            }
        }
    }

    private List<String> normalizeJsonPathPattern(String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return List.of();
        }

        return Arrays.stream(pattern.split("/"))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .toList();
    }

    private boolean isResourceReferencePath(
            List<String> path,
            List<List<String>> patterns
    ) {
        return patterns.stream().anyMatch(pattern -> matchesPath(path, pattern));
    }

    private boolean matchesPath(List<String> path, List<String> pattern) {
        if (path.size() != pattern.size()) {
            return false;
        }

        for (int i = 0; i < path.size(); i++) {
            String patternSegment = pattern.get(i);

            if (!"*".equals(patternSegment) && !patternSegment.equals(path.get(i))) {
                return false;
            }
        }

        return true;
    }

    private String toAbsoluteResourcePath(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }

        String relativePath = value.startsWith("/") ? value.substring(1) : value;

        return ApplicationURL.applicationURL(this.requestProvider.get()) + "/api/client/v7.0/ui-config/resources/" + relativePath;
    }
}
