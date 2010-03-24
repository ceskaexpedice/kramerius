package cz.incad.Kramerius;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JPanel;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.lizardtech.djvubean.DjVuBean;
import com.lizardtech.djvubean.DjVuImage;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.IKeys;



/** 
 * Servlet na ziskavani nahledu
 * @author pavels
 */
public class ThumbnailServlet extends GuiceServlet {
	
	private static final String SCALE_PARAMETER = "scale";
	private static final String PAGE_PARAMETER = "page";
	private static final String RAWDATA_PARAMETER = "rawdata";
	
	private static final String SCALED_HEIGHT_PARAMETER ="scaledHeight";
	protected ThumbnailStorage.Type type = ThumbnailStorage.Type.FEDORA;

	@Inject
	protected KConfiguration configuration;
	@Inject
	protected FedoraAccess fedoraAccess;
	
	@Override
	public void init() throws ServletException {}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uuid = req.getParameter(IKeys.UUID_PARAMETER);
		String raw = req.getParameter(RAWDATA_PARAMETER);
		if (uuid == null) throw new ServletException("uuid cannot be null!");
		String spage = req.getParameter(PAGE_PARAMETER); 
		// default je djvu s jednou straknou... tudiz index 0
		if (spage == null) spage="0";
		int page = 0; {
			try {
				page = Integer.parseInt(spage);
			} catch (NumberFormatException e) {
				log(e.getMessage());
			}
		}
		if (raw != null) {
			rawImage(req, resp, uuid, page);
		} else {
			ThumbnailStorage thumbStorage = this.type.createStorage();
			getInjector().injectMembers(thumbStorage);
			if (thumbStorage.checkExists(uuid)) {
				thumbStorage.redirectToServlet(uuid, resp);
			}  else {
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		}
	}

	private void rawImage(HttpServletRequest req, HttpServletResponse resp,
			String uuid, int page) throws IOException, MalformedURLException {
		String imageUrl = getDJVUServlet(uuid);
		DjVuBean bean = new DjVuBean();
		bean.setURL(new URL(imageUrl));
		// TODO !! Pozastavi thread
		DjVuImage djvuImage = bean.getImageWait();
		Rectangle pageBounds = djvuImage.getPageBounds(page);
		Image[] images = djvuImage.getImage(new JPanel(), new Rectangle(pageBounds.width,pageBounds.height));
		if (images.length == 1) {
			
			Image img = images[0];
			Image scaledImage = scale(img,pageBounds, req);

			BufferedImage bufImage = new BufferedImage(scaledImage.getWidth(null), scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics gr = bufImage.getGraphics();
			gr.drawImage(scaledImage,0,0,null);
			gr.dispose();
			
			resp.setContentType("image/jpeg");
			ServletOutputStream outputStream = resp.getOutputStream();
			ImageIO.write(bufImage, "jpg", outputStream);
		}
	}



	private String getDJVUServlet(String uuid) {
		String imagePath = this.configuration.getDJVUServletUrl()+"?"+IKeys.UUID_PARAMETER+"="+uuid;
    	return imagePath;
	}

	private Image scale(Image img, Rectangle pageBounds, HttpServletRequest req) {
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





	private Image scaleByHeight(Image img, Rectangle pageBounds, int height) {
		int nHeight = height;
		double div = (double)pageBounds.getHeight() / (double)nHeight;
		double nWidth = (double)pageBounds.getWidth() / div;
		Image scaledImage = img.getScaledInstance((int) nWidth, nHeight, Image.SCALE_DEFAULT);
		return scaledImage;
	}

	private Image scaleByPercent(Image img, Rectangle pageBounds, double percent) {
		if ((percent <= 0.95) || (percent >= 1.15)) {
			int nWidth = (int) (pageBounds.getWidth() * percent);
			int nHeight = (int) (pageBounds.getHeight() * percent);
			
			Image scaledImage = img.getScaledInstance(nWidth, nHeight, Image.SCALE_DEFAULT);
			return scaledImage;
		} else return img;
	}

	
	public static String rawContent(KConfiguration configuration, String uuid, HttpServletRequest request) {
		return configuration.getThumbServletUrl()+"?scaledHeight=" + KConfiguration.getKConfiguration().getScaledHeight() + "&uuid="+uuid+"&rawdata=true";
	}

	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}

	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}
}
