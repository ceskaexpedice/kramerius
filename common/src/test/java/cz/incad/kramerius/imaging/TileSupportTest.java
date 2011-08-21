package cz.incad.kramerius.imaging;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import cz.incad.kramerius.AbstractGuiceTestCase;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;

public class TileSupportTest extends AbstractGuiceTestCase {

    public static void main(String[] args) throws IOException {
        Injector injector = Guice.createInjector(new ImagingModuleForTest());
        DeepZoomTileSupport tileSupport = injector.getInstance(DeepZoomTileSupport.class);
        InputStream iStream = TileSupportTest.class.getResourceAsStream("res.jpg");
        TestCase.assertNotNull(iStream);
        BufferedImage img = ImageIO.read(iStream);

        int level = 10;
        String srow = "0";
        String scol = "0";

        long levels = tileSupport.getLevels(img, 1);
        double scale = tileSupport.getScale(level, levels);

        Dimension scaled = tileSupport.getScaledDimension(new Dimension(img.getWidth(null), img.getHeight(null)), scale);

        int rows = tileSupport.getRows(scaled);
        int cols = tileSupport.getCols(scaled);
        int base = Integer.parseInt(srow) * cols;
        base = base + Integer.parseInt(scol);

        BufferedImage tile = tileSupport.getTileFromBigImage(img, level, base, 1, ScalingMethod.BILINEAR, false);
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(tile));

        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.getContentPane().add(label);
        frm.pack();
        frm.setVisible(true);

    }

    @Test
    public void testTileSupport() throws IOException {

    }

    @Override
    protected Injector injector() {
        Injector injector = Guice.createInjector(new ImagingModuleForTest());
        return injector;
    }

}
