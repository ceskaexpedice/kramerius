package cz.incad.kramerius.iiif;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.ceskaexpedice.akubra.AkubraRepository;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.client.ClientProperties;

public class IIIFUtils {

    public static final String CACHE_RELS_EXT_LITERAL = "kramerius4://deepZoomCache";

    public static final Logger LOGGER = Logger.getLogger(IIIFUtils.class.getName());

    public static String iiifImageEndpoint(String pid, AkubraRepository akubraRepository) throws IOException {
        String url = akubraRepository.re().getTilesUrl(pid);
        if (url == null)
            return null;
        if (url.trim().equals(CACHE_RELS_EXT_LITERAL))
            return null;
        return url.replaceAll("[z|Z]oomify|deepZoom", "iiif");
    }

    public static void copyFromImageServer(Client c, String urlString, ByteArrayOutputStream bos, Response.ResponseBuilder builder) throws IOException {
        IIIFUtils.copyFromImageServer(c, urlString, bos, builder, null);
    }

    public static void copyFromImageServer(
            Client c,
            String urlString,
            ByteArrayOutputStream bos,
            Response.ResponseBuilder builder,
            String mimetype
    ) {
        c.property(ClientProperties.FOLLOW_REDIRECTS, true);
        WebTarget target = c.target(urlString);
        Response clientResponse = target.request(MediaType.WILDCARD).get();
        final InputStream input = clientResponse.readEntity(InputStream.class);
        StreamingOutput stream = output -> {
            try {
                IOUtils.copy(input, output);
            } catch (IOException e) {
                if (e.getCause() instanceof SocketException se &&
                        ("Connection reset".equals(se.getMessage()) || "Broken pipe".equals(se.getMessage()))) {
                    LOGGER.warning("Connection reset probably by client (or by repository)");
                } else {
                    LOGGER.log(Level.SEVERE, null, e);
                }
                throw new WebApplicationException(e);
            } finally {
                LOGGER.fine("closing connection to repository");
                IOUtils.closeQuietly(input);
                clientResponse.close(); // VERY IMPORTANT in Jersey 3
            }
        };
        builder.entity(stream);
        if (mimetype != null) {
            builder.type(mimetype);
        }
        jakarta.ws.rs.core.MultivaluedMap<String, Object> headers = clientResponse.getHeaders();
        if (headers.containsKey("Cache-Control")) {
            builder.header("Cache-Control", headers.getFirst("Cache-Control"));
        }
        if (headers.containsKey("Last-Modified")) {
            builder.header("Last-Modified", headers.getFirst("Last-Modified"));
        }
    }
}
