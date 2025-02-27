package cz.incad.Kramerius;
import static cz.incad.kramerius.utils.IOUtils.*;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;

import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.lp.GenerateThumbnail;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import org.ceskaexpedice.akubra.KnownDatastreams;

public class FullThumbnailImageServlet extends AbstractImageServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FullThumbnailImageServlet.class.getName());
    
    @Inject
    DeepZoomTileSupport tileSupport;

    
    
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
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	    try {
            String pid = req.getParameter(UUID_PARAMETER);
                
            if (akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW)) {
                String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_PREVIEW).getMimetype();
                resp.setContentType(mimeType);
                setDateHaders(pid,FedoraUtils.IMG_PREVIEW_STREAM, resp);
                setResponseCode(pid,FedoraUtils.IMG_PREVIEW_STREAM, req, resp);
                copyStreams(akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_PREVIEW).asInputStream(), resp.getOutputStream());
            } else {
                // TODO AK_NEW
                boolean accessible = true;
                if (accessible) {
                    BufferedImage scaled = GenerateThumbnail.scaleToFullThumb(pid, akubraRepository, tileSupport);
                    resp.setContentType(ImageMimeType.JPEG.getValue());
                    setDateHaders(pid, FedoraUtils.IMG_PREVIEW_STREAM, resp);
                    setResponseCode(pid, FedoraUtils.IMG_PREVIEW_STREAM, req, resp);
                    KrameriusImageSupport.writeImageToStream(scaled, "jpeg", resp.getOutputStream());
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }

        } catch (XPathExpressionException e) {
            LOGGER.severe(e.getLocalizedMessage());
        } catch(SecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
	}
}
