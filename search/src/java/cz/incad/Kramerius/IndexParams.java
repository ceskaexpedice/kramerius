/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.Kramerius;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Administrator
 */
public class IndexParams {

    public String datum = "/modsCollection/mods/originInfo[@transliteration='publisher']/dateIssued/text()";
    public String parent_title;
    public String parent_pid;
    public String path;
    public String root_title;
    public String root_model;
    public String abeceda_title;
    public String abeceda_autor;
    public HashMap<String, String> paramsMap = new HashMap<String, String>();

    public IndexParams(String pid, String model) {
        init(pid, model);
    }

    public String toUrlString() {
        StringBuffer s = new StringBuffer();
        try {
            Iterator iterator = paramsMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                s.append("&" + key + "=" + java.net.URLEncoder.encode(paramsMap.get(key), "UTF-8"));
            //s.append("&" + key + "=" + paramsMap.get(key));
            }
        } catch (Exception ex) {
            s.append(ex.toString());
        }
        return s.toString();
    }

    public void merge(IndexParams parentParams) {
        try {
            if (paramsMap.containsKey("PATH")) {
                paramsMap.put("PATH", parentParams.paramsMap.get("PATH") + "/" + paramsMap.get("PATH"));
            } else {
                paramsMap.put("PATH", parentParams.paramsMap.get("PATH"));
            }
            paramsMap.put("PARENT_MODEL", parentParams.paramsMap.get("MODEL"));
            paramsMap.put("PARENT_PID", parentParams.paramsMap.get("PID"));

            if (!paramsMap.containsKey("DATUM") && parentParams.paramsMap.containsKey("DATUM")) {
                paramsMap.put("DATUM", parentParams.paramsMap.get("DATUM"));
                parseDatum(parentParams.paramsMap.get("DATUM"));
            }
            paramsMap.put("ROOT_PID", parentParams.paramsMap.get("ROOT_PID"));
            paramsMap.put("ROOT_MODEL", parentParams.paramsMap.get("ROOT_MODEL"));
            paramsMap.put("ROOT_TITLE", parentParams.paramsMap.get("ROOT_TITLE"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDatum(String datumStr) {
        Integer dataInt;
        try {
            dataInt = Integer.parseInt(datumStr);
            paramsMap.put("ROK", datumStr);
        } catch (NumberFormatException ex) {
        }
        //Datum muze byt typu 1906 - 1945
        if (datumStr.contains("-")) {
            String begin = datumStr.split("-")[0].trim();
            String end = datumStr.split("-")[1].trim();

            try {
                dataInt = Integer.parseInt(begin);
                dataInt = Integer.parseInt(end);
                paramsMap.put("DATUM_BEGIN", begin);
                paramsMap.put("DATUM_END", end);
            } catch (NumberFormatException ex) {
            }
        }

        //Datum je typu dd.mm.yyyy
        try {
            DateFormat formatter = new SimpleDateFormat("dd.mm.yyyy");
            DateFormat outformatter = new SimpleDateFormat("yyyy");
            Date dateValue = formatter.parse(datumStr);

            paramsMap.put("ROK", outformatter.format(dateValue));
        } catch (Exception e) {
        }
    }

    public void init(String pid, String model) {
        try {
            //System.out.println(model);
            paramsMap.put("PATH", model);
            paramsMap.put("PID", pid);
            paramsMap.put("ROOT_PID", pid);
            paramsMap.put("MODEL", model);
            paramsMap.put("ROOT_MODEL", model);
            String command = FedoraUtils.fedoraUrl + "/get/" + pid + "/BIBLIO_MODS";
            Document contentDom = UrlReader.getDocument(command);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xPathStr = "";
            if (model.equals("periodicalvolume")) {
                xPathStr = "/modsCollection/mods/part/date/text()";
            } else if (model.equals("periodicalitem")) {
                xPathStr = "/modsCollection/mods/part/date/text()";
            } else if (model.equals("periodical")) {
                xPathStr = "/modsCollection/mods/originInfo[@transliteration='publisher']/dateIssued/text()";
            } else {
                xPathStr = "/modsCollection/mods/originInfo[@transliteration='publisher']/dateIssued/text()";
            }
            XPathExpression expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(contentDom, XPathConstants.NODE);

            if (node != null) {
                String datumStr = node.getNodeValue();
                paramsMap.put("DATUM", datumStr);
                parseDatum(datumStr);

            }

            xPathStr = "/modsCollection/mods/titleInfo/title/text()";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
            if (node != null) {
                paramsMap.put("ROOT_TITLE", node.getNodeValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
