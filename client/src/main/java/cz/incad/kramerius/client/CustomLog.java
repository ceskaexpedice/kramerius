package cz.incad.kramerius.client;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CustomLog extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(CustomLog.class.getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        String parameter = req.getParameter("message");
        LOGGER.info(parameter);
        resp.getWriter().println("");
    }

    
}
