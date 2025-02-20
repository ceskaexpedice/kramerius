package cz.incad.kramerius.rest.utils;

import java.io.IOException;
import java.util.logging.Logger;

import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.utils.RelsExtUtils;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.logging.Level;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

public class IIIFUtils {

    public static final Logger LOGGER = Logger.getLogger(IIIFUtils.class.getName());

    public static String iiifImageEndpoint(String pid, AkubraRepository akubraRepository) throws IOException {
        String url = RelsExtUtils.getRelsExtTilesUrl(pid, akubraRepository);
        if (url == null)
            return null;
        if (url.trim().equals(RelsExtUtils.CACHE_RELS_EXT_LITERAL))
            return null;
        return url.replaceAll("[z|Z]oomify|deepZoom", "iiif");
    }

    public static String iiifImageEndpoint(Document relsExt) throws IOException {
        String url = RelsExtUtils.getRelsExtTilesUrl(relsExt);
        if (url == null)
            return null;
        if (url.trim().equals(RelsExtUtils.CACHE_RELS_EXT_LITERAL))
            return null;
        return url.replaceAll("[z|Z]oomify|deepZoom", "iiif");
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
