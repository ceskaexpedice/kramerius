package cz.incad.kramerius.rest.api.client;

import java.nio.charset.Charset;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;














import org.apache.commons.codec.binary.Base64;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class CDKClient {


	public static void avail(Client c) {
		WebResource r = c
				.resource("http://mzk.summon.serialssolutions.com/2.0.0/search?s.q=forest&s.ff=ContentType,or,1,15");
		
	}
	
	public static void main(String[] args) throws SignatureException, ParseException {

		Client c = Client.create();
		method(c);

//		String exp =  "application/xml" + "\n" +
//				  "Tue, 30 Jun 2009 12:10:24 GMT" + "\n" +
//				  "api.summon.serialssolutions.com" + "\n" +
//				  "/2.0.0/search" + "\n" +
//				  "s.ff=ContentType,or,1,15" + "&" + "s.q=forest" + "\n";
//		String buildDigest = buildDigest("ed2ee2e0-65c1-11de-8a39-0800200c9a66", exp);
//		System.out.println(buildDigest);
	}

	// Host: api.summon.serialssolutions.com
	// Accept: application/xml
	// x-summon-date: Mon, 29 Jun 2009 23:07:20 GMT
	// Authorization: Summon test;TUDe5VCP520njOGCP8bg3uKR6OM=
	// x-summon-session-id: 0iy5u3VAkySQ3/Nbd7TT+WKdEYk=

	static String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	//static String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    static SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
    static {
    	formatter.setTimeZone(TimeZone.getTimeZone("GMT"));  
    }
    
	private static void method(Client c) throws SignatureException, ParseException {
		Date d = new Date();
		System.out.println(MediaType.APPLICATION_JSON);
		System.out.println(formatter.format(d));
		System.out.println(d.getTime());
		//Tue, 15 Oct 2013 09:30:18 GMT
		//System.out.println(formatter.parse("Tue, 15 Oct 2013 09:30:18 GMT").getTime() - d.getTime());
		
		//String key = "yB24UTPp94g5W5K6nG6EA1DP628GO27G";
		String key = "pavels";
		
		WebResource r = c.resource("http://api.summon.serialssolutions.com/2.0.0/search?s.q=forest");
		
		
		r.setProperty("x-summon-date", formatter.format(d));
		r.setProperty("Host", "api.summon.serialssolutions.com");
		r.setProperty("Accept", MediaType.APPLICATION_XML);
		
		//r.setProperty("x-summon-session-id", "b80ef825-6b72-4185-9039-f34259b78f5e");
		
		Map<String, String[]> pars = new HashMap<String, String[]>();
		pars.put("s.q", new String[] {"forest"});
		
		String idString = computeIdString(MediaType.APPLICATION_XML, formatter.format(d), "api.summon.serialssolutions.com", "/2.0.0/search", pars);
		System.out.println("constructed id string '"+idString+"'");
		
		String digest = buildDigest(key, idString);
		r.setProperty("Authorization", "Summon pavels;"+digest);
//
		String t = r/*.accept(MediaType.APPLICATION_XML)*/.get(String.class);
//
//		System.out.println(t);

	}

	public static String buildDigest(String key, String idString) throws SignatureException {
		  try {
		    String algorithm = "HmacSHA1";
		    Charset charset = Charset.forName("utf-8");
		    SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), algorithm);
		    Mac mac = Mac.getInstance(algorithm);
		    mac.init(signingKey);
		    return new String(Base64.encodeBase64(mac.doFinal(idString.getBytes(charset))), charset);
		  } catch (Exception e) {
		    throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		  }
		}
	
	private static String computeIdString(String acceptType, String date, String host,
			String path, Map<String, String[]> queryParameters) {
		return appendStrings(acceptType, date, host, path,
				computeSortedQueryString(queryParameters));
	}

	private static String computeSortedQueryString(
			Map<String, String[]> queryParameters) {
		List<String> parameterStrings = new ArrayList<String>();

		// for each parameter, get its key and values
		for (Map.Entry<String, String[]> entry : queryParameters.entrySet()) {

			// for each value, create a string in the format key=value
			for (String value : entry.getValue()) {
				parameterStrings.add(entry.getKey() + "=" + value);
			}
		}

		// sort the individual parameters
		Collections.sort(parameterStrings);
		StringBuilder queryString = new StringBuilder();

		// append strings together with the '&' character as a delimiter
		for (String parameterString : parameterStrings) {
			queryString.append(parameterString).append("&");
		}

		// remove any final trailing '&'
		if (queryString.length() > 0) {
			queryString.setLength(queryString.length() - 1);
		}
		return queryString.toString();
	}

	// append the strings together with '\n' as a delimiter
	public static String appendStrings(String... strings) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strings) {
			stringBuilder.append(string).append("\n");
		}
		return stringBuilder.toString();
	}
}
