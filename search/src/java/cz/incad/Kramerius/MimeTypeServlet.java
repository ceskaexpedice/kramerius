package cz.incad.Kramerius;

import com.google.inject.Inject;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ceskaexpedice.akubra.KnownDatastreams;

import java.io.IOException;
import java.util.logging.Level;

import static cz.incad.utils.IKeys.PID_PARAMETER;
import static cz.incad.utils.IKeys.UUID_PARAMETER;

public class MimeTypeServlet extends GuiceServlet {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(MimeTypeServlet.class.getName());
	
	@Inject
	SecuredAkubraRepository akubraRepository;

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
