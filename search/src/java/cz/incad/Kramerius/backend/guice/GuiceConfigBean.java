package cz.incad.Kramerius.backend.guice;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import cz.incad.kramerius.database.guice.DatabaseVersionGuiceModule;
import cz.incad.kramerius.document.guice.DocumentServiceModule;
import cz.incad.kramerius.imaging.guice.ImageModule;
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

                new ImageModule(), // images
                new I18NModule(), // i18n module
                new LoggedUsersModule(), new MailModule(), // mail service
                                                           // module

                new DocumentServiceModule(),
                new GuiceSecurityModule(), 
                new GuiceSecurityHTTPModule(),
                new LongRunningProcessModule(), // for long running processes

                new PrintModule(),
                new DatabaseVersionGuiceModule(), // db versions
                new ServletModule());

        
        return injector;
    }

    public static class Grapher {

        public void graph(String filename, Injector demoInjector) throws IOException {
        
            PrintWriter out = new PrintWriter(new File(filename), "UTF-8");
            Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
            GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);

            renderer.setOut(out).setRankdir("TB");

            injector.getInstance(InjectorGrapher.class)
              .of(demoInjector)
              .graph();
            }
      }

}
