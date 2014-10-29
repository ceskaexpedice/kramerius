package cz.incad.kramerius.utils.handle;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DisectHandle {
    
    public static Logger LOGGER = Logger.getLogger(DisectHandle.class.getName());
    
    
    public static String disectHandle(String requestURL) {
        //"dvju"
        try {
            StringBuffer buffer = new StringBuffer();
            URL url = new URL(requestURL);
            String path = url.getPath();
            String application = path;
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            if (tokenizer.hasMoreTokens()) {
                application = tokenizer.nextToken();
            }
            String handleServlet = path;
            if (tokenizer.hasMoreTokens()) {
                handleServlet = tokenizer.nextToken();
            }
            // check handle servlet
            while (tokenizer.hasMoreTokens()) {
                buffer.append(tokenizer.nextElement());
                if (tokenizer.hasMoreTokens()) {
                    buffer.append("/");
                }
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no handle>";
        }
    }

}
