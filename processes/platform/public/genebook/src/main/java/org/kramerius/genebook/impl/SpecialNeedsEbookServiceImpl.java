package org.kramerius.genebook.impl;

import com.google.inject.Inject;
import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.impl.MailerImpl;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.UserContentSpace;
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

    private static final String DEV_HARDCODED_PID = null;
    private static final String DEV_HARDCODED_RANGE = null;
    //private static final String DEV_HARDCODED_PID = "uuid:1f844c3d-9b0a-4970-bbf1-8d20e1bc7ded"; //tohle je jen na verejne instalaci
    //private static final String DEV_HARDCODED_RANGE = "1"; // range – all / book / * nebo např. "7-11,23"

    @Inject
    UserContentSpace userContentSpace;

    @Override
    public void sendEmailNotification(String emailFrom, java.util.List<Object> recipients, String subject, String text) throws MessagingException {
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
    public JSONObject scheduleRemoteJob(String exportServiceBaseUrl, String pid, String exportServiceAuthHeader, String k7ClientApiBasUrl) {
        if (DEV_HARDCODED_PID != null) {
            pid = DEV_HARDCODED_PID;
            LOGGER.warning("Using hardcoded PID '" + pid + "' for scheduling the job. This should not be used in production!");
        }
        LOGGER.info("Scheduling new Job for " + pid + " ...");
        if (!pid.matches("uuid:[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}")) {
            throw new IllegalArgumentException("PID must be in format 'uuid:xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'");
        }
        String uuid = pid.substring("uuid:".length());
        JSONObject input = new JSONObject()
                .put("uuid", uuid)
                .put("format", "epub")
                .put("dropSmall", true);
        //k7 base url
        if (k7ClientApiBasUrl != null) {
            input.put("apiBase", k7ClientApiBasUrl);
        }
        //range
        if (DEV_HARDCODED_RANGE != null) {
            input.put("range", DEV_HARDCODED_RANGE);
        } else {
            input.put("range", "all");
        }

        LOGGER.info(input.toString());

        String url = exportServiceBaseUrl + "/download";
        //LOGGER.info("url: " + url);
        JSONObject response = callHttpPost(url, input, exportServiceAuthHeader);
        //LOGGER.info("response: " + response.toString());
        return response;
    }

    @Override
    public JSONObject checkRemoteJob(String exportServiceBaseUrl, String jobId, String exportServiceAuthHeader) {
        LOGGER.info("Checking Job " + jobId + " ...");
        String url = exportServiceBaseUrl + "/exports/" + jobId;
        //LOGGER.info("url: " + url);
        JSONObject response = callHttpGet(url, exportServiceAuthHeader);
        //LOGGER.info("response: " + response.toString());
        return response;
    }

    @Override
    public File saveJobResultToTmpFile(String serviceApiBaseUrl, String pid, String exportServiceAuthHeader, JSONObject job) {
        try {
            if (!job.has("download_url")) {
                throw new RuntimeException("Download url has not been provided");
            }
            String downloadUrl = serviceApiBaseUrl + job.getString("download_url");
            String filename = job.getString("filename");

            LOGGER.info("Export Service filename: " + filename);
            LOGGER.info("Export Service download url: " + downloadUrl);
            File generatedTmpFile = File.createTempFile(createFilePrefix(filename, "kramerius_export_"), ".epub");
            LOGGER.info("Generated tmp file: " + generatedTmpFile.getAbsolutePath());

            long bytesRead = callHttpGetSavingResultToFile(downloadUrl, exportServiceAuthHeader, generatedTmpFile);
            //format to kB, MB, GB etc.
            LOGGER.info("Saved " + formatFileSize(bytesRead) + " into temporary file " + generatedTmpFile.getAbsolutePath());
            return generatedTmpFile;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save job result to tmp file", e);
        }
    }

    private String createFilePrefix(String originalFilename, String fallback) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return fallback;
        }
        String sanitized = originalFilename
                .trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        if (sanitized.isEmpty()) {
            return fallback;
        }
        if (sanitized.length() > 30) {
            sanitized = sanitized.substring(0, 20)
                    + "..."
                    + sanitized.substring(sanitized.length() - 5);
        }
        return sanitized;
    }

    private String formatFileSize(long bytesRead) {
        //return file size in human readable format
        if (bytesRead < 1024) {
            return bytesRead + " B";
        } else if (bytesRead < 1024 * 1024) {
            return String.format("%.2f kB", bytesRead / 1024.0);
        } else if (bytesRead < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytesRead / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytesRead / (1024.0 * 1024 * 1024));
        }
    }

    @Override
    public String saveFileToUserContentSpace(File file, DocumentType type, String user, String pid) {
        try {
            userContentSpace.storeBundle(new FileInputStream(file), user, pid, type, "{audit}");
            return userContentSpace.getToken(pid, user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file to user content space", e);
        }
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

    private long callHttpGetSavingResultToFile(String urlString, String authHeader, File outputFile) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", authHeader);

            int status = conn.getResponseCode();

            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            //ulozit Content-Type: application/octet-stream do souboru outFile

            long bytesReadTotal = 0;
            try (OutputStream os = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    bytesReadTotal += bytesRead;
                    os.write(buffer, 0, bytesRead);
                }
            }

            if (status < 200 || status >= 300) {
                String responseBody;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    responseBody = sb.toString();
                }
                throw new RuntimeException("Error downloading results: HTTP " + status + ": " + responseBody);
            }
            return bytesReadTotal;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
