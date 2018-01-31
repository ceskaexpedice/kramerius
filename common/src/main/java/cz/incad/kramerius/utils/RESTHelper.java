package cz.incad.kramerius.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.Base64Utils;

import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.solr.SolrUtils;

/**
 * Umoznuje se dotazovat na fedoru, ktera potrebuje autentizaci
 * 
 * @author pavels
 */
public class RESTHelper {

    public static Logger LOGGER = Logger.getLogger(RESTHelper.class.getName());
    
    public static InputStream inputStream(String urlString, String user, String pass) throws IOException {
        URLConnection uc = null;
        try {
            uc = openConnection(urlString, user, pass);
            return uc.getInputStream();
        } catch (IOException e) {
            HttpURLConnection httpUrl = (HttpURLConnection) uc;
            if (httpUrl != null) {
                int responseCode = httpUrl.getResponseCode();
                LOGGER.severe(urlString + " returned status code " + responseCode);
            }
            throw e;
        }
    }

    public static URLConnection openConnection(String urlString, String user, String pass)
            throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        String userPassword = user + ":" + pass;
        String encoded = Base64Utils.toBase64(userPassword.getBytes());
        URLConnection uc = url.openConnection();
        uc.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
        uc.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout", "10000")));
        uc.setRequestProperty("Authorization", "Basic " + encoded);
        return uc;
    }
    
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        Document solrDataInternal = SolrUtils.getSolrDataInternal(SolrUtils.UUID_QUERY + "\"" + "uuid:xxxx" );
        XMLUtils.print(solrDataInternal, System.out);
        
    }

}
