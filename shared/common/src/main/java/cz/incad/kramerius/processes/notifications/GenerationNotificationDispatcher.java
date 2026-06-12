package cz.incad.kramerius.processes.notifications;

import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.processes.cdk.CDKAPIKeySupport;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;

import javax.mail.MessagingException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Logger;

public class GenerationNotificationDispatcher {

    public static final String MODE_LOCAL = "local";
    public static final String MODE_CDK = "cdk";
    public static final String CDK_CALLBACK_URL_KEY = "generate.notification.cdk.callback_url";
    public static final String CDK_CALLBACK_API_KEY = "generate.notification.cdk.api_key";
    public static final String X_API_KEY = "X-API-KEY";

    private static final Logger LOGGER = Logger.getLogger(GenerationNotificationDispatcher.class.getName());

    private GenerationNotificationDispatcher() {
    }

    public static void notify(
            GenerationNotification notification,
            String mode,
            String callbackUrl,
            LocalMailConfiguration mailConfiguration,
            MailSender mailSender
    ) {
        if (!StringUtils.isAnyString(notification.getEmail())) {
            return;
        }
        if (MODE_CDK.equalsIgnoreCase(mode)) {
            notifyCdk(notification, callbackUrl);
        } else {
            notifyLocal(notification, mailConfiguration, mailSender);
        }
    }

    private static void notifyLocal(
            GenerationNotification notification,
            LocalMailConfiguration mailConfiguration,
            MailSender mailSender
    ) {
        String mailPropertiesFile = System.getProperty("user.home") + File.separator + ".kramerius4" + File.separator + "mail.properties";
        if (!new File(mailPropertiesFile).exists()) {
            throw new IllegalArgumentException("Mail properties file not found");
        }

        Configuration config = KConfiguration.getInstance().getConfiguration();
        String senderEmail = config.getString(mailConfiguration.getSenderKey(), null);
        if (senderEmail == null) {
            senderEmail = config.getString("administrator.email", null);
        }
        if (senderEmail == null) {
            LOGGER.warning("Sender email is not specified in configuration. Setup property '" + mailConfiguration.getSenderKey() + "' or 'administrator.email' to enable sending notification emails");
            return;
        }

        String subject = config.getString(mailConfiguration.getSubjectKey(), mailConfiguration.getDefaultSubject());
        String text = config.getString(mailConfiguration.getBodyKey(), "");
        if (!StringUtils.isAnyString(text) && StringUtils.isAnyString(mailConfiguration.getLegacyBodyKey())) {
            text = config.getString(mailConfiguration.getLegacyBodyKey(), "");
        }
        String k7DocUrl = StringUtils.isAnyString(mailConfiguration.getK7DocUrlTemplateKey())
                ? config.getString(mailConfiguration.getK7DocUrlTemplateKey(), "")
                : "";
        if (!StringUtils.isAnyString(text)) {
            text = mailConfiguration.defaultBody(k7DocUrl);
        }

        StringTemplate template = new StringTemplate(text);
        template.setAttribute("title", notification.getTitle());
        template.setAttribute("filename", notification.getFilename());
        template.setAttribute("link", notification.getDownloadUrl());
        template.setAttribute("pid", notification.getPid());
        template.setAttribute("token", notification.getDownloadToken());

        try {
            mailSender.send(senderEmail, Arrays.asList(notification.getEmail()), subject, template.toString());
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void notifyCdk(GenerationNotification notification, String callbackUrl) {
        String endpoint = StringUtils.isAnyString(callbackUrl)
                ? callbackUrl
                : KConfiguration.getInstance().getConfiguration().getString(CDK_CALLBACK_URL_KEY, null);
        if (!StringUtils.isAnyString(endpoint)) {
            throw new IllegalArgumentException("CDK notification callback URL is not specified. Setup process parameter 'notificationCallbackUrl' or property '" + CDK_CALLBACK_URL_KEY + "'");
        }

        JSONObject payload = new JSONObject();
        payload.put("pid", notification.getPid());
        payload.put("user", notification.getUser());
        payload.put("email", notification.getEmail());
        payload.put("documentType", notification.getDocumentType());
        payload.put("filename", notification.getFilename());
        payload.put("downloadToken", notification.getDownloadToken());
        payload.put("downloadUrl", notification.getDownloadUrl());
        payload.put("title", notification.getTitle());
        payload.put("downloadCDKToken", notification.getDownloadCDKToken());
        payload.put("source", notification.getSource());

        postJson(endpoint, payload);
    }

    private static void postJson(String urlString, JSONObject payload) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            String apiKey = callbackApiKey();
            if (StringUtils.isAnyString(apiKey)) {
                conn.setRequestProperty(X_API_KEY, apiKey);
            } else {
                throw new IllegalArgumentException("CDK notification callback API key is not specified. Setup property '" + CDK_CALLBACK_API_KEY + "'");
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new RuntimeException("CDK notification failed with HTTP " + status + ": " + readResponse(conn.getErrorStream()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String callbackApiKey() {
        String configuredApiKey = KConfiguration.getInstance().getConfiguration().getString(CDK_CALLBACK_API_KEY, null);
        if (StringUtils.isAnyString(configuredApiKey)) {
            LOGGER.info( String.format("CDK notification callback API key is configured in configuration. Using it. '%s'", configuredApiKey));
            return configuredApiKey;
        }
        String apiKey = new CDKAPIKeySupport().getApiKey();
        LOGGER.info(String.format("CDK notification callback API key is not configured in configuration. Using API key from CDK. '%s'", apiKey));
        return apiKey;
    }

    private static String readResponse(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        }
    }

    public interface MailSender {
        void send(String emailFrom, java.util.List<Object> recipients, String subject, String text) throws MessagingException;
    }

    public static class LocalMailConfiguration {
        private final String senderKey;
        private final String subjectKey;
        private final String bodyKey;
        private final String legacyBodyKey;
        private final String k7DocUrlTemplateKey;
        private final String defaultSubject;
        private final String defaultBodyFormat;

        public LocalMailConfiguration(
                String senderKey,
                String subjectKey,
                String bodyKey,
                String legacyBodyKey,
                String k7DocUrlTemplateKey,
                String defaultSubject,
                String defaultBodyFormat
        ) {
            this.senderKey = senderKey;
            this.subjectKey = subjectKey;
            this.bodyKey = bodyKey;
            this.legacyBodyKey = legacyBodyKey;
            this.k7DocUrlTemplateKey = k7DocUrlTemplateKey;
            this.defaultSubject = defaultSubject;
            this.defaultBodyFormat = defaultBodyFormat;
        }

        public String getSenderKey() {
            return senderKey;
        }

        public String getSubjectKey() {
            return subjectKey;
        }

        public String getBodyKey() {
            return bodyKey;
        }

        public String getLegacyBodyKey() {
            return legacyBodyKey;
        }

        public String getK7DocUrlTemplateKey() {
            return k7DocUrlTemplateKey;
        }

        public String getDefaultSubject() {
            return defaultSubject;
        }

        private String defaultBody(String k7DocUrl) {
            String body = defaultBodyFormat;
            if (StringUtils.isAnyString(k7DocUrl)) {
                body += "\nTitul je take dostupny v digitalni knihovne zde: " + k7DocUrl;
            }
            return body;
        }
    }
}
