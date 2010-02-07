package cz.incad.Kramerius;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.utils.JNDIUtils.*;
import static cz.incad.utils.WSSupport.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.imageio.ImageIO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.fedora.api.ObjectFactory;

import com.lizardtech.djvu.DjVuInfo;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuBean;
import com.lizardtech.djvubean.DjVuImage;

import cz.i.kramerius.gwtviewers.client.CalcTest;
import cz.i.kramerius.gwtviewers.server.pid.LexerException;
import cz.incad.Kramerius.ThumbnailStorage.Type;
import cz.incad.utils.IOUtils;
import cz.incad.utils.WSSupport;


/** 
 * Servlet na ziskavani nahledu
 * @author pavels
 */
public class ThumbnailServlet extends HttpServlet {

	private static final String UUID_PARAMETER = "uuid";
	private static final String SCALE_PARAMETER = "scale";
	private static final String PAGE_PARAMETER = "page";
	private static final String RAWDATA_PARAMETER = "rawdata";
	
	private static final String SCALED_HEIGHT_PARAMETER ="scaledHeight";
	private static final String DS_LOCATION = "";

	protected ThumbnailStorage.Type type = ThumbnailStorage.Type.DISK;
	
	@Override
	public void init() throws ServletException {}	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uuid = req.getParameter(UUID_PARAMETER);
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
			if (thumbStorage.checkExists(uuid)) {
				thumbStorage.redirectToServlet(uuid, resp);
			} else {
				thumbStorage.uploadThumbnail(uuid, req);
				thumbStorage.redirectToServlet(uuid, resp);
			}
		}

	}

//	public static void redirectFromDisk(HttpServletResponse resp, URL url, String pid) {
//		String homeFolder = System.getProperty("user.home");
//		File imgFolders = new File(homeFolder+File.pathSeparator+"images");
//		imgFolders.mkdirs();
//		File imgFile = new File(imgFolders, pid+".jpeg");
//		InputStream inputStream = n;
//		resp.setContentType("image/jpeg");
//		ServletOutputStream outputStream = resp.getOutputStream();
//		IOUtils.copyStreams(inputStream, outputStream);
// 	}

	private void rawImage(HttpServletRequest req, HttpServletResponse resp,
			String uuid, int page) throws IOException, MalformedURLException {
		String imageUrl = getDjVuImage(uuid);
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

	
	public static String rawContent(String uuid, HttpServletRequest request) {
		//http://localhost:8080/search/thumb?scale=0.3&uuid=17ba56f0-96f7-11de-a20b-000d606f5dc6
		//http://194.108.215.84:8080/search/thumb?scaledHeight=220&uuid=17ba56f0-96f7-11de-a20b-000d606f5dc6&rawdata=true
		//String requestURI = request.getRequestURI();
		//String returnURI = requestURI + "&"+RAWDATA_PARAMETER+"=true";
		
		//return "http://194.108.215.84:8080/search/thumb?scaledHeight=220&uuid="+uuid+"&rawdata=true";
		return "http://localhost:8080/search/thumb?scaledHeight=220&uuid="+uuid+"&rawdata=true";
	}
	
}
