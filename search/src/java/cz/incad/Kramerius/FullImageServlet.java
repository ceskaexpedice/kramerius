package cz.incad.Kramerius;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.utils.IKeys.*;
import static cz.incad.kramerius.utils.RESTHelper.*;
import static cz.incad.kramerius.utils.IOUtils.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Prepodava DJVU stream
 * @author pavels
 */
public class FullImageServlet extends AbstracThumbnailServlet {

	private static final String DEFAULT_MIMETYPE = "image/x.djvu";
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FullImageServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		OutputFormats outputFormat = null;
		String uuid = req.getParameter(UUID_PARAMETER);
		String outputFormatParam = req.getParameter(OUTPUT_FORMAT_PARAMETER);
		if (outputFormatParam != null) {
			outputFormat= OutputFormats.valueOf(outputFormatParam);
		}
		try {
			if (outputFormat == null) {
				Image image = rawFullImage(uuid);
				Rectangle rectangle = new Rectangle(image.getWidth(null), image.getHeight(null));
				Image scale = scale(image, rectangle, req);
				if (scale != null) {
					writeImage(resp, scale, OutputFormats.JPEG);
				} else resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				InputStream is = this.fedoraAccess.getImageFULL(uuid);
				if (outputFormat.equals(OutputFormats.RAW)) {
					String mimeType = this.fedoraAccess.getImageFULLMimeType(uuid);
					if (mimeType == null) mimeType = DEFAULT_MIMETYPE;
					resp.setContentType(mimeType);
					copyStreams(is, resp.getOutputStream());
				} else {
					Image rawImage = rawFullImage(uuid);
					writeImage(resp, rawImage, outputFormat);
				}
			}
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
	

//	static class XPATHFedoraNamespaceContext implements NamespaceContext {
//
//		static Map<String, String> MAPPING = new HashMap<String, String>();
//		{
//			MAPPING.put("", FedoraNamespaces.DC_NAMESPACE_URI);
//			MAPPING.put("", FedoraNamespaces.DC_NAMESPACE_URI);
//		}
//		@Override
//		public String getNamespaceURI(String arg0) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public String getPrefix(String arg0) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Iterator getPrefixes(String arg0) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//	}
}
