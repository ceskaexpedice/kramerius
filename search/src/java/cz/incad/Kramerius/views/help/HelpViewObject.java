package cz.incad.Kramerius.views.help;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import cz.incad.kramerius.utils.IOUtils;

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
        boolean available = textsService.isAvailable("help", localeProvider.get());
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
    
    public String getVersion() {
        try {
            InputStream revisions = getInputStream();
            if (revisions != null) {
                Properties props = new Properties();
                props.load(revisions);
                return props.getProperty("version");
            } else return "";
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return "";
        }
    }

    public InputStream getInputStream() throws IOException {
        InputStream revisions = this.getClass().getClassLoader().getResourceAsStream("build.properties");
        if (revisions != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copyStreams(revisions,true, bos, false);
            return new ByteArrayInputStream(bos.toByteArray());
        } else return null;
    }
    
    public String getRevision() {
    	try {
            InputStream revisions = getInputStream();
			if (revisions != null) {
				Properties props = new Properties();
				props.load(revisions);
				return props.getProperty("hash");
			} else return "";
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
			return "";
		}
    }
}
