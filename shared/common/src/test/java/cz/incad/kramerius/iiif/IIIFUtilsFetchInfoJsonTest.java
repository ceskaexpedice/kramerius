package cz.incad.kramerius.iiif;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class IIIFUtilsFetchInfoJsonTest {

    private HttpServer server;
    private String base;

    @Before
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = "http://127.0.0.1:" + server.getAddress().getPort();
        server.createContext("/ok/info.json", exchange -> {
            byte[] body = "{\"width\":3164}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/moved/info.json", exchange -> {
            byte[] body = "<html>301 Moved Permanently</html>".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Location", "/ok/info.json");
            exchange.sendResponseHeaders(301, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.createContext("/loop/info.json", exchange -> {
            exchange.getResponseHeaders().add("Location", "/loop/info.json");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/missing/info.json", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void returnsBodyOfDirectResponse() throws IOException {
        Assert.assertEquals("{\"width\":3164}", IIIFUtils.fetchInfoJson(base + "/ok/info.json"));
    }

    @Test
    public void followsRedirectInsteadOfReturningItsHtmlBody() throws IOException {
        Assert.assertEquals("{\"width\":3164}", IIIFUtils.fetchInfoJson(base + "/moved/info.json"));
    }

    @Test(expected = IOException.class)
    public void givesUpOnRedirectLoop() throws IOException {
        IIIFUtils.fetchInfoJson(base + "/loop/info.json");
    }

    @Test(expected = IOException.class)
    public void failsOnErrorStatus() throws IOException {
        IIIFUtils.fetchInfoJson(base + "/missing/info.json");
    }
}
