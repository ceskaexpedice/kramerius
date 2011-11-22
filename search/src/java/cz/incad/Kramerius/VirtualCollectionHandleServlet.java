package cz.incad.Kramerius;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * This is support for persistent URL
 * @author pavels
 */
public class VirtualCollectionHandleServlet extends GuiceServlet {

    private static final long serialVersionUID = 1L;
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(VirtualCollectionHandleServlet.class.getName());
    @Inject
    transient KConfiguration kConfiguration;

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            String requestURL = req.getRequestURL().toString();
            String collection = disectURL(requestURL);

            String applicationCotext = ApplicationURL.applicationContextPath(req);
            String redirectUrl = "/" + applicationCotext + "/?collection=" + collection;
            resp.sendRedirect(redirectUrl);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);;
            resp.sendError(500);
        }
    }

    private boolean isInUrl(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        return requestURL.contains("/vc/");
    }

    private String disectURL(String requestURL) throws Exception {
        if(requestURL.contains("/vc/")){
            return requestURL.substring(requestURL.indexOf("/vc/") + "/vc/".length());
        }else if(requestURL.contains("/collection/")){
            return requestURL.substring(requestURL.indexOf("/collection/") + "/collection/".length());
            
        }else if(requestURL.contains("/sbirka/")){
            return requestURL.substring(requestURL.indexOf("/sbirka/") + "/sbirka/".length());
        }else{
            throw new Exception("Should not be here");
        }
        

    }
}
