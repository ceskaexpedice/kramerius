package cz.incad.kramerius.client;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;

import cz.incad.kramerius.client.utils.RedirectHelp;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class PrintPartFunctionality extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(PrintPartFunctionality.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String k4host = RedirectHelp.redirectApplication(req)+"search/";
            if (!k4host.endsWith("/")) { k4host = k4host + "/"; }
            String xpos = req.getParameter("xpos");
            String ypos = req.getParameter("ypos");
            String width = req.getParameter("width");
            String height = req.getParameter("height");
            String pid = URLEncoder.encode(req.getParameter("pid"), "UTF-8");
            
            k4host = k4host + "localPrintPDF?pids="+pid+"&pagesize=A4&imgop=CUT&xpos="+xpos+"&ypos="+ypos+"&width="+width+"&height="+height;
            resp.sendRedirect(k4host);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        
    }
}
