package cz.incad.Kramerius;

import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.service.METSService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class METSServlet extends GuiceServlet {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(METSServlet.class.getName());

    public static final String PID = "pid";

    @Inject
    transient METSService service;
    @Inject
    transient KConfiguration configuration;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException {
        try {
            resp.setContentType("text/xml");
            String pid = req.getParameter(PID);
            service.exportMETS(pid, resp.getOutputStream());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
