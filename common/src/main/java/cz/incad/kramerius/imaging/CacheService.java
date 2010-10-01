package cz.incad.kramerius.imaging;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

/**
 * This service is suitable for caching deep zoom tiles
 * @author pavels
 *
 */
public interface CacheService {
	
	/**
	 * Prepare cache for one page
	 * @param uuid
	 */
	public void prepareCacheImage(String uuid);
	
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
	public void writeDeepZoomFullImage(String uuid, Image rawImage) throws IOException;
	
	/**
	 * Write deep zoom descriptor in cache
	 * @param uuid UUID of the page 
	 * @param rawImage Raw page image
	 * @param tileSize Size of tile
	 * @throws IOException
	 */
	public void writeDeepZoomDescriptor(String uuid,Image  rawImage, int tileSize) throws IOException;
	
	/**
	 * Gets descriptor input stream
	 * @param uuid UUID of the page
	 * @return
	 * @throws IOException
	 */
	public InputStream getDeepZoomDescriptorStream(String uuid) throws IOException;
	
	/**
	 * 
	 * @param uuid
	 * @param ilevel
	 * @param row
	 * @param col
	 * @return
	 * @throws IOException
	 */
	public boolean isDeepZoomTilePresent(String uuid, int ilevel, int row,int col) throws IOException;

	public void writeDeepZoomTile(String uuid, int ilevel, int row, int col, Image tile) throws IOException;

	public InputStream getDeepZoomTileStream(String uuid, int ilevel, int row, int col) throws IOException;
}
