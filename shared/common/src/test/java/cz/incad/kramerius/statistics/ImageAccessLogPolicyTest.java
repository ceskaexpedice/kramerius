package cz.incad.kramerius.statistics;

import org.junit.Assert;
import org.junit.Test;

/**
 * Cases taken from a network capture of the Kramerius web client (OpenSeadragon) on a
 * 12365x18331 px page with 256 px tiles, plus the non-tile requests the clients make.
 */
public class ImageAccessLogPolicyTest {

    private static boolean tile(String region, String size) {
        return ImageAccessLogPolicy.isViewerTile(region, size, "0", "default.jpg");
    }

    @Test
    public void viewerTilesAreNotLogged() {
        Assert.assertTrue(tile("4096,8192,4096,4096", "256,256")); // scale 16
        Assert.assertTrue(tile("12288,8192,77,4096", "5,256"));    // right edge, ceil(77/16) = 5
        Assert.assertTrue(tile("4096,16384,4096,1947", "256,122")); // bottom edge
        Assert.assertTrue(tile("12288,16384,77,1947", "5,122"));    // corner
        Assert.assertTrue(tile("6144,8192,2048,2048", "256,256"));  // scale 8
        Assert.assertTrue(tile("0,0,256,256", "256,256"));          // scale 1
        Assert.assertTrue(tile("full", "97,144"));                  // lowest level
        Assert.assertTrue(tile("4096,8192,4096,4096", "256,"));     // IIIF 2 style width only
        Assert.assertTrue(ImageAccessLogPolicy.isViewerTile("4096,8192,4096,4096", "256,256", "0", "default.webp"));
        Assert.assertTrue(ImageAccessLogPolicy.isViewerTile("4096,8192,4096,4096", "256,256", "0", null));
    }

    @Test
    public void everythingElseIsLogged() {
        Assert.assertFalse(tile("full", "max"));                    // whole page export
        Assert.assertFalse(tile("full", "full"));
        Assert.assertFalse(tile("1000,1000,300,300", "max"));       // crop export
        Assert.assertFalse(tile("1000,1000,300,300", "300,300"));   // crop not aligned to the grid
        // a crop anchored at the grid origin and smaller than a cell looks like an edge tile and is not logged (known limit)
        Assert.assertTrue(tile("0,0,300,300", "300,300"));
        Assert.assertFalse(tile("4096,8192,4096,4096", "!400,400")); // best fit thumbnail
        Assert.assertFalse(tile("full", "!400,400"));
        Assert.assertFalse(tile("full", "^max"));
        Assert.assertFalse(tile("full", "^!2000,2000"));
        Assert.assertFalse(tile("full", "^2000,"));
        Assert.assertFalse(tile("full", ",500"));
        Assert.assertFalse(tile("full", "600,"));                   // bigger than any tile
        Assert.assertFalse(tile("full", "pct:50"));
        Assert.assertFalse(tile("pct:10,10,50,50", "max"));
        Assert.assertFalse(tile("square", "256,256"));
        Assert.assertFalse(tile("4096,8192,4096,4096", "768,768")); // size above any tile size
        Assert.assertFalse(tile("4096,8192,4096,4096", "128,128")); // x not aligned to 256*32
        Assert.assertFalse(tile("4096,8192,4096,4096", "0,0"));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile("4096,8192,4096,4096", "256,256", "90", "default.jpg"));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile("4096,8192,4096,4096", "256,256", "!0", "default.jpg"));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile("4096,8192,4096,4096", "256,256", "0", "gray.png"));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile(null, "256,256", "0", "default.jpg"));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile("full", null, "0", "default.jpg"));
        Assert.assertFalse(tile("abc", "256,256"));
        Assert.assertFalse(tile("0,0,256,256", "abc"));
    }

    @Test
    public void cdkClientRequests512PxTiles() {
        // the CDK client enlarges the advertised 256 px tiles to 512 px (enlargeTiles in iiif-viewer.ts)
        Assert.assertTrue(tile("0,0,512,512", "512,512"));            // scale 1
        Assert.assertTrue(tile("1024,2048,1024,1024", "512,512"));    // scale 2, aligned to 1024
        Assert.assertTrue(tile("4096,8192,4096,4096", "512,512"));    // scale 8
        Assert.assertTrue(tile("12288,8192,77,4096", "10,512"));      // right edge at scale 8
        Assert.assertTrue(tile("full", "300,450"));                   // lowest level under 512
        Assert.assertTrue(tile("512,0,512,512", "256,256"));          // a 256 px tile at scale 2 starts at a multiple of 512
        Assert.assertFalse(tile("1024,1024,1024,1024", "1024,1024")); // above the default maximum
    }

    @Test
    public void tileIntentAndConfigurationDecideLogging() {
        // crop, no intent: logged
        Assert.assertTrue(ImageAccessLogPolicy.shouldLog("1000,1000,300,300", "300,300", "0", "default.jpg", 512, null, false));
        // crop declared as a tile by the client: not logged by default
        Assert.assertFalse(ImageAccessLogPolicy.shouldLog("1000,1000,300,300", "300,300", "0", "default.jpg", 512, "view, Tile", false));
        // tile by shape: not logged by default
        Assert.assertFalse(ImageAccessLogPolicy.shouldLog("4096,8192,4096,4096", "512,512", "0", "default.jpg", 512, null, false));
        // tiles switched on: everything is logged
        Assert.assertTrue(ImageAccessLogPolicy.shouldLog("4096,8192,4096,4096", "512,512", "0", "default.jpg", 512, "tile", true));
        Assert.assertTrue(ImageAccessLogPolicy.hasTileIntent("tile"));
        Assert.assertTrue(ImageAccessLogPolicy.hasTileIntent("view,tile"));
        Assert.assertFalse(ImageAccessLogPolicy.hasTileIntent("view,tiles"));
        Assert.assertFalse(ImageAccessLogPolicy.hasTileIntent(null));
    }

    @Test
    public void maximumTileSizeIsConfigurable() {
        Assert.assertTrue(ImageAccessLogPolicy.isViewerTile("0,0,1024,1024", "1024,1024", "0", "default.jpg", 1024));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile("0,0,1024,1024", "1024,1024", "0", "default.jpg", 512));
        Assert.assertFalse(ImageAccessLogPolicy.isViewerTile("0,0,512,512", "512,512", "0", "default.jpg", 256));
        Assert.assertTrue(ImageAccessLogPolicy.isViewerTile("0,0,512,512", "512,512", "0", "default.jpg", 0)); // 0 = default
    }
}
