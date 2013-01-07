package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import cz.incad.kramerius.ProcessSubtreeException;


/**
 * This service is suitable for caching deep zoom files
 * <p>
 * The created cache contains:
 * <ul>
 *  <li> generated tiles </li>
 *  <li> descriptor for deep zoom viewer </li>
 *  <li> resolution file (for getting original resolution) </li>
 * </ul>
 * </p>
 * @author pavels
 */
public interface DeepZoomCacheService {

    /**
     * Prepare cache for one page
     * 
     * @param pid PID of image
     * @param dimensionToFit how many levels should be prepared
     */
    public void prepareCacheImage(String pid, Dimension dimensionToFit);

    /**
     * Prepare cache for given image
     * 
     * @param pid PID of given image
     * @param dimensionToFit how many levels should be prepared
     * @param rawImage RAW Image
     */
    public void prepareCacheImage(String pid, Dimension dimensionToFit, BufferedImage rawImage);

    
    /**
     * Walk the Rels-Ext and search pages and cache them
     * 
     * @param pid Master pid
     * @throws IOException IO error has been occurred
     * @throws ProcessSubtreeException Cannot traverse object's tree 
     */
    public void prepareCacheForPID(String pid) throws IOException, ProcessSubtreeException;

    
    
    /**
     * Walk the Rels-Ext and search pages and cache them
     * 
     * @param pid Master pid 
     * @param pid how many levels should be prepared
     * @throws IOException IO error has been occurred
     * @throws ProcessSubtreeException Cannot traverse object's tree 
     */
    public void prepareCacheForPID(String pid, int levelOverTileSize) throws IOException, ProcessSubtreeException;
    
    /**
     * Returns true if deep zoom descriptor is present in cache
     * 
     * @param pid PID of page
     * @return true if descriptor is present in cache
     * @throws IOException IO error has been occurred
     */
    public boolean isDeepZoomDescriptionPresent(String pid) throws IOException;

    /**
     * Write raw image into cache.
     * 
     * @param pid PID of image
     * @param rawImage Raw image
     * @throws IOException Cannot searialize image
     */
    public void writeDeepZoomOriginalImage(String pid, BufferedImage rawImage) throws IOException;

    /**
     * Write deep zoom descriptor in cache
     * 
     * @param pid PID of the image
     * @param rawImage Raw page image
     * @param tileSize Size of tile
     * @throws IOException IO error has been occurred
     */
    public void writeDeepZoomDescriptor(String pid, BufferedImage rawImage, int tileSize) throws IOException;

    /**
     * Write resolution file 
     * @param pid PID of the image
     * @param dim Writing dimension
     * @throws IOException IO error has been occurred
     */
    public void writeResolution(String pid, Dimension dim) throws IOException;
 
    
    
    /**
     * Write deep zoom descriptor in cache
     * 
     * @param pid PID of the image
     * @param tileSize Size of tile
     * @throws IOException IO error has been occurred
     */
    public void writeDeepZoomDescriptor(String pid, Dimension dim, int tileSize) throws IOException;

    /**
     * Gets descriptor input stream
     * 
     * @param pid PID of the page
     * @return Descripiton of the image stream
     * @throws IOException IO error has been occurred
     */
    public InputStream getDeepZoomDescriptorStream(String pid) throws IOException;

    /**
     * Returns true if tile is present in cache
     * 
     * @param pid PID of the image
     * @param ilevel scale level
     * @param row Tile row
     * @param col Tile col
     * @return return true, if tile is present
     * @throws IOException IO error has been occurred
     */
    public boolean isDeepZoomTilePresent(String pid, int ilevel, int row, int col) throws IOException;

    /**
     * Write tile into cache
     * 
     * @param pid PID of the image
     * @param ilevel scale level
     * @param row Tile row
     * @param col Tile col
     * @param tile Tile image
     * @throws IOException IO error has been occurred
     */
    public void writeDeepZoomTile(String pid, int ilevel, int row, int col, BufferedImage tile) throws IOException;

    /**
     * Returns input stream of the tile
     * 
     * @param pid PID of the page
     * @param ilevel scale level
     * @param row Tile row
     * @param col Tile col
     * @return InputStream tile stream
     * @throws IOException IO error has been occurred
     */
    public InputStream getDeepZoomTileStream(String pid, int ilevel, int row, int col) throws IOException;

    /**
     * Returns true if full scaled image is present in cache
     * 
     * @param pid PID of the image
     * @return return true if i
     * @throws IOException IO error has been occurred
     */
    public boolean isDeepZoomOriginalPresent(String pid) throws IOException;


    /**
     * Returns tiled original
     * @param pid PID of original
     */
    public BufferedImage getDeepZoomOriginal(String pid) throws IOException;

    //TODO: remove
    public BufferedImage createDeepZoomOriginalImageFromFedoraRAW(String pid) throws IOException;

    /**
     * Returns true if resolution file is present in cache
     * @param pid PID of the image
     * @return true if the resolution file is present in cache
     * @throws IOException IO error has been occurred
     */
    public boolean isResolutionFilePresent(String pid) throws IOException;


    /**
     * Create resolution object from resolution file
     * @param pid PID of object
     * @return original resolution
     * @throws IOException IO error has been occurred
     */
    public Dimension getResolutionFromFile(String pid) throws IOException;

}
