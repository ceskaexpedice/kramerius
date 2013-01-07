package cz.incad.kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.logging.Level;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;

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
import cz.incad.kramerius.imaging.paths.DirPath;
import cz.incad.kramerius.imaging.paths.FilePath;
import cz.incad.kramerius.imaging.paths.Path;
import cz.incad.kramerius.imaging.paths.PathFilter;
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

    void prepareCacheImage(String pid, int levelsOverTile) {
        try {
            BufferedImage original = createDeepZoomOriginalImageFromFedoraRAW(pid);
            //KrameriusImageSupport.writeImageToStream(original, "jpeg", new FileOutputStream(new File(uuidFolder(uuid), uuid)));
            prepareCacheImage(pid, levelsOverTile, original);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    
    void prepareCacheImage(String pid, int levelsOverTile, BufferedImage rawImage) {
        try {
            DirPath uuidFolder = uuidFolder(pid);

            LOGGER.info("Preparing cache for pid "+pid+" in uuidFolder '"+uuidFolder.getName()+"'");           
            
            Dimension rawDim = new Dimension(rawImage.getWidth(), rawImage.getHeight());
            int levels = (int) tileSupport.getLevels(rawImage, 1);
            int startLevel = tileSupport.getClosestLevel(rawDim, tileSupport.getTileSize(), 1);
            int maxLevel = Math.min(levels, startLevel + levelsOverTile);
            
            writeDeepZoomDescriptor(pid, rawDim, tileSupport.getTileSize());
            writeResolution(pid, rawDim);
            
            for (int i = startLevel ; i < maxLevel; i++) {

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
            Dimension rawDimension = new Dimension(rawImage.getWidth(), rawImage.getHeight());
            writeDeepZoomDescriptor(pid, rawDimension, tileSupport.getTileSize());
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

   cz.incad.kramerius.imaging.paths.DirPath uuidFolder(String pid) throws IOException {
        try {
            PIDParser pidParser = new PIDParser(pid);
            pidParser.objectPid();

            Path uuidFolder =  this.discStructureStore.getUUIDFile(pidParser.getObjectId(), kConfiguration.getDeepZoomCacheDir());
            if (!uuidFolder.exists()) {
                return uuidFolder.makeDir();
            } else {
                return (DirPath) uuidFolder;
            }
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
                    try {
                        if (fedoraAccess.isImageFULLAvailable(pid)) {
                            LOGGER.fine("caching page " + (pageIndex++));
                            prepareCacheImage(pid, levelOverTileSize);
                        }
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                }
                
                @Override
                public boolean skipBranch(String pid, int level) {
                    return false;
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
                public boolean skipBranch(String pid, int level) {
                    return false;
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
        DirPath dpath = uuidFolder(pid);
        Path child = dpath.child(DEEP_ZOOM_DESC_FILE);
        return child != null;
    }

    @Override
    public void writeDeepZoomOriginalImage(String uuid, BufferedImage rawImage) throws IOException {
        DirPath dpath = uuidFolder(uuid);
        FilePath fpath = (FilePath) dpath.child(uuid);
        if (fpath == null) {
            fpath = dpath.createChildFile(uuid);
            ImageOutputStreamImpl iostr = fpath.openImageOutputStream();
            try {
                KrameriusImageSupport.writeImageToStream(rawImage, "JPG", iostr, kConfiguration.getDeepZoomJPEGQuality());
            } finally {
                if (iostr != null) {
                    iostr.close();
                }
            }
        }
    }

    public void writeResolution(String pid, Dimension dim) throws IOException {
        DirPath dpath = uuidFolder(pid);
        dpath.createChildFile(dim.width + "_x_" + dim.height + ".resolution");
    }

    @Override
    public void writeDeepZoomDescriptor(String pid, BufferedImage rawImage, int tileSize) throws IOException {
        Dimension dim = new Dimension(rawImage.getWidth(), rawImage.getHeight());
        writeDeepZoomDescriptorImpl(pid, dim, tileSize);
    }
    
    public void writeDeepZoomDescriptor(String pid, Dimension dim, int tileSize) throws IOException {
        writeDeepZoomDescriptorImpl(pid, dim, tileSize);
    }


    @Override
    public InputStream getDeepZoomDescriptorStream(String pid) throws IOException {
        DirPath dp = uuidFolder(pid);
        FilePath fp = dp.createChildFile(DEEP_ZOOM_DESC_FILE);
        return fp.openInputStream();
    }

    @Override
    public boolean isDeepZoomTilePresent(String pid, int ilevel, int row, int col) throws IOException {
        ilevel = repairLevel(pid, ilevel);
        DirPath dp = getTileImageFolder(pid, ilevel);
        return dp.child(getTileName(row, col)) != null;
    }

    @Override
    public void writeDeepZoomTile(String uuid, int ilevel, int row, int col, BufferedImage tile) throws IOException {
        DirPath dp = getTileImageFolder(uuid, ilevel);
        FilePath fp = dp.createChildFile(getTileName(row, col));
        ImageOutputStreamImpl fosI = fp.openImageOutputStream();
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
        DirPath dp = getTileImageFolder(pid, ilevel);
        FilePath fp = dp.createChildFile(getTileName(row, col));
        return fp.openInputStream();
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
        /*
        Dimension resolution = getResolutionFromFile(pid);
        if (resolution != null) {
            int closestLevel = tileSupport.getClosestLevel(resolution, tileSupport.getTileSize(), 1);
            if (closestLevel > ilevel) {
                return closestLevel;
            }
        }*/

        // no change
        return ilevel;
    }

    @Override
    public Dimension getResolutionFromFile(String pid) throws IOException {
        DirPath dp = uuidFolder(pid);
        Path[] resolutionFiles = dp.list(new PathFilter() {
            
            @Override
            public boolean accept(Path path) {
                return (path.getName().endsWith(".resolution"));
            }
        });        
        if ((resolutionFiles != null) && (resolutionFiles.length > 0)) {
            Path rp = resolutionFiles[0];
            String fileName = rp.getName();
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
        DirPath dirPath = uuidFolder(pid);
        Path fp = dirPath.child(pid);
        return fp != null;
    }

    @Override
    public synchronized BufferedImage getDeepZoomOriginal(String pid) throws IOException {
        if (isDeepZoomOriginalPresent(pid)) {
            DirPath dp = uuidFolder(pid);
            FilePath fp = dp.createChildFile(pid);
            BufferedImage bufImage = KrameriusImageSupport.readImage(fp.toURL(), ImageMimeType.JPEG, 0);
            return bufImage;
        } else
            throw new IOException("uuid not found in cache !");
    }

    @Override
    public BufferedImage createDeepZoomOriginalImageFromFedoraRAW(String pid) throws IOException {
        BufferedImage original;
        double val = kConfiguration.getConfiguration().getDouble("deepZoom.originalScaleFactor", 1.0);
        DeepZoomFullImageScaleFactor factor = DeepZoomFullImageScaleFactor.findFactor(val);
        if (factor == null)
            throw new IllegalStateException("factor cannot be '" + val + "'.  Only " + Arrays.asList(DeepZoomFullImageScaleFactor.getAllowedVals()) + " are allowed ");

        original = tileSupport.getScaledRawImage(pid, factor);
        return original;
    }

    void writeDeepZoomDescriptorImpl(String pid, Dimension dim, int tileSize) throws IOException {
        StringTemplate template = new StringTemplate("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Image TileSize=\"$tileSize$\" Overlap=\"0\" Format=\"jpg\" xmlns=\"http://schemas.microsoft.com/deepzoom/2008\"><Size Width=\"$width$\" Height=\"$height$\"/></Image>");
        template.setAttribute("tileSize", tileSize);
        template.setAttribute("width", dim.width);
        template.setAttribute("height", dim.height);
        Writer writer = deepZoomWriter(pid);
        try {
            writer.write(template.toString());
        } finally {
            writer.close();
        }

    }

    Writer deepZoomWriter(String pid) throws IOException {
        DirPath dp = uuidFolder(pid);
        FilePath fp = dp.createChildFile(DEEP_ZOOM_DESC_FILE);
        return fp.openWriter();
    }

    private DirPath getTileImageFolder(String pid, int level) throws IOException {
        DirPath oneImgFolder = uuidFolder(pid);
        DirPath levelFolder = oneImgFolder.createChildDir("" + level);
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
            
            Path uuidFile = this.discStructureStore.getUUIDFile(pidParser.getObjectId(), kConfiguration.getDeepZoomCacheDir());
            if (uuidFile != null && (uuidFile instanceof DirPath)) {
                Path[] resFiles = ((DirPath)uuidFile).list(new PathFilter() {
                    
                    @Override
                    public boolean accept(Path path) {
                        return path.getName().endsWith(".resolution");
                    }
                });
                
                return resFiles != null && resFiles.length > 0;
            } else return false;
        } catch (LexerException e) {
            throw new IOException(e);
        }
    }

}
