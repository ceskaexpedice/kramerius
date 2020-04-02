package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.rest.apiNew.ApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.ProxyAuthenticationRequiredException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

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

public abstract class AdminApiResource extends ApiResource {

    //TODO: move url into configuration
    private static final String AUTH_URL = "https://api.kramerius.cloud/api/v1/auth/validate_token";

    private static final String HEADER_PROCESS_AUTH_TOKEN = "process-auth-token";

    @Deprecated
    private static final String HEADER_AUTH_TOKEN = "auth-token";
    @Deprecated
    private static final String HEADER_TOKEN = "token";

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    Provider<User> userProvider;

    public final AuthenticatedUser getAuthenticatedUser() throws ProxyAuthenticationRequiredException {
        ClientAuthHeaders authHeaders = ClientAuthHeaders.extract(requestProvider);
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
                throw new InternalErrorException("error communicating with authentification service: %s", message);
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
                throw new InternalErrorException("error communicating with authentification service: %s", message);
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
            throw new InternalErrorException("error communicating with authentification service: %s ", e.getMessage());
        }
    }

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

    public String getProcessAuthToken() {
        return requestProvider.get().getHeader(HEADER_PROCESS_AUTH_TOKEN);
    }

    @Deprecated
    public String authToken() {
        return requestProvider.get().getHeader(HEADER_AUTH_TOKEN);
    }

    @Deprecated
    public String groupToken() {
        return requestProvider.get().getHeader(HEADER_TOKEN);
    }

    @Deprecated
    public String findLoggedUserKey() {
        //TODO: otestovat, nebo zmenit
        userProvider.get(); //TODO: neni uplne zrejme, proc tohle volat. Co se deje v AbstractLoggedUserProvider a LoggedUsersSingletonImpl vypada zmatecne
        return (String) requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
    }

    //TODO: proverit
    public String getRemoteAddress() {
        return IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
    }
}
