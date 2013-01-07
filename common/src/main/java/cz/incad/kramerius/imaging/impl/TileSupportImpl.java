package cz.incad.kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.imaging.DeepZoomFullImageScaleFactor;
import cz.incad.kramerius.imaging.DeepZoomTileSupport;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

/**
 * Basic implementation of {@link DeepZoomTileSupport}
 * @author pavels
 */
public class TileSupportImpl implements DeepZoomTileSupport {

	static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(TileSupportImpl.class.getName());
	
    //private static final int TILE_SIZE = 2048;
    private static final int TILE_SIZE = 512;

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    
    @Inject
    KConfiguration kConfiguration;
    
    @Override
    public int getTileSize() {
    	return KConfiguration.getInstance().getDeepZoomTileSize();
    }

    @Override
    public long getLevels(String pid, int minSize, DeepZoomFullImageScaleFactor scaleFactor) throws IOException {
        BufferedImage rawImg = getScaledRawImage(pid, scaleFactor);
        return getLevels( rawImg,minSize);
    }

    @Override
	public long getLevels( BufferedImage rawImg,int minSize) {
		int max = Math.max(rawImg.getHeight(null), rawImg.getWidth(null));
        return getLevelsInternal(max, minSize);
	}

    @Override
	public int getLevels(Dimension dim, int minSize) {
        int max = Math.max(dim.width, dim.height);
        return getLevelsInternal(max, minSize);
	}
	
    private int getLevelsInternal(int max, int minSize) {
        int currentMax = max;
        int level = 1;
        while(currentMax >= minSize) {
            currentMax = currentMax / 2;
            level+=1;
        }
        
        return level;
    }

    
    
    
//    public long getMaxLevels(String pid) {
//        Image rawImg = getRawImage(pid);
//        int max = Math.max(rawImg.getHeight(null), rawImg.getWidth(null));
//        int levels = Math.ceil(Math.log(max) / Math.log(2));
//        return levels;
//    }
    
    @Override
    public Dimension getScaledDimension(Dimension dim, int displayLevel, int maxLevel) {
        double scale = getScale(displayLevel, maxLevel);
        return getScaledDimension(dim,scale);
    }

    @Override
    @Deprecated
    public BufferedImage getRawImage(String pid) throws IOException {
    	LOGGER.info("reading raw image");
    	try {
            BufferedImage rawImg = KrameriusImageSupport.readImage(pid, FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, 0);
            return rawImg;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    
    @Override
    public BufferedImage getScaledRawImage(String pid, DeepZoomFullImageScaleFactor factor) throws IOException {
    	LOGGER.info("reading raw image");
    	try {
            BufferedImage rawImg = KrameriusImageSupport.readImage(pid, FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, 0);
            if (factor != DeepZoomFullImageScaleFactor.ORIGINAL) {
                double scale = factor.getValue();
                int scaledWidth = (int) (rawImg.getWidth() * scale);
                int scaledHeight = (int) (rawImg.getHeight() * scale);
                
        		ScalingMethod method = ScalingMethod.valueOf(KConfiguration.getInstance().getProperty(
        				"scalingMethod", "BICUBIC_STEPPED"));
        		boolean higherQuality = true;
                return KrameriusImageSupport.scale(rawImg, scaledWidth, scaledHeight, method, higherQuality);
            	
            } else return rawImg;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
	}
    
    @Override
    public Dimension getMaxSize(String pid, DeepZoomFullImageScaleFactor factor) throws IOException {
        Image rawImg = getRawImage(pid);
        return new Dimension(rawImg.getWidth(null), rawImg.getHeight(null));
    }



    @Override
    public BufferedImage getTile(String pid, int displayLevel, int displayTile, int minSize, ScalingMethod scalingMethod, boolean iterateScaling, DeepZoomFullImageScaleFactor scaledFactor) throws IOException {
    	BufferedImage image = getScaledRawImage(pid, scaledFactor);
    	return getTileFromBigImage(image, displayLevel, displayTile, minSize, scalingMethod, iterateScaling);
    }

    @Override
	public BufferedImage getTileFromBigImage(BufferedImage image,int displayLevel, int displayTile,
			int minSize, ScalingMethod method, boolean iterateScaling) {
		long maxLevel = getLevels(image, minSize);
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        Dimension originalDim = new Dimension(width, height);
        
        double scale = getScale(displayLevel, maxLevel);
        LOGGER.info("tilesupport calculated scale : "+scale);
        Dimension scaledDim = getScaledDimension(originalDim, scale);
        LOGGER.info("tilesupport calculated dim : "+scaledDim);
        
        //int rows = getRows(scaledDim);
        int cols = getCols(scaledDim);
        
        Image scaled = null;
        if ((width == scaledDim.width) && (height == scaledDim.height)) {
        	scaled = image;
        } else {
            scaled = KrameriusImageSupport.scale(image, scaledDim.width, scaledDim.height, method, iterateScaling);
        }

        
        
        int rowTile = displayTile / cols;
        int colTile = displayTile % cols;

        int tileStartY = rowTile * getTileSize();
        int tileStartX = colTile * getTileSize();
        
        int tileWidth = scaledDim.width >= getTileSize() ? Math.min(getTileSize(), scaledDim.width - tileStartX) : scaledDim.width;
        int tileHeight = scaledDim.height >= getTileSize() ? Math.min(getTileSize(), scaledDim.height - tileStartY) : scaledDim.height;
        
        BufferedImage buffImage = new BufferedImage(tileWidth, tileHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2d = (Graphics2D) buffImage.getGraphics();
        
        graphics2d.drawImage(scaled, -tileStartX,-tileStartY, null);
        return buffImage;
	}

    @Override
    public int getCols(Dimension scaledDim) {
        int cols = (int) (scaledDim.width / getTileSize());
        if (scaledDim.width % getTileSize() != 0) {cols += 1; }
        return cols;
    }

    @Override
    public int getRows(Dimension scaledDim) {
        int rows = scaledDim.height / getTileSize();
        if (scaledDim.height % getTileSize() != 0) {rows +=1;}
        return rows;
    }

    @Override
    public Dimension getScaledDimension(Dimension originalDim, double scale) {
        int scaledWidth  =(int) (originalDim.getWidth() / scale);
        if (scaledWidth == 0) scaledWidth = 1;

        int scaledHeight = (int) (originalDim.getHeight() / scale);
        if (scaledHeight == 0) scaledHeight = 1;
        
        return new Dimension(scaledWidth, scaledHeight);
    }

    @Override
    public double getScale(int displayLevel, long maxLevel) {
        long b = (maxLevel-1)-displayLevel;
        double scale = Math.pow(2, b);
        return scale;
    }

    @Override
    public double getClosestScale(Dimension originalSize, int sizeToFit, int minSize) {
        long maxLevel = getLevelsInternal(originalSize.width > originalSize.height ? originalSize.width : originalSize.height, minSize);
        int level = getClosestLevel(originalSize, sizeToFit, minSize);
        return getScale(level, maxLevel);
    }
    
    @Override
    public int getClosestLevel(Dimension originalSize, int sizeToFit, int minSize) {
        int maxLevel = getLevelsInternal(originalSize.width > originalSize.height ? originalSize.width : originalSize.height, minSize);
        for (int i = maxLevel - 1; i >1; i--) {
            double scale = getScale(i, maxLevel);
            int targetWidth = (int) (originalSize.width / scale);
            int targetHeight = (int) (originalSize.height / scale);
            if (targetWidth < sizeToFit && targetHeight < sizeToFit) return i;
        }
        return 0;
    }

}
