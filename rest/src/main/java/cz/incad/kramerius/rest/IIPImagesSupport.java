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

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.apache.hc.client5.http.async.methods.AbstractBinResponseConsumer;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.support.AsyncRequestBuilder;

import java.io.*;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IIPImagesSupport {

    public static final Logger LOGGER =
            Logger.getLogger(IIPImagesSupport.class.getName());

    private IIPImagesSupport() {}

    /** Non-blocking copy from IIP server */
    public static void nonBlockingCopyFromImageServer(
            HttpAsyncClient client,
            String urlString,
            final HttpServletResponse resp) throws IOException {

        final WritableByteChannel channel =
                Channels.newChannel(resp.getOutputStream());

        AsyncRequestProducer producer =
                AsyncRequestBuilder.get(urlString).build();

        AbstractBinResponseConsumer<Void> consumer =
                new AbstractBinResponseConsumer<>() {

                    @Override
                    public void releaseResources() {}

                    @Override
                    protected int capacityIncrement() {
                        return Integer.MAX_VALUE;
                    }

                    @Override
                    protected void data(ByteBuffer src, boolean endOfStream)
                            throws IOException {
                        try {
                            channel.write(src);
                        } catch (IOException e) {
                            if ("ClientAbortException"
                                    .equals(e.getClass().getSimpleName())) {
                                // client aborted – normal for image viewers
                            } else {
                                throw e;
                            }
                        }
                    }

                    @Override
                    protected void start(
                            org.apache.hc.core5.http.HttpResponse response,
                            ContentType contentType) throws IOException {

                        int statusCode = response.getCode();
                        resp.setStatus(statusCode);

                        if (statusCode == 200) {
                            resp.setContentType(contentType.getMimeType());
                            resp.setHeader(
                                    "Access-Control-Allow-Origin", "*");

                            var cacheControl =
                                    response.getLastHeader("Cache-Control");
                            if (cacheControl != null) {
                                resp.setHeader(
                                        cacheControl.getName(),
                                        cacheControl.getValue());
                            }

                            var lastModified =
                                    response.getLastHeader("Last-Modified");
                            if (lastModified != null) {
                                resp.setHeader(
                                        lastModified.getName(),
                                        lastModified.getValue());
                            }
                        }
                    }

                    @Override
                    protected Void buildResult() {
                        return null;
                    }
                };

        Future<?> future =
                client.execute(producer, consumer, null, null, null);

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /** Non-blocking copy from file */
    public static void nonBlockingCopyFromFile(
            String filePath,
            final HttpServletResponse resp) throws IOException {

        try (FileInputStream fis = new FileInputStream(filePath);
             FileChannel fileChannel = fis.getChannel();
             WritableByteChannel channel =
                     Channels.newChannel(resp.getOutputStream())) {

            fileChannel.transferTo(0, fileChannel.size(), channel);

        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /** Blocking copy from IIP server – Jakarta client */
    public static void blockingCopyFromImageServer(
            Client client,
            String urlString,
            ByteArrayOutputStream bos,
            ResponseBuilder builder) throws IOException {

        blockingCopyFromImageServer(
                client, urlString, bos, builder, null);
    }

    /** Blocking copy from IIP server – Jakarta client */
    public static void blockingCopyFromImageServer(
            Client client,
            String urlString,
            ByteArrayOutputStream bos,
            ResponseBuilder builder,
            String mimetype) throws IOException {

        WebTarget target = client.target(urlString);

        Response response = target
                .request(MediaType.WILDCARD)
                .get();

        InputStream input = response.readEntity(InputStream.class);

        StreamingOutput stream = output -> {
            try {
                IOUtils.copy(input, output);
            } catch (IOException e) {
                if (e.getCause() instanceof SocketException se &&
                        ("Connection reset".equals(se.getMessage())
                                || "Broken pipe".equals(se.getMessage()))) {
                    LOGGER.warning(
                            "Connection reset by client or repository");
                } else {
                    LOGGER.log(Level.SEVERE, null, e);
                }
                throw new WebApplicationException(e);
            } finally {
                IOUtils.closeQuietly(input);
            }
        };

        builder.entity(stream);

        if (mimetype != null) {
            builder.type(mimetype);
        }

        MultivaluedMap<String, Object> headers =
                response.getHeaders();

        if (headers.containsKey("Cache-Control")) {
            builder.header(
                    "Cache-Control",
                    headers.getFirst("Cache-Control"));
        }
        if (headers.containsKey("Last-Modified")) {
            builder.header(
                    "Last-Modified",
                    headers.getFirst("Last-Modified"));
        }
    }
}