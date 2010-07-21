package cz.incad.Kramerius;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuImage;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.fedora.Handler;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class AbstracThumbnailServlet extends GuiceServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(AbstracThumbnailServlet.class.getName());
	
	public static final String SCALE_PARAMETER = "scale";
	public static final String SCALED_HEIGHT_PARAMETER = "scaledHeight";
	public static final String SCALED_WIDTH_PARAMETER = "scaledWidth";
	public static final String OUTPUT_FORMAT_PARAMETER="outputFormat";
	
	@Inject
	protected transient KConfiguration configuration;

	@Inject
	@Named("securedFedoraAccess")
	protected transient FedoraAccess fedoraAccess;
	
	protected Image scale(Image img, Rectangle pageBounds, HttpServletRequest req) {
		String spercent = req.getParameter(SCALE_PARAMETER);
		String sheight = req.getParameter(SCALED_HEIGHT_PARAMETER);
		String swidth = req.getParameter(SCALED_WIDTH_PARAMETER);
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
		} else if (swidth != null){
			int width = 200; {
				try {
					width = Integer.parseInt(swidth);
				} catch (NumberFormatException e) {
					log(e.getMessage());
				}
			}
			return scaleByWidth(img,pageBounds, width);
		}else return null;
	}
	
	protected Image scaleByHeight(Image img, Rectangle pageBounds, int height) {
		int nHeight = height;
		double div = (double)pageBounds.getHeight() / (double)nHeight;
		double nWidth = (double)pageBounds.getWidth() / div;
		Image scaledImage = scale(img, (int)nWidth, nHeight);
		return scaledImage;
	}

	protected Image scaleByWidth(Image img, Rectangle pageBounds, int width) {
		int nWidth = width;
		double div = (double)pageBounds.getWidth() / (double)nWidth;
		double nHeight = (double)pageBounds.getHeight() / div;
		Image scaledImage = scale(img, nWidth,(int) nHeight);
		return scaledImage;
	}

	
	
	protected Image rawThumbnailImage(String uuid) throws XPathExpressionException, IOException, SecurityException {
		String mimetype = fedoraAccess.getThumbnailMimeType(uuid);
		if ((mimetype.equals(OutputFormats.JPEG.getMimeType())) ||
			(mimetype.equals(OutputFormats.PNG.getMimeType())))	{
			InputStream imageFULL = fedoraAccess.getThumbnail(uuid);
			return ImageIO.read(imageFULL);
			
		} else throw new IllegalArgumentException("unsupported mimetype '"+mimetype+"'");
		
	}
	
	protected Image rawFullImage(String uuid, HttpServletRequest request) throws IOException, MalformedURLException, XPathExpressionException {
		String mimetype = fedoraAccess.getImageFULLMimeType(uuid);
		if (mimetype.equals(OutputFormats.JPEG.getMimeType())) {
			InputStream imageFULL = fedoraAccess.getImageFULL(uuid);
			return ImageIO.read(imageFULL);
		} else if ((mimetype.equals(OutputFormats.DJVU.getMimeType())) ||
				  (mimetype.equals(OutputFormats.VNDDJVU.getMimeType())) ||
				  (mimetype.equals(OutputFormats.XDJVU.getMimeType()))){
			if (fedoraAccess.isImageFULLAvailable(uuid)) {
				URL url = new URL("fedora","",0,uuid+"/IMG_FULL", new Handler(fedoraAccess));
				com.lizardtech.djvu.Document doc = new com.lizardtech.djvu.Document(url);
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
			} else {
				throw new IOException("image is not available");
			}
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

//	public static String rawContent(KConfiguration configuration, String uuid, HttpServletRequest request) {
//		return currentURL(request)+"&scaledHeight=" + KConfiguration.getKConfiguration().getScaledHeight() + "&uuid="+uuid+"&rawdata=true";
//	}

//	protected String getDJVUServlet(String uuid, HttpServletRequest request) {
//		String path =  hiddenDJVUServlet(request);
//		path += "&"+IKeys.UUID_PARAMETER+"="+uuid+"&outputFormat=RAW";
//		return path;
//	}

//	public static String hiddenDJVUServlet(HttpServletRequest request) {
//		//"dvju"
//		try {
//			URL url = new URL(request.getRequestURL().toString());
//			String path = url.getPath();
//			StringBuffer buffer = new StringBuffer();
//			StringTokenizer tokenizer = new StringTokenizer(path,"/");
//			if(tokenizer.hasMoreTokens()) { buffer.append(tokenizer.nextToken()); }
//			buffer.append("/_djvu").append("?").append(RequestSecurityAcceptor.REMOTE_ADDRESS).append("=").append(request.getRemoteAddr());
//			String imagePath = url.getProtocol()+"://"+url.getHost()+":"+url.getPort()+"/"+buffer.toString();
//			return imagePath;
//		} catch (MalformedURLException e) {
//			LOGGER.log(Level.SEVERE, e.getMessage(), e);
//			return "<no url>";
//		}
//	}
	protected Image scaleByPercent(Image img, Rectangle pageBounds, double percent) {
		if ((percent <= 0.95) || (percent >= 1.15)) {
			int nWidth = (int) (pageBounds.getWidth() * percent);
			int nHeight = (int) (pageBounds.getHeight() * percent);
			Image scaledImage = scale(img, nWidth, nHeight);
			return scaledImage;
		} else return img;
	}

	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}

	
	

	public enum OutputFormats {
		JPEG("image/jpeg","jpg"),
		PNG("image/png","png"),

		VNDDJVU("image/vnd.djvu",null),
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
	
	public enum ScalingMethod {
		REPLICATE, AREA_AVERAGING, BILINEAR, BICUBIC, NEAREST_NEIGHBOR, BILINEAR_STEPPED, BICUBIC_STEPPED, NEAREST_NEIGHBOR_STEPPED
	}
	
	public static Image scale (Image img, int targetWidth, int targetHeight){
		KConfiguration config = KConfiguration.getInstance();
		ScalingMethod method = ScalingMethod.valueOf(config.getProperty("scalingMethod","BICUBIC_STEPPED"));
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

 
}
