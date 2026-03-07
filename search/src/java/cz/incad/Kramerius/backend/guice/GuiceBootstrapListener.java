package cz.incad.Kramerius.backend.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import cz.incad.Kramerius.statistics.formatters.guice.FormatterModule;
import cz.incad.kramerius.database.guice.DatabaseVersionGuiceModule;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.fedora.RepoSecureModule;
import cz.incad.kramerius.imaging.guice.ImageModule;
import cz.incad.kramerius.pdf.guice.PDFModule;
import cz.incad.kramerius.printing.guice.PrintModule;
import cz.incad.kramerius.processes.guice.ProcessModule;
import cz.incad.kramerius.rest.api.guice.GuiceBootstrap;
import cz.incad.kramerius.rest.api.guice.IiifServletModule;
import cz.incad.kramerius.rest.api.guice.RestBaseModule;
import cz.incad.kramerius.security.guice.GuiceSecurityModule;
import cz.incad.kramerius.security.impl.http.GuiceSecurityHTTPModule;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.service.guice.MailModule;
import cz.incad.kramerius.service.guice.ServicesModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.users.guice.LoggedUsersModule;
import cz.incad.kramerius.workmode.guice.WorkModeModule;
import cz.inovatika.dochub.guice.DocHubModule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * GuiceBootstrapListener
 * @author ppodsednik
 */
public class GuiceBootstrapListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {

        ServletContext ctx = event.getServletContext();

        Injector injector = Guice.createInjector(
                new ServletModule(),
                new RepoModule(),
                new RepoSecureModule(),
                new WorkModeModule(),

                new SolrModule(),
                new BaseModule(), // base  module
                new RestBaseModule(), // base  module
                new ServicesModule(), // base services
                new PDFModule(), // pdf services
                new ImageModule(), // images
                new I18NModule(), // i18n module
                new LoggedUsersModule(), new MailModule(), // mail service

                new DocHubModule(),

                new DocumentServiceModule(),
                new GuiceSecurityModule(),
                new GuiceSecurityHTTPModule(),
                new ProcessModule(), // for long running processes

                new PrintModule(), // printing
                new DatabaseVersionGuiceModule(), // db versions
                new FormatterModule() // statistics formatters
        );

        // store globally
        GuiceBootstrap.setInjector(injector);

        // store for servlets
        ctx.setAttribute(Injector.class.getName(), injector);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}