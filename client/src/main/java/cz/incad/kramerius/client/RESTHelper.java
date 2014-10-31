package cz.incad.kramerius.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import cz.incad.kramerius.client.cache.SimpleJSONResultsCache;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.utils.IOUtils;

//TODO: remove 
public class RESTHelper {
    
    public static final String CACHEABLE_PREFIXES = "";

    public static final boolean childrenURL(String su) throws MalformedURLException {
        URL url = new URL(su);
        String spath = url.getPath();
        //search/api/v5.0/item/uuid:0eaa6730-9068-11dd-97de-000d606f5dc6/children
        if (spath.startsWith("/search/api/v5.0/item") && (spath.endsWith("/children"))) {
            return true;
        } else return false;
    }

    public static boolean isCachable(String su) throws MalformedURLException {
        if (childrenURL(su)) return true;
        return false;
    }
    
    
    //http://localhost:8080/client/api/item/uuid:530719f5-ee95-4449-8ce7-12b0f4cadb22/children
    
    
    public static void fillResponse(String urlString, HttpServletResponse resp, HttpServletRequest req) throws IOException, URISyntaxException {
        fillResponse(urlString, resp, req.getHeader("Accept"));
    }

    public static void fillResponse(String urlString, HttpServletResponse resp, String accept) throws IOException, URISyntaxException {
        if (isCachable(urlString)) {
            if (SimpleJSONResultsCache.CACHE.isPresent(urlString)) {
                byte[] bytes = SimpleJSONResultsCache.CACHE.getJSONResult(urlString);
                resp.setContentType("application/json;charset=utf-8");
                resp.getOutputStream().write(bytes);
            } else {

                URLConnection uc = openConnection(urlString);
                HttpURLConnection hcon = (HttpURLConnection) uc;
                hcon.setRequestProperty("Accept", accept);
                hcon = (HttpURLConnection) customRedirect(resp, hcon, accept);

                copyHeaders(resp, hcon);

                String uniqString = hcon.getURL().toURI().toString();
                byte[] bytes =SimpleJSONResultsCache.CACHE.processThroughCache(uniqString, hcon);
                resp.setContentType(hcon.getContentType());
                IOUtils.copyStreams(new ByteArrayInputStream(bytes), resp.getOutputStream());
                
            }
        } else {
            URLConnection uc = openConnection(urlString);
            HttpURLConnection hcon = (HttpURLConnection) uc;
            hcon.setRequestProperty("Accept", accept);
            hcon = (HttpURLConnection) customRedirect(resp, hcon, accept);
            //hcon.setInstanceFollowRedirects(true);
            hcon.setRequestProperty("Accept", accept);

            copyHeaders(resp, hcon);
            int status = hcon.getResponseCode();
            resp.setStatus(status);
            copyStreams(resp, hcon, status);
            
        }
    }

    public static void fillResponse(String urlString, String user, String pass, HttpServletResponse resp, HttpServletRequest req) throws IOException, URISyntaxException {
        if (isCachable(urlString)) {
            if (SimpleJSONResultsCache.CACHE.isPresent(urlString)) {
                byte[] bytes = SimpleJSONResultsCache.CACHE.getJSONResult(urlString);
                resp.setContentType("application/json;charset=utf-8");
                resp.getOutputStream().write(bytes);
            } else {

                URLConnection uc = openConnection(urlString, user, pass);
                HttpURLConnection hcon = (HttpURLConnection) uc;
                hcon.setRequestProperty("Accept", req.getHeader("Accept"));
                hcon = (HttpURLConnection) customRedirect(resp, hcon, req.getHeader("Accept"));

                copyHeaders(resp, hcon);

                String uniqString = hcon.getURL().toURI().toString();
                byte[] bytes =SimpleJSONResultsCache.CACHE.processThroughCache(uniqString, hcon);
                resp.setContentType(hcon.getContentType());
                IOUtils.copyStreams(new ByteArrayInputStream(bytes), resp.getOutputStream());
                
            }
        } else {
            URLConnection uc = openConnection(urlString, user, pass);
            HttpURLConnection hcon = (HttpURLConnection) uc;
            hcon.setRequestProperty("Accept", req.getHeader("Accept"));
            hcon = (HttpURLConnection) customRedirect(resp, hcon, req.getHeader("Accept"));

            copyHeaders(resp, hcon);

            int status = hcon.getResponseCode();
            resp.setStatus(status);

            copyStreams(resp, hcon, status);
        }
    }

