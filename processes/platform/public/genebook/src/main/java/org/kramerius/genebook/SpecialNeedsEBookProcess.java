package org.kramerius.genebook;

import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.impl.MailerImpl;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.configuration.Configuration;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.json.JSONObject;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @see <a href="https://github.com/trineracz/alto-processing">alto-processing on Github</a>
 * <p>
 * #Sample configuration (configuration.properties)
 * <p>
 * generate.epub.service_api.base_url=https://alto-processing.trinera.cloud
 * generate.epub.service_api.auth_token=TOKEN
 * generate.epub.service_api.k7_base_url=https://api.kramerius.mzk.cz/search/
 * <p>
 * generate.epub.email.sender=kramerius-epub@trinera.cloud
 * generate.epub.email.lib_code=mzk
 */
public class SpecialNeedsEBookProcess {

    public static final Logger LOGGER = Logger.getLogger(SpecialNeedsEBookProcess.class.getName());

    public static final String GENERATE_EPUB_SERVICE_API_BASE_URL = "generate.epub.service_api.base_url";
    public static final String GENERATE_EPUB_SERVICE_API_AUTH_TOKEN = "generate.epub.service_api.auth_token";
    public static final String GENERATE_EPUB_SERVICE_API_K7_BASE_URL = "generate.epub.service_api.k7_base_url";

    public static final String GENERATE_EPUB_SUBJECT_SENDER = "generate.epub.email.sender";
    public static final String GENERATE_EPUB_SUBJECT_KEY = "generate.epub.email.subject";
    public static final String GENERATE_EPUB_TEXT_KEY = "generate.epub.email.text";
    public static final String GENERATE_EPUB_TEXT_LIB_CODE = "generate.epub.email.lib_code";

    @ProcessMethod
    public static void run(
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("user") String user,
            @ParameterName("email") @IsRequired String email
    ) {
        LOGGER.info("Generating EPUB for special needs");
        LOGGER.info("pid: " + pid);
        LOGGER.info("email: " + email);
        //0. extract configuration
        Configuration config = KConfiguration.getInstance().getConfiguration();
        String serviceApiBaseUrl = normalizUrl(config.getString(GENERATE_EPUB_SERVICE_API_BASE_URL));
        if (serviceApiBaseUrl == null) {
            throw new RuntimeException("Base URL for Epub export service is not specified in configuration. Please setup property '" + GENERATE_EPUB_SERVICE_API_BASE_URL + "' to enable Epub export functionality.");
        }
        LOGGER.info("serviceApiBaseUrl: " + serviceApiBaseUrl);
        String authToken = config.getString(GENERATE_EPUB_SERVICE_API_AUTH_TOKEN);
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Authentication token for Epub export service is not specified in environment variables. Please setup variable '" + GENERATE_EPUB_SERVICE_API_AUTH_TOKEN + "' to enable Epub export functionality.");
        }
        String authHeader = "Bearer " + authToken;
        String k7BaseUrl = normalizUrl(config.getString(GENERATE_EPUB_SERVICE_API_K7_BASE_URL));

        //1. schedule remote export Job
        JSONObject job = scheduleRemoteJob(serviceApiBaseUrl, pid, authHeader, k7BaseUrl);

        //2. wait for remote export Job to finish
        while (!job.getString("state").equals("completed")) {
            try {
                job = checkRemoteJob(serviceApiBaseUrl, job.getString("job_id"), authHeader);
                //LOGGER.info("job: " + job.toString());
                JSONObject progress = job.getJSONObject("progress");
                switch (job.getString("state")) {
                    case "pending": {
                        LOGGER.info("Waiting for Job to start ...");
                        break;
                    }
                    case "running": {
                        LOGGER.info("Job is running ...");
                        logProgress(progress);
                        break;
                    }
                    case "completed": {
                        LOGGER.info("Job has completed ...");
                        logProgress(progress);
                        break;
                    }
                    case "failed": {
                        LOGGER.info("Job has failed ...");
                        LOGGER.severe(job.getString("error"));
                        throw new RuntimeException("Job has failed with error: " + job.getString("error"));
                    }
                    default: {
                        LOGGER.info("Unknown job state: " + job.getString("state"));
                        break;
                    }
                }
                Thread.sleep(1000); //wait for 1 second before checking again
            } catch (final InterruptedException e) {
                throw new RuntimeException("Thread was interrupted while waiting for Epub export to finish", e);
            }
        }

        //3. process Job's results
        if (!job.has("download_url")) {
            throw new RuntimeException("Download url has not been provided");
        }
        String downloadUrl = serviceApiBaseUrl + job.getString("download_url");
        String filename = job.getString("filename");

