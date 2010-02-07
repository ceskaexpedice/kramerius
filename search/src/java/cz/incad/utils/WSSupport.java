package cz.incad.utils;

import static cz.incad.Kramerius.FedoraUtils.fedoraUrl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;

import com.google.gwt.user.server.Base64Utils;

import cz.i.kramerius.gwtviewers.server.pid.LexerException;
import cz.i.kramerius.gwtviewers.server.pid.PIDParser;
import cz.incad.Kramerius.ThumbnailServlet;

public class WSSupport {

	public static void uploadThumbnailAsDatastream(final String user, final String pwd, final String pid, final HttpServletRequest request) throws LexerException, NoSuchAlgorithmException, IOException {
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
	    
	    String rawContent = ThumbnailServlet.rawContent(parser.getObjectId(), request);
		port.addDatastream(pid, null, null, "THUMB", false, "image/jpeg", "HTTP", rawContent, "E", "A", "MD5",WSSupport.calcMD5SUM(rawContent) , "none");
	}

	public static String calcMD5SUM(String surl) throws IOException, NoSuchAlgorithmException {
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

	public static void redirectFromFedora(HttpServletResponse resp, URL url)
			throws IOException {
		InputStream inputStream = url.openStream();
		resp.setContentType("image/jpeg");
		ServletOutputStream outputStream = resp.getOutputStream();
		IOUtils.copyStreams(inputStream, outputStream);
	}

}
