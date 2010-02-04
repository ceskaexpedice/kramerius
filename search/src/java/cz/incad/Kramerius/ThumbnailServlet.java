package cz.incad.Kramerius;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.utils.IOUtils.*;
import static cz.incad.utils.JNDIUtils.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
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
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;
import org.fedora.api.ObjectFactory;

import com.google.gwt.user.server.Base64Utils;
import com.lizardtech.djvu.DjVuInfo;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvubean.DjVuBean;
import com.lizardtech.djvubean.DjVuImage;

import cz.i.kramerius.gwtviewers.client.CalcTest;
import cz.i.kramerius.gwtviewers.server.pid.LexerException;
import cz.i.kramerius.gwtviewers.server.pid.PIDParser;
import cz.incad.utils.IOUtils;


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

	// TODO! Dat do jndi
	private static final String FEDORA_ADMIN_USER_KEY="fedoraAdminUser";
	private static final String FEDORA_ADMIN_USER_PSWD="fedoraAdminPassword";
	
	@Override
	public void init() throws ServletException {}	

	protected static void uploadThumbnailToFedora(final String user, final String pwd, final String pid, final HttpServletRequest request) throws LexerException, NoSuchAlgorithmException, IOException {
		FedoraAPIMService service = null;
		FedoraAPIM port = null;
		Authenticator.setDefault(new Authenticator() { 
            protected PasswordAuthentication getPasswordAuthentication() { 
               return new PasswordAuthentication(user, pwd.toCharArray()); 
             } 
           }); 

        try {
            service = new FedoraAPIMService(new URL(fedoraUrl+"/wsdl?api=API-M"),
                    new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-M-Service"));
        } catch (MalformedURLException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        port = service.getPort(FedoraAPIM.class);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
        ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);

        PIDParser parser = new PIDParser(pid);
        parser.objectPid();
        
        String rawContent = rawContent(parser.getObjectId(), request);
		port.addDatastream(pid, null, null, "THUMB", false, "image/jpeg", "HTTP", rawContent, "E", "A", "MD5",calcMD5SUM(rawContent) , "none");
	}
	
	

	protected static String calcMD5SUM(String surl) throws IOException, NoSuchAlgorithmException {
		URL url = new URL(surl);
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		DigestInputStream digestInput = new DigestInputStream(is, MessageDigest.getInstance("MD5"));
		digestInput.on(true);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copyStreams(digestInput, bos);
		byte[] digest = digestInput.getMessageDigest().digest();
		digestInput.close();
		is.close();
		String base64 = Base64Utils.toBase64(digest);
		return base64;
	}
	


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
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
				URL url = new URL(getThumbnailFromFedora(uuid));
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					redirectFromFedora(resp, url);
				} else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
					String adminName = getJNDIValue(FEDORA_ADMIN_USER_KEY);
					String adminPswd = getJNDIValue(FEDORA_ADMIN_USER_PSWD);
					uploadThumbnailToFedora(adminName, adminPswd, "uuid:"+uuid, req);
					redirectFromFedora(resp, url);
				}
			}
		} catch (LexerException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static void redirectFromFedora(HttpServletResponse resp, URL url)
			throws IOException {
		InputStream inputStream = url.openStream();
		resp.setContentType("image/jpeg");
		ServletOutputStream outputStream = resp.getOutputStream();
		IOUtils.copyStreams(inputStream, outputStream);
	}
	
	
	

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
		return "http://194.108.215.84:8080/search/thumb?scaledHeight=220&uuid="+uuid+"&rawdata=true";
	}
	
}
