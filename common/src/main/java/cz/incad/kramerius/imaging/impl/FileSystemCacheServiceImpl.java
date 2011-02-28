package cz.incad.kramerius.imaging.impl;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.imageio.stream.FileImageOutputStream;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.imaging.DeepZoomFullImageScaleFactor;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.imaging.DiscStrucutreForStore;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Cache deepZoom objects (full images, tiles and dzi descritors) on the HDD
 * 
 * @author pavels
 */
public class FileSystemCacheServiceImpl implements DeepZoomCacheService {

    private static final String DEEP_ZOOM_DESC_FILE = "deep_zoom";

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FileSystemCacheServiceImpl.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    DeepZoomTileSupport tileSupport;
    @Inject
    KConfiguration kConfiguration;
    // CachingSupport cachingSupport= new CachingSupport();
    @Inject
    DiscStrucutreForStore discStructureStore;

    @Override
    public void prepareCacheImage(String uuid, Dimension deep) {
        try {
            BufferedImage original = createDeepZoomOriginalImageFromFedoraRAW(uuid);
            prepareCacheImage(uuid, deep, original);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void prepareCacheImage(String uuid, int levelsOverTile) {
        try {
            BufferedImage original = createDeepZoomOriginalImageFromFedoraRAW(uuid);
            //KrameriusImageSupport.writeImageToStream(original, "jpeg", new FileOutputStream(new File(uuidFolder(uuid), uuid)));
            prepareCacheImage(uuid, levelsOverTile, original);

        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    
    public void prepareCacheImage(String uuid, int levelsOverTile, BufferedImage rawImage) {
        try {
            File uuidFolder = uuidFolder(uuid);

            Dimension rawDim = new Dimension(rawImage.getWidth(), rawImage.getHeight());
            int levels = (int) tileSupport.getLevels(rawImage, 1);
            int startLevel = tileSupport.getClosestLevel(rawDim, tileSupport.getTileSize());
            int maxLevel = Math.min(levels, startLevel + levelsOverTile);
            
            // finished dimension == rawDim
            //Dimension finishedDimension = tileSupport.getScaledDimension(rawDim, (startLevel+levelsOverTile-1), levels);
            writeDeepZoomDescriptor(uuid, rawDim, tileSupport.getTileSize(), new File(uuidFolder, DEEP_ZOOM_DESC_FILE));
            writeResolution(uuid, rawDim);
            
            for (int i = startLevel + 1; i < maxLevel; i++) {

                int curLevel = i;
                double scale = tileSupport.getScale(curLevel, levels);
                Dimension scaled = tileSupport.getScaledDimension(new Dimension(rawImage.getWidth(null), rawImage.getHeight(null)), scale);

                int rows = tileSupport.getRows(scaled);
                int cols = tileSupport.getCols(scaled);
                for (int r = 0; r < rows; r++) {
                    int b = r * cols;
                    for (int c = 0; c < cols; c++) {
                        int cell = b + c;
                        ScalingMethod method = ScalingMethod.valueOf(kConfiguration.getProperty("deepZoom.scalingMethod", "BICUBIC_STEPPED"));
                        boolean highQuality = kConfiguration.getConfiguration().getBoolean("deepZoom.iterateScaling", true);
                        BufferedImage tile = this.tileSupport.getTileFromBigImage(rawImage, curLevel, cell, 1, method, highQuality);
                        writeDeepZoomTile(uuid, curLevel, r, c, tile);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    @Override
    public void prepareCacheImage(String uuid, Dimension dimToFit, BufferedImage rawImage) {
        try {
            File uuidFolder = uuidFolder(uuid);
            Dimension rawDimension = new Dimension(rawImage.getWidth(), rawImage.getHeight());
            writeDeepZoomDescriptor(uuid, rawDimension, tileSupport.getTileSize(), new File(uuidFolder, DEEP_ZOOM_DESC_FILE));
            writeResolution(uuid, rawDimension);

            int levels = (int) tileSupport.getLevels(rawImage, 1);
            for (int i = levels - 1; i > 0; i--) {
                int curLevel = i;
                System.out.println("Current level : " + curLevel);
                double scale = tileSupport.getScale(curLevel, levels);
                Dimension scaled = tileSupport.getScaledDimension(new Dimension(rawImage.getWidth(null), rawImage.getHeight(null)), scale);

                int rows = tileSupport.getRows(scaled);
                int cols = tileSupport.getCols(scaled);
                for (int r = 0; r < rows; r++) {
                    int b = r * cols;
                    for (int c = 0; c < cols; c++) {
                        int cell = b + c;
                        ScalingMethod method = ScalingMethod.valueOf(kConfiguration.getProperty("deepZoom.scalingMethod", "BICUBIC_STEPPED"));
                        boolean highQuality = kConfiguration.getConfiguration().getBoolean("deepZoom.iterateScaling", true);
                        BufferedImage tile = this.tileSupport.getTileFromBigImage(rawImage, curLevel, cell, 1, method, highQuality);
                        writeDeepZoomTile(uuid, curLevel, r, c, tile);
                    }
                }
                // pokud se vleze na dlazdici, dal uz nepokracuju
                if (!greaterThen(scaled, dimToFit)) {
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private File uuidFolder(String uuid) throws IOException {
        File uuidFolder = this.discStructureStore.getUUIDFile(uuid, KConfiguration.getInstance().getDeepZoomCacheDir());
        if (!uuidFolder.exists()) {
            boolean created = uuidFolder.mkdirs();
            if (!created)
                throw new IOException("cannot create uuid folder '" + uuidFolder + "'");
        }
        return uuidFolder;
    }

    private boolean greaterThen(Dimension scaled, Dimension dimToFit) {
        return scaled.width > dimToFit.width || scaled.height > dimToFit.height;
    }

    @Override
    public void prepareCacheForUUID(String uuid, final int levelOverTileSize) throws IOException {
        KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
        if (krameriusModel.equals(KrameriusModels.PAGE)) {
            prepareCacheImage(uuid, levelOverTileSize);
        } else {
            fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {

                private int pageIndex = 1;

                @Override
                public void handle(Element elm, FedoraRelationship relation, int level) {
                    if (relation.equals(FedoraRelationship.hasPage)) {
                        try {
                            String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
                            PIDParser pidParse = new PIDParser(pid);
                            pidParse.disseminationURI();
                            String uuid = pidParse.getObjectId();
                            LOGGER.info("caching page " + (pageIndex++));
                            prepareCacheImage(uuid, levelOverTileSize);
                        } catch (DOMException e) {
                            LOGGER.severe(e.getMessage());
                        } catch (LexerException e) {
                            LOGGER.severe(e.getMessage());
                        }

                    }
                }

                @Override
                public boolean breakProcess() {
                    return false;
                }

                @Override
                public boolean accept(FedoraRelationship relation) {
                    return relation.name().startsWith("has");
                }
            });
        }
    }

    @Override
    public void prepareCacheForUUID(String uuid) throws IOException {
        KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
        if (krameriusModel.equals(KrameriusModels.PAGE)) {
            prepareCacheImage(uuid, new Dimension(tileSupport.getTileSize(), tileSupport.getTileSize()));
        } else {
            fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {

                private int pageIndex = 1;

                @Override
                public void handle(Element elm, FedoraRelationship relation, int level) {
                    if (relation.equals(FedoraRelationship.hasPage)) {
                        try {
                            String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
                            PIDParser pidParse = new PIDParser(pid);
                            pidParse.disseminationURI();
                            String uuid = pidParse.getObjectId();
                            LOGGER.info("caching page " + (pageIndex++));
                            prepareCacheImage(uuid, new Dimension(tileSupport.getTileSize(), tileSupport.getTileSize()));
                        } catch (DOMException e) {
                            LOGGER.severe(e.getMessage());
                        } catch (LexerException e) {
                            LOGGER.severe(e.getMessage());
                        }

                    }
                }

                @Override
                public boolean breakProcess() {
                    return false;
                }

                @Override
                public boolean accept(FedoraRelationship relation) {
                    return relation.name().startsWith("has");
                }
            });
        }

    }

    @Override
    public boolean isDeepZoomDescriptionPresent(String uuid) throws IOException {
        File desc = new File(uuidFolder(uuid), DEEP_ZOOM_DESC_FILE);
        return desc.exists() && desc.canRead();
    }

    @Override
    public void writeDeepZoomOriginalImage(String uuid, BufferedImage rawImage) throws IOException {
        FileImageOutputStream fosI = new FileImageOutputStream(new File(uuidFolder(uuid), uuid));
        try {
            KrameriusImageSupport.writeImageToStream(rawImage, "JPG", fosI, kConfiguration.getDeepZoomJPEGQuality());
        } finally {
            if (fosI != null) {
                fosI.close();
            }
        }
    }

    public void writeResolution(String uuid, Dimension dim) throws IOException {
        File resFile = new File(uuidFolder(uuid), dim.width + "_x_" + dim.height + ".resolution");
        if (!resFile.exists()) {
            boolean resFileCreated = resFile.createNewFile();
            if (!resFileCreated)
                throw new IOException("cannot create res file '" + resFile.getAbsolutePath() + "'");
        }
    }

    @Override
    public void writeDeepZoomDescriptor(String uuid, BufferedImage rawImage, int tileSize) throws IOException {
        File file = new File(uuidFolder(uuid), uuid);
        Dimension dim = new Dimension(rawImage.getWidth(), rawImage.getHeight());
        writeDeepZoomDescriptor(uuid, dim, tileSize, file);
    }
    
    public void writeDeepZoomDescriptor(String uuid, Dimension dim, int tileSize) throws IOException {
        File file = new File(uuidFolder(uuid), uuid);
        writeDeepZoomDescriptor(uuid, dim, tileSize, file);
    }


    @Override
    public InputStream getDeepZoomDescriptorStream(String uuid) throws IOException {
        return new FileInputStream(new File(uuidFolder(uuid), DEEP_ZOOM_DESC_FILE));
    }

    @Override
    public boolean isDeepZoomTilePresent(String uuid, int ilevel, int row, int col) throws IOException {
        ilevel = repairLevel(uuid, ilevel);
        File tileImageFolder = getTileImageFolder(uuid, ilevel);
        File tileImageFile = new File(tileImageFolder, getTileName(row, col));
        if ((tileImageFile.exists()) && (tileImageFile.canRead())) {
            return true;
        } else
            return false;
    }

    @Override
    public void writeDeepZoomTile(String uuid, int ilevel, int row, int col, BufferedImage tile) throws IOException {
        File folder = getTileImageFolder(uuid, ilevel);
        File tileImageFile = new File(folder, getTileName(row, col));
        FileImageOutputStream fosI = new FileImageOutputStream(tileImageFile);
        try {
            KrameriusImageSupport.writeImageToStream(tile, "JPG", fosI, kConfiguration.getDeepZoomJPEGQuality());
        } finally {
            if (fosI != null) {
                fosI.close();
            }
        }
    }

    @Override
    public InputStream getDeepZoomTileStream(String uuid, int ilevel, int row, int col) throws IOException {
        ilevel = repairLevel(uuid, ilevel);
        File tileImageFile = new File(getTileImageFolder(uuid, ilevel), getTileName(row, col));
        return new FileInputStream(tileImageFile);
    }

    /**
     * We are changing level if user want to smaller image then the one which
     * fit to one tile
     * 
     * @param uuid
     * @param ilevel
     * @return
     * @throws IOException
     */
    private int repairLevel(String uuid, int ilevel) throws IOException {
        Dimension resolution = getResolutionFromFile(uuid);
        if (resolution != null) {
            int closestLevel = tileSupport.getClosestLevel(resolution, tileSupport.getTileSize());
            if (closestLevel > ilevel) {
                return closestLevel;
            }
        }

        // no change
        return ilevel;
    }

    public Dimension getResolutionFromFile(String uuid) throws IOException {
        File[] resolutionFiles = uuidFolder(uuid).listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return (pathname.getName().endsWith(".resolution"));
            }
        });
        if ((resolutionFiles != null) && (resolutionFiles.length > 0)) {
            File resFile = resolutionFiles[0];
            String fileName = resFile.getName();
            fileName = fileName.substring(0, fileName.length() - ".resolution".length());
            String[] values = fileName.split("_x_");
            if (values.length == 2) {
                int width = Integer.parseInt(values[0]);
                int height = Integer.parseInt(values[1]);
                return new Dimension(width, height);
            }
        }
        return null;
    }

    @Override
    public boolean isDeepZoomOriginalPresent(String uuid) throws IOException {
        File file = new File(uuidFolder(uuid), uuid);
        return file.exists() && file.canRead();
    }

    public synchronized BufferedImage getDeepZoomOriginal(String uuid) throws IOException {
        if (isDeepZoomOriginalPresent(uuid)) {
            File file = new File(uuidFolder(uuid), uuid);
            BufferedImage bufImage = KrameriusImageSupport.readImage(file.toURI().toURL(), ImageMimeType.JPEG, 0);
            return bufImage;
        } else
            throw new IOException("uuid not found in cache !");
    }

    @Override
    public BufferedImage createDeepZoomOriginalImageFromFedoraRAW(String uuid) throws IOException {
        BufferedImage original;
        double val = KConfiguration.getInstance().getConfiguration().getDouble("deepZoom.originalScaleFactor", 1.0);
        DeepZoomFullImageScaleFactor factor = DeepZoomFullImageScaleFactor.findFactor(val);
        if (factor == null)
            throw new IllegalStateException("factor cannot be '" + val + "'.  Only " + Arrays.asList(DeepZoomFullImageScaleFactor.getAllowedVals()) + " are allowed ");

        original = tileSupport.getScaledRawImage(uuid, factor);
        return original;
    }

    private void writeDeepZoomDescriptor(String uuid, Dimension dim, int tileSize, File file) throws IOException {
        StringTemplate template = new StringTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Image TileSize=\"$tileSize$\" Overlap=\"0\" Format=\"jpg\" xmlns=\"http://schemas.microsoft.com/deepzoom/2008\"><Size Width=\"$width$\" Height=\"$height$\"/></Image>");
        template.setAttribute("tileSize", tileSize);
        template.setAttribute("width", dim.width);
        template.setAttribute("height", dim.height);
        File deepZoom = new File(uuidFolder(uuid), DEEP_ZOOM_DESC_FILE);
        if (!deepZoom.exists()) {
            deepZoom.createNewFile();
        }
        FileWriterWithEncoding writer = new FileWriterWithEncoding(deepZoom, "UTF-8");
        try {
            writer.write(template.toString());
        } finally {
            writer.close();
        }

    }

    private File getTileImageFolder(String uuid, int level) throws IOException {
        File oneImgFolder = uuidFolder(uuid);
        File levelFolder = new File(oneImgFolder, "" + level);
        if (!levelFolder.exists()) {
            boolean created = levelFolder.mkdirs();
            if (!created) {
                throw new IOException("cannot create folder '" + levelFolder.getAbsolutePath() + "'");
            }
        }
        return levelFolder;
    }

    private String getTileName(int row, int col) {
        return row + "_" + col;
    }

    @Override
    public boolean isResolutionFilePresent(String uuid) throws IOException {
        File uuidFile = this.discStructureStore.getUUIDFile(uuid, kConfiguration.getDeepZoomCacheDir());
        if (uuidFile != null) {
            File[] resFiles = uuidFile.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".resolution");
                }
            });
            return resFiles != null && resFiles.length > 0;
        } else return false;
    }

}
