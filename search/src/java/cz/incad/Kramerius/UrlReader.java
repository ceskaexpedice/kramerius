/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.Kramerius;


import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Administrator
 */
public class UrlReader {
    public static Document getDocument(String urlStr) throws Exception {
        try {
            StringBuffer result = new StringBuffer();
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream(),
                    java.nio.charset.Charset.forName("UTF-8")));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            InputSource source = new InputSource(new StringReader(result.toString()));
            return builder.parse(source);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }
    
    public static String getString(String urlStr) {
        try {
            StringBuffer result = new StringBuffer();
            java.net.URL url = new java.net.URL(urlStr);

            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(url.openStream(),
                    java.nio.charset.Charset.forName("UTF-8")));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                result.append(inputLine);
            }
            in.close();
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
