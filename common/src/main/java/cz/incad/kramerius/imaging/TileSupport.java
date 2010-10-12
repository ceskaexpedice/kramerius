package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

/**
 * Helper class which supports tiles
 * @author pavels
 */
public interface TileSupport {
	
	/**
	 * Return default tile size
	 * @return
	 */
    public int getTileSize();

    /**
     * Returns number of leves for image
     * @param uuid
     * @param minSize the smallest size of image (in deepZoom protocol is 1px, in IIP is tileSize)
     * @return
     * @throws IOException
     */
    public long getLevels(String uuid, int minSize) throws IOException;

    /**
     * Calculate and returns number of leves of given image
     * @param image RAW Image
     * @param minSize the smallest size of image (in deepZoom protocol is 1px, in IIP is tileSize)
     * @return
     * @throws IOException
     */
    public long getLevels(BufferedImage image, int minSize) throws IOException;
    
    /**
     * Returns raw image
     * @param uuid
     * @return
     * @throws IOException
     * @TODO Move this method !!
     */
    public BufferedImage getRawImage(String uuid) throws IOException;

    /**
     * Returns max-size of the image
     * @param uuid
     * @return
     * @throws IOException
     */
    public Dimension getMaxSize(String uuid) throws IOException;

    /**
     * Calculates and returns scaled dimension
     * @param originalDim
     * @param scale
     * @return
     */
    public Dimension getScaledDimension(Dimension originalDim, double scale);
    
    /**
     * Returns one tile
     * @param uuid UUID of the raw image
     * @param displayLevel Scale level of the image
     * @param displayTile Tile coordinats 
     * @param minSize the smallest size of image (in deepZoom protocol is 1px, in IIP is tileSize)
     * @param scalingMethod TODO
     * @param iterateScaling TODO
     * @return
     * @throws IOException
     */
    public BufferedImage getTile(String uuid, int displayLevel, int displayTile, int minSize, ScalingMethod scalingMethod, boolean iterateScaling) throws IOException;
    
    /**
     * Returns tile from given image
     * @param displayLevel Scale level of the image
     * @param displayTile Tile coordinatns
     * @param minSize the smallest size of image (in deepZoom protocol is 1px, in IIP is tileSize)
     * @param method TODO
     * @param iterateScaling TODO
     */
    public BufferedImage getTile(BufferedImage image, int displayLevel, int displayTile, int minSize, ScalingMethod method, boolean iterateScaling) throws IOException;

    /**
     * Calculate and returns real scale
     * @param displayLevel 
     * @param maxLevel 
     * @return
     */
    public double getScale(int displayLevel, long maxLevel);

    /**
     * Returns number of cols in scaled dimension
     * @param scaledDim
     * @return
     */
    public int getCols(Dimension scaledDim);

    /**
     * Returns number of rows in scaled dimension
     * @param scaledDim
     * @return
     */
    public int getRows(Dimension scaledDim);
    
}
