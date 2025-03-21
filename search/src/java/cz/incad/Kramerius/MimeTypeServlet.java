package cz.incad.Kramerius;

import static cz.incad.utils.IKeys.PID_PARAMETER;
import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.KnownDatastreams;

public class MimeTypeServlet extends GuiceServlet {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(MimeTypeServlet.class.getName());
	
	@Inject
	@Named("securedAkubraAccess")
	AkubraRepository akubraRepository;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
	        String pid = req.getParameter(UUID_PARAMETER);
	        if (pid == null || pid.trim().equals("")) {
	            pid = req.getParameter(PID_PARAMETER);
	        }
			if ((pid != null) && (!pid.equals(""))) {
				String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
				resp.getWriter().println(mimeType);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

}
