package cz.incad.kramerius.client.forward;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import static cz.incad.utils.StringUtils.*;



public class DefaultModify implements URLPathModify{
    
    public static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DefaultModify.class.getName());
    
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

    
    @Override
    public String modifyPath(String path, HttpServletRequest request) {
        String appContextPath = applicationContextPath(request);
        return minus(path, appContextPath);
    }

    @Override
    public String modifyQuery(String query, HttpServletRequest request) {
        return query;
    }
}
