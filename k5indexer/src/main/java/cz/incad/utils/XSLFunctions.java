package cz.incad.utils;

import cz.incad.kramerius.k5indexer.FieldsConfig;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author alberto
 */
public class XSLFunctions {

    static final Logger logger = Logger.getLogger(XSLFunctions.class.getName());
    private static XSLFunctions _sharedInstance = null;

    private final Configuration config;
    private Map<String, String> collections = new HashMap<String, String>();
    Document colsDoc;

    private UTFSort utf_sort;

    public synchronized static XSLFunctions getInstance() throws Exception {
        if (_sharedInstance == null) {
            _sharedInstance = new XSLFunctions();
        }
        return _sharedInstance;
    }

    public XSLFunctions() throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        init(config.getString("k5indexer.xslt.url"),
                config.getString("k5indexer.xslt.user"),
                config.getString("k5indexer.xslt.pass"));
    }

    public XSLFunctions(String source, String user, String pass) throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        init(source, user, pass);

    }
    
    public static String fixDatum(String datum_str){
        logger.log(Level.INFO, "fixing datum for {0}...", datum_str);
        return datum_str;
    }

    public static String detailsToJson(String val, String model) throws JSONException {
        JSONObject js = new JSONObject();
        try {
            String[] parts = val.split("##", -1);
            if ("page".equals(model)) {
                js.put("label", parts[0]);
                js.put("type", parts[1]);
            } else if ("periodicalitem".equals(model)) {
                js.put("title", parts[0]);
                js.put("subtitle", parts[1]);
                js.put("date", parts[2]);
                js.put("number", parts[3]);
            } else if ("periodicalvolume".equals(model)) {
                js.put("date", parts[0]);
                js.put("number", parts[1]);
            } else if ("monographunit".equals(model)) {
                js.put("title", parts[0]);
                js.put("number", parts[1]);
            } else if ("internalpart".equals(model)) {
                js.put("type", parts[0]);
                js.put("title", parts[1]);
                js.put("subtitle", parts[2]);
                js.put("list", parts[3]);
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error parsing details model: {0}, value: {1}", new Object[]{model, val});
        }
        return js.toString();
    }

    public static String replace(String s, String s1, String s2) throws Exception {
        return s.replaceAll(s1, s2);

    }

    private void init(String source, String user, String pass) throws Exception {
        try {
            utf_sort = new UTFSort();
            utf_sort.init();
            InputStream inputStream = RESTHelper.inputStream(source + "/api/v5.0/vc", user, pass);
            StringWriter sw = new StringWriter();
            org.apache.commons.io.IOUtils.copy(inputStream, sw, "UTF-8");
            JSONArray vc = new JSONArray(sw.toString());

            colsDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element cols = colsDoc.createElement("collections");
            colsDoc.appendChild(cols);
            for (int i = 0; i < vc.length(); i++) {
                String pid = vc.getJSONObject(i).getString("pid");
                JSONObject descs = vc.getJSONObject(i).getJSONObject("descs");
                collections.put(pid, descs.toString());

                Element col = colsDoc.createElement("collection");
                col.setAttribute("pid", pid);
                Iterator it = descs.keys();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    Element des = colsDoc.createElement("desc");
                    des.setAttribute("lang", key);
                    des.setAttribute("label", descs.getString(key));
                    col.appendChild(des);
                }
                cols.appendChild(col);
            }
            logger.log(Level.INFO, "XSLFunctions initialized");
        } catch (Exception ex) {
            System.out.println("error in XSLFunctions" + ex);
            throw new Exception(ex);
        }
    }

    public static String prepareCzech(String s) throws Exception {
        return XSLFunctions.getInstance().utf_sort.translate(s);
    }

    public static String getCollectionNames(String pid) throws Exception {
        XSLFunctions xsl = XSLFunctions.getInstance();
        if (!xsl.collections.containsKey(pid)) {
            return xsl.collections.get(pid);
        } else {
            return "";
        }
    }

    public static Node getCollectionsDoc() throws Exception {
        return XSLFunctions.getInstance().colsDoc.getDocumentElement();
    }

//    public NodeList getCollectionNames(String pid) {
//        Document doc = new DocumentImpl();
//        if (!collections.containsKey(pid)) {
//            return collections.get(pid);
//        } else {
//            return "";
//        }
//    }
    public static String encode(String url) throws URIException {
        return URIUtil.encodeQuery(url);
    }
}
