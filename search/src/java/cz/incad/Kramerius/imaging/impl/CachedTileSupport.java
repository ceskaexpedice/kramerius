package cz.incad.Kramerius.imaging.impl;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.Kramerius.imaging.TileSupport;
import cz.incad.kramerius.FedoraAccess;

public class CachedTileSupport implements TileSupport {
    
    @Inject
    @Named("rawTileSupport")
     private TileSupport tileSupport;

    @Override
    public int getTileSize() {
        return tileSupport.getTileSize();
    }

    Map<String, Long> levelsCache = new HashMap<String, Long>();
    @Override
    public long getLevels(String uuid, int minSize) throws IOException {
        String key = uuid+"_"+minSize;
        if (!levelsCache.containsKey(key)) {
            levelsCache.put(key, tileSupport.getLevels(uuid, minSize));
        }
        return levelsCache.get(key);
    }

    Map<String, Image> rawImgs = new HashMap<String, Image>();
    @Override
    public Image getRawImage(String uuid) throws IOException {
        if (!rawImgs.containsKey(uuid)) {
            rawImgs.put(uuid, tileSupport.getRawImage(uuid));
        }
        return rawImgs.get(uuid);
    }

    @Override
    public Dimension getMaxSize(String uuid) throws IOException {
        Image rawImg = getRawImage(uuid);
        return new Dimension(rawImg.getWidth(null), rawImg.getHeight(null));
    }

    @Override
    public Dimension getScaledDimension(Dimension originalDim, double scale) {
        return tileSupport.getScaledDimension(originalDim, scale);
    }

    Map<String, BufferedImage> bufImageCache = new HashMap<String, BufferedImage>();
    @Override
    public BufferedImage getTile(String uuid, int displayLevel,
            int displayTile, int minSize) throws IOException {
        String key = uuid+"_"+displayLevel+"_"+displayTile+"_"+minSize;
        if (!bufImageCache.containsKey(key)) {
            bufImageCache.put(key, tileSupport.getTile(uuid, displayLevel, displayTile, minSize));
        }
        return bufImageCache.get(key);
    }

    @Override
    public double getScale(int displayLevel, long maxLevel) {
        return tileSupport.getScale(displayLevel, maxLevel);
    }

    @Override
    public int getCols(Dimension scaledDim) {
        return tileSupport.getCols(scaledDim);
    }

    @Override
    public int getRows(Dimension scaledDim) {
        return tileSupport.getRows(scaledDim);
    }
    
    
}
