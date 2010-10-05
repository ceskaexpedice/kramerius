package cz.incad.Kramerius.views.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;

public class HelpViewObject {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(HelpViewObject.class.getName());
    
    @Inject
    ServletContext servletContext;

    @Inject
    HttpServletRequest request;
    
    @Inject
    Provider<Locale> localeProvider;

    @Inject
    TextsService textsService;
    
    @Inject
    HttpServletResponse response;
    
    public HelpViewObject() {
    }
    
    public boolean getTextAccessible() {
        LOGGER.info("getTextAccessible");
        boolean available = textsService.isAvailable("help", localeProvider.get());
        LOGGER.info("text is available :"+available);
        return available;
    }
    
    public String getText() {
        try {
            return textsService.getText("help", localeProvider.get());
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return "";
        }
    }
    
    public void redirectToDefault() {
        try {
            if (this.localeProvider.get().getLanguage().equals("cs")) {
                this.response.sendRedirect("cs.jsp");
            } else {
                this.response.sendRedirect("en.jsp");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    
    public String getRevision() {
    	try {
			InputStream revisions = this.getClass().getClassLoader().getResourceAsStream("revision.properties");
			if (revisions != null) {
				Properties props = new Properties();
				props.load(revisions);
				return props.getProperty("revision");
			} else return "";
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
			return "";
		}
    }
}