        LOGGER.info("Filename: " + filename);
        LOGGER.info("Download url: " + downloadUrl);
        notifyUsersWithEmail(pid, filename, downloadUrl, email);
    }

    private static void logProgress(JSONObject progress) {
        if (progress.has("message") && !progress.getString("message").isBlank()) {
            LOGGER.info(progress.getString("message"));
        }
        Double percent = null;
        if (progress.has("percent")) {
            percent = progress.getDouble("percent");
        }
        if (percent != null) {
            LOGGER.info(percent + " %");
        }
    }

    private static String normalizUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static JSONObject scheduleRemoteJob(String exportApiBaseUrl, String pid, String authHeader, String k7BaseUrl) {
        LOGGER.info("Scheduling new Job for " + pid + " ...");
        if (!pid.matches("uuid:[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            throw new IllegalArgumentException("PID must be in format 'uuid:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'");
        }
        String uuid = pid.substring("uuid:".length());
        JSONObject input = new JSONObject()
                .put("uuid", uuid)
                .put("format", "epub")
                .put("range", "all")
                .put("dropSmall", true);
        if (k7BaseUrl != null) {
            input.put("apiBase", k7BaseUrl);
        }

        String url = exportApiBaseUrl + "/download";
        //LOGGER.info("url: " + url);
        JSONObject response = callHttpPost(url, input, authHeader);
        //LOGGER.info("response: " + response.toString());
        return response;
    }

    private static JSONObject checkRemoteJob(String exportApiBaseUrl, String jobId, String authHeader) {
        LOGGER.info("Checking Job " + jobId + " ...");
        String url = exportApiBaseUrl + "/exports/" + jobId;
        //LOGGER.info("url: " + url);
        JSONObject response = callHttpGet(url, authHeader);
        //LOGGER.info("response: " + response.toString());
        return response;
    }


    private static void notifyUsersWithEmail(String pid, String filename, String url, String email) {
        if (StringUtils.isAnyString(email)) {
            LOGGER.info("Email specified: " + email);
            String mailPropertiesFile = System.getProperty("user.home") + File.separator + ".kramerius4" + File.separator + "mail.properties";
            if (new File(mailPropertiesFile).exists()) {
                try {
                    Configuration config = KConfiguration.getInstance().getConfiguration();
                    String senderEmail = config.getString(GENERATE_EPUB_SUBJECT_SENDER, null);
                    if (senderEmail == null) {
                        senderEmail = config.getString("administrator.email", null); //fallback with general property
                    }
                    if (senderEmail == null) {
                        LOGGER.warning("Sender email is not specified in configuration!" +
                                " Setup property '" + GENERATE_EPUB_SUBJECT_SENDER + "' or 'administrator.email'" +
                                " to enable sending notification emails");
                        return;
                    }

                    String libCode = config.getString(GENERATE_EPUB_TEXT_LIB_CODE, "");
                    String subject = KConfiguration.getInstance().getConfiguration().getString(GENERATE_EPUB_TEXT_KEY, "EPUB připraven ke stažení");
                    String text = KConfiguration.getInstance().getConfiguration().getString(GENERATE_EPUB_SUBJECT_KEY, "");
                    if (text.isBlank()) {
                        text = "Dobrý den,\n";
                        text += "požádali jste o export titulu „$title$“ ve formátu EPUB pro stažení do vašeho zařízení.\n";
                        text += "Export byl dokončen a soubor je nyní připraven ke stažení.\n";
                        text += "Odkaz ke stažení: $link$\n";
                        if (!libCode.isBlank()) {
                            text += "Titul je také dostupný v digitální knihovně zde: https://www.digitalniknihovna.cz/" + libCode + "/uuid/$pid$\n";
                        }
                        text += "Přejeme příjemné čtení!";
                    }
                    StringTemplate template = new StringTemplate(text);
                    template.setAttribute("title", filename);
                    template.setAttribute("link", url);
                    template.setAttribute("pid", pid);
                    String body = template.toString();
                    LOGGER.info(body);
                    sendEmailNotification(senderEmail, Arrays.asList(email), subject, body);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalArgumentException("Mail properties file not found");
            }
        }
    }

    private static void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException {
        Mailer mailer = new MailerImpl();
        javax.mail.Session sess = mailer.getSession(null, null);
        MimeMessage msg = new MimeMessage(sess);

        msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
        msg.setFrom(new InternetAddress(emailFrom));
        for (Object recp : recipients) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recp.toString()));
        }
        msg.setSubject(subject, "UTF-8");
        msg.setText(text, "UTF-8");
        Transport.send(msg);
    }

    private static JSONObject callHttpPost(String urlString, JSONObject inputJson, String authHeader) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", authHeader);

            String requestBody = inputJson.toString();

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            int status = conn.getResponseCode();

            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                responseBody = sb.toString();
            }

            if (status < 200 || status >= 300) {
                throw new RuntimeException("HTTP " + status + ": " + responseBody);
            }

            return new JSONObject(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static JSONObject callHttpGet(String urlString, String authHeader) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", authHeader);

            int status = conn.getResponseCode();

            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                responseBody = sb.toString();
            }

            if (status < 200 || status >= 300) {
                throw new RuntimeException("HTTP " + status + ": " + responseBody);
            }

            return new JSONObject(responseBody);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

}
