package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;


import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

/**
 * Helper interface for supporting deep zoom protocol
 * 
 * @author pavels
 */
public interface DeepZoomTileSupport {

    /**
     * Return default tile size
     * 
     * @return tilesize
     */
    public int getTileSize();

    /**
     * Returns number of resolution's leves for image
     * 
     * @param pid Pid of the object
     * @param minSize the smallest size of image (in deepZoom protocol is 1px, in IIP is tileSize)
     * @return number of levels
     * @throws IOException IO error has been occurred
     */
    public long getLevels(String pid, int minSize, DeepZoomFullImageScaleFactor factor) throws IOException;

    /**
     * Calculate and returns number of leves of given image
     * 
     * @param image RAW Image
     * @param minSize
     *            the smallest size of image (in deepZoom protocol is 1px, in
     *            IIP is tileSize)
     * @return number of levels
     * @throws IOException IO error has been occurred
     */
    public long getLevels(BufferedImage image, int minSize) throws IOException;

    /**
     * Calculate and returns number of leves of given dimension
     * @param dim dimension
     * @param minSize
     *            the smallest size of image (in deepZoom protocol is 1px, in
     *            IIP is tileSize)
     */
    public int getLevels(Dimension dim, int minSize);

    
    /**
     */
    @Deprecated
    public BufferedImage getRawImage(String pid) throws IOException;

    /**
     * Returns scaled instance of original image
     * @param pid PID of object
     * @param factor Scaling factor
     * @return scaled image
     * @throws IOException IO error has been occurred
     */
    public BufferedImage getScaledRawImage(String pid, DeepZoomFullImageScaleFactor factor) throws IOException;

    /**
     * Returns max-size of the image
     * <p>
     * If the original is too big, you can configurate smaller image  as the original. It is defined by {@link DeepZoomFullImageScaleFactor}
     * </p>
     * @param pid PID of the object
     * @return maximal size of image 
     * @throws IOException IO error has been occurred
     * @see DeepZoomFullImageScaleFactor
     */
    public Dimension getMaxSize(String pid, DeepZoomFullImageScaleFactor factor) throws IOException;

    /**
     * Calculates and returns scaled dimension
     * 
     * @param originalDim Original dimension
     * @param scale Scaled factor
     * @return scaled dimension
     */
    public Dimension getScaledDimension(Dimension originalDim, double scale);

    /**
     * Returns one tile
     * 
     * @param pid PID of the raw image
     * @param displayLevel Scale level of the image
     * @param displayTile Tile coordinats
     * @param minSize
     *            the smallest size of image (in deepZoom protocol is 1px, in
     *            IIP is tileSize)
     * @param scalingMethod Scaling method
     * @param iterateScaling flag for determine if algorithm should use iterate scaling
     * @return one tile
     * @throws IOException IO error has been occurred
     */
    public BufferedImage getTile(String pid, int displayLevel, int displayTile, int minSize, ScalingMethod scalingMethod, boolean iterateScaling, DeepZoomFullImageScaleFactor scaleFactor) throws IOException;

    /**
     * Returns tile from given image
     * 
     * @param displayLevel Scale level of the image
     * @param displayTile Tile coordinatns
     * @param minSize
     *            the smallest size of image (in deepZoom protocol is 1px, in
     *            IIP is tileSize)
     * @param method Scaling method
     * @param iterateScaling flag for determine 
     * @param iterateScaling flag for determine if algorithm should use iterate scaling
     * @return one tile
     * @throws IOException IO error has been occurred
     */
    public BufferedImage getTileFromBigImage(BufferedImage image, int displayLevel, int displayTile, int minSize, ScalingMethod method, boolean iterateScaling) throws IOException;

    /**
     * Calculate and returns real scale
     * 
     * @param displayLevel
     * @param maxLevel
     * @return
     */
    public double getScale(int displayLevel, long maxLevel);

    /**
     * Returns scaled dimension
     * @param original Original dimension
     * @param displayLevel Displaying level
     * @param maxLevel Max level
     * @return Scaled dimension
     */
    public Dimension getScaledDimension(Dimension original, int displayLevel, int maxLevel);
    
    /**
     * Returns number of cols in scaled dimension
     * 
     * @param scaledDim Scaled dimension
     * @return number of calculated columns
     */
    public int getCols(Dimension scaledDim);

    /**
     * Returns number of rows in scaled dimension
     * 
     * @param scaledDim 
     * @return
     */
    public int getRows(Dimension scaledDim);

    
    /**
     * Returns the closest scaling factor from given original size and tile size 
     * @param originalSize Original dimension
     * @param sizeToFit Tile size
     * @param minSize Min tile size
     * @return Returns scaled factor
     */
    public double getClosestScale(Dimension originalSize, int sizeToFit, int minSize);

    /**
     * Calculate closest level for given dimension
     * @param originalDimension Original dimension 
     * @param sizeToFit Size to fit
     * @param minSize TODO
     * @return calculated level
     */
    public int getClosestLevel(Dimension originalDimension, int sizeToFit, int minSize);

}
