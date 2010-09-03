package cz.incad.Kramerius.imaging;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

public interface TileSupport {

    public int getTileSize();

    public long getLevels(String uuid, int minSize) throws IOException;

    public Image getRawImage(String uuid) throws IOException;

    public Dimension getMaxSize(String uuid) throws IOException;

    public Dimension getScaledDimension(Dimension originalDim, double scale);
    
    public BufferedImage getTile(String uuid, int displayLevel, int displayTile, int minSize) throws IOException;

    public double getScale(int displayLevel, long maxLevel);

    public int getCols(Dimension scaledDim);

    public int getRows(Dimension scaledDim);

}
