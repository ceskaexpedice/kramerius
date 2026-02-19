package cz.incad.kramerius.keycloak;


import cz.incad.kramerius.utils.conf.KConfiguration;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.inject.Inject;
import javax.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * IiiPresentationApi
 *
 * @author Martin Rumanek
 */
@Path("/auth")
public class KeycloakProxy {

    private static final Logger LOGGER = Logger.getLogger(KeycloakProxy.class.getName());


    private Provider<HttpServletRequest> requestProvider;

    private String keycloakUri;
    private String keycloakClientId;
    private String keycloakSecret;

    private OkHttpClient client;

    //username=krameriusAdmin&password=krameriusAdmin&client_id=krameriusClient&client_secret=kyPtgyMN7rFfPiJzgaaE90cpBryAQ4nG&grant_type=password

    @Inject
    public KeycloakProxy(Provider<HttpServletRequest> requestProvider) {
        this.requestProvider = requestProvider;
        this.client = new OkHttpClient();
        this.keycloakUri = KConfiguration.getInstance().getConfiguration().getString("keycloak.tokenurl");
        this.keycloakClientId = KConfiguration.getInstance().getConfiguration().getString("keycloak.clientId");
        this.keycloakSecret = KConfiguration.getInstance().getConfiguration().getString("keycloak.secret");
    }

    @POST
    @Path("token")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response token(String form) {

        String requestBody = form+"&client_id="+keycloakClientId+"&client_secret="+keycloakSecret+"&grant_type=password";

        Request request = new Request.Builder()
                .url(keycloakUri)
                .post(RequestBody.create(requestBody, okhttp3.MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()){

                return Response.status(response.code()).entity(response.challenges().toString()).build();
            }

            return Response.ok().entity(response.body().string()).build();
        } catch (IOException ex){
            return Response.serverError().entity(ex).build();
        }
    }


}
