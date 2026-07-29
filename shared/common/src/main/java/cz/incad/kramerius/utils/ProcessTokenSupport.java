package cz.incad.kramerius.utils;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class ProcessTokenSupport {

    private static final Logger LOGGER = Logger.getLogger(ProcessTokenSupport.class.getName());

    private ProcessTokenSupport() {
    }

    public static String fetchJwtToken(CloseableHttpClient httpClient) throws IOException {
        KConfiguration config = KConfiguration.getInstance();
        String clientId = config.getConfiguration().getString("process.token.clientId");
        String secret = config.getConfiguration().getString("process.token.secret");
        String extsPoint = config.getConfiguration().getString("api.exts.v7.point");

        if (extsPoint.endsWith("/")) {
            extsPoint = extsPoint.substring(0, extsPoint.length() - 1);
        }
        String url = String.format("%s/tokens/%s?secrets=%s", extsPoint, clientId, secret);

        LOGGER.info("Requesting JWT token from: " + extsPoint + "/tokens/" + clientId);
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");

        return httpClient.execute(request, response -> {
            int status = response.getCode();
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (status == 200) {
                JSONObject jsonResponse = new JSONObject(body);
                if (jsonResponse.has("access_token")) {
                    return jsonResponse.getString("access_token");
                }
                throw new IOException("Response does not contain 'access_token'. Body: " + body);
            }
            throw new IOException("Failed to fetch token. Status: " + status + ", Body: " + body);
        });
    }

    public static void setBearerToken(HttpUriRequestBase request, CloseableHttpClient httpClient) throws IOException {
        request.setHeader("Authorization", "Bearer " + fetchJwtToken(httpClient));
    }
}
