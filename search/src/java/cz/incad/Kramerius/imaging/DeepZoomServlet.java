package cz.incad.Kramerius.imaging;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.imaging.impl.CachingSupport;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class DeepZoomServlet extends AbstractImageServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(DeepZoomServlet.class.getName());
    
    
    @Inject
    TileSupport tileSupport;

    CachingSupport cachingSupport = new CachingSupport();
    
    
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String requestURL = req.getRequestURL().toString();
        String zoomUrl = disectZoom(requestURL);
        
        StringTokenizer tokenizer = new StringTokenizer(zoomUrl, "/");
        String uuid = tokenizer.nextToken();
        if (tokenizer.hasMoreTokens()) {
            String files = tokenizer.nextToken();
            String level = tokenizer.nextToken();
            String tile = tokenizer.nextToken();
            renderTile(uuid, level, tile, req, resp);
        } else {
            renderXML(uuid, req, resp);
        }
    }
    
    private void renderXML(String uuid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setDateHaders(uuid, resp);
        setResponseCode(uuid, req, resp);
        if (!cachingSupport.isDeepZoomDescriptionPresent(uuid)) {
            Image rawImage = tileSupport.getRawImage(uuid);
            cachingSupport.writeDeepZoomFullImage(uuid, rawImage);
            cachingSupport.writeDeepZoomDescriptor(uuid, rawImage, tileSupport.getTileSize());
        }
        InputStream inputStream = cachingSupport.openDeepZoomDescriptor(uuid);
        try {
            IOUtils.copyStreams(inputStream, resp.getOutputStream());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void renderTile(String uuid, String slevel, String stile, HttpServletRequest req,HttpServletResponse resp) throws IOException {
        setDateHaders(uuid, resp);
        setResponseCode(uuid, req, resp);
        try {
            int ilevel = Integer.parseInt(slevel);
            if (stile.contains(".")) {
                stile = stile.substring(0, stile.indexOf('.'));
                StringTokenizer tokenizer = new StringTokenizer(stile,"_");
                String scol = tokenizer.nextToken();
                String srow = tokenizer.nextToken();
                boolean tilePresent = cachingSupport.isDeepZoomTilePresent(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
                if (!tilePresent) {
                    File dFile = cachingSupport.getDeepZoomLevelsFile(uuid);
                    long levels = dFile != null ? Integer.parseInt(dFile.getName().substring("levels_".length())) : tileSupport.getLevels(uuid, 1);
                    double scale = tileSupport.getScale(ilevel, levels);
                    Dimension scaled = tileSupport.getScaledDimension(tileSupport.getMaxSize(uuid), scale);
                    int rows = tileSupport.getRows(scaled);
                    int cols = tileSupport.getCols(scaled);
                    int base = Integer.parseInt(srow) * cols;
                    base = base + Integer.parseInt(scol);
                    
                    BufferedImage tile = this.tileSupport.getTile(uuid, ilevel, base, 1);
                    cachingSupport.writeDeepZoomTile(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol), tile);
                }

                InputStream is = cachingSupport.openDeepZoomTile(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
                resp.setContentType(ImageMimeType.JPEG.getValue());
                IOUtils.copyStreams(is, resp.getOutputStream());
//                writeDeepZoomTile(String uuid, int level, int row, int col, Image tileImage) throws IOException {
            }
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            
        }
    }

    
    public static String disectZoom(String requestURL) {
        //"dvju"
        try {
            StringBuffer buffer = new StringBuffer();
            URL url = new URL(requestURL);
            String path = url.getPath();
            String application = path;
            StringTokenizer tokenizer = new StringTokenizer(path,"/");
            if (tokenizer.hasMoreTokens()) application = tokenizer.nextToken();
            String zoomServlet = path;
            if (tokenizer.hasMoreTokens()) zoomServlet = tokenizer.nextToken();
            // check handle servlet
            while(tokenizer.hasMoreTokens()) {
                buffer.append(tokenizer.nextElement());
                if (tokenizer.hasMoreTokens()) buffer.append("/");
            }
            return buffer.toString();
        } catch (MalformedURLException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return "<no handle>";
        }
    }
}
