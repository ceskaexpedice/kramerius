package org.kramerius.genebook.impl;

import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.impl.MailerImpl;
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
import java.util.logging.Logger;

public class SpecialNeedsEbookServiceImpl implements org.kramerius.genebook.SpecialNeedsEbookService {

    public static final Logger LOGGER = Logger.getLogger(SpecialNeedsEbookServiceImpl.class.getName());

    @Override
    public void sendEmailNotification(String emailFrom, java.util.List<Object> recipients, String subject, String text) throws MessagingException {
        //private void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException {
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

    @Override
    public JSONObject scheduleRemoteJob(String exportApiBaseUrl, String pid, String authHeader, String k7BaseUrl) {
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

    @Override
    public JSONObject checkRemoteJob(String exportApiBaseUrl, String jobId, String authHeader) {
        LOGGER.info("Checking Job " + jobId + " ...");
        String url = exportApiBaseUrl + "/exports/" + jobId;
        //LOGGER.info("url: " + url);
        JSONObject response = callHttpGet(url, authHeader);
        //LOGGER.info("response: " + response.toString());
        return response;
    }

    private JSONObject callHttpPost(String urlString, JSONObject inputJson, String authHeader) {
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

    private JSONObject callHttpGet(String urlString, String authHeader) {
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
