package org.kramerius.genebook;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.processes.notifications.GenerationNotification;
import cz.incad.kramerius.processes.notifications.GenerationNotificationDispatcher;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.guice.DocHubModule;
import org.apache.commons.configuration.Configuration;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.json.JSONObject;
import org.kramerius.genebook.impl.SpecialNeedsEbookServiceImpl;

import java.io.File;
import java.util.logging.Logger;

/**
 * @see <a href="https://github.com/trineracz/alto-processing">alto-processing on Github</a>
 * <p>
 * #Sample configuration (configuration.properties)
 * <p>
 * generate.epub.k7.client_api_base_url=https://api.kramerius.mzk.cz/search/api/client/v7.0
 * <p>
 * generate.epub.service_api.base_url=https://alto-processing.trinera.cloud
 * generate.epub.service_api.auth_token=TOKEN
 * <p>
 * generate.epub.email.sender=kramerius-epub-export@trinera.cloud
 * generate.epub.email.subject=Textový přepis připraven ke stažení
 * generate.epub.email.body.k7_doc_url_template=https://www.k7.trinera.cloud/uuid/$pid$
 */
public class SpecialNeedsEBookProcess {

    public static final Logger LOGGER = Logger.getLogger(SpecialNeedsEBookProcess.class.getName());

    //K7
    public static final String GENERATE_EPUB_K7_CLIENT_API_BASE_URL = "generate.epub.k7.client_api_base_url";
    //Export service
    public static final String GENERATE_EPUB_SERVICE_API_BASE_URL = "generate.epub.service_api.base_url";
    public static final String GENERATE_EPUB_SERVICE_API_AUTH_TOKEN = "generate.epub.service_api.auth_token";
    //email
    public static final String GENERATE_EPUB_EMAIL_SENDER = "generate.epub.email.sender";
    public static final String GENERATE_EPUB_EMAIL_SUBJECT = "generate.epub.email.subject";
    public static final String GENERATE_EPUB_EMAIL_BODY = "generate.epub.email.body";
    public static final String GENERATE_EPUB_EMAIL_BODY_K7_DOC_URL_TEMPLATE = "generate.epub.email.body.k7_doc_url_template";

    @ProcessMethod
    public static void run(
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("user") String user,
            @ParameterName("email")  String email,
            @ParameterName("notificationMode") String notificationMode,
            @ParameterName("notificationCallbackUrl") String notificationCallbackUrl,
            @ParameterName("notificationSource") String notificationSource
    ) {
        LOGGER.info("Generating EPUB for special needs");
        LOGGER.info("pid: " + pid);
        LOGGER.info("email: " + email);

        PluginContext pluginContext = PluginContextHolder.getContext();
        pluginContext.updateProcessName(String.format("Generování EPUB přepisu pro  %s, titul %s", user, pid));

        Injector injector = Guice.createInjector( //TODO: vyhodit, co není potřeba
                new DocHubModule(),
                new SolrModule(),
                new RepoModule(),
                new NullStatisticsModule(),
                //new PDFModule(),
                //new ProcessModule(),
                //new DocumentServiceModule(),
                new I18NModule()
        );

        SpecialNeedsEbookService serv = injector.getInstance(SpecialNeedsEbookServiceImpl.class);

        //0. extract configuration
        Configuration config = KConfiguration.getInstance().getConfiguration();
        String serviceApiBaseUrl = normalizUrl(config.getString(GENERATE_EPUB_SERVICE_API_BASE_URL));
        if (serviceApiBaseUrl == null) {
            throw new RuntimeException("Base URL for Export Service is not specified in configuration. Please setup property '" + GENERATE_EPUB_SERVICE_API_BASE_URL + "' to enable EPUB export functionality.");
        }
        LOGGER.info("serviceApiBaseUrl: " + serviceApiBaseUrl);
        String authToken = config.getString(GENERATE_EPUB_SERVICE_API_AUTH_TOKEN);
        if (authToken == null || authToken.isEmpty()) {
            throw new RuntimeException("Authentication token for Export Service is not specified in environment variables. Please setup variable '" + GENERATE_EPUB_SERVICE_API_AUTH_TOKEN + "' to enable EPUB export functionality.");
        }
        String authHeader = "Bearer " + authToken;
        String k7BaseUrl = normalizUrl(config.getString(GENERATE_EPUB_K7_CLIENT_API_BASE_URL));

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
                throw new RuntimeException("Thread was interrupted while waiting for EPUB export to finish", e);
            }
        }

        //3. process Job's results
        File tmpFile = serv.saveJobResultToTmpFile(serviceApiBaseUrl, pid, authHeader, job);
        LOGGER.info("Saved into tmp file: " + tmpFile.getAbsolutePath());
        String downloadToken = serv.saveFileToUserContentSpace(tmpFile, DocumentType.EPUB, user, pid);
        LOGGER.info("Download token: " + downloadToken);
        GenerationNotificationDispatcher.notify(
                new GenerationNotification.Builder()
                        .pid(pid)
                        .user(user)
                        .email(email)
                        .documentType("epub")
                        .filename(tmpFile.getName())
                        .downloadToken(downloadToken)
                        .downloadUrl(buildDownloadUrl(downloadToken))
                        .title(tmpFile.getName())
                        .source(notificationSource != null ? notificationSource : "genebook")
                        .build(),
                notificationMode,
                notificationCallbackUrl,
                new GenerationNotificationDispatcher.LocalMailConfiguration(
                        GENERATE_EPUB_EMAIL_SENDER,
                        GENERATE_EPUB_EMAIL_SUBJECT,
                        GENERATE_EPUB_EMAIL_BODY,
                        null,
                        GENERATE_EPUB_EMAIL_BODY_K7_DOC_URL_TEMPLATE,
                        "EPUB pripraven ke stazeni",
                        "Dobry den,\npozadali jste o export titulu \"$title$\" ve formatu EPUB pro stazeni do vaseho zarizeni.\nExport byl dokoncen a soubor je nyni pripraven ke stazeni.\nOdkaz ke stazeni: $link$\nPrejeme prijemne cteni!"
                ),
                serv::sendEmailNotification
        );
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

    private static String buildDownloadUrl(String token) {
        String k7BaseUrl = normalizUrl(KConfiguration.getInstance().getConfiguration().getString(GENERATE_EPUB_K7_CLIENT_API_BASE_URL));
        String link = String.format("%s/%s/%s/epub", k7BaseUrl, "userrequests/userspace", token);
        LOGGER.info("Download URL: " + link);
        return link;
    }
}
