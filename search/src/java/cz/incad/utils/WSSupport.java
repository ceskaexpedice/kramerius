package cz.incad.utils;


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
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;


import cz.incad.Kramerius.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class WSSupport {

	static Logger LOGGER = Logger.getLogger(WSSupport.class.getName());
	
	public static void uploadThumbnailAsDatastream(final KConfiguration configuration, final String pid, final HttpServletRequest request) throws LexerException, NoSuchAlgorithmException, IOException {
		FedoraAPIMService service = null;
		FedoraAPIM port = null;
		Authenticator.setDefault(new Authenticator() { 
	        protected PasswordAuthentication getPasswordAuthentication() { 
	           return new PasswordAuthentication(configuration.getFedoraUser(), configuration.getFedoraPass().toCharArray()); 
	         }
        });
	
		LOGGER.info("fedoraUser:"+configuration.getFedoraUser());
		LOGGER.info("fedoraPass:"+configuration.getFedoraPass());
        String spec = configuration.getFedoraHost()+"/wsdl?api=API-M";
		LOGGER.info("API-M"+spec);
	    try {
			service = new FedoraAPIMService(new URL(spec),
	                new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-M-Service"));
	    } catch (MalformedURLException e) {
	        System.out.println(e);
	        e.printStackTrace();
	    }
	    port = service.getPort(FedoraAPIM.class);
	    ((BindingProvider) port).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, configuration.getFedoraUser());
	    ((BindingProvider) port).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, configuration.getFedoraPass());
	    Binding binding = ((BindingProvider) port).getBinding();
	    List<Handler> chain = binding.getHandlerChain();
	    if (chain == null) {
	    	chain = new ArrayList<Handler>();
	    }
    	chain.add(new LoggingHandler());
	    LOGGER.info("adding logger to chain ");
	    binding.setHandlerChain(chain);
	    
	    
	    PIDParser parser = new PIDParser(pid);
	    parser.objectPid();
	    LOGGER.info("parsed object pid  ="+parser.getObjectId());

	    String rawContent = rawImage(configuration, parser.getObjectId(), request);
	    LOGGER.info("rawcontent  ="+rawContent);
		String nds = port.addDatastream(pid, FedoraUtils.IMG_THUMB, null, "Thumbnail", false, "image/jpeg", "HTTP", rawContent, "M", "A", "MD5",null, "none");
	}
	
	public static String rawImage(KConfiguration configuration, String uuid, HttpServletRequest request) {
		return configuration.getThumbServletUrl()+"?scaledHeight="+KConfiguration.getKConfiguration().getScaledHeight()+"&uuid="+uuid+"&rawdata=true";
	}

	public static String calcMD5SUM(String surl) throws IOException, NoSuchAlgorithmException {
		URL url = new URL(surl);
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		MessageDigest instance = MessageDigest.getInstance("MD5");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copyStreams(is, bos, instance);
		byte[] digest = instance.digest();

		String hexString = convertToHexa(digest);
		return hexString;
	}


	private static String md5string(byte[] bytes)
			throws NoSuchAlgorithmException {
		MessageDigest instance = MessageDigest.getInstance("MD5");
		byte[] digest = instance.digest(bytes);
		String hexString = convertToHexa(digest);
		return hexString;
	}


	private static byte[] imageBytes(InputStream is) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copyStreams(is, bos);
		is.close(); bos.flush();
		return bos.toByteArray();
	}


	private static InputStream imageInputStream(String surl)
			throws MalformedURLException, IOException {
		URL url = new URL(surl);
		URLConnection connection = url.openConnection();
		InputStream is = connection.getInputStream();
		return is;
	}

	private static String convertToHexa(byte[] digest) {
		StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<digest.length;i++) {
    		hexString.append(Integer.toHexString(0xFF & digest[i]));
    	}
		return hexString.toString();
	}

//	public static void redirectFromFedora(HttpServletResponse resp, URL url)
//			throws IOException {
//		InputStream inputStream = url.openStream();
//		resp.setContentType("image/jpeg");
//		ServletOutputStream outputStream = resp.getOutputStream();
//		IOUtils.copyStreams(inputStream, outputStream);
//	}

}
