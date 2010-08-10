package cz.incad.Kramerius;

import static cz.incad.utils.IKeys.*;
import static cz.incad.kramerius.utils.RESTHelper.*;
import static cz.incad.kramerius.utils.FedoraUtils.*;
import static cz.incad.kramerius.utils.IOUtils.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
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
import cz.incad.Kramerius.backend.guice.RequestIPaddressChecker;
import cz.incad.Kramerius.views.ApplicationURL;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Prepodava DJVU stream
 * @author pavels
 */
public class FullImageServlet extends AbstracThumbnailServlet {

	public static final String DEFAULT_MIMETYPE = "image/x.djvu";
	public static final String IMAGE_TYPE="imageType"; 
	public static final String PAGE="page";
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FullImageServlet.class.getName());

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		OutputFormats outputFormat = null;
		String uuid = req.getParameter(UUID_PARAMETER);
		String outputFormatParam = req.getParameter(OUTPUT_FORMAT_PARAMETER);
		if (outputFormatParam != null) {
			outputFormat= OutputFormats.valueOf(outputFormatParam);
		}
		int page = 0;
		String spage = req.getParameter(PAGE);
		if (spage != null) {
			page = Integer.parseInt(spage);
		}
		
		String imageType = req.getParameter(IMAGE_TYPE);
		try {
			// dotaz na image type
			if (imageType != null) {
				String type = this.fedoraAccess.getImageFULLMimeType(uuid);
				//resp.setContentType("plain/text");
				resp.getWriter().print(type);
			// pozadavek na zmenseni (prsou?)
			} else if (outputFormat == null) {
				Image image = rawFullImage(uuid, req, page);
				Rectangle rectangle = new Rectangle(image.getWidth(null), image.getHeight(null));
				Image scale = scale(image, rectangle, req);
				if (scale != null) {
					writeImage(resp, scale, OutputFormats.JPEG);
				} else resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			// transformace	
			} else {

				InputStream is = this.fedoraAccess.getImageFULL(uuid);
				if (outputFormat.equals(OutputFormats.RAW)) {
					String mimeType = this.fedoraAccess.getImageFULLMimeType(uuid);
					if (mimeType == null) mimeType = DEFAULT_MIMETYPE;
					resp.setContentType(mimeType);
					copyStreams(is, resp.getOutputStream());
				} else {
					Image rawImage = rawFullImage(uuid, req, page);
					writeImage(resp, rawImage, outputFormat);
				}
			}
		} catch(SecurityException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	
	public static String fullImageServlet(HttpServletRequest request) {
		return ApplicationURL.urlOfPath(request, InternalConfiguration.get().getProperties().getProperty("servlets.mapping.fullImage"));
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
