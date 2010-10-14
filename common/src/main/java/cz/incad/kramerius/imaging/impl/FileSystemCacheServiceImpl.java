package cz.incad.kramerius.imaging.impl;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.imaging.CacheService;
import cz.incad.kramerius.imaging.TileSupport;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Cache deepZoom objects (full images, tiles and dzi descritors) on the HDD
 * @author pavels
 */
public class FileSystemCacheServiceImpl implements CacheService {
	

	static java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(FileSystemCacheServiceImpl.class.getName());

	
	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	@Inject
	TileSupport tileSupport;
	@Inject
	KConfiguration kConfiguration;
	CachingSupport cachingSupport= new CachingSupport();

	
	@Override
	public void prepareCacheImage(String uuid, Dimension deep) {
		try {
			BufferedImage rawImage = getFullImage(uuid);
			prepareCacheImage(uuid, deep, rawImage);
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	
	@Override
	public void prepareCacheImage(String uuid, Dimension dimToFit, BufferedImage rawImage) {
		try {
			cachingSupport.writeDeepZoomDescriptor(uuid, rawImage, tileSupport.getTileSize());
			cachingSupport.writeDeepZoomFullImage(uuid, rawImage, kConfiguration.getDeepZoomJPEGQuality());

			int levels = (int) tileSupport.getLevels(rawImage, 1);
			System.out.println("Levels = "+levels);
			for (int i = levels-1; i >0; i--) {
				int curLevel = i;
				System.out.println("Current level : "+curLevel);
				double scale = tileSupport.getScale(curLevel, levels);
			    Dimension scaled = tileSupport.getScaledDimension(new Dimension(rawImage.getWidth(null), rawImage.getHeight(null)), scale);

			    int rows = tileSupport.getRows(scaled);
			    int cols = tileSupport.getCols(scaled);
			    for (int r = 0; r < rows; r++) {
					int b=r*cols;
					for (int c = 0; c < cols; c++) {
						int cell = b+c; 
						ScalingMethod method = ScalingMethod.valueOf(kConfiguration.getProperty(
								"deepZoom.scalingMethod", "BICUBIC_STEPPED"));
						boolean highQuality = kConfiguration.getConfiguration().getBoolean(
								"deepZoom.iterateScaling", true);
			            BufferedImage tile = this.tileSupport.getTile(rawImage, curLevel, cell, 1, method, highQuality);
			            cachingSupport.writeDeepZoomTile(uuid, curLevel, r,c, tile,kConfiguration.getDeepZoomJPEGQuality());
					}
			    }
			    // pokud se vleze na dlazdici, dal uz nepokracuju
			    if (!greaterThen(scaled, dimToFit)) {
			    	break;
			    }
			}
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}


	private boolean greaterThen(Dimension scaled, Dimension dimToFit) {
		return scaled.width > dimToFit.width && scaled.height > dimToFit.height;
	}


	@Override
	public void prepareCacheForUUID(String uuid) throws IOException {
		KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
		if (krameriusModel.equals(KrameriusModels.PAGE)) {
			prepareCacheImage(uuid,new Dimension(tileSupport.getTileSize(), tileSupport.getTileSize()));
		} else {
			fedoraAccess.processRelsExt(uuid, new RelsExtHandler() {
				
				private int pageIndex = 1;
				
				@Override
				public void handle(Element elm, FedoraRelationship relation, int level) {
					if (relation.equals(FedoraRelationship.hasPage)) {
						try {
							String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
							PIDParser pidParse = new PIDParser(pid);
							pidParse.disseminationURI();
							String uuid = pidParse.getObjectId();
							LOGGER.info("caching page "+(pageIndex++));
							prepareCacheImage(uuid, new Dimension(tileSupport.getTileSize(), tileSupport.getTileSize()));
						} catch (DOMException e) {
							LOGGER.severe(e.getMessage());
						} catch (LexerException e) {
							LOGGER.severe(e.getMessage());
						}

					}
				}
				
				@Override
				public boolean breakProcess() {
					return false;
				}
				
				@Override
				public boolean accept(FedoraRelationship relation) {
					return relation.name().startsWith("has");
				}
			});
		}
		
	}

	@Override
	public boolean isDeepZoomDescriptionPresent(String uuid) throws IOException {
		return this.cachingSupport.isDeepZoomDescriptionPresent(uuid);
	}

	@Override
	public void writeDeepZoomFullImage(String uuid, BufferedImage rawImage) throws IOException {
		this.cachingSupport.writeDeepZoomFullImage(uuid, rawImage, kConfiguration.getDeepZoomJPEGQuality());
	}

	@Override
	public void writeDeepZoomDescriptor(String uuid, BufferedImage rawImage, int tileSize) throws IOException {
		this.cachingSupport.writeDeepZoomDescriptor(uuid, rawImage, tileSize);
	}

	@Override
	public InputStream getDeepZoomDescriptorStream(String uuid) throws  IOException {
		return this.cachingSupport.openDeepZoomDescriptor(uuid);
	}

	@Override
	public boolean isDeepZoomTilePresent(String uuid, int ilevel, int row,
			int col) throws IOException {
		return this.cachingSupport.isDeepZoomTilePresent(uuid, ilevel, row, col);
	}

	@Override
	public void writeDeepZoomTile(String uuid, int ilevel, int row, int col,
			BufferedImage tile) throws IOException {
		this.cachingSupport.writeDeepZoomTile(uuid, ilevel, row, col, tile, kConfiguration.getDeepZoomJPEGQuality());
	}

	@Override
	public InputStream getDeepZoomTileStream(String uuid, int ilevel, int row,
			int col) throws IOException {
		return this.cachingSupport.openDeepZoomTile(uuid, ilevel, row, col);
	}


	@Override
	public boolean isFullImagePresent(String uuid) throws IOException {
		return this.cachingSupport.isDeepZoomFullImagePresent(uuid);
	}


	@Override
	public URL getFullImageURL(String uuid) throws MalformedURLException, IOException {
		return this.cachingSupport.getRawImageFile(uuid).toURI().toURL();
	}
	
	
	public synchronized BufferedImage getFullImage(String uuid) throws IOException {
		if (isFullImagePresent(uuid)) {
			BufferedImage bufImage = KrameriusImageSupport.readImage(getFullImageURL(uuid), ImageMimeType.JPEG, 0);
			return  bufImage;
		} else {
			BufferedImage bufImage = tileSupport.getRawImage(uuid);
			return bufImage;
		}
	}
}
