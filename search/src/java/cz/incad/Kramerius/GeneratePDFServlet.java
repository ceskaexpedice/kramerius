package cz.incad.Kramerius;

import static cz.incad.kramerius.FedoraNamespaces.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import cz.incad.utils.IKeys;

public class GeneratePDFServlet extends GuiceServlet {

    
    private static final Semaphore PDF_SEMAPHORE = new Semaphore(KConfiguration.getInstance().getConfiguration().getInt("pdfQueue.activeProcess",5));

	private static final long serialVersionUID = 1L;
	
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServlet.class.getName());
	
	public static final String UUID_FROM="uuidFrom";
	public static final String UUID_TO="uuidTo";
    public static final String HOW_MANY="howMany";
	public static final String PATH="path";
	
	@Inject
	GeneratePDFService service;
	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	@Inject
	KConfiguration configuration;
	@Inject
	SolrAccess solrAccess;
	
	
	
	@Override
    public void init() throws ServletException {
        super.init();
	}

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        boolean acquired = false; 
	    try {
	        acquired =  PDF_SEMAPHORE.tryAcquire();
	        if (acquired) {
	            try {
	                renderDynamicPDF(req, resp);
	            } catch (MalformedURLException e) {
	                LOGGER.log(Level.SEVERE, e.getMessage(),e);
	            } catch (IOException e) {
	                LOGGER.log(Level.SEVERE, e.getMessage(),e);
	            } catch (ProcessSubtreeException e) {
	                LOGGER.log(Level.SEVERE, e.getMessage(),e);
	            }
	        } else {
	            try {
	                LOGGER.fine("sending error to client");
                    renderErrorPagePDF(req, resp);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(),e);
                }
	        }

	    } finally {
		    if (acquired) PDF_SEMAPHORE.release();
		}
	}

    private void renderErrorPagePDF(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        RequestDispatcher dispatcher = req.getRequestDispatcher("serverbusy.jsp");
        //req.setAttribute("redirectURL", URLEncoder.encode(req.getParameter(arg0), "UTF-8"));
        dispatcher.forward(req, resp);
    }

    public void renderDynamicPDF(HttpServletRequest req, HttpServletResponse resp) throws MalformedURLException, IOException, ProcessSubtreeException {
        String imgServletUrl = ApplicationURL.applicationURL(req)+"/img";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
        	imgServletUrl = configuration.getApplicationURL()+"img";
        }
        String i18nUrl = ApplicationURL.applicationURL(req)+"/i18n";
        if ((configuration.getApplicationURL() != null) && (!configuration.getApplicationURL().equals(""))){
        	i18nUrl = configuration.getApplicationURL()+"i18n";
        }
        resp.setContentType("application/pdf");
        SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_mmhhss");
        resp.setHeader("Content-disposition","attachment; filename="+sdate.format(new Date())+".pdf");
        String from = req.getParameter(UUID_FROM);
        String howMany = req.getParameter(HOW_MANY);
        
        throw new NotImplementedException("not implemented exception");
        //service.dynamicPDFExport(parentUuid(from), from, Integer.parseInt(howMany), from, resp.getOutputStream(), imgServletUrl, i18nUrl);
    }

    /*
    public String parentUuid(String from) throws IOException {
        String[] pathOfUUIDs = solrAccess.getPath(from);
        if (pathOfUUIDs.length > 1) {
            return pathOfUUIDs[pathOfUUIDs.length-2];
        } else return pathOfUUIDs[pathOfUUIDs.length-1];
    }*/
	
	
}
