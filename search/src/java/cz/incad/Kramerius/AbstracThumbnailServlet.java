package cz.incad.Kramerius;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.net.httpserver.HttpServer;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.utils.FedoraUtils;
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
		//System.out.println("REQUEST PARAMS: sheight:"+sheight+"swidth:"+swidth);
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
		Image scaledImage = KrameriusImageSupport.scale(img, (int)nWidth, nHeight);
		return scaledImage;
	}

	protected Image scaleByWidth(Image img, Rectangle pageBounds, int width) {
		int nWidth = width;
		double div = (double)pageBounds.getWidth() / (double)nWidth;
		double nHeight = (double)pageBounds.getHeight() / div;
		Image scaledImage = KrameriusImageSupport.scale(img, nWidth,(int) nHeight);
		return scaledImage;
	}

	
	protected Image rawThumbnailImage(String uuid, int page) throws XPathExpressionException, IOException, SecurityException {
		return KrameriusImageSupport.readImage(uuid, FedoraUtils.IMG_THUMB_STREAM, this.fedoraAccess,page);
	}
	
	protected Image rawFullImage(String uuid, HttpServletRequest request, int page) throws IOException, MalformedURLException, XPathExpressionException {
		return KrameriusImageSupport.readImage(uuid, FedoraUtils.IMG_FULL_STREAM, this.fedoraAccess, page);
	}
	
	protected void writeImage(HttpServletRequest req, HttpServletResponse resp, Image scaledImage, OutputFormats format) throws IOException {
		if ((format.equals(OutputFormats.JPEG)) || 
			(format.equals(OutputFormats.PNG))) {
			resp.setContentType(format.getMimeType());

//			Last Modified   Fri Aug 27 2010 22:32:42 GMT+0200 (CET)
//			Last Fetched    Fri Aug 27 2010 22:32:43 GMT+0200 (CET)
//			Expires Tue Aug 31 2010 18:44:30 GMT+0200 (CET)
			
			setDateHaders(resp);
			setResponseCode(req, resp);
			OutputStream os = resp.getOutputStream();
			KrameriusImageSupport.writeImageToStream(scaledImage, format.getJavaFormat(), os);
			
			
		} else throw new IllegalArgumentException("unsupported mimetype '"+format+"'");
	}

    protected void setDateHaders(HttpServletResponse resp) {
        Calendar instance = Calendar.getInstance();
        instance.roll(Calendar.YEAR, 1);
        
        resp.setDateHeader("Last Modified", System.currentTimeMillis());
        resp.setDateHeader("Last Fetched", System.currentTimeMillis());
        resp.setDateHeader("Expires", instance.getTime().getTime());
    }
    
    
    protected void setResponseCode(HttpServletRequest request, HttpServletResponse response) {
        long dateHeader = request.getDateHeader("If-Modified-Since");
        if (dateHeader != -1) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
    }
	protected Image scaleByPercent(Image img, Rectangle pageBounds, double percent) {
		if ((percent <= 0.95) || (percent >= 1.15)) {
			int nWidth = (int) (pageBounds.getWidth() * percent);
			int nHeight = (int) (pageBounds.getHeight() * percent);
			Image scaledImage = KrameriusImageSupport.scale(img, nWidth, nHeight);
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
	
	
 
}
