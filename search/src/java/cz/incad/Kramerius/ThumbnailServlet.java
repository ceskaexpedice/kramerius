package cz.incad.Kramerius;

import static cz.incad.Kramerius.FedoraUtils.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

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

import org.apache.xalan.lib.sql.JNDIConnectionPool;

import com.lizardtech.djvu.DjVuInfo;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuBean;
import com.lizardtech.djvubean.DjVuImage;


/** 
 * Servlet na ziskavani nahledu
 * @author pavels
 */
public class ThumbnailServlet extends HttpServlet {

	private static final String UUID_PARAMETER = "uuid";
	private static final String SCALE_PARAMETER = "scale";
	private static final String PAGE_PARAMETER = "page";
	
	
	@Override
	public void init() throws ServletException {
	}	

	
	public Config getConfiguration() {
		return new Config();
	}




	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String sscale = req.getParameter(SCALE_PARAMETER);
		double scale = 1.0; {
			try {
				scale = Double.parseDouble(sscale);
			} catch (NumberFormatException e) {
				log(e.getMessage());
			}
		}
		
		String uuid = req.getParameter(UUID_PARAMETER);
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
		
		String imageUrl = getDjVuImage(uuid);
		DjVuBean bean = new DjVuBean();
		bean.setURL(new URL(imageUrl));
		// TODO !! Pozastavi thread
		DjVuImage djvuImage = bean.getImageWait();

		Rectangle pageBounds = djvuImage.getPageBounds(page);
		Image[] images = djvuImage.getImage(new JPanel(), new Rectangle(pageBounds.width,pageBounds.height));
		if (images.length == 1) {
			Image img = images[0];
			int nWidth = (int) (pageBounds.getWidth() * scale);
			int nHeight = (int) (pageBounds.getHeight() * scale);
			
			Image scaledImage = img.getScaledInstance(nWidth, nHeight, Image.SCALE_DEFAULT);
			BufferedImage bufImage = new BufferedImage(nWidth, nHeight, BufferedImage.TYPE_INT_RGB);
			Graphics gr = bufImage.getGraphics();
			gr.drawImage(scaledImage,0,0,null);
			gr.dispose();
			
			resp.setContentType("image/jpeg");
			ServletOutputStream outputStream = resp.getOutputStream();
			ImageIO.write(bufImage, "jpg", outputStream);
		}
	}
	
}
