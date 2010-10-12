package cz.incad.kramerius.imaging;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.stream.FileImageOutputStream;

import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport.ScalingMethod;
import junit.framework.TestCase;

public class KrameriusImageSupportTest extends TestCase {

	public void testResJPG() throws IOException {
		URL url = this.getClass().getResource("res.jpg");
		assertNotNull(url);
		BufferedImage img = KrameriusImageSupport.readImage(url, ImageMimeType.JPEG, 0);

		BufferedImage badScale = KrameriusImageSupport.scale(img, 455, 650,ScalingMethod.BILINEAR, false);
		FileImageOutputStream badFos = new FileImageOutputStream(new File("badFile.jpg"));
		KrameriusImageSupport.writeImageToStream(badScale, "jpeg", badFos,1.0f);

		BufferedImage goodScale = KrameriusImageSupport.scale(img, 455, 650);
		FileImageOutputStream goodFos = new FileImageOutputStream(new File("goodFile.jpg"));
		KrameriusImageSupport.writeImageToStream(goodScale, "jpeg", goodFos,1.0f);

	}
	
}
