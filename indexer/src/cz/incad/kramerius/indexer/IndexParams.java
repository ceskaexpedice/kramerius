/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.indexer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
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
    private static final Logger logger = Logger.getLogger(IndexParams.class.getName());
    public String datum = "/modsCollection/mods/originInfo[@transliteration='publisher']/dateIssued/text()";
    public String parent_title;
    public String parent_pid;
    public String path;
    public String root_title;
    public String root_model;
    public String abeceda_title;
    public String abeceda_autor;
    public String relsExtIndex;
    public HashMap<String, String> paramsMap = new HashMap<String, String>();

    public IndexParams(String pid, String model, Document contentDom, int _relsExtIndex) {
        init(pid.substring(5), model, contentDom, _relsExtIndex);
    }

    public IndexParams(String pid, Document contentDom) {
        init(pid.substring(5), null, contentDom, 0);
    }
    
    public void setParam(String name, String value){
        paramsMap.put(name, value);
    }

    public IndexParams(String pid, String model,
            String path, String pid_path, String _parent_model, String _parent_pid, String _datum,
            String _root_pid, String _root_model, String _root_title, String _language, String _relsExtIndex) {

        paramsMap.put("PATH", path);
        paramsMap.put("PID_PATH", pid_path);

        paramsMap.put("PARENT_MODEL", _parent_model);
        paramsMap.put("PARENT_PID", _parent_pid);

        paramsMap.put("DATUM", _datum);
        parseDatum(_datum);

        paramsMap.put("ROOT_PID", _root_pid);
        paramsMap.put("ROOT_MODEL", _root_model);
        paramsMap.put("ROOT_TITLE", _root_title);
        paramsMap.put("LANGUAGE", _language);
        paramsMap.put("ROOT_TITLE", _relsExtIndex);
    }

    public ArrayList<String> toArrayList(String pages) {
        ArrayList<String> s = new ArrayList<String>();
        Iterator iterator = paramsMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            s.add(key);
            s.add(paramsMap.get(key));
        //s.append("&" + key + "=" + paramsMap.get(key));
        }
        s.add("PAGESCOUNT");
        s.add(pages);
        return s;
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

    public void addPath(String s){
        if (paramsMap.containsKey("PATH")) {
                paramsMap.put("PATH", paramsMap.get("PATH") + "/" + s);
            }
    }

    public void merge(IndexParams parentParams) {
        try {
//            if (paramsMap.containsKey("PATH")) {
//                paramsMap.put("PATH", parentParams.paramsMap.get("PATH") + "/" + paramsMap.get("PATH"));
//            } else {
//                //paramsMap.put("PATH", parentParams.paramsMap.get("PATH"));
//            }
            paramsMap.put("PATH", parentParams.paramsMap.get("PATH"));
            if (paramsMap.containsKey("PID_PATH")) {
                paramsMap.put("PID_PATH", parentParams.paramsMap.get("PID_PATH") + "/" + paramsMap.get("PID_PATH"));
            } else {
                paramsMap.put("PID_PATH", parentParams.paramsMap.get("PID_PATH"));
            }

            paramsMap.put("LEVEL", Integer.toString(paramsMap.get("PATH").split("/").length - 1));
            paramsMap.put("PARENT_MODEL", parentParams.paramsMap.get("MODEL"));
            paramsMap.put("PARENT_PID", parentParams.paramsMap.get("PID"));

            if (!paramsMap.containsKey("DATUM") && parentParams.paramsMap.containsKey("DATUM")) {
                paramsMap.put("DATUM", parentParams.paramsMap.get("DATUM"));
                parseDatum(parentParams.paramsMap.get("DATUM"));
            }
            paramsMap.put("ROOT_PID", parentParams.paramsMap.get("ROOT_PID"));
            paramsMap.put("ROOT_MODEL", parentParams.paramsMap.get("ROOT_MODEL"));
            paramsMap.put("ROOT_TITLE", parentParams.paramsMap.get("ROOT_TITLE"));
            if (parentParams.paramsMap.containsKey("LANGUAGE")) {
                paramsMap.put("LANGUAGE", parentParams.paramsMap.get("LANGUAGE"));
            }

        } catch (Exception e) {
            logger.severe("error in IndexParams.merge: " + e.toString());
            
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

            try {
            String begin = datumStr.split("-")[0].trim();
            String end = datumStr.split("-")[1].trim();
                dataInt = Integer.parseInt(begin);
                dataInt = Integer.parseInt(end);
                paramsMap.put("DATUM_BEGIN", begin);
                paramsMap.put("DATUM_END", end);
            } catch (Exception ex) {
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

    private void init(String pid, String model, Document contentDom, int _relsExtIndex) {
        try {

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr;
            Node node;
            if (model == null) {
                expr = xpath.compile("//RDF/Description/*");
                model = "undefined";
                NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node childnode = nodes.item(i);
                    String nodeName = childnode.getNodeName();
                    if (nodeName.contains("hasModel")) {
                        model = childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1];
                        model = model.split(":")[1];
                        break;
                    }
                }
            }
            //System.out.println(model);
            paramsMap.put("RELS_EXT_INDEX", Integer.toString(_relsExtIndex));
            paramsMap.put("PATH", model);
            paramsMap.put("PID_PATH", pid);
            paramsMap.put("LEVEL", "0");

            paramsMap.put("PID", pid);
            paramsMap.put("ROOT_PID", pid);
            paramsMap.put("MODEL", model);
            paramsMap.put("ROOT_MODEL", model);
            factory = XPathFactory.newInstance();
            xpath = factory.newXPath();
            String xPathStr = "";
//logger.info("pid: " + pid + " -> model: " + model);
            String prefix = "/digitalObject/datastream[@ID='BIBLIO_MODS']/datastreamVersion[last()]/xmlContent/modsCollection/mods/";
            if (model.equals("periodicalvolume")) {
                xPathStr = prefix + "part/date/text()";
            } else if (model.equals("periodicalitem")) {
                xPathStr = prefix + "part/date/text()";
            } else if (model.equals("periodical")) {
                xPathStr = prefix + "originInfo[@transliteration='publisher']/dateIssued/text()";
            } else {
                xPathStr = prefix + "originInfo[@transliteration='publisher']/dateIssued/text()";
            }
            

//logger.info("pid: " + pid + " -> model: " + model + " -> xPathStr: " + xPathStr);
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(contentDom, XPathConstants.NODE);

            if (node != null) {
                String datumStr = node.getNodeValue();
                paramsMap.put("DATUM", datumStr);
//logger.info("datumStr: " + datumStr);
                parseDatum(datumStr);
            }

            xPathStr = "//modsCollection/mods/titleInfo/title/text()";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
            if (node != null) {
                paramsMap.put("ROOT_TITLE", node.getNodeValue().trim().replace("\n", ""));
            }

            xPathStr = "//modsCollection/mods/language/languageTerm/text()";
            expr = xpath.compile(xPathStr);
            node = (Node) expr.evaluate(contentDom, XPathConstants.NODE);
            if (node != null) {
                paramsMap.put("LANGUAGE", node.getNodeValue());
            }
            
            

        } catch (Exception e) {
            logger.severe("IndexParams.init error: " + model + " " + pid);
            logger.severe(e.toString());
        }
    }
}
