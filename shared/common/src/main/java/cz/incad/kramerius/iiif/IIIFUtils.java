package cz.incad.kramerius.iiif;

import java.io.IOException;
import java.util.logging.Logger;

import org.ceskaexpedice.akubra.AkubraRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import cz.incad.kramerius.utils.RESTHelper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

public class IIIFUtils {

    public static final String CACHE_RELS_EXT_LITERAL = "kramerius4://deepZoomCache";

    public static final Logger LOGGER = Logger.getLogger(IIIFUtils.class.getName());

    private static final int MAX_INFO_JSON_REDIRECTS = 5;

    public static String iiifImageEndpoint(String pid, AkubraRepository akubraRepository) throws IOException {
        String url = akubraRepository.re().getTilesUrl(pid);
        if (url == null)
            return null;
        if (url.trim().equals(CACHE_RELS_EXT_LITERAL))
            return null;
        return url.replaceAll("[z|Z]oomify|deepZoom", "iiif");
    }

    /**
     * Downloads info.json from the image server. Redirects are followed by hand, because
     * HttpURLConnection never follows a redirect between http and https (image servers commonly
     * redirect the http tiles-url stored in RELS-EXT to https) and would hand over the html body
     * of the 3xx response instead.
     */
    public static String fetchInfoJson(String urlString) throws IOException {
        String current = urlString;
        for (int hop = 0; hop <= MAX_INFO_JSON_REDIRECTS; hop++) {
            HttpURLConnection con = (HttpURLConnection) RESTHelper.openConnection(current, "", "");
            con.setInstanceFollowRedirects(false);
            try {
                int status = con.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    try (InputStream is = con.getInputStream()) {
                        return IOUtils.toString(is, StandardCharsets.UTF_8);
                    }
                }
                if (!isRedirect(status)) {
                    throw new IOException(String.format("%s returned status code %d", current, status));
                }
                String location = con.getHeaderField("Location");
                if (location == null || location.trim().isEmpty()) {
                    throw new IOException(String.format("%s returned status code %d without Location", current, status));
                }
                URL target = new URL(new URL(current), location.trim());
                String protocol = target.getProtocol();
                if (!"http".equals(protocol) && !"https".equals(protocol)) {
                    throw new IOException(String.format("%s redirects to unsupported url %s", current, target));
                }
                current = target.toString();
            } finally {
                con.disconnect();
            }
        }
        throw new IOException(String.format("Too many redirects for %s", urlString));
    }

    private static boolean isRedirect(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER || status == 307 || status == 308;
    }

    public static void copyFromImageServer(Client c, String urlString, ByteArrayOutputStream bos, ResponseBuilder builder) throws IOException {
        IIIFUtils.copyFromImageServer(c, urlString, bos, builder, null);
    }

    public static void copyFromImageServer(Client c, String urlString, ByteArrayOutputStream bos, ResponseBuilder builder, String mimetype)
            throws IOException {
        c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);
        WebResource r = c.resource(urlString);
        ClientResponse clientResponse = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(ClientResponse.class);
        final InputStream input = clientResponse.getEntityInputStream();
        StreamingOutput stream = new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    IOUtils.copy(input, output);
                } catch (IOException e) {
                    if (e.getCause() != null && e.getCause() instanceof SocketException
                            && (e.getCause().getMessage().equals("Connection reset")
                            || e.getCause().getMessage().equals("Broken pipe"))) {
                        LOGGER.warning("Connection reset probably by client (or by repository)");
                    } else {
                        LOGGER.log(Level.SEVERE, null, e);
                    }
                    throw new WebApplicationException(e);
                } finally {
                    LOGGER.fine("closing connection to repository");
                    IOUtils.closeQuietly(input);
                }
            }
        };
        builder.entity(stream);

        if (mimetype != null) {
            builder.type(mimetype);
        }

        // added by filter
        //builder.header("Access-Control-Allow-Origin", "*");
        MultivaluedMap<String, String> headers = clientResponse.getHeaders();
        if (headers.containsKey("Cache-Control")) {
            builder.header("Cache-Control", headers.getFirst("Cache-Control"));
        }
        if (headers.containsKey("Last-Modified")) {
            builder.header("Last-Modified", headers.getFirst("Last-Modified"));
        }
    }

}
