package cz.incad.kramerius.utils.imgs;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import junit.framework.Assert;
import junit.framework.TestCase;

public class KrameriusImageSupportTest extends TestCase {

    public void testReadingPDF() throws IOException {
        compareImages(readImage("200f21f3-07ff-4903-ab99-7c0cb557eb51.pdf", ImageMimeType.PDF, 0), readImage("page_0.png", ImageMimeType.PNG, 0));
        compareImages(readImage("200f21f3-07ff-4903-ab99-7c0cb557eb51.pdf", ImageMimeType.PDF, 1), readImage("page_1.png", ImageMimeType.PNG, 0));
        compareImages(readImage("200f21f3-07ff-4903-ab99-7c0cb557eb51.pdf", ImageMimeType.PDF, 2), readImage("page_2.png", ImageMimeType.PNG, 0));
    }

    
    private boolean compareImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y))
                        return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private BufferedImage readImage(String resource,ImageMimeType type, int page) throws IOException {
        URL pdfRes = KrameriusImageSupportTest.class.getResource(resource);
        Assert.assertNotNull(pdfRes);
        BufferedImage readImage = KrameriusImageSupport.readImage(pdfRes, type, page);
        Assert.assertNotNull(readImage);
        return readImage;
    }
    
 
}
