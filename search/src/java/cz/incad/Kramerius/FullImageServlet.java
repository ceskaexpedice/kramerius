package cz.incad.Kramerius;

import static cz.incad.utils.IKeys.*;
import static cz.incad.kramerius.utils.RESTHelper.*;
import static cz.incad.kramerius.utils.FedoraUtils.*;
import static cz.incad.kramerius.utils.IOUtils.*;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.Right;
import cz.incad.kramerius.security.RightsManager;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.SortingRightsUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import cz.incad.kramerius.utils.solr.SolrUtils;

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
                String type = this.fedoraAccess.getImageFULLMimeType(uuid);
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
                InputStream is = this.fedoraAccess.getImageFULL(uuid);
                if (outputFormat.equals(OutputFormats.RAW)) {
                    String asFileParam = req.getParameter("asFile");
                    String mimeType = this.fedoraAccess.getImageFULLMimeType(uuid);
                    if (mimeType == null)
                        mimeType = DEFAULT_MIMETYPE;
                    resp.setContentType(mimeType);
                    setDateHaders(uuid, FedoraUtils.IMG_FULL_STREAM, resp);
                    setResponseCode(uuid,FedoraUtils.IMG_FULL_STREAM, req, resp);
                    if ((asFileParam != null) && (asFileParam.equals("true"))) {
                        Document dc = this.fedoraAccess.getDC(uuid);
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


    public static String fullImageServlet(HttpServletRequest request) {
        return ApplicationURL.urlOfPath(request, InternalConfiguration.get().getProperties().getProperty("servlets.mapping.fullImage"));
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
