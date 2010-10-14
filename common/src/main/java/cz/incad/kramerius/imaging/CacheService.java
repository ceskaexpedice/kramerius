package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This service is suitable for caching deep zoom tiles
 * @author pavels
 */
public interface CacheService {
	
	/**
	 * Prepare cache for one page
	 * @param uuid UUID of image
	 * @param dimensionToFit how many levels should be prepared
	 */
	public void prepareCacheImage(String uuid, Dimension dimensionToFit);
	

	
	/**
	 * Prepare cache for given image
	 * @param uuid UUID of given image
	 * @param dimensionToFit how many levels should be prepared
	 * @param rawImage RAW Image
	 */
	public void prepareCacheImage(String uuid, Dimension dimensionToFit, BufferedImage rawImage);
	
	
	/**
	 * Walk through Rels-Ext and search pages and cache them
	 * @param uuid Master uuuid
	 * @throws IOException
	 */
	public void prepareCacheForUUID(String uuid) throws IOException;

	/**
	 * Returns true if deep zoom descriptor is present in cache
	 * @param uuid UUID of page
	 * @return
	 * @throws IOException
	 */
	public boolean isDeepZoomDescriptionPresent(String uuid) throws IOException;
	
	/**
	 * Write raw image into cache.
	 * @param uuid
	 * @param rawImage
	 * @throws IOException
	 */
	public void writeDeepZoomFullImage(String uuid, BufferedImage rawImage) throws IOException;
	
	/**
	 * Write deep zoom descriptor in cache
	 * @param uuid UUID of the page 
	 * @param rawImage Raw page image
	 * @param tileSize Size of tile
	 * @throws IOException
	 */
	public void writeDeepZoomDescriptor(String uuid,BufferedImage  rawImage, int tileSize) throws IOException;
	
	/**
	 * Gets descriptor input stream
	 * @param uuid UUID of the page
	 * @return
	 * @throws IOException
	 */
	public InputStream getDeepZoomDescriptorStream(String uuid) throws IOException;
	
	/**
	 * Returns true if tile is present in cache
	 * @param uuid UUID of page
	 * @param ilevel scale level
	 * @param row Tile row
	 * @param col Tile col
	 * @return
	 * @throws IOException
	 */
	public boolean isDeepZoomTilePresent(String uuid, int ilevel, int row,int col) throws IOException;

	/**
	 * Write tile into cache
	 * @param uuid UUID of page
	 * @param ilevel scale level
	 * @param row Tile row
	 * @param col Tile col
	 * @param tile Tile image
	 * @throws IOException
	 */
	public void writeDeepZoomTile(String uuid, int ilevel, int row, int col, BufferedImage tile) throws IOException;

	/**
	 * Returns input stream of the tile
	 * @param uuid UUID of the page
	 * @param ilevel scale level
	 * @param row Tile row
	 * @param col Tile col
	 * @return
	 * @throws IOException
	 */
	public InputStream getDeepZoomTileStream(String uuid, int ilevel, int row, int col) throws IOException;


	/**
	 * Returns true if full image is present in cache
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	public boolean isFullImagePresent(String uuid) throws IOException;


	/**
	 * Returns url of full image, needs to be URL because of djvu :( ble
	 * @param uuid
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public URL getFullImageURL(String uuid) throws MalformedURLException, IOException;
	
	public BufferedImage getFullImage(String uuid) throws IOException;

	
}
