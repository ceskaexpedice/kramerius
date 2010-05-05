package cz.incad.kramerius.utils.imgs;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuImage;

import cz.incad.kramerius.utils.IOUtils;


public class KrameriusImageSupport {

	public static Image readImage(URL url, ImageMimeType type) throws IOException {
		if (type.isSupportedbyJava()) {
			return ImageIO.read(url.openStream());
		} else if ((type.equals(ImageMimeType.DJVU)) || 
					(type.equals(ImageMimeType.VNDDJVU)) ||
				  (type.equals(ImageMimeType.XDJVU))){
	        System.out.println("url = "+url);
			com.lizardtech.djvu.Document doc = new com.lizardtech.djvu.Document(url);
	        doc.setAsync(false);
	        DjVuPage[] p = new DjVuPage[1];
	        //read page from the document - index 0, priority 1, favorFast true
	        p[0] = doc.getPage(0, 1, true);
	        p[0].setAsync(false);
	        DjVuImage djvuImage = new DjVuImage(p, true);
			Rectangle pageBounds = djvuImage.getPageBounds(0);
			Image[] images = djvuImage.getImage(new JPanel(), new Rectangle(pageBounds.width,pageBounds.height));
			if (images.length == 1) {
				Image img = images[0];
				return img;
			} else return null;
		} else throw new IllegalArgumentException("unsupported mimetype '"+type.getValue()+"'");
	}

	public static void writeImageToStream(Image scaledImage, String javaFormat,
			OutputStream os) throws IOException {
		BufferedImage bufImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics gr = bufImage.getGraphics();
		gr.drawImage(scaledImage,0,0,null);
		gr.dispose();
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(bufImage, javaFormat, bos);
		IOUtils.copyStreams(new ByteArrayInputStream(bos.toByteArray()), os);
	}
}
