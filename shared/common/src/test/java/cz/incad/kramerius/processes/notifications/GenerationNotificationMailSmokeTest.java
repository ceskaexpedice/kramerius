package cz.incad.kramerius.processes.notifications;

import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.impl.MailerImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;

public class GenerationNotificationMailSmokeTest {

    private static final String ENABLED = "mail.smoke.enabled";
    private static final String TO = "mail.smoke.to";
    private static final String FROM = "mail.smoke.from";

    private String recipient;
    private String sender;

    @Before
    public void setUp() {
        Assume.assumeTrue(Boolean.parseBoolean(value(ENABLED, "MAIL_SMOKE_ENABLED", "false")));

        recipient = requiredValue(TO, "MAIL_SMOKE_TO");
        sender = requiredValue(FROM, "MAIL_SMOKE_FROM");

        File mailProperties = new File(System.getProperty("user.home"), ".kramerius4" + File.separator + "mail.properties");
        Assert.assertTrue("Mail properties file not found: " + mailProperties.getAbsolutePath(), mailProperties.exists());
    }

    @Test
    public void sendsPdfDownloadNotificationMail() {
        Configuration config = KConfiguration.getInstance().getConfiguration();
        config.setProperty("generate.pdf.email.sender", sender);
        config.setProperty("generate.pdf.subject", "Kramerius PDF smoke test");
        config.setProperty("generate.pdf.email.body", "PDF smoke test for $pid$\nDownload: $link$\nToken: $token$");

        GenerationNotificationDispatcher.notify(
                notification("pdf", "genpdf", "uuid:00000000-0000-0000-0000-000000000001", "pdf-smoke-token", "https://kramerius.example/userrequests/userspace/pdf-smoke-token/pdf"),
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
                new RealMailSender()
        );
    }

    @Test
    public void sendsEpubDownloadNotificationMail() {
        Configuration config = KConfiguration.getInstance().getConfiguration();
        config.setProperty("generate.epub.email.sender", sender);
        config.setProperty("generate.epub.email.subject", "Kramerius EPUB smoke test");
        config.setProperty("generate.epub.email.body", "EPUB smoke test for $title$\nDownload: $link$\nToken: $token$");

        GenerationNotificationDispatcher.notify(
                notification("epub", "genebook", "uuid:00000000-0000-0000-0000-000000000002", "epub-smoke-token", "https://kramerius.example/userrequests/userspace/epub-smoke-token/epub"),
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
                new RealMailSender()
        );
    }

    private GenerationNotification notification(String type, String source, String pid, String token, String url) {
        return new GenerationNotification.Builder()
                .pid(pid)
                .user("mail-smoke-test")
                .email(recipient)
                .documentType(type)
                .filename("mail-smoke-test." + type)
                .downloadToken(token)
                .downloadUrl(url)
                .title("Mail smoke test " + type)
                .source(source)
                .build();
    }

    private static String requiredValue(String propertyName, String envName) {
        String configured = value(propertyName, envName, null);
        Assert.assertNotNull("Set system property '" + propertyName + "' or environment variable '" + envName + "'", configured);
        Assert.assertFalse("Value for '" + propertyName + "'/'" + envName + "' must not be empty", configured.trim().isEmpty());
        return configured;
    }

    private static String value(String propertyName, String envName, String defaultValue) {
        String value = System.getProperty(propertyName);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envName);
        }
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    private static class RealMailSender implements GenerationNotificationDispatcher.MailSender {
        @Override
        public void send(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException {
            Mailer mailer = new MailerImpl();
            Session session = mailer.getSession(null, null);
            Assert.assertNotNull("Mailer did not create javax.mail.Session", session);

            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/plain; charset=UTF-8");
            message.setFrom(new InternetAddress(emailFrom));
            for (Object recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.toString()));
            }
            message.setSubject(subject, "UTF-8");
            message.setText(text, "UTF-8");

            Transport.send(message);
        }
    }
}
