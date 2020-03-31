package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.server.Base64Utils;

import cz.incad.kramerius.utils.conf.KConfiguration;

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

        LOGGER.log(Level.FINE, String.format("Opening connection %s", urlString));

        return uc;
    }

    public static void connectAndHandleRedirect(String urlString, String user, String pass, HandleConnectionResponse response) throws MalformedURLException, IOException {
        connectAndHandleRedirect(urlString, user,pass, 0, response);
    }
    public static void connectAndHandleRedirect(String urlString, String user, String pass, int level, HandleConnectionResponse response) throws MalformedURLException, IOException {
        HttpURLConnection con = (HttpURLConnection) openConnection(urlString, user, pass);
        con.connect();
        int code = con.getResponseCode();
        switch (code) {
            case HttpURLConnection.HTTP_OK:
                response.handleResponse(con);
                break;
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_MOVED_TEMP:
                int max = KConfiguration.getInstance().getConfiguration().getInt("http.redirect.max", 2);
                if (level < max) {
                    Map<String, List<String>> headerFields = con.getHeaderFields();
                    if (headerFields.containsKey("Location")) {
                        List<String> location = headerFields.get("Location");
                        if (!location.isEmpty()) {
                            connectAndHandleRedirect(location.get(0), user, pass, level+1, response);
                        }
                    }
                } else {
                    response.handleResponse(con);
                }
                break;
            default:
                response.handleResponse(con);
                break;

        }

    }

    @FunctionalInterface
    public static interface HandleConnectionResponse {

        public void handleResponse(HttpURLConnection uc);
    }

}
