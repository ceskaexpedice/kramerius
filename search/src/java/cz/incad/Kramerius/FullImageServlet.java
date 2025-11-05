package cz.incad.Kramerius;

import static cz.incad.utils.IKeys.*;
import static cz.incad.kramerius.utils.IOUtils.*;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.ceskaexpedice.akubra.KnownDatastreams;
import org.w3c.dom.Document;

import com.google.inject.Inject;

import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

/**
 * Prepodava DJVU stream
 * 
 * @author pavels
 */
public class FullImageServlet extends AbstractImageServlet {

    public static final String DEFAULT_MIMETYPE = "image/x.djvu";
    public static final String IMAGE_TYPE = "imageType";
    public static final String PAGE = "page";

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FullImageServlet.class.getName());

    @Inject
    DeepZoomCacheService cacheService;
    @Inject
    DeepZoomTileSupport tileSupport;

    
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OutputFormats outputFormat = null;
        String uuid = req.getParameter(UUID_PARAMETER);
        String outputFormatParam = req.getParameter(OUTPUT_FORMAT_PARAMETER);
        if (outputFormatParam != null) {
            outputFormat = OutputFormats.valueOf(outputFormatParam);
        }
        int page = 0;
        String spage = req.getParameter(PAGE);
        if (spage != null) {
            page = Integer.parseInt(spage);
        }

        // TODO: Predelat pres akce
        String imageType = req.getParameter(IMAGE_TYPE);
        try {
            // dotaz na image type
            if (imageType != null) {
                String type = akubraRepository.getDatastreamMetadata(uuid, KnownDatastreams.IMG_FULL).getMimetype();
                // resp.setContentType("plain/text");
                resp.getWriter().print(type);
                // pozadavek na zmenseni (prsou?)
            } else if (outputFormat == null) {
                long start = System.currentTimeMillis();
                BufferedImage image = rawFullImage(uuid, req, page);
                
                // writeDeepZoomFiles(uuid, image);

                Rectangle rectangle = new Rectangle(image.getWidth(null), image.getHeight(null));
                BufferedImage scale = scale(image, rectangle, req, getScalingMethod());
                if (scale != null) {
                    start = System.currentTimeMillis();
                    setDateHaders(uuid,FedoraUtils.IMG_FULL_STREAM, resp);
                    setResponseCode(uuid,FedoraUtils.IMG_FULL_STREAM, req, resp);
                    start = System.currentTimeMillis();
                    writeImage(req, resp, scale, OutputFormats.JPEG);
                } else
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                // transformace
            } else {
                InputStream is = akubraRepository.datastreamExists(uuid, KnownDatastreams.IMG_FULL) ?  akubraRepository.getDatastreamContent(uuid, KnownDatastreams.IMG_FULL).asInputStream() : null;
                if (outputFormat.equals(OutputFormats.RAW)) {
                    String asFileParam = req.getParameter("asFile");
                    String mimeType = akubraRepository.getDatastreamMetadata(uuid, KnownDatastreams.IMG_FULL).getMimetype();
                    if (mimeType == null)
                        mimeType = DEFAULT_MIMETYPE;
                    resp.setContentType(mimeType);
                    setDateHaders(uuid, FedoraUtils.IMG_FULL_STREAM, resp);
                    setResponseCode(uuid,FedoraUtils.IMG_FULL_STREAM, req, resp);
                    if ((asFileParam != null) && (asFileParam.equals("true"))) {
                        Document dc = akubraRepository.getDatastreamContent(uuid, KnownDatastreams.BIBLIO_DC).asDom(false);
                        String title = DCUtils.titleFromDC(dc);
                        if (title == null) {
                            title = "unnamed";
                        }
                        String fileSuffix = mimeType.substring(mimeType.indexOf('/') + 1);
                        resp.setHeader("Content-disposition", "attachment; filename=" + title + "." + fileSuffix);
                    }
                    copyStreams(is, resp.getOutputStream());
                } else {
                    BufferedImage rawImage = rawFullImage(uuid, req, page);
                    // writeDeepZoomFiles(uuid, rawImage);

                    setDateHaders(uuid,FedoraUtils.IMG_FULL_STREAM, resp);
                    setResponseCode(uuid,FedoraUtils.IMG_FULL_STREAM, req, resp);
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


    @Override
    protected BufferedImage rawFullImage(String uuid, HttpServletRequest request, int page) throws IOException, MalformedURLException, XPathExpressionException {
        if (cacheService.isDeepZoomOriginalPresent(uuid)) {
            BufferedImage bufImage = cacheService.getDeepZoomOriginal(uuid);
            return bufImage;
        } else {
            return super.rawFullImage(uuid, request, page);
        }
    }

    @Override
    public ScalingMethod getScalingMethod() {
        KConfiguration config = KConfiguration.getInstance();
        ScalingMethod method = ScalingMethod.valueOf(config.getProperty("fullImage.scalingMethod", "BICUBIC_STEPPED"));
        return method;
    }

    @Override
    public boolean turnOnIterateScaling() {
        KConfiguration config = KConfiguration.getInstance();
        boolean highQuality = config.getConfiguration().getBoolean("fullImage.iterateScaling", true);
        return highQuality;
    }

    // static class XPATHFedoraNamespaceContext implements NamespaceContext {
    //
    // static Map<String, String> MAPPING = new HashMap<String, String>();
    // {
    // MAPPING.put("", FedoraNamespaces.DC_NAMESPACE_URI);
    // MAPPING.put("", FedoraNamespaces.DC_NAMESPACE_URI);
    // }
    // @Override
    // public String getNamespaceURI(String arg0) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public String getPrefix(String arg0) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //
    // @Override
    // public Iterator getPrefixes(String arg0) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    // }
    
    
}
