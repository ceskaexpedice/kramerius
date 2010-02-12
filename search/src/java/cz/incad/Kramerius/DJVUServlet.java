package cz.incad.Kramerius;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.utils.IKeys.*;
import static cz.incad.kramerius.utils.RESTHelper.*;
import static cz.incad.kramerius.utils.IOUtils.*;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Prepodava DJVU stream
 * @author pavels
 */
public class DJVUServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uuid = req.getParameter(UUID_PARAMETER);
		String djVuImage = getDjVuImage(KConfiguration.getKConfiguration(), uuid);
		InputStream is = inputStream(djVuImage, KConfiguration.getKConfiguration().getFedoraUser(),KConfiguration.getKConfiguration().getFedoraPass());
		resp.setContentType("image/x.djvu");
		copyStreams(is, resp.getOutputStream());
		
	}
	
}