    private static void setExpireHeadersForJSONs( HttpServletResponse resp) {

        Date lastModifiedDate = new Date();
        Calendar instance = Calendar.getInstance();
        instance.roll(Calendar.YEAR, 1);
        resp.setDateHeader("Last Modified", lastModifiedDate.getTime());
        resp.setDateHeader("Last Fetched", System.currentTimeMillis());
        resp.setDateHeader("Expires", instance.getTime().getTime());
    }

    
    private static void copyStreams(HttpServletResponse resp,
            HttpURLConnection hcon, int status) throws IOException {
        if (status == 200) {
            InputStream is = hcon.getInputStream();
            resp.setContentType(hcon.getContentType());
            IOUtils.copyStreams(is, resp.getOutputStream());
        }
    }



    public static InputStream inputStream(String urlString, String accept) throws IOException {
        URLConnection uc = openConnection(urlString);
        HttpURLConnection hcon = (HttpURLConnection) uc;
        hcon.setRequestProperty("Accept", accept);
        //hcon.setInstanceFollowRedirects(true);
        hcon = (HttpURLConnection) customRedirect(null, hcon, accept);
        return uc.getInputStream();
    }


    public static InputStream inputStream(String urlString) throws IOException {
        URLConnection uc = openConnection(urlString);
        HttpURLConnection hcon = (HttpURLConnection) uc;
        //hcon.setInstanceFollowRedirects(true);
        return uc.getInputStream();
    }
    
    public static URLConnection openConnection(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection uc = url.openConnection();
        HttpURLConnection hcon = (HttpURLConnection) uc;
        //hcon.setInstanceFollowRedirects(true);;
        //hcon = (HttpURLConnection) customRedirect(null,hcon,null);

        uc.setReadTimeout(Integer.parseInt("10000"));
        uc.setConnectTimeout(Integer.parseInt("10000"));
        return uc;
    }

    public static InputStream inputStream(String urlString, String user, String pass) throws IOException {
        URLConnection uc = openConnection(urlString, user, pass);
        return uc.getInputStream();
    }

    public static URLConnection customRedirect(HttpServletResponse resp, HttpURLConnection urlCon, String accept) throws IOException {
        int code = urlCon.getResponseCode();
        if (code >= 300 && code <= 307) {
            String headerField = urlCon.getHeaderField("Location");
            URL url = new URL(headerField);
            URLConnection retCon = url.openConnection();
            if (resp != null)  copyHeaders(resp, urlCon);
            if (accept != null)  retCon.setRequestProperty("Accept", accept);
            return retCon;
        } else return urlCon;
    }
    
    public static URLConnection openConnection(String urlString, String user,
            String pass) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URLConnection uc = url.openConnection();
        uc.setReadTimeout(Integer.parseInt("10000"));
        uc.setConnectTimeout(Integer.parseInt("10000"));
        
        String userPassword = user + ":" + pass;
        String encoded = Base64.encodeBase64String(userPassword.getBytes());
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
                String lastModifKey = "Last Modified";
                String lastFetchKey = "Last Fetched";
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
    }

    public static boolean isJSONType(HttpURLConnection hcon)  {
        Map<String, List<String>> headerFields = hcon.getHeaderFields();
        List<String> contentType = headerFields.get("Content-Type");
        if (contentType.size() == 1 && contentType.get(0).startsWith("application/json")) {
            return true;
        } else return false;
    }
    
}
