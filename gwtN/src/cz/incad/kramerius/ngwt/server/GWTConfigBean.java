package cz.incad.kramerius.ngwt.server;

import javax.servlet.ServletContextEvent;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;


public class GWTConfigBean extends GuiceServletContextListener {



	@Override
	protected Injector getInjector() {
		Injector injector = Guice.createInjector(new GwtModule(),new ServletModule());
	    return injector;
	}

}
