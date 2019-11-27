/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package cz.incad.Kramerius.imaging;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.Kramerius.imaging.utils.ZoomChangeFromReplicated;
import cz.incad.kramerius.MostDesirable;
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
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

/**
 * @author pavels
 *
 */
public class ZoomifyServlet extends AbstractImageServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ZoomifyServlet.class.getName());
    
    @Inject
    DeepZoomCacheService cacheService;

    @Inject
    DeepZoomTileSupport tileSupport;

    @Inject
    StatisticsAccessLog accessLog;

    @Inject
    IsActionAllowed actionAllowed;
    
    @Inject
    Provider<User> userProvider;
    
    @Inject
    SolrAccess solrAccess;
    
    @Inject
    MostDesirable mostDesirable;

    
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String requestURL = req.getRequestURL().toString();
            String zoomUrl = DeepZoomServlet.disectZoom(requestURL);
            StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
            String pid = tokenizer.nextToken();
            String rest = tokenizer.hasMoreTokens() ?  tokenizer.nextToken() : "";

            if (this.fedoraAccess.isObjectAvailable(pid)) {
                ObjectPidsPath[] paths = solrAccess.getPath(pid);
                boolean premited = false;
                for (ObjectPidsPath pth : paths) {
                    premited = this.actionAllowed.isActionAllowed(userProvider.get(), SecuredActions.READ.getFormalName(),pid,null,pth);
                    if (premited) break;
                }
                
                if (premited) {
                    if (rest.equals("ImageProperties.xml")) {
                        renderXMLDescriptor(pid, req, resp);
                    } else {
                        if (tokenizer.hasMoreTokens()) {
                            String files = tokenizer.nextToken();
                            String ext = "jpg";
                            if (files.contains(".")) {
                                ext = files.substring(files.indexOf('.')+1);
                                files = files.substring(0, files.indexOf('.'));
                            }
                            StringTokenizer substokenizer = new StringTokenizer(files,"-");
                            if (substokenizer.countTokens() == 3) {
                                String level = substokenizer.nextToken();
                                String x = substokenizer.nextToken();
                                String y = substokenizer.nextToken();
                                renderTile(pid, level, x, y, ext, req, resp);
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

    private void renderXMLDescriptor(String pid, HttpServletRequest req, HttpServletResponse resp) throws IOException, XPathExpressionException {
	    try {
	    	this.accessLog.reportAccess(pid, FedoraUtils.IMG_FULL_STREAM);
        } catch (Exception e) {
			LOGGER.severe("cannot write statistic records");
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
    	
    	setDateHaders(pid,FedoraUtils.IMG_FULL_STREAM, resp);
        setResponseCode(pid,FedoraUtils.IMG_FULL_STREAM, req, resp);
        mostDesirable.saveAccess(pid, new java.util.Date());

        String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
        if (relsExtUrl != null) {
            if (!relsExtUrl.equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL)) {
                try {
                    renderIIPrenderXMLDescriptor(pid, resp, relsExtUrl);
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
    
    private void renderEmbededDZIDescriptor(String uuid, HttpServletResponse resp) throws IOException, FileNotFoundException, XPathExpressionException {
        if (!cacheService.isDeepZoomDescriptionPresent(uuid)) {
            Dimension rawDim = KrameriusImageSupport.readDimension(uuid, FedoraUtils.IMG_FULL_STREAM, fedoraAccess, 0);
            cacheService.writeDeepZoomDescriptor(uuid, rawDim, tileSupport.getTileSize());
            cacheService.writeResolution(uuid, rawDim);
        }
        InputStream inputStream = cacheService.getDeepZoomDescriptorStream(uuid);
        
        //<IMAGE_PROPERTIES WIDTH="8949" HEIGHT="6684" NUMTILES="945" NUMIMAGES="1" VERSION="1.8" TILESIZE="256" />
        
        resp.setContentType("application/xml");
        
        try {
            Document document = XMLUtils.parseDocument(inputStream);
            Element docelement = document.getDocumentElement();
            String tileSize = docelement.getAttribute("TileSize");
            Element sizeElement = XMLUtils.findElement(docelement, "Size");
            String width = sizeElement.getAttribute("Width");
            String height = sizeElement.getAttribute("Height");

            int iWidth = Integer.parseInt(width);
            int iHeight = Integer.parseInt(height);
            int iTileSize = Integer.parseInt(tileSize);
            
            double nTilesX = iWidth / iTileSize;
            int iNTilesX = 0;
            
            if (Math.floor(nTilesX) < nTilesX) {
                iNTilesX = (int) (Math.floor(nTilesX)+1);
            } else {
                iNTilesX =  (int) Math.floor(nTilesX);
            }
            
            double nTilesY = iHeight / iTileSize;
            int iNTilesY = 0;
            if (Math.floor(nTilesY) < iNTilesY) {
                iNTilesY = (int) (Math.floor(nTilesY)+1);
            } else {
                iNTilesY =  (int) Math.floor(nTilesY);
            }
            
            
            
            StringBuffer buffer = new StringBuffer();
            buffer.append("<IMAGE_PROPERTIES WIDTH=\"").append(width).append('"').append(" HEIGHT=\"").append(height).append('"');
            buffer.append("  NUMIMAGES='1' ");
            buffer.append("  NUMTILES='").append(iNTilesX*iNTilesY).append("'");
            buffer.append("  VERSION='1.8' TILESIZE=\"").append(tileSize).append("\" />");
            IOUtils.copyStreams(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), resp.getOutputStream());
            
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    
    private void renderIIPrenderXMLDescriptor(String uuid, HttpServletResponse resp, String url) throws MalformedURLException, IOException, SQLException, XPathExpressionException {
    	String urlForStream = getURLForStream(uuid, url);
    	if (useFromReplicated()) {
    		Document relsEXT = this.fedoraAccess.getRelsExt(uuid);
    		urlForStream = ZoomChangeFromReplicated.zoomifyAddress(relsEXT, uuid);
    	}
        if (urlForStream != null) {
            StringTemplate dziUrl = stGroup().getInstanceOf("zoomify");
            if (urlForStream.endsWith("/")) urlForStream = urlForStream.substring(0, urlForStream.length()-1);
            dziUrl.setAttribute("url", urlForStream);
            copyFromImageServer(dziUrl.toString(), resp);
        }
    }

	private boolean useFromReplicated() {
		boolean useFromReplicated = KConfiguration.getInstance().getConfiguration().getBoolean("zoom.useFromReplicated",false);
		return useFromReplicated;
	}
    
    private void renderTile(String pid, String slevel, String x, String y, String ext, HttpServletRequest req, HttpServletResponse resp) throws IOException, XPathExpressionException {
        setDateHaders(pid, FedoraUtils.IMG_FULL_STREAM, resp);
        setResponseCode(pid,FedoraUtils.IMG_FULL_STREAM, req, resp);
        String relsExtUrl = RelsExtHelper.getRelsExtTilesUrl(pid, this.fedoraAccess);
        if (relsExtUrl != null) {
            if (!relsExtUrl.equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL)) {
                try {
                    renderIIPTile(pid, slevel, x,y, ext, resp, relsExtUrl);
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                // srow = y
                // scol = x
                renderEmbededTile(pid, slevel,x,y,ext, req, resp);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    
    private void renderEmbededTile(String pid, String slevel, String x, String y,String ext, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            
            if (!cacheService.isResolutionFilePresent(pid)) {
                Dimension rawDim = KrameriusImageSupport.readDimension(pid, FedoraUtils.IMG_FULL_STREAM, fedoraAccess, 0);
                cacheService.writeResolution(pid, rawDim);
            }

            Dimension originalResolution = cacheService.getResolutionFromFile(pid);
            int requestedLevel = Integer.parseInt(slevel);
            int maxLevels = tileSupport.getLevels(originalResolution, tileSupport.getTileSize());

            int offset = tileSupport.getClosestLevel(originalResolution , tileSupport.getTileSize(), 1);
            //deepzoom level
            int offsetLevel = requestedLevel + (offset);
                
            String srow = y;
            String scol = x;
            
            boolean tilePresent = cacheService.isDeepZoomTilePresent(pid, offsetLevel, Integer.parseInt(srow), Integer.parseInt(scol));
            if (!tilePresent) {
                // File dFile = cacheService.getDeepZoomLevelsFile(uuid);
                BufferedImage original = null;
                if (cacheService.isDeepZoomOriginalPresent(pid)) {
                    original = cacheService.getDeepZoomOriginal(pid);
                } else {
                    original = cacheService.createDeepZoomOriginalImageFromFedoraRAW(pid);
                    cacheService.writeDeepZoomOriginalImage(pid, original);
                }

                double scale = tileSupport.getScale(requestedLevel, maxLevels);
                //ilevel = 
                // TODO: vyzkouset
                Dimension scaled = tileSupport.getScaledDimension(new Dimension(original.getWidth(null), original.getHeight(null)), scale);
                int rows = tileSupport.getRows(scaled);
                int cols = tileSupport.getCols(scaled);
                int base = Integer.parseInt(srow) * cols;
                base = base + Integer.parseInt(scol);

                LOGGER.info("scale is "+scale+" and dimension is "+scaled);
                
                BufferedImage tile = this.tileSupport.getTileFromBigImage(original, requestedLevel, base, tileSupport.getTileSize(), getScalingMethod(), turnOnIterateScaling());
                cacheService.writeDeepZoomTile(pid, offsetLevel, Integer.parseInt(srow), Integer.parseInt(scol), tile);
            }
            InputStream is = cacheService.getDeepZoomTileStream(pid, offsetLevel, Integer.parseInt(srow), Integer.parseInt(scol));
            resp.setContentType(ImageMimeType.JPEG.getValue());
            IOUtils.copyStreams(is, resp.getOutputStream());

        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void renderIIPTile(String uuid, String slevel, String x,String y, String ext, HttpServletResponse resp, String url) throws SQLException, UnsupportedEncodingException, IOException, XPathExpressionException {
        String dataStreamUrl = getURLForStream(uuid, url);
        if (useFromReplicated()) {
    		Document relsEXT = this.fedoraAccess.getRelsExt(uuid);
    		dataStreamUrl = ZoomChangeFromReplicated.zoomifyAddress(relsEXT, uuid);
        }
        if (dataStreamUrl != null) {
            StringTemplate tileUrl = stGroup().getInstanceOf("zoomifytile");
            //setStringTemplateModel(uuid, dataStreamPath, tileUrl, fedoraAccess);
            if (dataStreamUrl.endsWith("/")) dataStreamUrl = dataStreamUrl.substring(0, dataStreamUrl.length()-1);
            tileUrl.setAttribute("url", dataStreamUrl);
            tileUrl.setAttribute("level", slevel);
            tileUrl.setAttribute("x", x);
            tileUrl.setAttribute("y", y);
            tileUrl.setAttribute("ext", ext);
            copyFromImageServer(tileUrl.toString(), resp);
        }
    }
}
