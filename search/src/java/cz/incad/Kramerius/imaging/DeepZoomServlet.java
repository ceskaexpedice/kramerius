package cz.incad.Kramerius.imaging;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Document;

import com.google.inject.Inject;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

public class DeepZoomServlet extends AbstractImageServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DeepZoomServlet.class.getName());

    public static final String CACHE_KEY_PARAMETER = "cache";

    @Inject
    DeepZoomTileSupport tileSupport;

    @Inject
    DeepZoomCacheService cacheService;

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String requestURL = req.getRequestURL().toString();
            String zoomUrl = disectZoom(requestURL);
            StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
            String uuid = tokenizer.nextToken();
            if (this.fedoraAccess.isContentAccessible(uuid)) {
                String stringMimeType = this.fedoraAccess.getImageFULLMimeType(uuid);
                ImageMimeType mimeType = ImageMimeType.loadFromMimeType(stringMimeType);
                if ((mimeType != null) && (!hasNoSupportForMimeType(mimeType))) {
                    resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                } else {
                    if (tokenizer.hasMoreTokens()) {
                        String files = tokenizer.nextToken();
                        String level = tokenizer.nextToken();
                        String tile = tokenizer.nextToken();
                        renderTile(uuid, level, tile, req, resp);
                    } else {
                        renderDZI(uuid, req, resp);
                    }
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean hasNoSupportForMimeType(ImageMimeType mimeType) {
        return (!mimeType.equals(ImageMimeType.PDF));
    }

    private void renderDZI(String uuid, HttpServletRequest req, HttpServletResponse resp) throws IOException, XPathExpressionException {
        setDateHaders(uuid, resp);
        setResponseCode(uuid, req, resp);
        if (imageFromIIPServer(uuid)) {
            try {
                renderIIPDZIDescriptor(uuid, resp);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            renderEmbededDZIDescriptor(uuid, resp);
        }
    }

    public boolean imageFromIIPServer(String uuid) {
        try {
            Object tiles = getRelsExtTilesUrl(uuid);
            return tiles != null;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
        }
        return false;
    }


    private void renderIIPDZIDescriptor(String uuid, HttpServletResponse resp) throws MalformedURLException, IOException, SQLException, XPathExpressionException {
        String dataStreamPath = getPathForFullImageStream(uuid);
        if (dataStreamPath != null) {
            StringTemplate dziUrl = stGroup().getInstanceOf("dziurl");
            setStringTemplateModel(uuid, dataStreamPath, dziUrl, fedoraAccess);
            copyFromImageServer(dziUrl.toString(), resp);
        }
    }

    private void renderEmbededDZIDescriptor(String uuid, HttpServletResponse resp) throws IOException, FileNotFoundException, XPathExpressionException {
        if (!cacheService.isDeepZoomDescriptionPresent(uuid)) {
            Dimension rawDim = KrameriusImageSupport.readDimension(uuid, FedoraUtils.IMG_FULL_STREAM, fedoraAccess, 0);
            int levelsOverTile = KConfiguration.getInstance().getConfiguration().getInt("deepZoom.numberStepsOverTile", 1);
            int tileLevel = tileSupport.getClosestLevel(new Dimension(rawDim.width, rawDim.height), tileSupport.getTileSize());
            Dimension scaledDimension = tileSupport.getScaledDimension(rawDim, tileLevel+levelsOverTile);
            
            cacheService.writeDeepZoomDescriptor(uuid, scaledDimension, tileSupport.getTileSize());
        }
        InputStream inputStream = cacheService.getDeepZoomDescriptorStream(uuid);
        try {
            IOUtils.copyStreams(inputStream, resp.getOutputStream());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void renderTile(String uuid, String slevel, String stile, HttpServletRequest req, HttpServletResponse resp) throws IOException, XPathExpressionException {
        setDateHaders(uuid, resp);
        setResponseCode(uuid, req, resp);
        if (imageFromIIPServer(uuid)) {
            try {
                renderIIPTile(uuid, slevel, stile, resp);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            renderEmbededTile(uuid, slevel, stile, req, resp);
        }
    }

    private void renderIIPTile(String uuid, String slevel, String stile, HttpServletResponse resp) throws SQLException, UnsupportedEncodingException, IOException, XPathExpressionException {
        String dataStreamPath = getPathForFullImageStream(uuid);
        if (dataStreamPath != null) {
            StringTemplate tileUrl = stGroup().getInstanceOf("tileurl");
            setStringTemplateModel(uuid, dataStreamPath, tileUrl, fedoraAccess);
            tileUrl.setAttribute("level", slevel);
            tileUrl.setAttribute("tile", stile);
            copyFromImageServer(tileUrl.toString(), resp);
        }
    }

    private void renderEmbededTile(String uuid, String slevel, String stile, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int ilevel = Integer.parseInt(slevel);
            if (stile.contains(".")) {
                stile = stile.substring(0, stile.indexOf('.'));
                StringTokenizer tokenizer = new StringTokenizer(stile, "_");
                String scol = tokenizer.nextToken();
                String srow = tokenizer.nextToken();
                Dimension originalResolution = cacheService.getResolutionFromFile(uuid);
                int maxLevels = tileSupport.getLevels(originalResolution, 1);
                
                Dimension scaledResolution = tileSupport.getScaledDimension(originalResolution, ilevel,maxLevels);
                if ((scaledResolution.width <= tileSupport.getTileSize()) && (scaledResolution.height <= tileSupport.getTileSize())) {
                    // obrazek se vejde na jednu dlazdici, vracime velky nahled
                    
                    if (fedoraAccess.isFullthumbnailAvailable(uuid)) {
                        String mimeType = this.fedoraAccess.getFullThumbnailMimeType(uuid);
                        resp.setContentType(mimeType);
                        setDateHaders(uuid, resp);
                        setResponseCode(uuid, req, resp);
                        copyStreams(fedoraAccess.getFullThumbnail(uuid), resp.getOutputStream());
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                    
                } else {
                    // bereme z cache nebo pocitame, vykresulejeme, ukladame a vracime 
                    boolean tilePresent = cacheService.isDeepZoomTilePresent(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
                    if (!tilePresent) {
                        // File dFile = cacheService.getDeepZoomLevelsFile(uuid);
                        BufferedImage original = null;
                        if (cacheService.isDeepZoomOriginalPresent(uuid)) {
                            original = cacheService.getDeepZoomOriginal(uuid);
                        } else {
                            original = cacheService.createDeepZoomOriginalImageFromFedoraRAW(uuid);
                            cacheService.writeDeepZoomOriginalImage(uuid, original);
                        }

                        long levels = tileSupport.getLevels(original, 1);
                        double scale = tileSupport.getScale(ilevel, levels);

                        Dimension scaled = tileSupport.getScaledDimension(new Dimension(original.getWidth(null), original.getHeight(null)), scale);
                        int rows = tileSupport.getRows(scaled);
                        int cols = tileSupport.getCols(scaled);
                        int base = Integer.parseInt(srow) * cols;
                        base = base + Integer.parseInt(scol);

                        BufferedImage tile = this.tileSupport.getTileFromBigImage(original, ilevel, base, 1, getScalingMethod(), turnOnIterateScaling());
                        cacheService.writeDeepZoomTile(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol), tile);
                    }
                    InputStream is = cacheService.getDeepZoomTileStream(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
                    resp.setContentType(ImageMimeType.JPEG.getValue());
                    IOUtils.copyStreams(is, resp.getOutputStream());
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static String disectZoom(String requestURL) {
        // "dvju"
        try {
            StringBuffer buffer = new StringBuffer();
            URL url = new URL(requestURL);
            String path = url.getPath();
            String application = path;
            StringTokenizer tokenizer = new StringTokenizer(path, "/");
            if (tokenizer.hasMoreTokens())
                application = tokenizer.nextToken();
            String zoomServlet = path;
            if (tokenizer.hasMoreTokens())
                zoomServlet = tokenizer.nextToken();
            // check handle servlet
            while (tokenizer.hasMoreTokens()) {
                buffer.append(tokenizer.nextElement());
                if (tokenizer.hasMoreTokens())
                    buffer.append("/");
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no handle>";
        }
    }

    @Override
    public ScalingMethod getScalingMethod() {
        ScalingMethod method = ScalingMethod.valueOf(KConfiguration.getInstance().getProperty("deepZoom.scalingMethod", "BICUBIC_STEPPED"));
        return method;
    }

    @Override
    public boolean turnOnIterateScaling() {
        boolean highQuality = KConfiguration.getInstance().getConfiguration().getBoolean("deepZoom.iterateScaling", true);
        return highQuality;
    }
}
