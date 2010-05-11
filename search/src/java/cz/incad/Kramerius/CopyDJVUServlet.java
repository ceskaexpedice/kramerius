package cz.incad.Kramerius;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;
import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.servlet.GuiceFilter;

import cz.incad.Kramerius.AbstracThumbnailServlet.OutputFormats;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;

/**
 * FEdora vyzaduje autentizaci, knihovna djvu zase nepodporuje.  
 * Tento servlet uzmozni pristup na djvu obrazky bez nutnosti se autentizovat. Pristup je mozny jenom z localhostu
 * @author pavels
 *
 */
public class CopyDJVUServlet extends GuiceServlet {
	
	@Inject
	@Named("securedFedoraAccess")
	private FedoraAccess fedoraAccess;

	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String uuid = req.getParameter(UUID_PARAMETER);
			String mimeType = this.fedoraAccess.getImageFULLMimeType(uuid);
			if (mimeType == null) mimeType = FullImageServlet.DEFAULT_MIMETYPE;
			InputStream is = this.fedoraAccess.getImageFULL(uuid);
			resp.setContentType(mimeType);
			copyStreams(is, resp.getOutputStream());
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}

	
}
