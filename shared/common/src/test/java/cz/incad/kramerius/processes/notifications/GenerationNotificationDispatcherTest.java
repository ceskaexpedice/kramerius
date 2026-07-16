package cz.incad.kramerius.processes.notifications;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import cz.incad.kramerius.processes.cdk.CDKAPIKeySupport;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GenerationNotificationDispatcherTest {

    private static final String RECIPIENT = "reader@example.org";
    private static final String SENDER = "kramerius@example.org";

    private String originalUserHome;
    private Path userHome;

    @Before
    public void setUp() throws IOException {
        originalUserHome = System.getProperty("user.home");
        userHome = Files.createTempDirectory("kramerius-notification-test");
        Path krameriusHome = userHome.resolve(".kramerius4");
        Files.createDirectories(krameriusHome);
        Files.createFile(krameriusHome.resolve("mail.properties"));
        System.setProperty("user.home", userHome.toString());

        new CDKAPIKeySupport().init();
    }

    @After
    public void tearDown() {
        System.setProperty("user.home", originalUserHome);
    }

    @Test
    public void sendsPdfMailNotification() {
        CapturingMailSender mailSender = new CapturingMailSender();
        Configuration config = KConfiguration.getInstance().getConfiguration();
        config.setProperty("generate.pdf.email.sender", SENDER);
        config.setProperty("generate.pdf.subject", "PDF ready");
        config.setProperty("generate.pdf.email.body", "PDF link $link$ for $pid$");

        GenerationNotificationDispatcher.notify(
                notification("pdf", "genpdf", "uuid:pdf", "pdf-token", "https://kramerius.example/userspace/pdf-token/pdf"),
                GenerationNotificationDispatcher.MODE_LOCAL,
                null,
                new GenerationNotificationDispatcher.LocalMailConfiguration(
                        "generate.pdf.email.sender",
                        "generate.pdf.subject",
                        "generate.pdf.email.body",
                        "generate.pdf.text",
                        null,
                        "Download notification",
                        "Download notification,\ndownload is accessible here $link$"
                ),
                mailSender
        );

        Assert.assertEquals(SENDER, mailSender.emailFrom);
        Assert.assertEquals("PDF ready", mailSender.subject);
        Assert.assertEquals(RECIPIENT, mailSender.recipients.get(0));
        Assert.assertTrue(mailSender.text.contains("https://kramerius.example/userspace/pdf-token/pdf"));
        Assert.assertTrue(mailSender.text.contains("uuid:pdf"));
    }

    @Test
    public void sendsTextMailNotification() {
        CapturingMailSender mailSender = new CapturingMailSender();
        Configuration config = KConfiguration.getInstance().getConfiguration();
        config.setProperty("generate.text.email.sender", SENDER);
        config.setProperty("generate.text.email.subject", "TEXT ready");
        config.setProperty("generate.text.email.body", "TEXT link $link$ for $title$");

        GenerationNotificationDispatcher.notify(
                notification("text", "gentext", "uuid:text", "text-token", "https://kramerius.example/userspace/text-token/text"),
                GenerationNotificationDispatcher.MODE_LOCAL,
                null,
                new GenerationNotificationDispatcher.LocalMailConfiguration(
                        "generate.text.email.sender",
                        "generate.text.email.subject",
                        "generate.text.email.body",
                        null,
                        "generate.text.email.body.k7_doc_url_template",
                        "Textovy prepis pripraven ke stazeni",
                        "Dobry den,\nOdkaz ke stazeni: $link$"
                ),
                mailSender
        );

        Assert.assertEquals(SENDER, mailSender.emailFrom);
        Assert.assertEquals("TEXT ready", mailSender.subject);
        Assert.assertEquals(RECIPIENT, mailSender.recipients.get(0));
        Assert.assertTrue(mailSender.text.contains("https://kramerius.example/userspace/text-token/text"));
        Assert.assertTrue(mailSender.text.contains("Generated text"));
    }

    @Test
    public void sendsEbookMailNotification() {
        CapturingMailSender mailSender = new CapturingMailSender();
        Configuration config = KConfiguration.getInstance().getConfiguration();
        config.setProperty("generate.epub.email.sender", SENDER);
        config.setProperty("generate.epub.email.subject", "EPUB ready");
        config.setProperty("generate.epub.email.body", "EPUB link $link$ for $filename$");

        GenerationNotificationDispatcher.notify(
                notification("epub", "genebook", "uuid:epub", "epub-token", "https://kramerius.example/userspace/epub-token/epub"),
                GenerationNotificationDispatcher.MODE_LOCAL,
                null,
                new GenerationNotificationDispatcher.LocalMailConfiguration(
                        "generate.epub.email.sender",
                        "generate.epub.email.subject",
                        "generate.epub.email.body",
                        null,
                        "generate.epub.email.body.k7_doc_url_template",
                        "EPUB pripraven ke stazeni",
                        "Dobry den,\nOdkaz ke stazeni: $link$"
                ),
                mailSender
        );

        Assert.assertEquals(SENDER, mailSender.emailFrom);
        Assert.assertEquals("EPUB ready", mailSender.subject);
        Assert.assertEquals(RECIPIENT, mailSender.recipients.get(0));
        Assert.assertTrue(mailSender.text.contains("https://kramerius.example/userspace/epub-token/epub"));
        Assert.assertTrue(mailSender.text.contains("generated.epub"));
    }

    @Test
    public void sendsPdfNotificationToCdkCallback() throws IOException {
        AtomicReference<JSONObject> capturedPayload = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/notifications", exchange -> captureJson(exchange, capturedPayload));
        server.start();
        try {
            String callbackUrl = "http://localhost:" + server.getAddress().getPort() + "/notifications";
            GenerationNotificationDispatcher.notify(
                    notification("pdf", "genpdf", "uuid:pdf", "pdf-token", "https://kramerius.example/userspace/pdf-token/pdf"),
                    GenerationNotificationDispatcher.MODE_CDK,
                    callbackUrl,
                    new GenerationNotificationDispatcher.LocalMailConfiguration(
                            "generate.pdf.email.sender",
                            "generate.pdf.subject",
                            "generate.pdf.email.body",
                            "generate.pdf.text",
                            null,
                            "Download notification",
                            "Download notification,\ndownload is accessible here $link$"
                    ),
                    new CapturingMailSender()
            );
        } finally {
            server.stop(0);
        }

        JSONObject payload = capturedPayload.get();
        Assert.assertNotNull(payload);
        Assert.assertEquals("pdf", payload.getString("documentType"));
        Assert.assertEquals("genpdf", payload.getString("source"));
        Assert.assertEquals("uuid:pdf", payload.getString("pid"));
        Assert.assertEquals(RECIPIENT, payload.getString("email"));
        Assert.assertEquals("pdf-token", payload.getString("downloadToken"));
    }

    private static GenerationNotification notification(String type, String source, String pid, String token, String url) {
        return new GenerationNotification.Builder()
                .pid(pid)
                .user("reader")
                .email(RECIPIENT)
                .documentType(type)
                .filename(type.equals("epub") ? "generated.epub" : "generated." + type)
                .downloadToken(token)
                .downloadUrl(url)
                .title(type.equals("text") ? "Generated text" : "Generated " + type)
                .source(source)
                .build();
    }

    private static void captureJson(HttpExchange exchange, AtomicReference<JSONObject> capturedPayload) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        capturedPayload.set(new JSONObject(body));
        exchange.sendResponseHeaders(204, -1);
        try (OutputStream responseBody = exchange.getResponseBody()) {
            responseBody.write(new byte[0]);
        }
    }

    private static class CapturingMailSender implements GenerationNotificationDispatcher.MailSender {
        private String emailFrom;
        private List<Object> recipients;
        private String subject;
        private String text;

        @Override
        public void send(String emailFrom, List<Object> recipients, String subject, String text) {
            this.emailFrom = emailFrom;
            this.recipients = recipients;
            this.subject = subject;
            this.text = text;
        }
    }
}
