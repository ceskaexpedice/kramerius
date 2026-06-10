package org.kramerius.genpdf;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lowagie.text.DocumentException;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.guice.PDFModule;
import cz.incad.kramerius.processes.notifications.GenerationNotification;
import cz.incad.kramerius.processes.notifications.GenerationNotificationDispatcher;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.guice.DocHubModule;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.kramerius.genpdf.impl.GenerateFullPDFServiceImpl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class GenerateFullPDFProcess {

    public static final String GENERATE_PDF_TEXT_KEY = "generate.pdf.text";
    public static final String GENERATE_PDF_SUBJECT_KEY = "generate.pdf.subject";
    public static final String GENERATE_PDF_EMAIL_SENDER_KEY = "generate.pdf.email.sender";
    public static final String GENERATE_PDF_EMAIL_BODY_KEY = "generate.pdf.email.body";

    public static Logger LOGGER = Logger.getLogger(GenerateFullPDFProcess.class.getName());

    @ProcessMethod
    public static void generate(
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("user") @IsRequired String user,
            @ParameterName("roles") @IsRequired String roles,
            @ParameterName("providedByLicense") String providedByLicenses,
            @ParameterName("locale") String locale,
            @ParameterName("email") String email,
            @ParameterName("notificationMode") String notificationMode,
            @ParameterName("notificationCallbackUrl") String notificationCallbackUrl,
            @ParameterName("notificationSource") String notificationSource
    ) {

        LOGGER.info("Generating PDF");
        LOGGER.info("pid: " + pid);
        LOGGER.info("user: " + user);
        LOGGER.info("roles: " + roles);
        LOGGER.info("locale: " + locale);
        LOGGER.info("email: " + email);

        System.setProperty(ArgumentLocalesProvider.LOCALE_PROPERTY_KEY, locale);

        System.setProperty("user.uid", user);
        System.setProperty("user.roles", roles);
        System.setProperty("user.providedByLicense", providedByLicenses);
        System.setProperty("date", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

        Injector injector = Guice.createInjector(
                new DocHubModule(),
                new SolrModule(),
                new RepoModule(),
                new NullStatisticsModule(),
                new PDFModule(),
                new ProcessModule(),
                new DocumentServiceModule(),
                new I18NModule()
        );

        GenerateFullPDFService serv = injector.getInstance(GenerateFullPDFService.class);
        try {

            //String pid, String user, String providedByLicense
            PluginContext pluginContext = PluginContextHolder.getContext();
            pluginContext.updateProcessName(String.format("Generování pdf pro  %s, pid %s, pod licencí %s, jazyková mutace %s", user, pid, providedByLicenses, locale));

            String token = serv.generate(pid, user, providedByLicenses);

            String api = KConfiguration.getInstance().getConfiguration().getString("api.client.point");
            String link = String.format("%s/%s/%s/pdf", api, "userrequests/userspace", token);
            GenerationNotificationDispatcher.notify(
                    new GenerationNotification.Builder()
                            .pid(pid)
                            .user(user)
                            .email(email)
                            .documentType("pdf")
                            .filename(pid + ".pdf")
                            .downloadToken(token)
                            .downloadUrl(link)
                            .title(pid)
                            .source(notificationSource != null ? notificationSource : "genpdf")
                            .build(),
                    notificationMode,
                    notificationCallbackUrl,
                    new GenerationNotificationDispatcher.LocalMailConfiguration(
                            GENERATE_PDF_EMAIL_SENDER_KEY,
                            GENERATE_PDF_SUBJECT_KEY,
                            GENERATE_PDF_EMAIL_BODY_KEY,
                            GENERATE_PDF_TEXT_KEY,
                            null,
                            "Download notification",
                            "Download notification,\ndownload is accessible here $link$"
                    ),
                    serv::sendEmailNotification
            );
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OutOfRangeException e) {
            throw new RuntimeException(e);
        }
    }
}
