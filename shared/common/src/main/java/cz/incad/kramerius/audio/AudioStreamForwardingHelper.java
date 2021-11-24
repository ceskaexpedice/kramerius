package cz.incad.kramerius.audio;

import com.google.inject.Inject;
import cz.incad.kramerius.audio.urlMapping.RepositoryUrlManager;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class helps API endpoint to work as proxy by translating HTTP headers, status codes, and payload from remote Audio repository
 * <p>
 * Content of audio files to be served to client is stored in external Audio repository (web server).
 * FOXML objects in Kramerius Repository (model:track) contain audio datastreams (MP3 and/or OGG and/or WAV),
 * that are externally referenced datastreams with url to Audio repository.
 * Mapping PID+dsID -> URL is handled by RepositoryUrlManager.
 * <p>
 * Request: Client -> Audio proxy (gets URL from RepositoryUrlManager) -> external Audio repository
 * Response: Client <- Audio proxy <- external Audio repository
 */
public class AudioStreamForwardingHelper {

    public static Logger LOGGER = Logger.getLogger(AudioStreamForwardingHelper.class.getName());

    protected static final String CONNECTION_RESET = "Connection reset";
    protected static final String BROKEN_PIPE = "Broken pipe";

    @Inject
    RepositoryUrlManager urlManager;

    HttpClient httpClient = HttpClientBuilder.create().build();

    public void forwardHttpHEAD(AudioStreamId id, HttpServletRequest clientToProxyRequest, ResponseBuilder responseBuilder) throws IOException {
        //LOGGER.fine(id.toString());
        try {
            URL url = urlManager.getAudiostreamRepositoryUrl(id);
            if (url == null) {
                throw new IllegalArgumentException("url for id " + id.toString() + " is null");
            }
            LOGGER.log(Level.FINE, "forwarding HEAD {0}", url);
            HttpHead repositoryRequest = new HttpHead(url.toURI());
            forwardSelectedRequestHeaders(clientToProxyRequest, repositoryRequest);
            //printRepositoryRequestHeaders(repositoryRequest);
            HttpResponse repositoryResponse = httpClient.execute(repositoryRequest);
            //printRepositoryResponseHeaders(repositoryResponse);
            forwardSelectedResponseHeaders(repositoryResponse, responseBuilder);
            forwardResponseCode(repositoryResponse, responseBuilder);
        } catch (URISyntaxException ex) {
            Logger.getLogger(AudioStreamForwardingHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public void forwardHttpGET(AudioStreamId id, HttpServletRequest clientToProxyRequest, ResponseBuilder responseBuilder) throws IOException {
        //LOGGER.fine(id.toString());
        try {
            URL url = urlManager.getAudiostreamRepositoryUrl(id);
            if (url == null) {
                throw new IllegalArgumentException("url for id " + id.toString() + " is null");
            }
            LOGGER.log(Level.FINE, "forwarding GET {0}", url);
            HttpGet proxyToRepositoryRequest = new HttpGet(url.toURI());
            forwardSelectedRequestHeaders(clientToProxyRequest, proxyToRepositoryRequest);
            //printRepositoryRequestHeaders(proxyToRepositoryRequest);
            HttpResponse repositoryToProxyResponse = httpClient.execute(proxyToRepositoryRequest);
            //printRepositoryResponseHeaders(repositoryToProxyResponse);
            forwardSelectedResponseHeaders(repositoryToProxyResponse, responseBuilder);
            forwardResponseCode(repositoryToProxyResponse, responseBuilder);
            try (InputStream in = repositoryToProxyResponse.getEntity().getContent()) {
                forwardData(in, responseBuilder);
            }
            responseBuilder.status(repositoryToProxyResponse.getStatusLine().getStatusCode());
        } catch (URISyntaxException ex) {
            Logger.getLogger(AudioStreamForwardingHelper.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex);
        }
    }

    private void forwardSelectedResponseHeaders(HttpResponse repositoryResponse, ResponseBuilder builder) {
        ResponseHeaderForwarder forwarder = new ResponseHeaderForwarder(repositoryResponse, builder);
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
        forwarder.forwardHeaderIfPresent("Accept");
    }

    private void forwardData(final InputStream input, ResponseBuilder responseBuilder) {
        ByteArrayOutputStream responseData = new ByteArrayOutputStream();
        try {
            IOUtils.copy(input, responseData);
            /*byte[] buffer = new byte[10240];
            int bytesForwarded;
            while ((bytesForwarded = input.read(buffer)) != -1) {
                responseData.write(buffer, 0, bytesForwarded);
            }*/
            responseBuilder.entity(responseData.toByteArray());
        } catch (IOException e) {
            if (e.getCause() != null && e.getCause() instanceof SocketException
                    && (e.getCause().getMessage().equals(CONNECTION_RESET) || e.getCause().getMessage().equals(BROKEN_PIPE))) {
                LOGGER.warning("Connection reset probably by client (or by repository)");
            } else {
                LOGGER.log(Level.SEVERE, null, e);
            }
            throw new WebApplicationException(e);
        }
    }

    private void forwardResponseCode(HttpResponse repositoryResponse, ResponseBuilder builder) {
        int repositoryResponseCode = repositoryResponse.getStatusLine().getStatusCode();
        builder.status(repositoryResponseCode);
    }

    private void printRepositoryRequestHeaders(HttpGet repositoryRequest) {
        System.err.println("================================");
        System.err.println("remote-repository request headers(" + repositoryRequest.getAllHeaders().length + "):");
        System.err.println("================================");
        printHeaders(repositoryRequest.getAllHeaders());
        System.err.println("================================");
        System.err.println("\n");
    }

    private void printRepositoryResponseHeaders(HttpResponse repositoryResponse) {
        System.err.println("================================");
        System.err.println("remote-repository response headers(" + repositoryResponse.getAllHeaders().length + "):");
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

    /**
     * Forwarder copies response headers from remote-repository-->audio-proxy to audio-proxy-->client.
     *
     * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
     */
    public class ResponseHeaderForwarder {

        private final HttpResponse repositoryResponse;
        private final ResponseBuilder proxyResponse;

        /**
         * Initializes Forwarder.
         *
         * @param repositoryResponse response from repository to proxy
         * @param proxyResponse      response from proxy to client
         */
        public ResponseHeaderForwarder(HttpResponse repositoryResponse, ResponseBuilder proxyResponse) {
            this.repositoryResponse = repositoryResponse;
            this.proxyResponse = proxyResponse;
        }

        /**
         * Forwards value of header if header is found. Uses only first appearance
         * of the header therefore doesn't work correctly for multiple headers with
         * same name.
         *
         * @param headerName name of header
         * @return value of header if header is found or null
         */
        public String forwardHeaderIfPresent(String headerName) {
            Header header = repositoryResponse.getFirstHeader(headerName);
            if (header != null) {
                //System.err.println("found response header " + header.getName() + ": " + header.getValue());
                proxyResponse.header(header.getName(), header.getValue());
                return header.getValue();
            }
            return null;
        }
    }

}
