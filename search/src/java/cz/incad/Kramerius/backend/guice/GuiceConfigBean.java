package cz.incad.Kramerius.backend.guice;

import java.util.Enumeration;

import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceConfigBean extends GuiceServletContextListener {

	public GuiceConfigBean() {
		super();
	}

	
	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		String realPath = servletContextEvent.getServletContext().getRealPath("WEB-INF/lib");
		System.setProperty(LongRunninProcessModul.DEFAULT_LIBS_KEY, realPath);
		super.contextInitialized(servletContextEvent);
	}

	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(new BaseModule(), new LongRunninProcessModul());
	    return injector;
	}
}
