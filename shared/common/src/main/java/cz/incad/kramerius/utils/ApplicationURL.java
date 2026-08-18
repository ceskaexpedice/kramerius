package cz.incad.kramerius.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.utils.conf.KConfiguration;



/**
 * Helper class for determining application URL. 
 * @author pavels
 */
public class ApplicationURL {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(ApplicationURL.class.getName());

	
	/**
	 * Returns protocol, server and port 
	 * @param request Processing request
	 * @return
	 */
    public static String getServerAndPort(HttpServletRequest request) {
        try {
            String string = request.getRequestURL().toString();
            URL url = new URL(string);
            return url.getProtocol()+"://"+url.getHost()+extractPort(url);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no url>";
        }

    }

	/**
	 * Returns full application URL
	 * @param request HTTP Request
	 * @return full application URL
	 */
    public static String applicationURL(HttpServletRequest request) {
        try {
            String url = request.getRequestURL().toString();
            String header = firstHeaderValue(request.getHeader("x-forwarded-host"));
            if (header != null) {
                String requestUri = request.getRequestURI();
                String protocol = forwardedProtocol(request);
                String port = firstHeaderValue(request.getHeader("x-forwarded-port"));
                url = createURL(hostWithForwardedPort(header, protocol, port), protocol, requestUri);
            } else if (request.getHeader("x-forwarded-proto") != null) {
                URL requestUrl = new URL(url);
                url = createURL(requestUrl.getAuthority(), forwardedProtocol(request), request.getRequestURI());
            }
            return applicationURL(url);
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no url>";
        }
    }

    static String applicationURL(String string) throws MalformedURLException {
        URL url = new URL(string);
        String application = applicationContextPath(url);
        String port = extractPort(url);
        String aURL = url.getProtocol() + "://" + url.getHost() + port + "/" + application;
        return aURL;
    }

	/**
	 * Extracts port from given url or return empty string (if port is < 0)
	 * @param url
	 * @return
	 */
    public static String extractPort(URL url) {
        if (url.getPort() > 0) {
            return ":"+url.getPort();
        } else return "";
    }


    
    /**
     * Returns application context from given url
     * @param url Processing url
     * @return
     */
    public static String applicationContextPath(URL url) {
        String path = url.getPath();
        String application = path;
        StringTokenizer tokenizer = new StringTokenizer(path,"/");
        if (tokenizer.hasMoreTokens()) application = tokenizer.nextToken();
        return application;
    }
    
    /**
     * Returns application context from given reqest
     * @param request Processing request
     * @return
     */
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

    public static String urlFromRequest(HttpServletRequest httpReq) throws MalformedURLException {
        String header = firstHeaderValue(httpReq.getHeader("x-forwarded-host"));
        if (header != null) {
            String requestUri = httpReq.getRequestURI();
            String protocol = forwardedProtocol(httpReq);
            String port = firstHeaderValue(httpReq.getHeader("x-forwarded-port"));
            String createdURL = createURL(hostWithForwardedPort(header, protocol, port), protocol, requestUri);
            return createdURL;
        } else {
            String string = httpReq.getRequestURL().toString();
            return string;
        }
    }
    
    static String createURL(String headerField, String protocol, String requestUri) {
        if (headerField.startsWith("/")) headerField = headerField.substring(1);
        if (headerField.endsWith("/")) headerField = headerField.substring(0, headerField.lastIndexOf('/'));
        if (requestUri.startsWith("/")) {
            requestUri = requestUri.substring(1);
        }
        String urlString = protocol+"://"+headerField+"/"+requestUri;
        return urlString;
    }

    private static String forwardedProtocol(HttpServletRequest request) throws MalformedURLException {
        String forwardedProto = firstHeaderValue(request.getHeader("x-forwarded-proto"));
        if (forwardedProto != null) {
            return forwardedProto.toLowerCase();
        }
        return new URL(request.getRequestURL().toString()).getProtocol();
    }

    private static String firstHeaderValue(String header) {
        if (header == null) {
            return null;
        }
        String first = header.split(",", 2)[0].trim();
        if (header.contains(",")) {
            LOGGER.fine(String.format("Forwarded header contains more than one value: %s. Picking up the first one",
                    Arrays.toString(header.split(","))));
        }
        return first.isEmpty() ? null : first;
    }

    private static String hostWithForwardedPort(String host, String protocol, String port) {
        if (port == null || port.isBlank() || hostContainsPort(host) || isDefaultPort(protocol, port)) {
            return host;
        }
        return host + ":" + port;
    }

    private static boolean hostContainsPort(String host) {
        return host.matches("^\\[[^]]+]:\\d+$") || (!host.startsWith("[") && host.lastIndexOf(':') > host.lastIndexOf(']'));
    }

    private static boolean isDefaultPort(String protocol, String port) {
        return ("http".equalsIgnoreCase(protocol) && "80".equals(port))
                || ("https".equalsIgnoreCase(protocol) && "443".equals(port));
    }

}
