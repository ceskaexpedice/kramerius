package cz.incad.Kramerius.backend.guice;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.google.inject.Injector;

/**
 * Base servlet for injecting. 
 * @author pavels
 * TODO: Do it by guice way
 */
public class GuiceServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		super.init();
		Injector injector = getInjector();
		injector.injectMembers(this);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Injector injector = getInjector();
		injector.injectMembers(this);
	}

	protected Injector getInjector() {
		return (Injector) getServletContext().getAttribute(Injector.class.getName());
	}

	
}
