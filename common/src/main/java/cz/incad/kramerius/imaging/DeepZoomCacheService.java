package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import cz.incad.kramerius.ProcessSubtreeException;

/**
 * This service is suitable for caching deep zoom tiles
 * 
 * @author pavels
 */
public interface DeepZoomCacheService {

    /**
     * Prepare cache for one page
     * 
     * @param pid
     *            PID of image
     * @param dimensionToFit
     *            how many levels should be prepared
     */
    public void prepareCacheImage(String pid, Dimension dimensionToFit);

    /**
     * Prepare cache for given image
     * 
     * @param pid
     *            PID of given image
     * @param dimensionToFit
     *            how many levels should be prepared
     * @param rawImage
     *            RAW Image
     */
    public void prepareCacheImage(String pid, Dimension dimensionToFit, BufferedImage rawImage);

    
    /**
     * Walk through Rels-Ext and search pages and cache them
     * 
     * @param pid
     *            Master uuuid
     * @throws IOException
     * @throws ProcessSubtreeException 
     */
    public void prepareCacheForPID(String pid) throws IOException, ProcessSubtreeException;

    public void prepareCacheForPID(String pid, int levelOverTileSize) throws IOException, ProcessSubtreeException;
    
    /**
     * Returns true if deep zoom descriptor is present in cache
     * 
     * @param pid
     *            PID of page
     * @return
     * @throws IOException
     */
    public boolean isDeepZoomDescriptionPresent(String pid) throws IOException;

    /**
     * Write raw image into cache.
     * 
     * @param pid
     * @param rawImage
     * @throws IOException
     */
    public void writeDeepZoomOriginalImage(String pid, BufferedImage rawImage) throws IOException;

    /**
     * Write deep zoom descriptor in cache
     * 
     * @param pid
     *            PID of the page
     * @param rawImage
     *            Raw page image
     * @param tileSize
     *            Size of tile
     * @throws IOException
     */
    public void writeDeepZoomDescriptor(String pid, BufferedImage rawImage, int tileSize) throws IOException;

    public void writeDeepZoomDescriptor(String pid, Dimension dim, int tileSize) throws IOException;

    /**
     * Gets descriptor input stream
     * 
     * @param pid
     *            PID of the page
     * @return
     * @throws IOException
     */
    public InputStream getDeepZoomDescriptorStream(String pid) throws IOException;

    /**
     * Returns true if tile is present in cache
     * 
     * @param pid
     *            PID of page
     * @param ilevel
     *            scale level
     * @param row
     *            Tile row
     * @param col
     *            Tile col
     * @return
     * @throws IOException
     */
    public boolean isDeepZoomTilePresent(String pid, int ilevel, int row, int col) throws IOException;

    /**
     * Write tile into cache
     * 
     * @param pid
     *            PID of page
     * @param ilevel
     *            scale level
     * @param row
     *            Tile row
     * @param col
     *            Tile col
     * @param tile
     *            Tile image
     * @throws IOException
     */
    public void writeDeepZoomTile(String pid, int ilevel, int row, int col, BufferedImage tile) throws IOException;

    /**
     * Returns input stream of the tile
     * 
     * @param pid
     *            PID of the page
     * @param ilevel
     *            scale level
     * @param row
     *            Tile row
     * @param col
     *            Tile col
     * @return
     * @throws IOException
     */
    public InputStream getDeepZoomTileStream(String pid, int ilevel, int row, int col) throws IOException;

    /**
     * Returns true if full image is present in cache
     * 
     * @param pid
     * @return
     * @throws IOException
     */
    public boolean isDeepZoomOriginalPresent(String pid) throws IOException;

    /**
     * Returns url of full image, needs to be URL because of djvu :( ble
     * 
     * @param pid
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    // public URL getFullImageURL(String uuid) throws MalformedURLException,
    // IOException;

    public BufferedImage getDeepZoomOriginal(String pid) throws IOException;

    public BufferedImage createDeepZoomOriginalImageFromFedoraRAW(String pid) throws IOException;

    public boolean isResolutionFilePresent(String pid) throws IOException;

    public Dimension getResolutionFromFile(String pid) throws IOException;

}
