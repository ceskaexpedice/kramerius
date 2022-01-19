package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.ApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.ProxyAuthenticationRequiredException;
import cz.incad.kramerius.rest.apiNew.exceptions.UnauthorizedException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.adapters.spi.KeycloakAccount;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AdminApiResource extends ApiResource {

    public static Logger LOGGER = Logger.getLogger(AdminApiResource.class.getName());

    private static final String HEADER_PARENT_PROCESS_AUTH_TOKEN = "parent-process-auth-token";

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;

    @Inject
    RightsResolver rightsResolver;

    //TODO: cleanup

    //private static final AuthenticatedUser ANONYMOUS = new AuthenticatedUser("anonymous", "anonymous", new ArrayList<>());

//    public final AuthenticatedUser getAuthenticatedUserByOauth() throws ProxyAuthenticationRequiredException {
//        KeycloakAccount keycloakAccount = null;
//        try {
//            keycloakAccount = (KeycloakAccount) requestProvider.get().getAttribute(KeycloakAccount.class.getName());
//        }catch (Throwable th){
//            LOGGER.log(Level.INFO,"Error retrieving KeycloakAccount", th);
//        }
//        if (keycloakAccount == null){
//            return  ANONYMOUS;
//        }
//        return new AuthenticatedUser(keycloakAccount.getPrincipal().getName(), keycloakAccount.getPrincipal().getName(), new ArrayList<>(keycloakAccount.getRoles()));
//    }

    /*public final AuthenticatedUser getAuthenticatedUserByOauth() throws ProxyAuthenticationRequiredException {
        ClientAuthHeaders authHeaders = extractClientAuthHeaders();
        //System.out.println(authHeaders);
        try {
            URL url = new URL(AUTH_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            con.setRequestProperty(ClientAuthHeaders.AUTH_HEADER_CLIENT, authHeaders.getClient());
            con.setRequestProperty(ClientAuthHeaders.AUTH_HEADER_UID, authHeaders.getUid());
            con.setRequestProperty(ClientAuthHeaders.AUTH_HEADER_ACCESS_TOKEN, authHeaders.getAccessToken());
            int status = con.getResponseCode();

            //error with not 200
            if (status != 200) {
                String message = "response status " + status;
                String body = inputstreamToString(con.getErrorStream());
                System.err.println(body);
                if (!body.isEmpty()) {
                    JSONObject bodyJson = new JSONObject(body);
                    if (bodyJson.has("errors")) {
                        JSONArray errors = bodyJson.getJSONArray("errors");
                        if (errors.length() > 0) {
                            message = errors.getString(0);
                        }
                    }
                }
                throw new InternalErrorException("error communicating with authentication service: %s", message);
            }
            String body = inputstreamToString(con.getInputStream());
            JSONObject bodyJson = new JSONObject(body);

            //error with 200 but not success
            if (!bodyJson.getBoolean("success")) {
                String message = "";
                if (bodyJson.has("errors")) {
                    JSONArray errors = bodyJson.getJSONArray("errors");
                    if (errors.length() > 0) {
                        message = errors.getString(0);
                    }
                }
                throw new InternalErrorException("error communicating with authentication service: %s", message);
            }

            //success
            JSONObject data = bodyJson.getJSONObject("data");
            String id = data.getString("uid");
            String name = data.getString("name");
            List<String> roles = Collections.emptyList();
            if (data.has("roles") && data.get("roles") != null && !data.isNull("roles")) {
                roles = commaSeparatedItemsToList(data.getString("roles"));
            }
            return new AuthenticatedUser(id, name, roles);
        } catch (IOException e) {
            throw new InternalErrorException("error communicating with authentication service: %s ", e.getMessage());
        }
    }*/

    private String inputstreamToString(InputStream in) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            return content.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private List<String> commaSeparatedItemsToList(String commaSeparated) {
        List<String> result = new ArrayList<>();
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return result;
        }
        String[] items = commaSeparated.split(",");
        for (String item : items) {
            result.add(item.trim());
        }
        return result;
    }

    public String getParentProcessAuthToken() {
        return requestProvider.get().getHeader(HEADER_PARENT_PROCESS_AUTH_TOKEN);
    }

    @Deprecated
    public String findLoggedUserKey() {
        //TODO: otestovat, nebo zmenit
        userProvider.get(); //TODO: neni uplne zrejme, proc tohle volat. Co se deje v AbstractLoggedUserProvider a LoggedUsersSingletonImpl vypada zmatecne
        return (String) requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
    }

    @Deprecated
    public void checkCurrentUserByJsessionidIsAllowedToPerformGlobalSecuredAction(SecuredActions action) {
        User user = this.userProvider.get();
        if (user == null || user.getLoginname().equals("not_logged")) {
            throw new UnauthorizedException(); //401
        } else {
            boolean allowed = this.rightsResolver.isActionAllowed(user, action.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to perform global action '%s'", user.getLoginname(), action.getFormalName()); //403
            }
        }
    }
}
