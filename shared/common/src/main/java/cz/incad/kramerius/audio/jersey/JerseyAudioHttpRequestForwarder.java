package cz.incad.kramerius.audio.jersey;

import cz.incad.kramerius.audio.AbstractAudioHttpRequestForwarder;
import cz.incad.kramerius.audio.AudioHttpRequestForwarder;
import cz.incad.kramerius.audio.GetRequestHeaderForwarder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @deprecated use AudioStreamForwardingHelper instead
 */
public class JerseyAudioHttpRequestForwarder extends AbstractAudioHttpRequestForwarder<Response.ResponseBuilder> implements AudioHttpRequestForwarder<Response.ResponseBuilder> {

    public static final Logger LOGGER = Logger.getLogger(JerseyResponseHeaderForwarder.class.getName());

    final HttpServletRequest clientToProxyRequest;
    final Response.ResponseBuilder responseBuilder;

    public JerseyAudioHttpRequestForwarder(HttpServletRequest clientToProxyRequest, Response.ResponseBuilder respBuilder) {
        this.clientToProxyRequest = clientToProxyRequest;
        this.responseBuilder = respBuilder;
    }

    public Response.ResponseBuilder forwardGetRequest(URL url) throws IOException, URISyntaxException {
        LOGGER.log(Level.FINE, "forwarding {0}", url);
        HttpGet proxyToRepositoryRequest = new HttpGet(url.toURI());
        forwardSelectedRequestHeaders(clientToProxyRequest, proxyToRepositoryRequest);
        //printRepositoryRequestHeaders(repositoryRequest);
        HttpResponse repositoryToProxyResponse = httpClient.execute(proxyToRepositoryRequest);
        //printRepositoryResponseHeaders(repositoryResponse);
        forwardSelectedResponseHeaders(repositoryToProxyResponse, responseBuilder);
        forwardResponseCode(repositoryToProxyResponse, responseBuilder);
        try (InputStream in = repositoryToProxyResponse.getEntity().getContent()) {
            forwardData(in, this.responseBuilder);
        }
        return responseBuilder;
    }


    public Response.ResponseBuilder forwardHeadRequest(URL url) throws IOException, URISyntaxException {
        LOGGER.log(Level.FINE, "forwarding {0}", url);
        HttpHead repositoryRequest = new HttpHead(url.toURI());
        forwardSelectedRequestHeaders(clientToProxyRequest, repositoryRequest);
        //printRepositoryRequestHeaders(repositoryRequest);
        HttpResponse repositoryResponse = httpClient.execute(repositoryRequest);
        //printRepositoryResponseHeaders(repositoryResponse);
        forwardSelectedResponseHeaders(repositoryResponse, this.responseBuilder);
        forwardResponseCode(repositoryResponse, this.responseBuilder);

        return this.responseBuilder;
    }

    private void forwardSelectedResponseHeaders(HttpResponse repositoryResponse, Response.ResponseBuilder builder) {
        JerseyResponseHeaderForwarder forwarder = new JerseyResponseHeaderForwarder(repositoryResponse, builder);
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


    private void forwardData(final InputStream input, Response.ResponseBuilder respBuilder) {
        StreamingOutput stream = new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    IOUtils.copy(input, output);
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
        };
        respBuilder.entity(stream);
    }

    private void forwardResponseCode(HttpResponse repositoryResponse, Response.ResponseBuilder builder) {
        int repositoryResponseCode = repositoryResponse.getStatusLine().getStatusCode();
        builder.status(repositoryResponseCode);
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
