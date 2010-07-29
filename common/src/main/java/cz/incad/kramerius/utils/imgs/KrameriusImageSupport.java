package cz.incad.kramerius.utils.imgs;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.xml.xpath.XPathExpressionException;

import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuImage;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.fedora.Handler;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;


public class KrameriusImageSupport {

	public static Image readImage(String uuid, String stream, FedoraAccess fedoraAccess) throws XPathExpressionException, IOException {
		String mimetype = fedoraAccess.getMimeTypeForStream("uuid:"+uuid, stream);
		ImageMimeType loadFromMimeType = ImageMimeType.loadFromMimeType(mimetype);
		URL url = new URL("fedora","",0,uuid+"/"+stream, new Handler(fedoraAccess));
		return readImage(url, loadFromMimeType);
	}
	
	public static Image readImage(URL url, ImageMimeType type) throws IOException {
		if (type.javaNativeSupport()) {
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
	
	public static Image scale (Image img, int targetWidth, int targetHeight){
		KConfiguration config = KConfiguration.getInstance();
		ScalingMethod method = ScalingMethod.valueOf(config.getProperty("scalingMethod","BICUBIC_STEPPED"));
		//System.out.println("SCALE:"+method+" width:"+targetWidth+" height:"+targetHeight);
		switch (method){
		case REPLICATE:
			return img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_REPLICATE);
		case AREA_AVERAGING:
    		return img.getScaledInstance(targetWidth, targetHeight, Image.SCALE_AREA_AVERAGING);
		case BILINEAR:
    		return getScaledInstanceJava2D(toBufferedImage(img),targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, false);
		case BICUBIC:
    		return getScaledInstanceJava2D(toBufferedImage(img),targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, false);
		case NEAREST_NEIGHBOR:
    		return getScaledInstanceJava2D(toBufferedImage(img),targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, false);
		case BILINEAR_STEPPED:
    		return getScaledInstanceJava2D(toBufferedImage(img),targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
		case BICUBIC_STEPPED:
    		return getScaledInstanceJava2D(toBufferedImage(img),targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
		case NEAREST_NEIGHBOR_STEPPED:
    		return getScaledInstanceJava2D(toBufferedImage(img),targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, true);
		}
		return null;
	}
	
	 /**
     * Convenience method that returns a scaled instance of the
     * provided {@code BufferedImage}.
     *
     * @param img the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param hint one of the rendering hints that corresponds to
     *    {@code RenderingHints.KEY_INTERPOLATION} (e.g.
     *    {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
     *    {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
     * @param higherQuality if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in downscaling cases, where
     *    {@code targetWidth} or {@code targetHeight} is
     *    smaller than the original dimensions, and generally only when
     *    the {@code BILINEAR} hint is specified)
     * @return a scaled version of the original {@code BufferedImage}
     */
    private static BufferedImage getScaledInstanceJava2D(BufferedImage img,
                                           int targetWidth,
                                           int targetHeight,
                                           Object hint,
                                           boolean higherQuality){
  
        int type = (img.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = (BufferedImage)img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }
        
        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }
    
    private static BufferedImage toBufferedImage(Image img) {
    	BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null),BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bufferedImage;
    }
    
    public static enum ScalingMethod {
    	REPLICATE, AREA_AVERAGING, BILINEAR, BICUBIC, NEAREST_NEIGHBOR, BILINEAR_STEPPED, BICUBIC_STEPPED, NEAREST_NEIGHBOR_STEPPED
    }


}

