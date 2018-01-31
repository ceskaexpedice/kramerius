/*
 * Copyright (C) 2012 Martin Řehánek <rehan at mzk.cz>
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.audio.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.StreamingOutput;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

import cz.incad.kramerius.audio.AbstractAudioHttpRequestForwarder;
import cz.incad.kramerius.audio.AudioHttpRequestForwarder;
import cz.incad.kramerius.audio.GetRequestHeaderForwarder;

/**
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class ServletAudioHttpRequestForwarder extends AbstractAudioHttpRequestForwarder<Void> implements AudioHttpRequestForwarder<Void> {

    private static final Logger LOGGER = Logger.getLogger(ServletAudioHttpRequestForwarder.class.getName());
    
    final HttpServletResponse proxyToClientResponse;
    final HttpServletRequest clientToProxyRequest;

    public ServletAudioHttpRequestForwarder(HttpServletRequest clientToProxyRequest, HttpServletResponse proxyToClientResponse) {
        this.clientToProxyRequest = clientToProxyRequest;
        this.proxyToClientResponse = proxyToClientResponse;
    }

    public Void forwardGetRequest(URL url) throws IOException, URISyntaxException {
        LOGGER.log(Level.INFO, "forwarding {0}", url);
        HttpGet proxyToRepositoryRequest = new HttpGet(url.toURI());
        forwardSelectedRequestHeaders(clientToProxyRequest, proxyToRepositoryRequest);
        //printRepositoryRequestHeaders(repositoryRequest);
        HttpResponse repositoryToProxyResponse = httpClient.execute(proxyToRepositoryRequest);
        //printRepositoryResponseHeaders(repositoryResponse);
        forwardSelectedResponseHeaders(repositoryToProxyResponse, proxyToClientResponse);
        forwardResponseCode(repositoryToProxyResponse, proxyToClientResponse);
        forwardData(repositoryToProxyResponse.getEntity().getContent(), proxyToClientResponse.getOutputStream());
        return null;
    }
    
    
    

    public Void forwardHeadRequest(URL url) throws IOException, URISyntaxException {
        LOGGER.log(Level.INFO, "forwarding {0}", url);
        HttpHead repositoryRequest = new HttpHead(url.toURI());
        forwardSelectedRequestHeaders(clientToProxyRequest, repositoryRequest);
        //printRepositoryRequestHeaders(repositoryRequest);
        HttpResponse repositoryResponse = httpClient.execute(repositoryRequest);
        //printRepositoryResponseHeaders(repositoryResponse);
        forwardSelectedResponseHeaders(repositoryResponse, proxyToClientResponse);
        forwardResponseCode(repositoryResponse, proxyToClientResponse);
        return null;
    }

    private void forwardSelectedResponseHeaders(HttpResponse repositoryResponse, HttpServletResponse clientResponse) {
        ServletResponseHeaderForwarder forwarder = new ServletResponseHeaderForwarder(repositoryResponse, clientResponse);
        forwarder.forwardHeaderIfPresent("Content-Range");
        forwarder.forwardHeaderIfPresent("Content-Type");
        forwarder.forwardHeaderIfPresent("Content-Length");
        forwarder.forwardHeaderIfPresent("Etag");
        forwarder.forwardHeaderIfPresent("Accept-Ranges");
        //forwarder.forwardHeaderIfPresent("Transfer-Encoding");
        forwarder.forwardHeaderIfPresent("Date");
        forwarder.forwardHeaderIfPresent("Last-Modified");
        //co s content encoding?
    }

    private void forwardSelectedRequestHeaders(HttpServletRequest clientRequest, HttpRequestBase repositoryRequest) {
        GetRequestHeaderForwarder forwarder = new GetRequestHeaderForwarder(clientRequest, repositoryRequest);
        //Pragma a Cache-control must be allways forwarded 
        forwarder.forwardHeaderIfPresent("Pragma");
        forwarder.forwardHeaderIfPresent("Cache-Control");
        forwarder.forwardHeaderIfPresent("Range");
        forwarder.forwardHeaderIfPresent("If-Range");
        //tady si nejsem jisty
        forwarder.forwardHeaderIfPresent("Accept");
    }

    
    private void forwardData(InputStream input, ServletOutputStream output) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesForwarded;
            while ((bytesForwarded = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesForwarded);
            }
        } catch (IOException ex) {
            if (ex.getCause() != null && ex.getCause() instanceof SocketException
                    && (ex.getCause().getMessage().equals(CONNECTION_RESET) || ex.getCause().getMessage().equals(BROKEN_PIPE))) {
                LOGGER.warning("Connection reset probably by client (or by repository)");
            } else {
                if (!"ClientAbortException".equals(ex.getClass().getSimpleName())) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    // Do nothing, request was cancelled by client. This is usual audio player behavior.
                }
            }
        } finally {
            try {
                LOGGER.fine("closing connection to repository");
                input.close();
                //output stream should not be closed here because it is closed by container. 
                //If closed here, for example filters won't be able to write to stream anymore
            } catch (IOException ex1) {
                LOGGER.log(Level.SEVERE, "Failed to close connection to repository", ex1);
            }
        }
    }

    private void forwardResponseCode(HttpResponse repositoryResponse, HttpServletResponse clientResponse) {
        int repositoryResponseCode = repositoryResponse.getStatusLine().getStatusCode();
        LOGGER.log(Level.FINE, "forwarding status code {0}", repositoryResponseCode);
        clientResponse.setStatus(repositoryResponseCode);
    }

    private void printRepositoryRequestHeaders(HttpGet repositoryRequest) {
        System.err.println("================================");
        System.err.println("downloader request headers(" + repositoryRequest.getAllHeaders().length + "):");
        System.err.println("================================");
        printHeaders(repositoryRequest.getAllHeaders());
        System.err.println("================================");
        System.err.println("\n");
    }

    private void printRepositoryResponseHeaders(HttpResponse repositoryResponse) {
        System.err.println("================================");
        System.err.println("downloader response headers(" + repositoryResponse.getAllHeaders().length + "):");
        System.err.println("================================");
        printHeaders(repositoryResponse.getAllHeaders());
        System.err.println("================================");
        System.err.println("\n");
    }

    private static void printHeaders(Header[] headers) {
        for (int i = 0; i < headers.length; i++) {
            Header header = headers[i];
            System.err.println(header.getName() + ": " + header.getValue());
        }
    }
}
