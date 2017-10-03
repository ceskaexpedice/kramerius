package cz.incad.kramerius.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.utils.IOUtils;

public class RESTHelper {
    
    public static final String READ_TIMEOUT = "readTimeout";
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";


    public static void fillResponse(String urlString,HttpServletRequest req, HttpServletResponse resp,  Map<String, String> settings) throws IOException, URISyntaxException {
        fillResponse(urlString,req ,resp, req.getHeader("Accept"), settings);
    }

    public static void fillResponse(String urlString, HttpServletRequest req, HttpServletResponse resp, String accept, Map<String, String> settings) throws IOException, URISyntaxException {

            URLConnection uc = openConnection(req,urlString, settings);
            HttpURLConnection hcon = (HttpURLConnection) uc;
            hcon.setRequestProperty("Accept", accept);
            hcon = (HttpURLConnection) customRedirect(req, resp, hcon, accept);
            //hcon.setInstanceFollowRedirects(true);
            hcon.setRequestProperty("Accept", accept);

            copyHeaders(resp, hcon);
            int status = hcon.getResponseCode();
            resp.setStatus(status);
            copyStreams(resp, hcon, status);
    }

    public static void fillResponse(String urlString, String user, String pass, HttpServletRequest req, HttpServletResponse resp,  Map<String, String> settings) throws IOException, URISyntaxException {

            URLConnection uc = openConnection(req, urlString, user, pass, settings);
            HttpURLConnection hcon = (HttpURLConnection) uc;
            hcon.setRequestProperty("Accept", req.getHeader("Accept"));
            hcon = (HttpURLConnection) customRedirect(req, resp, hcon, req.getHeader("Accept"));

            copyHeaders(resp, hcon);

            int status = hcon.getResponseCode();
            resp.setStatus(status);

            copyStreams(resp, hcon, status);
    }

    
    private static void copyStreams(HttpServletResponse resp,
            HttpURLConnection hcon, int status) throws IOException {
        if (status == 200) {
            InputStream is = hcon.getInputStream();
            resp.setContentType(hcon.getContentType());
            IOUtils.copyStreams(is, resp.getOutputStream());
        }
    }

    public static InputStream inputStream(String urlString, String accept, HttpServletRequest req, Map<String, String> settings) throws IOException {
        URLConnection uc = openConnection(req, urlString,settings);
        HttpURLConnection hcon = (HttpURLConnection) uc;
        hcon.setRequestProperty("Accept", accept);
        hcon = (HttpURLConnection) customRedirect(req, null, hcon, accept);
        return uc.getInputStream();
    }


    public static InputStream inputStream(String urlString,HttpServletRequest req,  Map<String, String> settings) throws IOException {
        URLConnection uc = openConnection(req, urlString, settings);
        HttpURLConnection hcon = (HttpURLConnection) uc;
        return uc.getInputStream();
    }
    
    public static URLConnection openConnection(HttpServletRequest request, String urlString, Map<String, String> settings) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection uc = url.openConnection();
        HttpURLConnection hcon = (HttpURLConnection) uc;

        if (settings.containsKey(CONNECTION_TIMEOUT) && settings.containsKey(READ_TIMEOUT)) {
            uc.setReadTimeout(Integer.parseInt(settings.get(READ_TIMEOUT)));
            uc.setConnectTimeout(Integer.parseInt(settings.get(CONNECTION_TIMEOUT)));
        }
        uc.setRequestProperty(IPAddressUtils.X_IP_FORWARD, request.getRemoteAddr());
        return uc;
    }

    public static InputStream inputStream(String urlString, String user, String pass, HttpServletRequest req, Map<String, String> settings) throws IOException {
        URLConnection uc = openConnection(req, urlString, user, pass, settings);
        return uc.getInputStream();
    }

    public static URLConnection customRedirect(HttpServletRequest req, HttpServletResponse resp, HttpURLConnection urlCon, String accept) throws IOException {
        int code = urlCon.getResponseCode();
        if (code >= 300 && code <= 307) {
            String headerField = urlCon.getHeaderField("Location");
            URL url = new URL(headerField);
            URLConnection retCon = url.openConnection();
            ((HttpURLConnection)retCon).setRequestProperty("REMOTE_ADDR", req.getRemoteAddr());
            if (resp != null)  copyHeaders(resp, urlCon);
            if (accept != null)  retCon.setRequestProperty("Accept", accept);
            return retCon;
        } else return urlCon;
    }
    
    public static URLConnection openConnection(HttpServletRequest request, String urlString, String user,
            String pass, Map<String, String> settings) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection uc = url.openConnection();

        if (settings.containsKey(CONNECTION_TIMEOUT) && settings.containsKey(READ_TIMEOUT)) {
            uc.setReadTimeout(Integer.parseInt(settings.get(READ_TIMEOUT)));
            uc.setConnectTimeout(Integer.parseInt(settings.get(CONNECTION_TIMEOUT)));
        }

        String userPassword = user + ":" + pass;
        String encoded = Base64.encodeBase64String(userPassword.getBytes());

        uc.setRequestProperty(IPAddressUtils.X_IP_FORWARD, request.getRemoteAddr());
        uc.setRequestProperty("Authorization", "Basic " + encoded);
        return uc;
    }

    public static void copyHeaders(HttpServletResponse resp,
            HttpURLConnection hcon) {
        
        
        Map<String, List<String>> headerFields = hcon.getHeaderFields();
        List<String> nullfields = headerFields.get(null);
        for (String val : nullfields) {
            if (val.contains(":")) {
                String[] vals = val.split(":");
                String lastModifKey = "Last-Modified";
                String lastFetchKey = "Last-Fetched";
                if (vals.length >= 2) {
                    if (vals[0].equals(lastModifKey)) {
                        resp.setHeader(lastModifKey, vals[1]);
                    }
                    if (vals[0].equals(lastFetchKey)) {
                        resp.setHeader(lastFetchKey, vals[1]);
                    }
                }
            }
        }

        String expiresKey = "Expires";
        if (headerFields.containsKey(expiresKey)) {
            List<String> list = headerFields.get(expiresKey);
            for (String val : list) {
                resp.setHeader(expiresKey, val);
            }
        }
        
        String contentDisp = "Content-disposition";
        if (headerFields.containsKey(contentDisp)) {
            List<String> list = headerFields.get(contentDisp);
            for (String val : list) {
                resp.setHeader(contentDisp, val);
            }
        }
    }

    public static boolean isJSONType(HttpURLConnection hcon)  {
        Map<String, List<String>> headerFields = hcon.getHeaderFields();
        List<String> contentType = headerFields.get("Content-Type");
        if (contentType.size() == 1 && contentType.get(0).startsWith("application/json")) {
            return true;
        } else return false;
    }
    
}
