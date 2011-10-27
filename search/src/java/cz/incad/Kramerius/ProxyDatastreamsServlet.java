package cz.incad.Kramerius;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;
import static cz.incad.utils.IKeys.*;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;

public class ProxyDatastreamsServlet extends GuiceServlet {

	private static final long serialVersionUID = 1L;

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ProxyDatastreamsServlet.class.getName());
	
	@Inject
	@Named("securedFedoraAccess")
	private transient FedoraAccess fedoraAccess;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String pid = req.getParameter(PID_PARAMETER);
		String dsName = req.getParameter(DS_NAME);
		checkNull(PID_PARAMETER, pid, resp);
		checkNull(DS_NAME, dsName, resp);
		String mimeType = this.fedoraAccess.getMimeTypeForStream(pid, dsName);
		InputStream is = this.fedoraAccess.getDataStream(pid, dsName);
		resp.setContentType(mimeType);
		copyStreams(is, resp.getOutputStream());
	}

	private void checkNull(String paramName, String param, HttpServletResponse resp) throws IOException {
		if ((param == null) || (param.trim().equals(""))) {
			LOGGER.severe("missing parameter '"+paramName+"'");
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

}
