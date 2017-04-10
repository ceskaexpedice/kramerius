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
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.Kramerius.imaging.utils.ZoomChangeFromReplicated;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.security.IsActionAllowed;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RelsExtHelper;
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

    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    SolrAccess solrAccess;
    
    
    @Inject
    StatisticsAccessLog accessLog;
    
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
            String pid = tokenizer.nextToken();
            if (this.fedoraAccess.isObjectAvailable(pid)) {
                ObjectPidsPath[] paths = solrAccess.getPath(pid);
                boolean premited = false;
                for (ObjectPidsPath pth : paths) {
                    premited = this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(),pid,null,pth);
                    if (premited) break;
                }
                
                if (premited) {
                    String stringMimeType = this.fedoraAccess.getImageFULLMimeType(pid);
                    ImageMimeType mimeType = ImageMimeType.loadFromMimeType(stringMimeType);
                    if ((mimeType != null) && (!hasNoSupportForMimeType(mimeType))) {
                        resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
                    } else {
                        if (tokenizer.hasMoreTokens()) {
                            String files = tokenizer.nextToken();
                            String level = tokenizer.nextToken();
                            String tile = tokenizer.nextToken();
                            renderTile(pid, level, tile, req, resp);
                        } else {
                            if (this.fedoraAccess.isContentAccessible(pid)) {
                                renderDZI(pid, req, resp);
                            } else {
                                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            }
                        }
                    }
                    
                } else {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private boolean hasNoSupportForMimeType(ImageMimeType mimeType) {
        return (!mimeType.equals(ImageMimeType.PDF));
    }

    private void renderDZI(String pid, HttpServletRequest req, HttpServletResponse resp) throws IOException, XPathExpressionException {
    	// report access

        try {
        	this.accessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
		} catch (Exception e) {
			LOGGER.severe("cannot write statistic records");
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}

    	setDateHaders(pid,FedoraUtils.IMG_FULL_STREAM, resp);
        setResponseCode(pid,FedoraUtils.IMG_FULL_STREAM, req, resp);
        String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
        if (relsExtUrl != null) {
            if (!relsExtUrl.equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL)) {
                try {
                    renderIIPDZIDescriptor(pid, resp, relsExtUrl);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                renderEmbededDZIDescriptor(pid, resp);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    


    private void renderIIPDZIDescriptor(String uuid, HttpServletResponse resp, String url) throws MalformedURLException, IOException, SQLException, XPathExpressionException {
        String urlForStream = getURLForStream(uuid, url);
    	if (useFromReplicated()) {
    		Document relsEXT = this.fedoraAccess.getRelsExt(uuid);
    		urlForStream = ZoomChangeFromReplicated.deepZoomAddress(relsEXT, uuid);
    	}
        if (urlForStream != null) {
            StringTemplate dziUrl = stGroup().getInstanceOf("ndzi");
            if (urlForStream.endsWith("/")) urlForStream = urlForStream.substring(0, urlForStream.length()-1);
            dziUrl.setAttribute("url", urlForStream);
            copyFromImageServer(dziUrl.toString(), resp);
        }
    }

    private void renderEmbededDZIDescriptor(String uuid, HttpServletResponse resp) throws IOException, FileNotFoundException, XPathExpressionException {
        if (!cacheService.isDeepZoomDescriptionPresent(uuid)) {
            Dimension rawDim = KrameriusImageSupport.readDimension(uuid, FedoraUtils.IMG_FULL_STREAM, fedoraAccess, 0);
            int levelsOverTile = KConfiguration.getInstance().getConfiguration().getInt("deepZoom.numberStepsOverTile", 1);
            int tileLevel = tileSupport.getClosestLevel(new Dimension(rawDim.width, rawDim.height), tileSupport.getTileSize(), 1);
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

    private void renderTile(String pid, String slevel, String stile, HttpServletRequest req, HttpServletResponse resp) throws IOException, XPathExpressionException {
        setDateHaders(pid, FedoraUtils.IMG_FULL_STREAM, resp);
        setResponseCode(pid,FedoraUtils.IMG_FULL_STREAM, req, resp);
        String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
        if (relsExtUrl != null) {
            if (!relsExtUrl.equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL)) {
                try {
                    renderIIPTile(pid, slevel, stile, resp, relsExtUrl);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                renderEmbededTile(pid, slevel, stile, req, resp);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void renderIIPTile(String uuid, String slevel, String stile, HttpServletResponse resp, String url) throws SQLException, UnsupportedEncodingException, IOException, XPathExpressionException {
        String dataStreamUrl = getURLForStream(uuid, url);
    	if (useFromReplicated()) {
    		Document relsEXT = this.fedoraAccess.getRelsExt(uuid);
    		dataStreamUrl = ZoomChangeFromReplicated.zoomifyAddress(relsEXT, uuid);
    	}
        if (dataStreamUrl != null) {
            StringTemplate tileUrl = stGroup().getInstanceOf("ntile");
            //setStringTemplateModel(uuid, dataStreamPath, tileUrl, fedoraAccess);
            if (dataStreamUrl.endsWith("/")) dataStreamUrl = dataStreamUrl.substring(0, dataStreamUrl.length()-1);
            tileUrl.setAttribute("url", dataStreamUrl);
            tileUrl.setAttribute("level", slevel);
            tileUrl.setAttribute("tile", stile);
            copyFromImageServer(tileUrl.toString(), resp);
        }
    }

    private void renderEmbededTile(String pid, String slevel, String stile, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int ilevel = Integer.parseInt(slevel);
            if (stile.contains(".")) {
                stile = stile.substring(0, stile.indexOf('.'));
                StringTokenizer tokenizer = new StringTokenizer(stile, "_");
                String scol = tokenizer.nextToken();
                String srow = tokenizer.nextToken();
                Dimension originalResolution = cacheService.getResolutionFromFile(pid);
                int maxLevels = tileSupport.getLevels(originalResolution, 1);
                
                Dimension scaledResolution = tileSupport.getScaledDimension(originalResolution, ilevel,maxLevels);
                if ((scaledResolution.width <= tileSupport.getTileSize()) && (scaledResolution.height <= tileSupport.getTileSize())) {
                    // obrazek se vejde na jednu dlazdici, vracime velky nahled
                    
                    if (fedoraAccess.isFullthumbnailAvailable(pid)) {
                        String mimeType = this.fedoraAccess.getFullThumbnailMimeType(pid);
                        resp.setContentType(mimeType);
                        setDateHaders(pid,FedoraUtils.IMG_FULL_STREAM, resp);
                        setResponseCode(pid, FedoraUtils.IMG_FULL_STREAM, req, resp);
                        copyStreams(fedoraAccess.getFullThumbnail(pid), resp.getOutputStream());
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                    
                } else {
                    // bereme z cache nebo pocitame, vykresulejeme, ukladame a vracime 
                    boolean tilePresent = cacheService.isDeepZoomTilePresent(pid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
                    if (!tilePresent) {
                        // File dFile = cacheService.getDeepZoomLevelsFile(uuid);
                        BufferedImage original = null;
                        if (cacheService.isDeepZoomOriginalPresent(pid)) {
                            original = cacheService.getDeepZoomOriginal(pid);
                        } else {
                            original = cacheService.createDeepZoomOriginalImageFromFedoraRAW(pid);
                            cacheService.writeDeepZoomOriginalImage(pid, original);
                        }

                        long levels = tileSupport.getLevels(original, 1);
                        double scale = tileSupport.getScale(ilevel, levels);

                        Dimension scaled = tileSupport.getScaledDimension(new Dimension(original.getWidth(null), original.getHeight(null)), scale);
                        int rows = tileSupport.getRows(scaled);
                        int cols = tileSupport.getCols(scaled);
                        int base = Integer.parseInt(srow) * cols;
                        base = base + Integer.parseInt(scol);

                        BufferedImage tile = this.tileSupport.getTileFromBigImage(original, ilevel, base, 1, getScalingMethod(), turnOnIterateScaling());
                        cacheService.writeDeepZoomTile(pid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol), tile);
                    }
                    InputStream is = cacheService.getDeepZoomTileStream(pid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
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
    
    
	private boolean useFromReplicated() {
		boolean useFromReplicated = KConfiguration.getInstance().getConfiguration().getBoolean("zoom.useFromReplicated",false);
		return useFromReplicated;
	}

}
