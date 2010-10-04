package cz.incad.kramerius.imaging.impl;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

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
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class CacheServiceImpl implements CacheService {
	

	java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(CacheServiceImpl.class.getName());
	
	@Inject
	@Named("securedFedoraAccess")
	FedoraAccess fedoraAccess;
	@Inject
	TileSupport tileSupport;
	@Inject
	KConfiguration kConfiguration;
	CachingSupport cachingSupport= new CachingSupport();
	
	@Override
	public void prepareCacheImage(String uuid) {
		try {
			Image rawImage = tileSupport.getRawImage(uuid);
			cachingSupport.writeDeepZoomDescriptor(uuid, rawImage, tileSupport.getTileSize());
			cachingSupport.writeDeepZoomFullImage(uuid, rawImage, kConfiguration.getDeepZoomJPEGQuality());
			int levels = (int) tileSupport.getLevels(uuid, 1);
			for (int i = 1; i < 6; i++) {
                int curLevel = levels-i;
				double scale = tileSupport.getScale(curLevel, levels);
                Dimension scaled = tileSupport.getScaledDimension(tileSupport.getMaxSize(uuid), scale);
                int rows = tileSupport.getRows(scaled);
                int cols = tileSupport.getCols(scaled);
                for (int r = 0; r < rows; r++) {
					int b=r*cols;
					for (int c = 0; c < cols; c++) {
						int cell = b+c; 
	                    BufferedImage tile = this.tileSupport.getTile(uuid, curLevel, cell, 1);
	                    cachingSupport.writeDeepZoomTile(uuid, curLevel, r,c, tile,kConfiguration.getDeepZoomJPEGQuality());
					}
				}
			}
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		}
	}

	@Override
	public void prepareCacheForUUID(String uuid) throws IOException {
		KrameriusModels krameriusModel = fedoraAccess.getKrameriusModel(uuid);
		if (krameriusModel.equals(KrameriusModels.PAGE)) {
			prepareCacheImage(uuid);
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
							prepareCacheImage(uuid);
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
	public void writeDeepZoomFullImage(String uuid, Image rawImage) throws IOException {
		this.cachingSupport.writeDeepZoomFullImage(uuid, rawImage, kConfiguration.getDeepZoomJPEGQuality());
	}

	@Override
	public void writeDeepZoomDescriptor(String uuid, Image rawImage, int tileSize) throws IOException {
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
			Image tile) throws IOException {
		this.cachingSupport.writeDeepZoomTile(uuid, ilevel, row, col, tile, kConfiguration.getDeepZoomJPEGQuality());
	}

	@Override
	public InputStream getDeepZoomTileStream(String uuid, int ilevel, int row,
			int col) throws IOException {
		return this.cachingSupport.openDeepZoomTile(uuid, ilevel, row, col);
	}

	
	
}
