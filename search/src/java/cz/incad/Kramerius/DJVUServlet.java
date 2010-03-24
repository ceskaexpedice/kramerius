package cz.incad.Kramerius;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.utils.IKeys.*;
import static cz.incad.kramerius.utils.RESTHelper.*;
import static cz.incad.kramerius.utils.IOUtils.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Prepodava DJVU stream
 * @author pavels
 */
public class DJVUServlet extends GuiceServlet {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(DJVUServlet.class.getName());
	
	@Inject
	FedoraAccess fedoraAccess;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uuid = req.getParameter(UUID_PARAMETER);
		try {
			InputStream is = this.fedoraAccess.getDJVU(uuid);
			resp.setContentType("image/x.djvu");
			copyStreams(is, resp.getOutputStream());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}
	
	
}
