package cz.incad.Kramerius.imaging.utils;

import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
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

    public static void httpRequestCDKPARAMETERS(String source, User user, String remoteAddr, HttpGet get) {
        String paremeters = prepareHeader(source, user, remoteAddr);
        if (paremeters != null && !paremeters.isEmpty()) {
            get.setHeader("CDK-TOKEN-PARAMETERS", paremeters);
            get.setHeader("CDK_TOKEN_PARAMETERS", paremeters); // deprecated

        }
    }
    public static  String prepareHeader(String source, User user, String remoteAddr) {
        String prefixHeaders = KConfiguration.getInstance().getConfiguration()
                .getString("cdk.shibboleth.forward.headers");

        // no user session attributes in case of no federation
        String header = "";

        boolean shibbolethAttributes = KConfiguration.getInstance().getConfiguration()
                .getBoolean("cdk.collections.sources." + source + ".shibboleth_attributes", true);

        if (shibbolethAttributes) {
            Map<String, String> attributes = user.getSessionAttributes();
            header = header + attributes.keySet().stream().map(key -> {
                return "header_" + key + "=" + attributes.get(key);
            }).collect(Collectors.joining("|"));
        }

        if (remoteAddr != null ) {
            header = header + "|" + "header_ip_address=" + remoteAddr;
        }
        // TODO: Source
        if (StringUtils.isAnyString(prefixHeaders)) {
            header = prefixHeaders + header;
        }
        return header;
    }


}
