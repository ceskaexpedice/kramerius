package cz.incad.Kramerius;
import static cz.incad.kramerius.utils.IOUtils.*;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.AbstractImageServlet.OutputFormats;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.lp.GenerateThumbnail;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import cz.incad.kramerius.utils.solr.SolrUtils;

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
                
            if (fedoraAccess.isFullthumbnailAvailable(pid)) {
                String mimeType = this.fedoraAccess.getFullThumbnailMimeType(pid);
                resp.setContentType(mimeType);
                setDateHaders(pid,FedoraUtils.IMG_PREVIEW_STREAM, resp);
                setResponseCode(pid,FedoraUtils.IMG_PREVIEW_STREAM, req, resp);
                copyStreams(fedoraAccess.getFullThumbnail(pid), resp.getOutputStream());
            } else {
                if (fedoraAccess.isContentAccessible(pid)) {
                    BufferedImage scaled = GenerateThumbnail.scaleToFullThumb(pid, fedoraAccess, tileSupport);
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
