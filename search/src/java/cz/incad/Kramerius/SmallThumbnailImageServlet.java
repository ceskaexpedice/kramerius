package cz.incad.Kramerius;

import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import org.apache.commons.io.IOUtils;
import org.ceskaexpedice.akubra.KnownDatastreams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;

import static cz.incad.utils.IKeys.UUID_PARAMETER;

/**
 * Servlet na ziskavani malych nahledu - filmovy pas
 *
 * @author pavels
 */
public class SmallThumbnailImageServlet extends AbstractImageServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SmallThumbnailImageServlet.class.getName());

    public static final String PAGE_PARAMETER = "page";
    public static final String RAWDATA_PARAMETER = "rawdata";

    private static final String DEFAULT_MIMETYPE = "image/jpeg";

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OutputFormats outputFormat = null;
        String pid = req.getParameter(UUID_PARAMETER);
        // TODO: Change it !!
        pid = akubraRepository.re().getFirstViewablePidInTree(pid);

        String outputFormatParam = req.getParameter(OUTPUT_FORMAT_PARAMETER);
        if (outputFormatParam != null) {
            outputFormat = OutputFormats.valueOf(outputFormatParam);
        }
        try {
            if (outputFormat == null) {
                BufferedImage image = rawThumbnailImage(pid, 0);
                Rectangle rectangle = new Rectangle(image.getWidth(null), image.getHeight(null));
                BufferedImage scale = scale(image, rectangle, req, getScalingMethod());
                if (scale != null) {
                    setDateHaders(pid, FedoraUtils.IMG_THUMB_STREAM, resp);
                    setResponseCode(pid, FedoraUtils.IMG_THUMB_STREAM, req, resp);
                    writeImage(req, resp, scale, OutputFormats.JPEG);
                } else resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                InputStream is = akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_THUMB) ?  akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_THUMB).asInputStream() : null;
                if (outputFormat.equals(OutputFormats.RAW)) {
                    rawContent(req, resp, pid, is);
                } else {
                    BufferedImage rawImage = rawThumbnailImage(pid, 0);
                    setDateHaders(pid, FedoraUtils.IMG_THUMB_STREAM, resp);
                    setResponseCode(pid, FedoraUtils.IMG_THUMB_STREAM, req, resp);
                    writeImage(req, resp, rawImage, outputFormat);
                }
            }
        } catch (SecurityException e) {
            LOGGER.log(Level.INFO, e.getMessage());
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // TODO: Extract to standalone servlet
    public void rawContent(HttpServletRequest req, HttpServletResponse resp, String uuid, InputStream is) throws IOException, XPathExpressionException, SQLException {
        String mimeType = akubraRepository.getDatastreamMetadata(uuid, KnownDatastreams.IMG_THUMB).getMimetype();
        if (mimeType == null) mimeType = DEFAULT_MIMETYPE;
        resp.setContentType(mimeType);
        setDateHaders(uuid, mimeType, resp);
        setResponseCode(uuid, FedoraUtils.IMG_THUMB_STREAM, req, resp);
        IOUtils.copy(is, resp.getOutputStream());
    }

    public SecuredAkubraRepository getAkubraRepository() {
        return akubraRepository;
    }

    public void setAkubraRepository(SecuredAkubraRepository akubraRepository) {
        this.akubraRepository = akubraRepository;
    }


    @Override
    public ScalingMethod getScalingMethod() {
        KConfiguration config = KConfiguration.getInstance();
        ScalingMethod method = ScalingMethod.valueOf(config.getProperty("thumbImage.scalingMethod", "BICUBIC_STEPPED"));
        return method;
    }


    @Override
    public boolean turnOnIterateScaling() {
        KConfiguration config = KConfiguration.getInstance();
        boolean highQuality = config.getConfiguration().getBoolean("thumbImage.iterateScaling", true);
        return highQuality;
    }

}
