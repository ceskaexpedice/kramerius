package org.kramerius.genpdf;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lowagie.text.DocumentException;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.guice.PDFModule;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.dochub.guice.DocHubModule;
import org.antlr.stringtemplate.StringTemplate;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.kramerius.genpdf.impl.GenerateFullPDFServiceImpl;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class GenerateFullPDFProcess {

    public static final String GENERATE_PDF_TEXT_KEY = "generate.pdf.text";
    public static final String GENERATE_PDF_SUBJECT_KEY = "generate.pdf.subject";

    public static Logger LOGGER = Logger.getLogger(GenerateFullPDFProcess.class.getName());

    @ProcessMethod
    public static void generate(
            @ParameterName("pid") @IsRequired String pid,
            @ParameterName("user") @IsRequired  String user,
            @ParameterName("roles") @IsRequired  String roles,
            @ParameterName("providedByLicense")   String providedByLicenses,
            @ParameterName("locale") String locale,
            @ParameterName("email") String email
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

        // special needs license vs public license
        GenerateFullPDFService serv = injector.getInstance(GenerateFullPDFService.class);
        try {
            String token = serv.generate(pid, user, providedByLicenses);
            if (StringUtils.isAnyString(email)) {
                LOGGER.info("Email specified: " + email);
                try {
                    String administratorEmail = KConfiguration.getInstance().getConfiguration().getString("administrator.email");
                    String text = KConfiguration.getInstance().getConfiguration().getString(GENERATE_PDF_SUBJECT_KEY, "Download notification, \ndownload is accessible here $link$");
                    String subject = KConfiguration.getInstance().getConfiguration().getString(GENERATE_PDF_SUBJECT_KEY, "Download notification");
                    String api =  KConfiguration.getInstance().getConfiguration().getString("api.client.point");
                    String link = String.format("%s/%s/%s", api, "userrequests/userspace", token);
                    StringTemplate template = new StringTemplate(text);
                    template.setAttribute("link", link);
                    serv.sendEmailNotification(administratorEmail, Arrays.asList(email), subject, template.toString());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (OutOfRangeException e) {
            throw new RuntimeException(e);
        }
    }

}
