package cz.incad.kramerius.client;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Principal;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HeaderPropertiesServlet extends HttpServlet {

    
    public static final Logger LOGGER = Logger.getLogger(HeaderPropertiesServlet.class.getName());
    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOGGER.info(" header properties ");
                
        resp.setContentType("text/plain");
        StringWriter writer = new StringWriter();
        writer.write(">>> Tak nevim <<< \n");
        
        Enumeration nms = req.getHeaderNames();
        while(nms.hasMoreElements()) {
            String key = nms.nextElement().toString();
            writer.write(key+":"+req.getHeader(key));
            writer.write("\n");
        }
        
        writer.write(req.getRemoteUser()); writer.write("\n");
        
        Principal principal = req.getUserPrincipal();
        if (principal != null) {
            writer.write("principal:"+principal.toString()); writer.write("\n");
        }
        
        resp.getWriter().write(writer.toString());
    }

}
