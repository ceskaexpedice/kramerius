package cz.incad.kramerius.rest.api.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DisectZoom {

    public static final Logger LOGGER = Logger.getLogger(DisectZoom.class.getName());
    private DisectZoom() {}

    public static String disectZoom(String requestURL) {
        try {
            StringBuffer buffer = new StringBuffer();
            URL url = new URL(requestURL);
            String path = url.getPath();
            String application = path;
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            if (tokenizer.hasMoreTokens())
                application = tokenizer.nextToken();
            String zoomServlet = path;
            if (tokenizer.hasMoreTokens())
                zoomServlet = tokenizer.nextToken();
            // check handle servlet
            while (tokenizer.hasMoreTokens()) {
                buffer.append(tokenizer.nextElement());
                if (tokenizer.hasMoreTokens())
                    buffer.append("/");
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no handle>";
        }
    }
	
}
