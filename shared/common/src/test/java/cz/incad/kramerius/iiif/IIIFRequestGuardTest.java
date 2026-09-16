package cz.incad.kramerius.iiif;

import org.junit.Assert;
import org.junit.Test;

public class IIIFRequestGuardTest {

    @Test
    public void smallNumericTileDoesNotRequireProbe() {
        Assert.assertTrue(IIIFRequestGuard.isSmallTileRequest("0,0,512,512", "512,", 512));
        Assert.assertTrue(IIIFRequestGuard.isSmallTileRequest("10,20,256,300", "^!132,124", 512));
        Assert.assertTrue(IIIFRequestGuard.isSmallTileRequest("10,20,256,300", ",125", 512));
    }

    @Test
    public void fullOrPercentRegionsRequireProbe() {
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("full", "512,", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("square", "512,", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("pct:0,0,10,10", "512,", 512));
    }

    @Test
    public void largeNumericRegionRequiresProbe() {
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,513,512", "512,", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,513", "512,", 512));
    }

    @Test
    public void largeSizeRequiresProbe() {
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", "513,", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", ",513", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", "!513,512", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", "^!512,513", 512));
    }

    @Test
    public void maxAndPercentSizesRequireProbe() {
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", "max", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", "pct:50", 512));
    }

    @Test
    public void invalidInputRequiresProbe() {
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe(null, "512,", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512", "512,", 512));
        Assert.assertTrue(IIIFRequestGuard.requiresInfoJsonProbe("0,0,512,512", "abc,", 512));
    }
}
