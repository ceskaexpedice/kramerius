package cz.incad.Kramerius;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JPanel;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuBean;
import com.lizardtech.djvubean.DjVuImage;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.utils.IKeys;

public class AbstracThumbnailServlet extends GuiceServlet {

	public static final String SCALE_PARAMETER = "scale";
	public static final String SCALED_HEIGHT_PARAMETER = "scaledHeight";
	public static final String OUTPUT_FORMAT_PARAMETER="outputFormat";
	
	@Inject
	protected KConfiguration configuration;
	@Inject
	protected FedoraAccess fedoraAccess;
	
	protected Image scale(Image img, Rectangle pageBounds, HttpServletRequest req) {
		String spercent = req.getParameter(SCALE_PARAMETER);
		String sheight = req.getParameter(SCALED_HEIGHT_PARAMETER);
		if (spercent != null) {
			double percent = 1.0; {
				try {
					percent = Double.parseDouble(spercent);
				} catch (NumberFormatException e) {
					log(e.getMessage());
				}
			}
			return scaleByPercent(img, pageBounds, percent);
		} else if (sheight != null){
			int height = 200; {
				try {
					height = Integer.parseInt(sheight);
				} catch (NumberFormatException e) {
					log(e.getMessage());
				}
			}
			return scaleByHeight(img,pageBounds, height);
		} else return null;
	}
	
	protected Image scaleByHeight(Image img, Rectangle pageBounds, int height) {
		int nHeight = height;
		double div = (double)pageBounds.getHeight() / (double)nHeight;
		double nWidth = (double)pageBounds.getWidth() / div;
		Image scaledImage = img.getScaledInstance((int) nWidth, nHeight, Image.SCALE_DEFAULT);
		return scaledImage;
	}

	
	
	protected Image rawThumbnailImage(String uuid) throws XPathExpressionException, IOException {
		String mimetype = fedoraAccess.getThumbnailMimeType(uuid);
		if ((mimetype.equals(OutputFormats.JPEG.getMimeType())) ||
			(mimetype.equals(OutputFormats.PNG.getMimeType())))	{
			InputStream imageFULL = fedoraAccess.getThumbnail(uuid);
			return ImageIO.read(imageFULL);
			
		} else throw new IllegalArgumentException("unsupported mimetype '"+mimetype+"'");
		
	}
	
	protected Image rawFullImage(String uuid) throws IOException, MalformedURLException, XPathExpressionException {
		String mimetype = fedoraAccess.getImageFULLMimeType(uuid);
		if (mimetype.equals(OutputFormats.JPEG.getMimeType())) {
			InputStream imageFULL = fedoraAccess.getImageFULL(uuid);
			return ImageIO.read(imageFULL);
		} else if ((mimetype.equals(OutputFormats.DJVU.getMimeType())) || 
				  (mimetype.equals(OutputFormats.XDJVU.getMimeType()))){
			String imageUrl = getDJVUServlet(uuid);
	        com.lizardtech.djvu.Document doc = new com.lizardtech.djvu.Document(new URL(imageUrl));
	        doc.setAsync(true);
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
			
		} else throw new IllegalArgumentException("unsupported mimetype '"+mimetype+"'");
		
	}
	protected void writeImage(HttpServletResponse resp, Image scaledImage, OutputFormats format) throws IOException {
		if ((format.equals(OutputFormats.JPEG)) || 
			(format.equals(OutputFormats.PNG))) {
			resp.setContentType(format.getMimeType());
			OutputStream os = resp.getOutputStream();
			KrameriusImageSupport.writeImageToStream(scaledImage, format.getJavaFormat(), os);
		} else throw new IllegalArgumentException("unsupported mimetype '"+format+"'");
	}

	public static String rawContent(KConfiguration configuration, String uuid, HttpServletRequest request) {
		return configuration.getThumbServletUrl()+"?scaledHeight=" + KConfiguration.getKConfiguration().getScaledHeight() + "&uuid="+uuid+"&rawdata=true";
	}

	protected String getDJVUServlet(String uuid) {
		String imagePath = this.configuration.getDJVUServletUrl()+"?"+IKeys.UUID_PARAMETER+"="+uuid+"&outputFormat=RAW";
		return imagePath;
	}
	protected Image scaleByPercent(Image img, Rectangle pageBounds, double percent) {
		if ((percent <= 0.95) || (percent >= 1.15)) {
			int nWidth = (int) (pageBounds.getWidth() * percent);
			int nHeight = (int) (pageBounds.getHeight() * percent);
			Image scaledImage = img.getScaledInstance(nWidth, nHeight, Image.SCALE_DEFAULT);
			return scaledImage;
		} else return img;
	}


	public enum OutputFormats {
		JPEG("image/jpeg","jpg"),
		PNG("image/png","png"),
		XDJVU("image/x.djvu",null),
		DJVU("image/djvu",null),
		RAW(null,null);
		
		String mimeType;
		String javaFormat;

		private OutputFormats(String mimeType, String javaFormat) {
			this.mimeType = mimeType;
			this.javaFormat = javaFormat;
		}

		public String getMimeType() {
			return mimeType;
		}

		public String getJavaFormat() {
			return javaFormat;
		}

	}
}
