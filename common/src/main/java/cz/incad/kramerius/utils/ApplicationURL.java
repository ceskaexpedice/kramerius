package cz.incad.kramerius.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class ApplicationURL {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ApplicationURL.class.getName());
	
	public static String getServerAndPort(HttpServletRequest request) {
        try {
            String string = request.getRequestURL().toString();
            URL url = new URL(string);
            return url.getProtocol()+"://"+url.getHost()+":"+url.getPort();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no url>";
        }
	    
	}

	
	public static String applicationURL(HttpServletRequest request) {
		//"dvju"
		try {
			String string = request.getRequestURL().toString();
			URL url = new URL(string);
			String application = applicationContextPath(url);
			String aURL = url.getProtocol()+"://"+url.getHost()+":"+url.getPort()+"/"+application;
			return aURL;
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "<no url>";
		}

	}

    public static String applicationContextPath(URL url) {
        String path = url.getPath();
        String application = path;
        StringTokenizer tokenizer = new StringTokenizer(path,"/");
        if (tokenizer.hasMoreTokens()) application = tokenizer.nextToken();
        return application;
    }
    public static String applicationContextPath(HttpServletRequest request) {
        try {
            String string = request.getRequestURL().toString();
            URL url = new URL(string);
            return applicationContextPath(url);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no url>";
        }
    }

	public static String urlOfPath(HttpServletRequest request,  String path) {
		KConfiguration conf = KConfiguration.getInstance();
		if ((conf.getApplicationURL() != null) && (!conf.getApplicationURL().equals(""))) {
			return conf.getApplicationURL() +path;
		} else {
			return applicationURL(request)+"/"+path;
		}
	}
}
