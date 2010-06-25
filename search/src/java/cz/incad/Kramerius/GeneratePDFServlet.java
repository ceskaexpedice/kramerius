package cz.incad.Kramerius;

import static cz.incad.kramerius.FedoraNamespaces.*;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.views.ApplicationURL;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.utils.IKeys;

public class GeneratePDFServlet extends GuiceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServlet.class.getName());
	
	public static final String UUID_FROM="uuidFrom";
	public static final String UUID_TO="uuidTo";
	public static final String PATH="path";
	
	@Inject
	transient GeneratePDFService service;
	@Inject
	@Named("securedFedoraAccess")
	transient FedoraAccess fedoraAccess;
	@Inject
	transient KConfiguration configuration;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
				URL url = new URL(req.getRequestURL().toString());
				//TODO: Najit context.. jde to.
				String djvuUrl = ApplicationURL.applicationURL(req)+"/djvu";
				if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
					djvuUrl = configuration.getApplicationURL()+"djvu";
				}
				String i18nUrl = ApplicationURL.applicationURL(req)+"/i18n";
				if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
					i18nUrl = configuration.getApplicationURL()+"i18n";
				}
				resp.setContentType("application/pdf");
				SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
			    resp.setHeader("Content-disposition","attachment; filename="+sdate.format(new Date())+".pdf");
				String from = req.getParameter(UUID_FROM);
				String to = req.getParameter(UUID_TO);
				String path = req.getParameter(PATH);
				List<String> pathList = Arrays.asList(path.split("/"));
				service.dynamicPDFExport(pathList, from, to, from, resp.getOutputStream(), djvuUrl,i18nUrl);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	
}
