package cz.incad.Kramerius;

import static cz.incad.kramerius.FedoraNamespaces.*;

import java.io.IOException;
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

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.utils.IKeys;

public class GeneratePDFServlet extends GuiceServlet {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServlet.class.getName());
	
	public static final String UUID_FROM="uuidFrom";
	public static final String UUID_TO="uuidTo";
	public static final String PATH="path";
	
	@Inject
	GeneratePDFService service;
	@Inject
	FedoraAccess fedoraAccess;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			
				resp.setContentType("application/pdf");
				SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
			    resp.setHeader("Content-disposition","attachment; filename="+sdate.format(new Date())+".pdf");
				String from = req.getParameter(UUID_FROM);
				String to = req.getParameter(UUID_TO);
				String path = req.getParameter(PATH);
				List<String> pathList = Arrays.asList(path.split("/"));
				service.dynamicPDFExport(pathList, from, to, from, resp.getOutputStream());

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
	
	
}
