package cz.incad.Kramerius.imaging;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.antlr.stringtemplate.StringTemplate;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class DeepZoomServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(DeepZoomServlet.class.getName());
    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    @Named("cachedTileSupport")
    TileSupport tileSupport;

    
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
            renderXML(uuid, resp);
        }
    }
    
    private void renderXML(String uuid, HttpServletResponse resp) throws IOException {
        StringTemplate template = new StringTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Image TileSize=\"$tileSize$\" Overlap=\"0\" Format=\"jpg\" xmlns=\"http://schemas.microsoft.com/deepzoom/2008\"><Size Width=\"$width$\" Height=\"$height$\"/></Image>");
        template.setAttribute("tileSize", tileSupport.getTileSize());
        Image rawImage = tileSupport.getRawImage(uuid);
        template.setAttribute("width", rawImage.getWidth(null));
        template.setAttribute("height", rawImage.getHeight(null));
        resp.getWriter().println(template.toString());
    }

    private void renderTile(String uuid, String slevel, String stile, HttpServletRequest req,HttpServletResponse resp) throws IOException {
        
        try {
            int ilevel = Integer.parseInt(slevel);
            if (stile.contains(".")) {
                stile = stile.substring(0, stile.indexOf('.'));
                StringTokenizer tokenizer = new StringTokenizer(stile,"_");
                String scol = tokenizer.nextToken();
                String srow = tokenizer.nextToken();
                

                long levels = tileSupport.getLevels(uuid, 1);
                double scale = tileSupport.getScale(ilevel, levels);
                Dimension scaled = tileSupport.getScaledDimension(tileSupport.getMaxSize(uuid), scale);
                int rows = tileSupport.getRows(scaled);
                int cols = tileSupport.getCols(scaled);
                int base = Integer.parseInt(srow) * cols;
                base = base + Integer.parseInt(scol);
                
                BufferedImage tile = this.tileSupport.getTile(uuid, ilevel, base, 1);
                KrameriusImageSupport.writeImageToStream(tile, "JPEG", resp.getOutputStream());
//                return template.toString();
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
