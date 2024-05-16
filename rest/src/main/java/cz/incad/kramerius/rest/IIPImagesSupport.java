/*
 * Copyright (C) Apr 11, 2024 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.client.methods.AsyncByteConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;

import cz.incad.kramerius.rest.apiNew.client.v70.ItemsResource;

public class IIPImagesSupport {

    public static final Logger LOGGER = Logger.getLogger(IIPImagesSupport.class.getName());
    
    private IIPImagesSupport() {}

    /** non blocking copy from iip server */
    public static void nonBlockingCopyFromImageServer(HttpAsyncClient client, String urlString, final HttpServletResponse resp)
            throws IOException {
        final WritableByteChannel channel = Channels.newChannel(resp.getOutputStream());
    
        Future<Void> responseFuture = client.execute(HttpAsyncMethods.createGet(urlString), new AsyncByteConsumer<Void>() {
            @Override
            protected void onByteReceived(ByteBuffer byteBuffer, IOControl ioControl) throws IOException {
                try {
                    channel.write(byteBuffer);
                } catch (IOException e) {
                    if ("ClientAbortException".equals(e.getClass().getSimpleName())) {
                        // Do nothing, request was cancelled by client. This is usual image viewers behavior.
                    } else {
                        throw e;
                    }
                }
            }
    
            @Override
            protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
                int statusCode = response.getStatusLine().getStatusCode();
                resp.setStatus(statusCode);
                if (statusCode == 200) {
                    LOGGER.info("writing response, status code, ..."+statusCode);
                    resp.setContentType(response.getEntity().getContentType().getValue());
                    ItemsResource.LOGGER.fine(String.format("Set access-control-header %s ", "Access-Control-Allow-Origin *"));
                    resp.setHeader("Access-Control-Allow-Origin", "*");
                    Header cacheControl = response.getLastHeader("Cache-Control");
                    if (cacheControl != null) resp.setHeader(cacheControl.getName(), cacheControl.getValue());
                    Header lastModified = response.getLastHeader("Last-Modified");
                    if (lastModified != null) resp.setHeader(lastModified.getName(), lastModified.getValue());
    
                }
            }
    
            @Override
            protected Void buildResult(HttpContext httpContext) throws Exception {
                return null;
            }
        }, null);
    
        try {
            responseFuture.get(); // wait for request
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            throw new IOException(e.getMessage());
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE,e.getMessage());
            throw new IOException(e.getMessage());
        }
    }

    /** Non blocking copy from file */
    public static void nonBlockingCopyFromFile(String filePath, final HttpServletResponse resp) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
                FileChannel fileChannel = fileInputStream.getChannel();
                WritableByteChannel channel = Channels.newChannel(resp.getOutputStream());
                fileChannel.transferTo(0, fileChannel.size(), channel);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /** Blocking copy from IIP server - jersey output */
    public static void blockingCopyFromImageServer(Client c, String urlString, ByteArrayOutputStream bos, ResponseBuilder builder) throws IOException {
        IIPImagesSupport.blockingCopyFromImageServer(c,urlString, bos, builder, null);
    }

    /** Blocking copy from IIP server - jersey output */
    public static void blockingCopyFromImageServer(Client c, String urlString, ByteArrayOutputStream bos, ResponseBuilder builder,String mimetype)
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
    
        MultivaluedMap<String, String> headers = clientResponse.getHeaders();
        if (headers.containsKey("Cache-Control")) {
            builder.header("Cache-Control", headers.getFirst("Cache-Control"));
        }
        if (headers.containsKey("Last-Modified")) {
            builder.header("Last-Modified", headers.getFirst("Last-Modified"));
        }
    }
}
