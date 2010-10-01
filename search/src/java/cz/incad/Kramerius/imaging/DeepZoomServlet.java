package cz.incad.Kramerius.imaging;

import static cz.incad.kramerius.utils.IOUtils.copyStreams;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.ReadableByteChannel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.AbstractImageServlet;
import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.CacheService;
import cz.incad.kramerius.imaging.TileSupport;
import cz.incad.kramerius.imaging.impl.CachingSupport;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class DeepZoomServlet extends AbstractImageServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(DeepZoomServlet.class.getName());
    
    public static final String CACHE_KEY_PARAMETER = "cache";
    
    
    @Inject
    TileSupport tileSupport;

    @Inject
    @Named("fedora3")
    Provider<Connection> fedora3Provider;

    @Inject 
    CacheService cacheService;
    
    
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

    private void renderDZI(String uuid, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setDateHaders(uuid, resp);
        setResponseCode(uuid, req, resp);
        String iipServer = KConfiguration.getInstance().getUrlOfIIPServer();
        if (!iipServer.equals("")) {
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

    private void renderIIPDZIDescriptor(String uuid, HttpServletResponse resp) throws MalformedURLException, IOException, SQLException {
        String dataStreamPath = getDataStreamPath(uuid);
        if (dataStreamPath != null) {
            StringTemplate dziUrl = stGroup().getInstanceOf("dziurl");
            setStringTemplateModel(uuid, dataStreamPath, dziUrl);
            copyFromImageServer(dziUrl.toString(),resp);
        }
    }

    private void setStringTemplateModel(String uuid, String dataStreamPath,
            StringTemplate template) throws UnsupportedEncodingException, IOException {
        List<String> folderList = new ArrayList<String>();
        File currentFile = new File(dataStreamPath);
        while(!currentFile.getName().equals("data")) {
            folderList.add(0,URLEncoder.encode(currentFile.getName(), "UTF-8"));
            currentFile = currentFile.getParentFile();
        }
        template.setAttribute("dataPath", KConfiguration.getInstance().getFedoraDataFolderInIIPServer());
        template.setAttribute("folderList", folderList);
        template.setAttribute("iipServer", KConfiguration.getInstance().getUrlOfIIPServer());
        String smimeType = fedoraAccess.getMimeTypeForStream("uuid:"+uuid, "IMG_FULL");
        ImageMimeType mimeType = ImageMimeType.loadFromMimeType(smimeType);
        if (mimeType != null) {
            String extension = mimeType.getDefaultFileExtension();
            if (!dataStreamPath.endsWith("."+extension)) {
                template.setAttribute("extension", "."+extension);
            } else {
                template.setAttribute("extension", "");
            }
        } else {
            template.setAttribute("extension", "");
        }
    }

    private String getDataStreamPath(String uuid) throws SQLException {
        Connection connection = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            connection = this.fedora3Provider.get();
            pstm = connection.prepareStatement("select * from datastreampaths where token like ?");
            pstm.setString(1, "uuid:"+uuid+"+IMG_FULL%");
            rs = pstm.executeQuery();
            if(rs.next()) {
                return rs.getString("path");
            } else return null;
        } finally {
            if (rs != null) {
                try { 
                    rs.close(); 
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (pstm != null) {
                try {
                    pstm.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }

    private void renderEmbededDZIDescriptor(String uuid,
            HttpServletResponse resp) throws IOException, FileNotFoundException {
        if (!cacheService.isDeepZoomDescriptionPresent(uuid)) {
            Image rawImage = tileSupport.getRawImage(uuid);
            cacheService.writeDeepZoomFullImage(uuid, rawImage);
            cacheService.writeDeepZoomDescriptor(uuid, rawImage, tileSupport.getTileSize());
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

    private void renderTile(String uuid, String slevel, String stile, HttpServletRequest req,HttpServletResponse resp) throws IOException {
        setDateHaders(uuid, resp);
        setResponseCode(uuid, req, resp);
        String iipServer = KConfiguration.getInstance().getUrlOfIIPServer();
        if (!iipServer.equals("")) {
            try {
                renderIIPTile(uuid,slevel, stile, resp);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }else {
            renderEmbededTile(uuid, slevel, stile, resp);
        }
    }
    
    
    private void renderIIPTile(String uuid, String slevel, String stile, HttpServletResponse resp) throws SQLException, UnsupportedEncodingException, IOException {
        String dataStreamPath = getDataStreamPath(uuid);
        if (dataStreamPath != null) {
            StringTemplate tileUrl = stGroup().getInstanceOf("tileurl");
            setStringTemplateModel(uuid, dataStreamPath, tileUrl);
            tileUrl.setAttribute("level", slevel);
            tileUrl.setAttribute("tile", stile);
            copyFromImageServer(tileUrl.toString(),resp);
        }
    }

    private void renderEmbededTile(String uuid, String slevel, String stile,
            HttpServletResponse resp) throws IOException {
        try {
            int ilevel = Integer.parseInt(slevel);
            if (stile.contains(".")) {
                stile = stile.substring(0, stile.indexOf('.'));
                StringTokenizer tokenizer = new StringTokenizer(stile,"_");
                String scol = tokenizer.nextToken();
                String srow = tokenizer.nextToken();
                boolean tilePresent = cacheService.isDeepZoomTilePresent(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
                if (!tilePresent) {
//                    File dFile = cacheService.getDeepZoomLevelsFile(uuid);
                    long levels =  tileSupport.getLevels(uuid, 1);
                    double scale = tileSupport.getScale(ilevel, levels);
                    Dimension scaled = tileSupport.getScaledDimension(tileSupport.getMaxSize(uuid), scale);
                    int rows = tileSupport.getRows(scaled);
                    int cols = tileSupport.getCols(scaled);
                    int base = Integer.parseInt(srow) * cols;
                    base = base + Integer.parseInt(scol);
                    
                    BufferedImage tile = this.tileSupport.getTile(uuid, ilevel, base, 1);
                    cacheService.writeDeepZoomTile(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol), tile);
                }
                InputStream is = cacheService.getDeepZoomTileStream(uuid, ilevel, Integer.parseInt(srow), Integer.parseInt(scol));
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
    
    
    private void copyFromImageServer(String urlString, HttpServletResponse resp) throws MalformedURLException, IOException {
        System.out.println(urlString);
        URLConnection con = RESTHelper.openConnection(urlString, "", "");
        String contentType = con.getContentType();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStreams(con.getInputStream(), bos);
        //System.out.println(new String(bos.toByteArray()));
        copyStreams(new ByteArrayInputStream(bos.toByteArray()), resp.getOutputStream());
        resp.setContentType(contentType);
    }
    
    private StringTemplateGroup stGroup() {
        InputStream is = this.getClass().getResourceAsStream("iipforward.stg");
        StringTemplateGroup grp = new StringTemplateGroup(new InputStreamReader(is), DefaultTemplateLexer.class);
        return grp;
    }

}
