package cz.incad.Kramerius;

import java.awt.Desktop.Action;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.views.ApplicationURL;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;

/**
 * This servlet produces bundles as properties or as xml via http protocol. <br>
 * This is useful for xslt transformations
 * @author pavels
 */
public class I18NServlet extends GuiceServlet {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(I18NServlet.class.getName());
	
	@Inject
	TextsService textsService;
	@Inject
	ResourceBundleService resourceBundleService;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String action = req.getParameter("action");
		if (action == null) action = Actions.text.name();
		Actions selectedAction = Actions.valueOf(action);
		selectedAction.doAction(getServletContext(), req, resp, this.textsService, this.resourceBundleService);
	}

	public static String i18nServlet(HttpServletRequest request) {
		return ApplicationURL.urlOfPath(request, InternalConfiguration.get().getProperties().getProperty("servlets.mapping.i18n"));
	}

	static enum Actions {
	

		text {
			@Override
			public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TextsService tserv, ResourceBundleService rserv) {
				try {
					String parameter = req.getParameter("name");
					Locale locale = locale(req);
					String text = tserv.getText(parameter, locale);
					StringBuffer formatBundle = formatText(text, parameter);
					resp.setContentType("application/xhtml+xml");
					resp.setCharacterEncoding("UTF-8");
					resp.getWriter().write(formatBundle.toString());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					try {
						resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					} catch (IOException e1) {}
				}
			}

		}, 
		bundle {

			@Override
			public void doAction(ServletContext context, HttpServletRequest req, HttpServletResponse resp, TextsService tserv, ResourceBundleService rserv) {
				try {
					String parameter = req.getParameter("name");
					Locale locale = locale(req);
					ResourceBundle resourceBundle = rserv.getResourceBundle(parameter, locale);
					StringBuffer formatBundle = formatBundle(resourceBundle, parameter);
					resp.setContentType("application/xhtml+xml");
					resp.setCharacterEncoding("UTF-8");
					resp.getWriter().write(formatBundle.toString());
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					try {
						resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					} catch (IOException e1) {}
				}
				
			}
			
		};

		abstract void doAction(ServletContext context,  HttpServletRequest req, HttpServletResponse resp,TextsService tserv, ResourceBundleService rserv);
		

		static Locale locale(HttpServletRequest req) {
			String lang = req.getParameter("lang");
			String country = req.getParameter("country");
			Locale locale = new Locale(lang, country);
			return locale;
		}
		
		static StringBuffer formatBundle(ResourceBundle bundle, String bundleName) {
			StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			buffer.append("<bundle name='").append(bundleName).append("'>\n");
			Set<String> keySet = bundle.keySet();
			for (String key : keySet) {
				buffer.append("<value key='"+key+"'>").append(bundle.getString(key)).append("</value>");
			}
			buffer.append("\n</bundle>");
			return buffer;
		}
		
		static StringBuffer formatText(String text, String textName) {
			StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			buffer.append("<text name='").append(textName).append("'>\n");
			buffer.append(text);
			buffer.append("\n</text>");
			return buffer;
		}
		
	}
}
