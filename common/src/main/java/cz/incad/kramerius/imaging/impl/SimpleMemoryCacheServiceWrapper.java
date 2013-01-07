package cz.incad.kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.imaging.DeepZoomCacheService;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Implementation can cache full images in memory. <br> 
 * Number of objects in the memory is limited by property 'deepZoom.memoryCache.numberOfObjects'. <br>
 * Default value is 1. <br>
 * @author pavels
 */
public class SimpleMemoryCacheServiceWrapper implements DeepZoomCacheService {
	
	static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(SimpleMemoryCacheServiceWrapper.class.getName());
	
	@Inject
	@Named("fileSystemCache")
	DeepZoomCacheService wrappingInstance;

	@Inject
	KConfiguration kConfiguration;
	
	private MemoryCache memoryCache = new MemoryCache();
	
	
	@Override
	public void prepareCacheImage(String pid, Dimension dimensionToFit) {
		try {
			BufferedImage rawImage = getDeepZoomOriginal(pid);
			prepareCacheImage(pid, dimensionToFit, rawImage);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	@Override
	public void prepareCacheImage(String uuid, Dimension dimensionToFit,
			BufferedImage rawImage) {
		this.wrappingInstance.prepareCacheImage(uuid, dimensionToFit);
		this.memoryCache.registerToCache(uuid, rawImage);
	}

	@Override
	public void prepareCacheForPID(String uuid) throws IOException, ProcessSubtreeException {
		this.wrappingInstance.prepareCacheForPID(uuid);
	}

	@Override
	public boolean isDeepZoomDescriptionPresent(String uuid) throws IOException {
		return this.wrappingInstance.isDeepZoomDescriptionPresent(uuid);
	}

	@Override
	public synchronized void writeDeepZoomOriginalImage(String uuid, BufferedImage rawImage)
			throws IOException {
		this.wrappingInstance.writeDeepZoomOriginalImage(uuid, rawImage);
		this.memoryCache.registerToCache(uuid, rawImage);
	}


	@Override
	public void writeDeepZoomDescriptor(String uuid, BufferedImage rawImage,
			int tileSize) throws IOException {
		this.wrappingInstance.writeDeepZoomDescriptor(uuid, rawImage, tileSize);
	}

	@Override
	public InputStream getDeepZoomDescriptorStream(String uuid)
			throws IOException {
		return this.wrappingInstance.getDeepZoomDescriptorStream(uuid);
	}

	@Override
	public boolean isDeepZoomTilePresent(String uuid, int ilevel, int row,
			int col) throws IOException {
		return this.wrappingInstance.isDeepZoomTilePresent(uuid, ilevel, row, col);
	}

	@Override
	public void writeDeepZoomTile(String uuid, int ilevel, int row, int col,
			BufferedImage tile) throws IOException {
		this.wrappingInstance.writeDeepZoomTile(uuid, ilevel, row, col, tile);
	}

	@Override
	public InputStream getDeepZoomTileStream(String uuid, int ilevel, int row,
			int col) throws IOException {
		return this.wrappingInstance.getDeepZoomTileStream(uuid, ilevel, row, col);
	}

	@Override
	public boolean isDeepZoomOriginalPresent(String uuid) throws IOException {
		return this.wrappingInstance.isDeepZoomOriginalPresent(uuid);
	}
	

   @Override
    public void prepareCacheForPID(String pid, int levelOverTileSize) throws IOException, ProcessSubtreeException {
       this.wrappingInstance.prepareCacheForPID(pid, levelOverTileSize);
    }


//	@Override
//	public URL getFullImageURL(String uuid) throws MalformedURLException,
//			IOException {
//		return this.wrappingInstance.getFullImageURL(uuid);
//	}


    @Override
	public BufferedImage getDeepZoomOriginal(String uuid) throws IOException {
		BufferedImage bufImage = memoryCache.getFromCache(uuid);
		if (bufImage == null) {
			bufImage =  this.wrappingInstance.getDeepZoomOriginal(uuid);
			this.memoryCache.registerToCache(uuid, bufImage);
		}
		return bufImage;
	}

    
	@Override
    public void writeDeepZoomDescriptor(String uuid, Dimension dim, int tileSize) throws IOException {
	    this.wrappingInstance.writeDeepZoomDescriptor(uuid, dim, tileSize);
	}

	

    @Override
    public void writeResolution(String pid, Dimension dim) throws IOException {
        this.wrappingInstance.writeResolution(pid, dim);
    }



    static class MemoryCache {
		
		private ReentrantLock lock = new ReentrantLock();
		private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
		private List<UUIDTimestamp> timestamps = new ArrayList<UUIDTimestamp>();

		private int getCacheLimit() {
			int cacheLimit = KConfiguration.getInstance().getConfiguration().getInt("deepZoom.memoryCache.numberOfObjects",20);
			return cacheLimit;
		}

		private boolean ensureSize() {
			if (this.images.size()>=getCacheLimit()) {
				UUIDTimestamp uuid = timestamps.remove(0);
				this.images.remove(uuid.getUuid());
				return true;
			} else return false;
		}

		private boolean contains(String uuid) {
			return this.images.containsKey(uuid);
		}
		
		public BufferedImage getFromCache(String uuid) {
			try {
				lock.lock();
				if (contains(uuid)) return this.images.get(uuid);
				else return null;
			} finally {
				lock.unlock();
			}
			
		}
		
		public void registerToCache(String uuid, BufferedImage image) {
			try {
				lock.lock();
				if (contains(uuid)) return;
				ensureSize();
				this.images.put(uuid, image);
				this.timestamps.add(new UUIDTimestamp(System.currentTimeMillis(), uuid));
				Collections.sort(this.timestamps);
			} finally {
				lock.unlock();
			}
		}
	}
	
	static class UUIDTimestamp implements Comparable<UUIDTimestamp>{
		
		private long timeStamp;
		private String uuid;
		
		public UUIDTimestamp(long timeStamp, String uuid) {
			super();
			this.timeStamp = timeStamp;
			this.uuid = uuid;
		}

		public long getTimeStamp() {
			return timeStamp;
		}

		public String getUuid() {
			return uuid;
		}

		@Override
		public int compareTo(UUIDTimestamp o) {
			return (this.timeStamp<o.getTimeStamp() ? -1 : (this.timeStamp==o.getTimeStamp() ? 0 : 1));
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UUIDTimestamp other = (UUIDTimestamp) obj;
			if (timeStamp != other.timeStamp)
				return false;
			if (uuid == null) {
				if (other.uuid != null)
					return false;
			} else if (!uuid.equals(other.uuid))
				return false;
			return true;
		}
	}


	@Override
	public BufferedImage createDeepZoomOriginalImageFromFedoraRAW(String uuid)
			throws IOException {
		return this.wrappingInstance.createDeepZoomOriginalImageFromFedoraRAW(uuid);
	}

    @Override
    public boolean isResolutionFilePresent(String uuid) throws IOException {
        return this.wrappingInstance.isResolutionFilePresent(uuid);
    }

    @Override
    public Dimension getResolutionFromFile(String uuid) throws IOException {
        return this.wrappingInstance.getResolutionFromFile(uuid);
    }
}
