
package cz.incad.utils;

import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.UTFSort;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author alberto
 */
public class XSLFunctions {

    private final Configuration config;
    private Map<String, String> collections = new HashMap<String, String>();
    Document colsDoc;

    UTFSort utf_sort;
    
    public XSLFunctions() throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        init(config.getString("applicationUrl"), null, null);
    }
    
    public XSLFunctions(String source, String user, String pass) throws Exception {
        config = KConfiguration.getInstance().getConfiguration();
        init(source, user, pass);
        
    }
    
    public String detailsToJson(String val, String model){
        String s = "";
        return s;
    }
    
    public String replace(String s, String s1, String s2) throws Exception {
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
                Iterator it =descs.keys();
                while(it.hasNext()){
                    String key = (String) it.next();
                    Element des = colsDoc.createElement("desc");
                    des.setAttribute("lang", key);
                    des.setAttribute("label", descs.getString(key));
                    col.appendChild(des);
                }
                cols.appendChild(col);
            }
        } catch (Exception ex) {
            System.out.println("error in XSLFunctions" + ex);
            throw new Exception(ex);
        }
    }

    public String prepareCzech(String s) throws Exception {
        return utf_sort.translate(s);
    }

    public String getCollectionNames(String pid) {
        if (!collections.containsKey(pid)) {
            return collections.get(pid);
        } else {
            return "";
        }
    }

    public Node getCollectionsDoc() {
       return colsDoc.getDocumentElement();
    }

//    public NodeList getCollectionNames(String pid) {
//        Document doc = new DocumentImpl();
//        if (!collections.containsKey(pid)) {
//            return collections.get(pid);
//        } else {
//            return "";
//        }
//    }
    
    public String encode(String url) throws URIException {
        return URIUtil.encodeQuery(url);
    }
}
