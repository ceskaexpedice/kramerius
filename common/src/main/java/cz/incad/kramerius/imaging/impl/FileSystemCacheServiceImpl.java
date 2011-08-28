package cz.incad.kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.stream.FileImageOutputStream;

import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.output.FileWriterWithEncoding;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.TreeNodeProcessor;
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
    public void prepareCacheImage(String pid, Dimension deep) {
        try {
            BufferedImage original = createDeepZoomOriginalImageFromFedoraRAW(pid);
            prepareCacheImage(pid, deep, original);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public void prepareCacheImage(String pid, int levelsOverTile) {
        try {
            BufferedImage original = createDeepZoomOriginalImageFromFedoraRAW(pid);
            //KrameriusImageSupport.writeImageToStream(original, "jpeg", new FileOutputStream(new File(uuidFolder(uuid), uuid)));
            prepareCacheImage(pid, levelsOverTile, original);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    
    public void prepareCacheImage(String pid, int levelsOverTile, BufferedImage rawImage) {
        try {
            File uuidFolder = uuidFolder(pid);

            Dimension rawDim = new Dimension(rawImage.getWidth(), rawImage.getHeight());
            int levels = (int) tileSupport.getLevels(rawImage, 1);
            int startLevel = tileSupport.getClosestLevel(rawDim, tileSupport.getTileSize());
            int maxLevel = Math.min(levels, startLevel + levelsOverTile);
            
            writeDeepZoomDescriptor(pid, rawDim, tileSupport.getTileSize(), new File(uuidFolder, DEEP_ZOOM_DESC_FILE));
            writeResolution(pid, rawDim);
            
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
                        writeDeepZoomTile(pid, curLevel, r, c, tile);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    @Override
    public void prepareCacheImage(String pid, Dimension dimToFit, BufferedImage rawImage) {
        try {
            File uuidFolder = uuidFolder(pid);
            Dimension rawDimension = new Dimension(rawImage.getWidth(), rawImage.getHeight());
            writeDeepZoomDescriptor(pid, rawDimension, tileSupport.getTileSize(), new File(uuidFolder, DEEP_ZOOM_DESC_FILE));
            writeResolution(pid, rawDimension);

            int levels = (int) tileSupport.getLevels(rawImage, 1);
            for (int i = levels - 1; i > 0; i--) {
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
                        writeDeepZoomTile(pid, curLevel, r, c, tile);
                    }
                }
                // image fit to one tile, this is the end for me
                if (!greaterThen(scaled, dimToFit)) {
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private File uuidFolder(String pid) throws IOException {
        try {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();

            File uuidFolder = this.discStructureStore.getUUIDFile(pidParser.getObjectId(), KConfiguration.getInstance().getDeepZoomCacheDir());
            if (!uuidFolder.exists()) {
                boolean created = uuidFolder.mkdirs();
                if (!created)
                    throw new IOException("cannot create uuid folder '" + uuidFolder + "'");
            }
            return uuidFolder;
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

    private boolean greaterThen(Dimension scaled, Dimension dimToFit) {
        return scaled.width > dimToFit.width || scaled.height > dimToFit.height;
    }

    @Override
    public void prepareCacheForPID(String pid, final int levelOverTileSize) throws IOException, ProcessSubtreeException {
        if (fedoraAccess.isImageFULLAvailable(pid)) {
            prepareCacheImage(pid, levelOverTileSize);
        } else {
            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
                private int pageIndex = 1;
                
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    LOGGER.fine("caching page " + (pageIndex++));
                    prepareCacheImage(pid, levelOverTileSize);
                    
                }
                
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }
            });
        }
    }

    @Override
    public void prepareCacheForPID(String pid) throws IOException, ProcessSubtreeException {

        if (fedoraAccess.isImageFULLAvailable(pid)) {
            prepareCacheImage(pid, new Dimension(tileSupport.getTileSize(), tileSupport.getTileSize()));
        } else {

            fedoraAccess.processSubtree(pid, new TreeNodeProcessor() {
                private int pageIndex = 1;
                @Override
                public void process(String pid, int level) throws ProcessSubtreeException {
                    LOGGER.fine("caching page " + (pageIndex++));
                    prepareCacheImage(pid, new Dimension(tileSupport.getTileSize(), tileSupport.getTileSize()));
                }
                
                @Override
                public boolean breakProcessing(String pid, int level) {
                    return false;
                }
            
            });
            
        }

    }

    @Override
    public boolean isDeepZoomDescriptionPresent(String pid) throws IOException {
        File desc = new File(uuidFolder(pid), DEEP_ZOOM_DESC_FILE);
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

    public void writeResolution(String pid, Dimension dim) throws IOException {
        File resFile = new File(uuidFolder(pid), dim.width + "_x_" + dim.height + ".resolution");
        if (!resFile.exists()) {
            boolean resFileCreated = resFile.createNewFile();
            if (!resFileCreated)
                throw new IOException("cannot create res file '" + resFile.getAbsolutePath() + "'");
        }
    }

    @Override
    public void writeDeepZoomDescriptor(String pid, BufferedImage rawImage, int tileSize) throws IOException {
        File file = new File(uuidFolder(pid), pid);
        Dimension dim = new Dimension(rawImage.getWidth(), rawImage.getHeight());
        writeDeepZoomDescriptor(pid, dim, tileSize, file);
    }
    
    public void writeDeepZoomDescriptor(String pid, Dimension dim, int tileSize) throws IOException {
        File file = new File(uuidFolder(pid), pid);
        writeDeepZoomDescriptor(pid, dim, tileSize, file);
    }


    @Override
    public InputStream getDeepZoomDescriptorStream(String pid) throws IOException {
        return new FileInputStream(new File(uuidFolder(pid), DEEP_ZOOM_DESC_FILE));
    }

    @Override
    public boolean isDeepZoomTilePresent(String pid, int ilevel, int row, int col) throws IOException {
        ilevel = repairLevel(pid, ilevel);
        File tileImageFolder = getTileImageFolder(pid, ilevel);
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
    public InputStream getDeepZoomTileStream(String pid, int ilevel, int row, int col) throws IOException {
        ilevel = repairLevel(pid, ilevel);
        File tileImageFile = new File(getTileImageFolder(pid, ilevel), getTileName(row, col));
        return new FileInputStream(tileImageFile);
    }

    /**
     * We are changing level if user want to smaller image then the one which
     * fit to one tile
     * 
     * @param pid
     * @param ilevel
     * @return
     * @throws IOException
     */
    private int repairLevel(String pid, int ilevel) throws IOException {
        Dimension resolution = getResolutionFromFile(pid);
        if (resolution != null) {
            int closestLevel = tileSupport.getClosestLevel(resolution, tileSupport.getTileSize());
            if (closestLevel > ilevel) {
                return closestLevel;
            }
        }

        // no change
        return ilevel;
    }

    public Dimension getResolutionFromFile(String pid) throws IOException {
        File[] resolutionFiles = uuidFolder(pid).listFiles(new FileFilter() {

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
    public boolean isDeepZoomOriginalPresent(String pid) throws IOException {
        File file = new File(uuidFolder(pid), pid);
        return file.exists() && file.canRead();
    }

    public synchronized BufferedImage getDeepZoomOriginal(String pid) throws IOException {
        if (isDeepZoomOriginalPresent(pid)) {
            File file = new File(uuidFolder(pid), pid);
            BufferedImage bufImage = KrameriusImageSupport.readImage(file.toURI().toURL(), ImageMimeType.JPEG, 0);
            return bufImage;
        } else
            throw new IOException("uuid not found in cache !");
    }

    @Override
    public BufferedImage createDeepZoomOriginalImageFromFedoraRAW(String pid) throws IOException {
        BufferedImage original;
        double val = KConfiguration.getInstance().getConfiguration().getDouble("deepZoom.originalScaleFactor", 1.0);
        DeepZoomFullImageScaleFactor factor = DeepZoomFullImageScaleFactor.findFactor(val);
        if (factor == null)
            throw new IllegalStateException("factor cannot be '" + val + "'.  Only " + Arrays.asList(DeepZoomFullImageScaleFactor.getAllowedVals()) + " are allowed ");

        original = tileSupport.getScaledRawImage(pid, factor);
        return original;
    }

    private void writeDeepZoomDescriptor(String pid, Dimension dim, int tileSize, File file) throws IOException {
        StringTemplate template = new StringTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Image TileSize=\"$tileSize$\" Overlap=\"0\" Format=\"jpg\" xmlns=\"http://schemas.microsoft.com/deepzoom/2008\"><Size Width=\"$width$\" Height=\"$height$\"/></Image>");
        template.setAttribute("tileSize", tileSize);
        template.setAttribute("width", dim.width);
        template.setAttribute("height", dim.height);
        File deepZoom = new File(uuidFolder(pid), DEEP_ZOOM_DESC_FILE);
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

    private File getTileImageFolder(String pid, int level) throws IOException {
        File oneImgFolder = uuidFolder(pid);
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
    public boolean isResolutionFilePresent(String pid) throws IOException {
        try {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();
            
            File uuidFile = this.discStructureStore.getUUIDFile(pidParser.getObjectId(), kConfiguration.getDeepZoomCacheDir());
            if (uuidFile != null) {
                File[] resFiles = uuidFile.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".resolution");
                    }
                });
                return resFiles != null && resFiles.length > 0;
            } else return false;
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

}
