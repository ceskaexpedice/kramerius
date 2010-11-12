package cz.incad.Kramerius.imaging;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

/**
 * Skelet of support of IIP protocol 
 * @author pavels
 */
public class IIPServlet extends GuiceServlet {

    private static final int TILE_SIZE = 64;

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(IIPServlet.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    DeepZoomTileSupport tileSupport;
    
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
            
            BufferedImage scaled = cachedConvert(uuid, swidth, cvt);
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

    private HashMap<String, BufferedImage> cacheForConvert = new HashMap<String, BufferedImage>();
    
    private BufferedImage cachedConvert(String uuid, String swidth, String cvt) throws IOException {
        String key = uuid+"_"+swidth+"_"+cvt;
        if (!cacheForConvert.containsKey(key)) {
            cacheForConvert.put(key, convert(uuid, swidth, cvt));
        }
        return cacheForConvert.get(key);
    }
    
    private BufferedImage convert(String uuid, String swidth,String cvt) throws IOException {
        BufferedImage rawImage = cachedImage(uuid);
        int rawImageWidth = rawImage.getWidth(null);
        int rawImageHeight = rawImage.getHeight(null);
        int expectedWidth = Integer.parseInt(swidth);
        int expectedHeight = (int) (expectedWidth * ((double)rawImageHeight/ (double)rawImageWidth));
        BufferedImage scaled = KrameriusImageSupport.scale(rawImage, expectedWidth, expectedHeight);
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
    private BufferedImage cachedTile(String uuid, int displayLevel, int displayTile) throws IOException {
        String key = uuid+"_"+displayLevel+"_"+displayTile;
        if (!cacheForTiles.containsKey(key)) {
            //TODO: CHANGE IT cacheForTiles.put(key, tileSupport.getTileFromBigImage(uuid, displayLevel, displayTile, tileSupport.getTileSize(), null, false));
        }
        return cacheForTiles.get(key);
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
//TODO: CHANGE IT            Dimension dim = tileSupport.getMaxSize(uuid);
//            String r = "Max-size:"+dim.getWidth()+" "+ dim.getHeight()+"\n";
//            buffer.append(r);
    }

    private void resolutionNumber(StringBuffer buffer, String uuid, HttpServletRequest req,
            HttpServletResponse resp) throws IOException {
            long levels = cachedLevels(uuid);
            String r ="Resolution-number:"+levels+"\n";
            buffer.append(r);
        
    }


    private HashMap<String, BufferedImage> rawImages = new HashMap<String, BufferedImage>();
    private BufferedImage cachedImage(String uuid) {
        if (!rawImages.containsKey(uuid)) {
            try {
                rawImages.put(uuid, tileSupport.getRawImage(uuid));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return rawImages.get(uuid);
    }
    

    private HashMap<String, Long> levels = new HashMap<String, Long>();
    private long cachedLevels(String uuid) throws IOException {
        if (!levels.containsKey(uuid)) {
            //levels.put(uuid, tileSupport.getLevels(uuid, tileSupport.getTileSize()));
        }
        return levels.get(uuid);
    }
    

    private void tileSize(StringBuffer buffer, HttpServletRequest request, HttpServletResponse res) throws IOException {
        String r = "Tile-size:"+tileSupport.getTileSize()+" "+tileSupport.getTileSize()+"\n";
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
