package cz.incad.Kramerius.backend.guice;

import java.io.File;

import javax.portlet.ProcessAction;
import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import cz.incad.Kramerius.exts.menu.main.guice.MenuConfiguration;
import cz.incad.kramerius.Constants;
import cz.incad.kramerius.database.guice.DatabaseVersionGuiceModule;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.imaging.guice.ImageModule;
import cz.incad.kramerius.pdf.guice.PDFModule;
import cz.incad.kramerius.printing.guice.PrintModule;
import cz.incad.kramerius.processes.guice.LongRunningProcessModule;
import cz.incad.kramerius.security.guice.GuiceSecurityModule;
import cz.incad.kramerius.security.impl.http.GuiceSecurityHTTPModule;
import cz.incad.kramerius.service.guice.I18NModule;
import cz.incad.kramerius.service.guice.MailModule;
import cz.incad.kramerius.users.guice.LoggedUsersModule;

public class GuiceConfigBean extends GuiceServletContextListener {

    public GuiceConfigBean() {
        super();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String realPath = servletContextEvent.getServletContext().getRealPath("WEB-INF/lib");
        System.setProperty(LongRunningProcessModule.DEFAULT_LIBS_KEY, realPath);
        super.contextInitialized(servletContextEvent);
    }

    @Override
    protected Injector getInjector() {
        Injector injector = Guice.createInjector(
                new BaseModule(), // base  module
                
                new PDFModule(), // pdf services
                
                new ImageModule(), // images
                new I18NModule(), // i18n module
                new LoggedUsersModule(), new MailModule(), // mail service
                                                           // module

                new DocumentServiceModule(),
                new GuiceSecurityModule(), 
                new GuiceSecurityHTTPModule(),
                new LongRunningProcessModule(), // for long running processes

                new PrintModule(), // printing
                new DatabaseVersionGuiceModule(), // db versions
                
                new MenuConfiguration(), // menu modules
                
                new ServletModule()
                
        );

        
        return injector;
    }


    @Provides
    @Named("fontsDir")
    public File getWebAppsFontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        return new File(dirName);
    }
}
