package cz.incad.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.apache.http.HttpStatus.SC_OK;

public class IndexList {
    public static void main(String[] args) {
        File inputFile = new File("/Users/vlahoda/Desktop/monograph.json");

        InputStream is = null;
        try {
            is = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + inputFile);
        }

        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);
        JSONArray results = object.getJSONArray("results");
        for (int i = 2000; i < results.length(); i++) {
            String pid = results.getJSONObject(i).keySet().toArray()[0].toString().replace("info:fedora/","");
            String uri = "http://172.16.2.105/search/api/v4.6/processes?def=reindex";


            try {

                HttpURLConnection httpClient =  (HttpURLConnection) new URL(uri).openConnection();
                String auth = "krameriusAdmin:krameriusAdmin";
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                String authHeaderValue = "Basic " + new String(encodedAuth);
                httpClient.setRequestProperty("Authorization", authHeaderValue);

                //add reuqest header
                httpClient.setRequestMethod("POST");
                httpClient.setRequestProperty("User-Agent", "Mozilla/5.0");
                httpClient.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                httpClient.setRequestProperty("Content-type", "application/json");


                String body = "{\"parameters\":[\"fromKrameriusModel\",\""+pid+"\",\""+pid+"\"]}";
                // Send post request
                httpClient.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(httpClient.getOutputStream())) {
                    wr.writeBytes(body);
                    wr.flush();
                }

                int responseCode = httpClient.getResponseCode();
                System.out.println("\nSending 'POST' request to URL : " + uri);
                System.out.println("Post parameters : " + body);
                System.out.println("Response Code : " + responseCode);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
