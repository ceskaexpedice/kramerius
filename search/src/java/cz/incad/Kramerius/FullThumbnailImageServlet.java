package cz.incad.Kramerius;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;
import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import cz.incad.Kramerius.AbstractImageServlet.OutputFormats;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

public class FullThumbnailImageServlet extends AbstractImageServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FullThumbnailImageServlet.class.getName());
    
	@Override
	public ScalingMethod getScalingMethod() {
		KConfiguration config = KConfiguration.getInstance();
		ScalingMethod method = ScalingMethod.valueOf(config.getProperty(
				"fullThumbnail.scalingMethod", "BICUBIC_STEPPED"));
		return method;
	}

	@Override
	public boolean turnOnIterateScaling() {
		KConfiguration config = KConfiguration.getInstance();
		boolean highQuality = config.getConfiguration().getBoolean(
				"fullThumbnail.iterateScaling", true);
		return highQuality;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	    try {
	        String uuid = req.getParameter(UUID_PARAMETER);
	        if (fedoraAccess.isFullthumbnailAvailable(uuid)) {
	            String mimeType = this.fedoraAccess.getFullThumbnailMimeType(uuid);
	            resp.setContentType(mimeType);
	            setDateHaders(uuid, resp);
	            setResponseCode(uuid, req, resp);
	            copyStreams(fedoraAccess.getFullThumbnail(uuid), resp.getOutputStream());
	        } else {
	            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
	        }
        } catch (XPathExpressionException e) {
            LOGGER.severe(e.getLocalizedMessage());
        }
	}
}
