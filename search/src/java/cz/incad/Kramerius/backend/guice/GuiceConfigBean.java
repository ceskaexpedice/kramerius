package cz.incad.Kramerius.backend.guice;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

import cz.incad.Kramerius.security.GuiceSecurityModule;
import cz.incad.kramerius.security.impl.http.GuiceSecurityHTTPModule;

public class GuiceConfigBean extends GuiceServletContextListener {

	public GuiceConfigBean() {
		super();
	}

	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
	    String realPath = servletContextEvent.getServletContext().getRealPath("WEB-INF/lib");
		System.setProperty(LongRunningProcessModul.DEFAULT_LIBS_KEY, realPath);
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(new BaseModule(), // base module
		                                            
		                                            new cz.incad.kramerius.security.guice.GuiceSecurityModule(),
		                                            new GuiceSecurityHTTPModule(),
		                                            
		                                            new LongRunningProcessModul(), // for long running processes
		                                            new ServletModule());

		return injector;
	}

    class Grapher {
//        private void graph(String filename, Injector demoInjector) throws IOException {
//          PrintWriter out = new PrintWriter(new File(filename), "UTF-8");
//
//          Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
//          GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
//          renderer.setOut(out).setRankdir("TB");
//
//          injector.getInstance(InjectorGrapher.class)
//              .of(demoInjector)
//              .graph();
//        }
      }

}
