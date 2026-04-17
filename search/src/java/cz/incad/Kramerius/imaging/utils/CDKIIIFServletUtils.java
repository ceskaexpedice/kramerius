package cz.incad.Kramerius.imaging.utils;

import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.classic.methods.HttpGet;

import jakarta.servlet.http.HttpServletResponse;

public class CDKIIIFServletUtils {

    public static void copyCacheHeaders(org.apache.hc.client5.http.impl.classic.CloseableHttpResponse sourceResponse, HttpServletResponse targetResponse) {
        org.apache.hc.core5.http.Header cacheControl = sourceResponse.getLastHeader("Cache-Control");
        if (cacheControl != null) {
            targetResponse.setHeader("Cache-Control", cacheControl.getValue());
        }
        org.apache.hc.core5.http.Header lastModified = sourceResponse.getLastHeader("Last-Modified");
        if (lastModified != null) {
            targetResponse.setHeader("Last-Modified", lastModified.getValue());
        }
        org.apache.hc.core5.http.Header etag = sourceResponse.getLastHeader("ETag");
        if (etag != null) {
            targetResponse.setHeader("ETag", etag.getValue());
        }
    }

    public static String configuredApiKey(String source) {
        String apikey = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.collections.sources." + source + ".apikey");
        return apikey;
    }

    public static void httpRequestAPIKey(String source, HttpGet get) {
        String apiKey = configuredApiKey(source);
        if (apiKey != null && !apiKey.isEmpty()) { get.addHeader("X-API-KEY", apiKey);}
    }
}
