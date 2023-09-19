package cz.incad.kramerius.rest.api.replication.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;



import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;

public abstract class AbstractTileResource {
	
	private  Client c; 
	public AbstractTileResource() {
		super();
		this.c = Client.create();
	}

	public static final Logger LOGGER = Logger.getLogger(AbstractTileResource.class.getName());
	
	protected void reportAccess(AggregatedAccessLogs accessLogs, String pid) {
	    try {
	        accessLogs.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
	    } catch (Exception e) {
	        LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid, e);
	    }
	}

	protected void copyFromImageServer(String urlString, ByteArrayOutputStream bos, ResponseBuilder builder) throws IOException {
		copyFromImageServer(urlString, bos, builder, null);
	}

	protected void copyFromImageServer(String urlString, ByteArrayOutputStream bos, ResponseBuilder builder, String mimetype) throws IOException {
		c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);        
        WebResource r = c.resource(urlString);
        ClientResponse clientResponse = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(ClientResponse.class);
        final InputStream input = clientResponse.getEntityInputStream();
        StreamingOutput stream = new StreamingOutput() {
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
                try {
                    IOUtils.copy(input, output);
                } catch (IOException e) {
                    if (e.getCause() != null && e.getCause() instanceof SocketException
                    		&& (e.getCause().getMessage().equals("Connection reset") || e.getCause().getMessage().equals("Broken pipe"))) {
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

        builder.header("Access-Control-Allow-Origin", "*");
		MultivaluedMap<String,String> headers = clientResponse.getHeaders();
		if (headers.containsKey("Cache-Control")) {
			builder.header("Cache-Control", headers.getFirst("Cache-Control"));
		}
		if (headers.containsKey("Last-Modified")) {
			builder.header("Last-Modified", headers.getFirst("Last-Modified"));
		}
		
	}
	
	
	public static void main(String[] args) throws IOException {
		Client c = Client.create();
		c.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, true);        
		WebResource r = c.resource("https://imageserver.difmoe.eu/editor/uuid:3ea38826-01cb-4098-9994-6ffb3d4c8054/uuid:517969ea-0c7d-4693-8bfd-9fae71f2042a/ImageProperties.xml");

		//jerseyversion
		ClientResponse clientResponse = r.accept(MediaType.MEDIA_TYPE_WILDCARD).get(ClientResponse.class);
		MultivaluedMap<String,String> headers = clientResponse.getHeaders();
		System.out.println(headers);
		String first = headers.getFirst("Cache-Control");
		
		InputStream entityInputStream = clientResponse.getEntityInputStream();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copy(entityInputStream, bos);
		
		System.out.println(new String(bos.toByteArray(), "UTF-8"));
	}
}
