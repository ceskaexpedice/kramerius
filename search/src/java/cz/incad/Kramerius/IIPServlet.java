package cz.incad.Kramerius;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import sun.util.logging.resources.logging;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

/**
 * Skelet of support of IIP protocol 
 * @author pavels
 */
public class IIPServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(IIPServlet.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        copyFromImageServer(req, resp);
        Enumeration parameterNames = req.getParameterNames();
        Map<String, String> originalNames = new HashMap<String, String>();
        while(parameterNames.hasMoreElements()) {
            String origName = (String) parameterNames.nextElement();
            originalNames.put(origName.toUpperCase(), origName);
        }
        System.out.println(req.getQueryString());
        String uuid = req.getParameter(originalNames.get("FIF"));
        if (originalNames.containsKey("JTL")) {
            String jtl = req.getParameter(originalNames.get("JTL"));
            drawTile(uuid, jtl, req, resp);
        } else if (originalNames.containsKey("CVT")) {
            //http://localhost:8080/search/iip?FIF=4308eb80-b03b-11dd-a0f6-000d606f5dc6&SDS=0,90&CNT=1.0&WID=178&QLT=99&CVT=jpeg
            // FlashPix 1.0 // nepodporovano
            String sds = req.getParameter(originalNames.get("SDS"));
            // contrast // nepodporovano
            String cnt = req.getParameter(originalNames.get("CNT"));
            // sirka
            String swidth = req.getParameter(originalNames.get("WID"));
            // kvalita // nepodporovano
            String qlt = req.getParameter(originalNames.get("QLT"));
            String cvt = req.getParameter(originalNames.get("CVT"));
            
            Image scaled = cachedConvert(uuid, swidth, cvt);
            KrameriusImageSupport.writeImageToStream(scaled, cvt, resp.getOutputStream());
            
        } else {
            StringBuffer buffer = new StringBuffer();
            String[] parameterValues = req.getParameterValues(originalNames.get("OBJ"));   
            for (String action : parameterValues) {
                if (action.startsWith("IIP,")) {
                    empty(buffer, req,resp);
                }
                if (action.equals("Tile-size")) {
                    tileSize(buffer, req, resp);
                }
                if (action.equals("Resolution-number")) {
                    resolutionNumber(buffer, uuid, req, resp);
                }
                
                if (action.equals("Horizontal-views")) {
                    horizontalViews(buffer, req, resp);
                }
                if (action.equals("Vertical-views")) {
                    verticalViews(buffer, req, resp);
                }
                if (action.equals("Max-size")) {
                    maxSize(buffer,uuid, req, resp);
                }
            }
            resp.getWriter().println(buffer.toString());
        }
    }

    private HashMap<String, Image> cacheForConvert = new HashMap<String, Image>();
    
    private Image cachedConvert(String uuid, String swidth, String cvt) throws IOException {
        String key = uuid+"_"+swidth+"_"+cvt;
        if (!cacheForConvert.containsKey(key)) {
            cacheForConvert.put(key, convert(uuid, swidth, cvt));
        }
        return cacheForConvert.get(key);
    }
    
    private Image convert(String uuid, String swidth,String cvt) throws IOException {
        Image rawImage = cachedImage(uuid);
        int rawImageWidth = rawImage.getWidth(null);
        int rawImageHeight = rawImage.getHeight(null);
        int expectedWidth = Integer.parseInt(swidth);
        int expectedHeight = (int) (expectedWidth * ((double)rawImageHeight/ (double)rawImageWidth));
        Image scaled = KrameriusImageSupport.scale(rawImage, expectedWidth, expectedHeight);
        return scaled;
    }

    private void drawTile(String uuid, String jtl, HttpServletRequest req,HttpServletResponse resp) throws IOException {
        try {
            String[] vals = jtl.split(",");
            if ((vals != null) && (vals.length == 2)) {
                int displayLevel = Integer.parseInt(vals[0]);
                int displayTile = Integer.parseInt(vals[1]);
                
                BufferedImage buffImage = cachedTile(uuid, displayLevel, displayTile);
                KrameriusImageSupport.writeImageToStream(buffImage, "JPEG", resp.getOutputStream());
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private HashMap<String, BufferedImage> cacheForTiles = new HashMap<String, BufferedImage>();
    private BufferedImage cachedTile(String uuid, int displayLevel, int displayTile) {
        String key = uuid+"_"+displayLevel+"_"+displayTile;
        if (!cacheForTiles.containsKey(key)) {
            cacheForTiles.put(key, tile(uuid, displayLevel, displayTile));
        }
        return cacheForTiles.get(key);
    }
    private BufferedImage tile(String uuid, int displayLevel, int displayTile) {
        Image image = cachedImage(uuid);
        long maxLevel = levels(uuid);
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        long b = (maxLevel-1)-displayLevel;
        double scale = Math.pow(2, b);
        
        int scaledWidth  =(int) (width / scale);
        int scaledHeight = (int) (height / scale);
        int rows = scaledHeight / 256;
        if (scaledHeight % 256 != 0) {rows +=1;}
        
        int cols = scaledWidth / 256;
        if (scaledWidth % 256 != 0) {cols += 1; }
        
        Image scaled = KrameriusImageSupport.scale(image, scaledWidth, scaledHeight);
        int rowTile = displayTile / cols;
        int colTile = displayTile % cols;
        
        int tileStartY = rowTile * 256;
        int tileStartX = colTile * 256;
        
        int tileWidth = Math.min(256, scaledWidth - tileStartX);
        int tileHeight = Math.min(256, scaledHeight - tileStartY);
        
        BufferedImage buffImage = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2d = (Graphics2D) buffImage.getGraphics();
        //graphics2d.drawImage(scaled, 0, 0, tileWidth, tileHeight, tileStartX, tileStartY, scaledWidth, scaledHeight, null);
        
        graphics2d.drawImage(scaled, -tileStartX,-tileStartY, null);
        return buffImage;
    }

    private void empty(StringBuffer buffer, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String r = "IIP:1.0\n";
        buffer.append(r);
    }

    private void verticalViews(StringBuffer buffer, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String r = "Vertical-views:0\n";
        buffer.append(r);
    }

    private void horizontalViews(StringBuffer buffer, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
        String r = "Horizontal-views:0\n";
        buffer.append(r);
    }

    private void maxSize(StringBuffer buffer, String uuid, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
            Image rawImg = cachedImage(uuid);
            String r = "Max-size:"+rawImg.getWidth(null)+" "+ rawImg.getHeight(null)+"\n";
            buffer.append(r);
    }

    private void resolutionNumber(StringBuffer buffer, String uuid, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
            long levels = cachedLevels(uuid);
            String r ="Resolution-number:"+levels+"\n";
            buffer.append(r);
        
    }

    private Image image(String uuid) throws XPathExpressionException,
            IOException {
        Image rawImg = KrameriusImageSupport.readImage(uuid, FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, 0);
        return rawImg;
    }

    private HashMap<String, Image> rawImages = new HashMap<String, Image>();
    private Image cachedImage(String uuid) {
        if (!rawImages.containsKey(uuid)) {
            try {
                rawImages.put(uuid, image(uuid));
            } catch (XPathExpressionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return rawImages.get(uuid);
    }
    

    private HashMap<String, Long> levels = new HashMap<String, Long>();
    private long cachedLevels(String uuid) {
        if (!levels.containsKey(uuid)) {
            levels.put(uuid, levels(uuid));
        }
        return levels.get(uuid);
    }
    
    private long levels(String uuid) {
        Image rawImg = cachedImage(uuid);
        int max = Math.max(rawImg.getHeight(null), rawImg.getWidth(null));
        int currentMax = max;
        int level = 1;
        while(currentMax > 256) {
            currentMax = currentMax / 2;
            level+=1;
        }
        
        return level;
    }

    private void tileSize(StringBuffer buffer, HttpServletRequest request, HttpServletResponse res) throws IOException {
        String r = "Tile-size:256 256\n";
        buffer.append(r);
    }

    
    
    private void copyFromImageServer(HttpServletRequest req,
            HttpServletResponse resp) throws MalformedURLException, IOException {
//        String urlString = "http://192.168.1.5/fcgi-bin/iipsrv.fcgi";
      
        String urlString = "http://194.108.215.42/fcgi-bin/iipsrv.fcgi";
        urlString += "?"+req.getQueryString();
        System.out.println(urlString);
        URLConnection con = RESTHelper.openConnection(urlString, "", "");
        String contentType = con.getContentType();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStreams(con.getInputStream(), bos);
        //System.out.println(new String(bos.toByteArray()));
        copyStreams(new ByteArrayInputStream(bos.toByteArray()), resp.getOutputStream());
        resp.setContentType(contentType);
    }
}
