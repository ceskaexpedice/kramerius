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
import cz.incad.kramerius.imaging.TileSupport;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class TileSupportImpl implements TileSupport {

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
    public long getLevels(String uuid, int minSize) throws IOException {
        Image rawImg = getRawImage(uuid);
        return getLevels( rawImg,minSize);
    }

	public long getLevels( Image rawImg,int minSize) {
		int max = Math.max(rawImg.getHeight(null), rawImg.getWidth(null));
        return getLevelsInternal(max, minSize);
	}

    private long getLevelsInternal(int max, int minSize) {
        int currentMax = max;
        int level = 1;
        while(currentMax >= minSize) {
            currentMax = currentMax / 2;
            level+=1;
        }
        
        return level;
    }

    
//    public long getMaxLevels(String uuid) {
//        Image rawImg = getRawImage(uuid);
//        int max = Math.max(rawImg.getHeight(null), rawImg.getWidth(null));
//        int levels = Math.ceil(Math.log(max) / Math.log(2));
//        return levels;
//    }
    
    @Override
    public Image getRawImage(String uuid) throws IOException {
        try {
            Image rawImg = KrameriusImageSupport.readImage(uuid, FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, 0);
            return rawImg;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Dimension getMaxSize(String uuid) throws IOException {
        Image rawImg = getRawImage(uuid);
        return new Dimension(rawImg.getWidth(null), rawImg.getHeight(null));
    }



    @Override
    public BufferedImage getTile(String uuid, int displayLevel, int displayTile, int minSize) throws IOException {
    	Image image = getRawImage(uuid);
    	return getTile(image, displayLevel, displayTile, minSize);
    }

	public BufferedImage getTile(Image image,int displayLevel, int displayTile,
			int minSize) {
		long maxLevel = getLevels(image, minSize);
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        Dimension originalDim = new Dimension(width, height);

        double scale = getScale(displayLevel, maxLevel);
        Dimension scaledDim = getScaledDimension(originalDim, scale);
        
        //int rows = getRows(scaledDim);
        int cols = getCols(scaledDim);
        
        Image scaled = null;
        if ((width == scaledDim.width) && (height == scaledDim.height)) {
        	scaled = image;
        } else {
            scaled = KrameriusImageSupport.scale(image, scaledDim.width, scaledDim.height);
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

    public int getCols(Dimension scaledDim) {
        int cols = (int) (scaledDim.width / getTileSize());
        if (scaledDim.width % getTileSize() != 0) {cols += 1; }
        return cols;
    }

    public int getRows(Dimension scaledDim) {
        int rows = scaledDim.height / getTileSize();
        if (scaledDim.height % getTileSize() != 0) {rows +=1;}
        return rows;
    }

    public Dimension getScaledDimension(Dimension originalDim, double scale) {
        int scaledWidth  =(int) (originalDim.getWidth() / scale);
        if (scaledWidth == 0) scaledWidth = 1;

        int scaledHeight = (int) (originalDim.getHeight() / scale);
        if (scaledHeight == 0) scaledHeight = 1;
        
        return new Dimension(scaledWidth, scaledHeight);
    }

    public double getScale(int displayLevel, long maxLevel) {
        long b = (maxLevel-1)-displayLevel;
        double scale = Math.pow(2, b);
        return scale;
    }

    
    

	public static void main(String[] args) {
        TileSupportImpl tileSupport = new TileSupportImpl();
        //4224 x 3168
        int width = 4224;
        int height = 3168;
        long levelsInternal = tileSupport.getLevelsInternal(Math.max(4224, 3168), 1);
        System.out.println(levelsInternal);
        
        double scale = tileSupport.getScale((int) (levelsInternal-1), levelsInternal);
        System.out.println(scale);
        int scaledWidth  =(int) (width / scale);
        int scaledHeight = (int) (height / scale);
        
        int rows = scaledHeight / TILE_SIZE;
        if (scaledHeight % TILE_SIZE != 0) {rows +=1;}
        
        int cols = scaledWidth / TILE_SIZE;
        if (scaledWidth % TILE_SIZE != 0) {cols += 1; }
        
        System.out.println("rows:"+rows);
        System.out.println("cols:"+cols);
    }
}
