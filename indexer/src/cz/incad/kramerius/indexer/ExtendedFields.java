package cz.incad.kramerius.indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Alberto
 * Handles and manages the fields not directly present in doc FOXML
 * 
 */
public class ExtendedFields {

    private static final Logger logger = Logger.getLogger(ExtendedFields.class.getName());
    private String root_title;
    private int relsExtIndex;
    private ArrayList<String> pid_paths;
    private ArrayList<String> model_paths;
    private FedoraOperations fo;
    FedoraAccess fa;
    HashMap<String, String> models_cache;
    HashMap<String, String> dates_cache;
    HashMap<String, String> root_title_cache;
    Date datum;
    String datum_str;
    String rok;
    String datum_begin;
    String datum_end;
    String xPathStr;
    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath = factory.newXPath();
    XPathExpression expr;
    private final String prefix = "//mods:mods/";
    DateFormat df;
    DateFormat solrDateFormat;

    public ExtendedFields(FedoraOperations fo) throws IOException {
        this.fo = fo;
        KConfiguration config = KConfiguration.getInstance();
        this.fa = new FedoraAccessImpl(config);
        models_cache = new HashMap<String, String>();
        dates_cache = new HashMap<String, String>();
        root_title_cache = new HashMap<String, String>();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        df = new SimpleDateFormat(config.getProperty("mods.date.format", "dd.MM.yyyy"));
        solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
    }

    public void clearCache(){
        models_cache.clear();
        dates_cache.clear();
        root_title_cache.clear();
        
    }
    
    public void setFields(String pid) throws Exception {
        pid_paths = new ArrayList<String>();
        pid_paths = fo.getPidPaths(pid);
        relsExtIndex = fo.getRelsIndex(pid);
        model_paths = new ArrayList<String>();
        for (String s : pid_paths) {
            model_paths.add(getModelPath(s));
        }
        setRootTitle();
        setDate();
    }

    private String getModelPath(String pid_path) throws IOException {
        String[] pids = pid_path.split("/");
        StringBuilder model_path = new StringBuilder();

        String model;
        for (String s : pids) {
            if (models_cache.containsKey(s)) {
                model_path.append(models_cache.get(s)).append("/");
            } else {
                model = fa.getKrameriusModelName(s);
                model_path.append(model).append("/");
                models_cache.put(s, model);
            }
        }
        return model_path.deleteCharAt(model_path.length()-1).toString();
    }

    public HashMap<String, String> toArray() {
        HashMap<String, String> paramsMap = new HashMap<String, String>();
        return paramsMap;
    }

    public String toXmlString(int pageNum) {
        StringBuilder sb = new StringBuilder();
        for (String s : pid_paths) {
            sb.append("<field name=\"pid_path\">").append(s).append(pageNum == 0 ? "" : "/@" + pageNum).append("</field>");
            String[] pids = s.split("/");
            if(pageNum != 0){
                sb.append("<field name=\"parent_pid\">").append(pids[pids.length - 1]).append("</field>");
            }else{
                if(pids.length==1){
                    sb.append("<field name=\"parent_pid\">").append(pids[0]).append("</field>");
                }else{
                    sb.append("<field name=\"parent_pid\">").append(pids[pids.length - 2]).append("</field>");
                }
            }
            
        }
        int level = pid_paths.get(0).split("/").length - 1;
        if(pageNum != 0){
            level++;
        }
        for (String s : model_paths) {
            if(pageNum != 0){
                sb.append("<field name=\"model_path\">").append(s).append("/page").append("</field>");
            }else{
                sb.append("<field name=\"model_path\">").append(s).append("</field>");
            }
        }
        sb.append("<field name=\"rels_ext_index\">").append(relsExtIndex).append("</field>");
        sb.append("<field name=\"root_title\">").append(root_title).append("</field>");
        sb.append("<field name=\"root_pid\">").append(pid_paths.get(0).split("/")[0]).append("</field>");
        sb.append("<field name=\"level\">").append(level).append("</field>");
        sb.append("<field name=\"datum_str\">").append(datum_str).append("</field>");
        if(datum!=null){
            sb.append("<field name=\"datum\">").append(solrDateFormat.format(datum)).append("</field>");
        }
        if (!rok.equals("")) {
            sb.append("<field name=\"rok\">").append(rok).append("</field>");
        }
        if (!datum_begin.equals("")) {
            sb.append("<field name=\"datum_begin\">").append(datum_begin).append("</field>");
        }
        if (!datum_end.equals("")) {
            sb.append("<field name=\"datum_end\">").append(datum_end).append("</field>");
        }
        return sb.toString();
    }

    private void setRootTitle() throws Exception {
        String root_pid = pid_paths.get(0).split("/")[0];
        if(root_title_cache.containsKey(root_pid)){
            root_title = root_title_cache.get(root_pid);
        }else{
            Document doc = fa.getDC(root_pid);
            root_title =  DCUtils.titleFromDC(doc);
            root_title_cache.put(root_pid, root_title);
        }
    }

    private void setDate() throws Exception {
        datum_str = "";
        rok = "";
        datum_begin = "";
        datum_end = "";
        datum = null;
        for (int j = 0; j < pid_paths.size(); j++) {
            String[] pid_path = pid_paths.get(j).split("/");
            for (int i = pid_path.length - 1; i > -1; i--) {
                String pid = pid_path[i];
                Document foxml = fa.getBiblioMods(pid);
                if (dates_cache.containsKey(pid)) {
                    datum_str = dates_cache.get(pid);
                    parseDatum(datum_str);
                    return;
                }  
                xPathStr = prefix + "mods:part/mods:date/text()";
                expr = xpath.compile(xPathStr);
                Node node = (Node) expr.evaluate(foxml, XPathConstants.NODE);
                if (node != null) {
                    datum_str = node.getNodeValue();
                    parseDatum(datum_str);
                    dates_cache.put(pid, datum_str);
                    return;
                } else {
                    xPathStr = prefix + "mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()";
                    expr = xpath.compile(xPathStr);
                    node = (Node) expr.evaluate(foxml, XPathConstants.NODE);
                    if (node != null) {
                        datum_str = node.getNodeValue();
                        parseDatum(datum_str);
                        dates_cache.put(pid, datum_str);
                        return;
                    }
                }
            }
        }
        
    }

    private void parseDatum(String datumStr) {        
        try{
            Date dateValue = df.parse(datumStr);
            DateFormat outformatter = new SimpleDateFormat("yyyy");
            rok = outformatter.format(dateValue);
            datum = dateValue;
        }catch(Exception e){
            if(datumStr.matches("\\d\\d\\d\\d")){ //rok
                rok = datumStr;
                datum_begin = rok;
                datum_end = rok;
            }else if (datumStr.matches("\\d\\d--")) {  //Datum muze byt typu 18--
                datum_begin = datumStr.substring(0,2)+"00";
                datum_end = datumStr.substring(0,2)+"99";
            }else if (datumStr.matches("\\d---")) {  //Datum muze byt typu 187-
                datum_begin = datumStr.substring(0,3)+"0";
                datum_end = datumStr.substring(0,3)+"9";
            }else if (datumStr.matches("\\d\\d\\d\\d[\\s]*-[\\s]*\\d\\d\\d\\d")) {  //Datum muze byt typu 1906 - 1945
                String begin = datumStr.split("-")[0].trim();
                String end = datumStr.split("-")[1].trim();
                datum_begin = begin;
                datum_end = end;
            }
        }
    }
}
