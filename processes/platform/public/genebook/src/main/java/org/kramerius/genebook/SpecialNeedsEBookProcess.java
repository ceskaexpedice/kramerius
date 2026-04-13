package org.kramerius.genebook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.configuration.Configuration;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.json.JSONObject;
import org.kramerius.genebook.impl.SpecialNeedsEbookServiceImpl;

import javax.mail.MessagingException;
import java.io.File;
import java.util.Arrays;
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
            //TODO: for special_needs_ebook and also special_needs_text
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("user") String user,
            @ParameterName("email") @IsRequired String email
    ) {
        LOGGER.info("Generating EPUB for special needs");
        LOGGER.info("pid: " + pid);
        LOGGER.info("email: " + email);

        Injector injector = Guice.createInjector(
                //new DocHubModule(),
                //new SolrModule(),
                //new RepoModule(),
                //new NullStatisticsModule(),
                //new PDFModule(),
                //new ProcessModule(),
                //new DocumentServiceModule(),
                //new I18NModule()
        );

        SpecialNeedsEbookService serv = injector.getInstance(SpecialNeedsEbookServiceImpl.class);

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
        JSONObject job = serv.scheduleRemoteJob(serviceApiBaseUrl, pid, authHeader, k7BaseUrl);

        //2. wait for remote export Job to finish
        while (!job.getString("state").equals("completed")) {
            try {
                job = serv.checkRemoteJob(serviceApiBaseUrl, job.getString("job_id"), authHeader);
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
        notifyUsersWithEmail(pid, filename, downloadUrl, email, serv);
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

    private static void notifyUsersWithEmail(String pid, String filename, String url, String email, SpecialNeedsEbookService serv) {
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
                    serv.sendEmailNotification(senderEmail, Arrays.asList(email), subject, body);
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalArgumentException("Mail properties file not found");
            }
        }
    }
}
