
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.XSLFunctions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author alberto
 */
public class KrameriusDocument {

    private static final Logger logger = Logger.getLogger(KrameriusDocument.class.getName());
    private final Configuration config;
    private final FieldsConfig fieldsConfig;
    
    FedoraAccess fa;
    Commiter commiter;

    XPathFactory factory = XPathFactory.newInstance();
    XPath xpath;
    XPathExpression expr;

    private ArrayList<String> pid_paths;
    private ArrayList<String> model_paths;
    private ArrayList<Integer> rels_ext_indexes;
    List<String> predicates;

    DateFormat df;
    DateFormat solrDateFormat;

    private HashMap<String, Document> ds_cache;

    public KrameriusDocument() throws IOException, JSONException{ 
        config = KConfiguration.getInstance().getConfiguration();
        
        fa = new FedoraAccessImpl(KConfiguration.getInstance(), null);

        predicates = Arrays.asList(KConfiguration.getInstance().getPropertyList("fedora.treePredicates"));
        fieldsConfig = FieldsConfig.getInstance();
        commiter = Commiter.getInstance();

        factory = XPathFactory.newInstance();

        xpath = factory.newXPath();
        xpath.setNamespaceContext(new FedoraNamespaceContext());

        df = new SimpleDateFormat(config.getString("mods.date.format", "dd.MM.yyyy"));
        df.setLenient(false);
        solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        
    }

    private void add(SolrInputDocument doc) throws SolrServerException, IOException {
        commiter.add(doc);
    }

    private void getPidPaths(String pid) throws IllegalAccessException, Exception {
        if (Indexer.pid_paths_cache.containsKey(pid)) {
            pid_paths = Indexer.pid_paths_cache.get(pid);
        } else {
            ArrayList<String> c_pid_paths = new ArrayList<String>();
            c_pid_paths.add(pid);
            getPidPaths(c_pid_paths);
            Indexer.pid_paths_cache.put(pid, c_pid_paths);
            pid_paths = c_pid_paths;
        }
        logger.log(Level.FINE, "pid_paths for {0} : {1}", new Object[]{pid, pid_paths});
    }

    private void setContextFields(SolrInputDocument doc, String pid) throws Exception {
        getPidPaths(pid);
        model_paths = new ArrayList<String>();

        
        for (String s : pid_paths) {
            model_paths.add(getModelPath(s));
            doc.addField("pid_path", s);
            doc.addField("level", s.split("/").length - 1);

            String[] pids = s.split("/");
                if (pids.length == 1) {
                    doc.addField("parent_pid", pids[0]);
                } else {
                    doc.addField("parent_pid", pids[pids.length - 2]);
                }
            
        }
        setRootTitle(doc);

        rels_ext_indexes = getRelsIndexByPath(pid_paths);
        for (Integer i : rels_ext_indexes) {
            doc.addField("rels_ext_index", i);
        }

        String root_model = model_paths.get(0).split("/")[0];
        doc.addField("root_model", root_model);
        for (String s : model_paths) {
                doc.addField("model_path", s);
            
        }


        setDate(doc);
    }

    private void setRootTitle(SolrInputDocument doc) throws Exception {
        String root_pid = pid_paths.get(0).split("/")[0];
        doc.addField("root_pid", root_pid);
        String root_title;
        if (Indexer.root_title_cache.containsKey(root_pid)) {
            doc.addField("root_title", Indexer.root_title_cache.get(root_pid));
        } else {
            root_title = DCUtils.titleFromDC(fa.getDC(root_pid));
            doc.addField("root_title", root_title);
            Indexer.root_title_cache.put(root_pid, root_title);
        }
    }

    private void parseDatum(String datumStr, SolrInputDocument doc) {
        String rok = "";
        String datum_begin = "";
        String datum_end = "";
        Date datum = null;
        DateFormat outformatter = new SimpleDateFormat("yyyy");
        try {
            Date dateValue = df.parse(datumStr);
            rok = outformatter.format(dateValue);
            datum = dateValue;
        } catch (ParseException e) {
            if (datumStr.matches("\\d\\d\\d\\d")) { //rok
                rok = datumStr;
                datum_begin = rok;
                datum_end = rok;
            } else if (datumStr.matches("\\d\\d--")) {  //Datum muze byt typu 18--
                datum_begin = datumStr.substring(0, 2) + "00";
                datum_end = datumStr.substring(0, 2) + "99";
            } else if (datumStr.matches("\\d\\d\\.-\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d")) {  //Datum muze byt typu 19.-20.03.1890

                String end = datumStr.split("-")[1].trim();
                try {
                    Date dateValue = df.parse(end);
                    rok = outformatter.format(dateValue);
                    datum = dateValue;
                } catch (ParseException ex) {
                    logger.log(Level.FINE, "Cant parse date {0}", datumStr);
                }
            } else if (datumStr.matches("\\d---")) {  //Datum muze byt typu 187-
                datum_begin = datumStr.substring(0, 3) + "0";
                datum_end = datumStr.substring(0, 3) + "9";
            } else if (datumStr.matches("\\d\\d\\d\\d[\\s]*-[\\s]*\\d\\d\\d\\d")) {  //Datum muze byt typu 1906 - 1945
                String begin = datumStr.split("-")[0].trim();
                String end = datumStr.split("-")[1].trim();
                datum_begin = begin;
                datum_end = end;
            }
        }
        doc.addField("datum_str", datumStr);
        if (datum != null) {
            doc.addField("datum", solrDateFormat.format(datum));
        } 
        if (!rok.equals("")) {
            doc.addField("rok", rok);
        }
        if (!datum_begin.equals("")) {
            doc.addField("datum_begin", datum_begin);
        }
        if (!datum_end.equals("")) {
            doc.addField("datum_end", datum_end);
        }
    }

    private void setDate(SolrInputDocument doc) throws Exception {

        for (int j = 0; j < pid_paths.size(); j++) {
            String[] pid_path = pid_paths.get(j).split("/");
            for (int i = pid_path.length - 1; i > -1; i--) {
                String pid = pid_path[i];
                if (Indexer.dates_cache.containsKey(pid)) {
                    parseDatum(Indexer.dates_cache.get(pid), doc);
                    return;
                }
                Document mods = fa.getBiblioMods(pid);
                expr = xpath.compile("mods:modsCollection/mods:mods/mods:part/mods:date/text()");
                Node node = (Node) expr.evaluate(mods, XPathConstants.NODE);
                if (node != null) {
                    String datum_str = node.getNodeValue();
                    parseDatum(datum_str, doc);
                    Indexer.dates_cache.put(pid, datum_str);
                    return;
                } else {
                    expr = xpath.compile("mods:modsCollection/mods:mods/mods:originInfo[@transliteration='publisher']/mods:dateIssued/text()");
                    node = (Node) expr.evaluate(mods, XPathConstants.NODE);
                    if (node != null) {
                        String datum_str = node.getNodeValue();
                        parseDatum(datum_str, doc);
                        Indexer.dates_cache.put(pid, datum_str);
                        return;
                    } else {
                        expr = xpath.compile("mods:modsCollection/mods:mods/mods:originInfo/mods:dateIssued/text()");
                        node = (Node) expr.evaluate(mods, XPathConstants.NODE);
                        if (node != null) {
                            String datum_str = node.getNodeValue();
                            parseDatum(datum_str, doc);
                            Indexer.dates_cache.put(pid, datum_str);
                            return;
                        }
                    }
                }
            }
        }

    }

    private String getModelPath(String pid_path) throws IOException {
        String[] pids = pid_path.split("/");
        StringBuilder model_path = new StringBuilder();

        String model;
        for (String s : pids) {
            if (Indexer.models_cache.containsKey(s)) {
                model_path.append(Indexer.models_cache.get(s)).append("/");
            } else {
                model = fa.getKrameriusModelName(s);
                model_path.append(model).append("/");
                Indexer.models_cache.put(s, model);
            }
        }
        return model_path.deleteCharAt(model_path.length() - 1).toString();
    }

    private ArrayList<Integer> getRelsIndexByPath(ArrayList<String> pid_paths) throws Exception {

        ArrayList<Integer> idxs = new ArrayList<Integer>();
        String parent;
        String fedoraPid;
        String uuid;
        if (!pid_paths.isEmpty()) {
            for (String s : pid_paths) {
                String[] pids = s.split("/");
                if (pids.length == 1) {
                    idxs.add(0);
                } else {
                    fedoraPid = "info:fedora/" + pids[pids.length - 1];
                    parent = pids[pids.length - 2];
                    Document relsExt = fa.getRelsExt(parent);
                    Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description", FedoraNamespaces.RDF_NAMESPACE_URI);
                    List<Element> els = XMLUtils.getElements(descEl);
                    int i = 0;
                    for (Element el : els) {
                        if (predicates.contains(el.getLocalName())) {
                            if (el.hasAttribute("rdf:resource")) {
                                uuid = el.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                                if (uuid.equals(fedoraPid)) {
                                    idxs.add(i);
                                    break;
                                }
                                i++;
                            }
                        }
                    }
                }
            }
        } else {
            idxs.add(0);
        }
        return idxs;
    }

    private void getPidPaths(ArrayList<String> pid_paths) throws InstantiationException, IllegalAccessException, Exception {
        String first;
        boolean changed = false;
        ArrayList<String> old = new ArrayList<String>(pid_paths);
        pid_paths.clear();
        for (int i = 0; i < old.size(); i++) {
            first = old.get(i).split("/")[0];
            ArrayList<String> p = RIndexHelper.getParentsArray(first);
            if (!p.isEmpty()) {
                for (String s : p) {
                    if (Indexer.pid_paths_cache.containsKey(s)) {
                        ArrayList<String> parent_pid_paths = Indexer.pid_paths_cache.get(s);
                        for(String pp: parent_pid_paths){
                            pid_paths.add(pp + "/" + old.get(i));
                        }
                    } else {
                        if (!old.get(i).contains(s)) {
                            changed = true;
                            pid_paths.add(s + "/" + old.get(i));
                        } else {
                            logger.log(Level.WARNING, "Cyclic reference on {0}", s);
                            Indexer.warnings++;
                            pid_paths.add(old.get(i));
                        }
                    }
                }
            } else {
                pid_paths.add(old.get(i));
            }
        }
        if (changed) {
            getPidPaths(pid_paths);
        }
    }

    /**
     * make the document
     */
    private SolrInputDocument makeDoc(String pid, Document dc, Document rels, Document profile) throws JSONException, IOException, SolrServerException, ParserConfigurationException, SAXException, XPathExpressionException, Exception {
        SolrInputDocument doc = new SolrInputDocument();
        

        //Set fields 
        doc.addField("PID", pid);
        
        String title = DCUtils.titleFromDC(dc);
        doc.addField(fieldsConfig.getMappedField("title"), title);
        String model = fa.getKrameriusModelName(pid);
        doc.addField(fieldsConfig.getMappedField("fedora_model"), model);
        
        expr = xpath.compile("rdf:RDF/rdf:Description/kramerius:handle");
        doc.addField("handle", expr.evaluate(rels, XPathConstants.STRING));
        expr = xpath.compile("rdf:RDF/rdf:Description/kramerius:policy");
        doc.addField("dostupnost", expr.evaluate(rels, XPathConstants.STRING));
        
        expr = xpath.compile("apia:objectProfile/apia:objCreateDate");
        doc.addField("created_date", expr.evaluate(profile, XPathConstants.STRING));
        expr = xpath.compile("apia:objectProfile/apia:objLastModDate");
        doc.addField("modified_date", expr.evaluate(profile, XPathConstants.STRING));
        expr = xpath.compile("apia:objectProfile/apia:objState");
        doc.addField("status", expr.evaluate(profile, XPathConstants.STRING));

        boolean isPDF = false;
        if (fa.isImageFULLAvailable(pid)) {
            doc.addField("viewable", true);
            String mimeType = fa.getImageFULLMimeType(pid);
            isPDF = mimeType.indexOf("pdf") > -1;
            doc.addField("img_full_mime", mimeType);
        } else {
            doc.addField("viewable", false);
        }

        //Fields from resource index
        setContextFields(doc, pid);

        //Set fields in fields.json configuration file
        Iterator it = fieldsConfig.dataStreams();
        while (it.hasNext()) {
            String dsname = (String) it.next();
            addDataStreamFields(dsname, doc, pid, fieldsConfig.getDataStream(dsname));
        }

        if(isPDF){
            KrameriusPDFDocument pdf = new KrameriusPDFDocument(pid, fa.getImageFULL(pid));
            int pages = pdf.getPagesCount();
            if(pages == 1){
                doc.addField("text_ocr", pdf.getPage(1));
            }else{
                for(int i=1; i<=pages; i++){
                    String pagePid = pid + Indexer.PDF_PAGE_SEPARATOR + i;
                    SolrInputDocument pageDoc = doc.deepCopy();
                    pageDoc.setField("PID", pagePid);
                    pageDoc.setField("fedora_model", "page");
                    
                    pageDoc.setField("parent_pid", pid);
                    pageDoc.removeField("pid_path");
                    pageDoc.removeField("level");
                    for (String s : pid_paths) {
                        pageDoc.addField("pid_path", s + Indexer.PID_PATH_SEPARATOR + pagePid);
                        pageDoc.addField("level", s.split("/").length);
                    }
                    doc.removeField("model_path");
                    for (String s : model_paths) {
                            doc.addField("model_path", s + "/page");
                    }
                    pageDoc.addField("text_ocr", pdf.getPage(i));
                    commiter.add(pageDoc);
                    Indexer.pdfpages++;
                }
            }
            pdf.closeDocument();
        }else{
            if (fa.isStreamAvailable(pid, "TEXT_OCR")) {
                StringWriter sw = new StringWriter();
                org.apache.commons.io.IOUtils.copy(fa.getDataStream(pid, "TEXT_OCR"), sw, "UTF-8");
                doc.addField("text_ocr", sw.toString());
            }
        }
        
        
        //Fields with special procesing
        setProcessedFields(doc);
        
        commiter.add(doc);
        ds_cache.clear();
        return doc;
    }
    
    private void setProcessedFields(SolrInputDocument doc) throws Exception {
        List<String> browseModels = Arrays.asList(config.getStringArray("k5indexer.browseModels"));
        if(browseModels.contains(doc.getField("fedora_model").getValue().toString())){
            String b = XSLFunctions.prepareCzech(doc.getField("title").getValue().toString());
            doc.addField("browse_title", b);
        }
        
        String a = XSLFunctions.prepareCzech(doc.getField("autor").getValue().toString());
        doc.addField("browse_autor", a);
        
    }

    private JSONObject makeJson(String pid) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        json.put("PID", pid);
        json.put("fedora_model", fa.getKrameriusModelName(pid));
        if (fa.isStreamAvailable(pid, "TEXT_OCR")) {
            StringWriter sw = new StringWriter();
            org.apache.commons.io.IOUtils.copy(fa.getDataStream(pid, "TEXT_OCR"), sw, "UTF-8");
            json.put("text_ocr", sw.toString());
        }
        //addDC(json, pid);
        return json;
    }
    

    private void addDataStreamFields(String dsname, SolrInputDocument doc, String pid, JSONObject jsonDs) throws IOException, ParserConfigurationException, SAXException, JSONException, XPathExpressionException {
        if (fa.isStreamAvailable(pid, dsname)) {
            Document dom;
            if (ds_cache.containsKey(dsname)) {
                dom = ds_cache.get(dsname);
            } else {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                cz.incad.kramerius.utils.IOUtils.copyStreams(fa.getDataStream(pid, dsname), bos);
                dom = XMLUtils.parseDocument(new ByteArrayInputStream(bos.toByteArray()), true);
            }
            String prefix = jsonDs.getString("prefix");
            JSONObject fields = jsonDs.getJSONObject("fields");
            Iterator it = fields.keys();
            while (it.hasNext()) {
                String indexname = (String) it.next();
                JSONArray paths = fields.optJSONArray(indexname);
                if (paths != null) {
                    for (int i = 0; i < paths.length(); i++) {
                        getXpathValue(indexname, prefix + paths.getString(i), doc, dom);
                    }
                } else {
                    getXpathValue(indexname, prefix + fields.optString(indexname), doc, dom);
                }

            }
        }
    }

    private void getXpathValue(String indexname, String xPathStr, SolrInputDocument doc, Document dom) throws XPathExpressionException {
        expr = xpath.compile(xPathStr);
        NodeList nodes = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            doc.addField(indexname, nodes.item(i).getTextContent());
        }
    }
    
    private void indexOne(String rpid, boolean onlyUpdate, Document rels) throws Exception {
        logger.log(Level.INFO, "{0}. indexing {1}", new Object[]{++Indexer.total, rpid});
        ds_cache = new HashMap<String, Document>();
        
        Document dc = fa.getDC(rpid);
        ds_cache.put("DC", dc);
        Document profile = fa.getObjectProfile(rpid);

        if (onlyUpdate) {
            //Check dates in index and fedora
            Date indexDate = null;
            IndexDocs indexDoc = new IndexDocs("PID:\"" + rpid + "\"");
            indexDoc.setFl("PID,modified_date");
            Iterator it = indexDoc.iterator();
            if (it.hasNext()) {
                //Only check if document is already indexed
                JSONObject doc = (JSONObject) it.next();
                try {
                    indexDate = solrDateFormat.parse(doc.getString("modified_date"));

                    expr = xpath.compile("apia:objectProfile/apia:objLastModDate");
                    String nodeValue = (String) expr.evaluate(profile, XPathConstants.STRING);
                    try {
                        Date fedoraDate = solrDateFormat.parse(nodeValue);
                        if (!fedoraDate.after(indexDate)) {
                            logger.log(Level.INFO, "Document {0} is up to date. Date in fedora: {1}, index date: {2} Skipping", 
                                    new Object[]{rpid, fedoraDate, indexDate});
                            Indexer.skipped++;
                            return;
                        }

                    } catch (ParseException e2) {
                        logger.log(Level.INFO, "Problem parsing modified_date in fedora, document {0} will be reindexed. ({1})", new Object[]{rpid, e2});
                    }
                } catch (ParseException e1) {
                    logger.log(Level.INFO, "Problem parsing modified_date in index, document {0} will be reindexed. ({1})", new Object[]{rpid, e1});
                }
            }
        }
        makeDoc(rpid, dc, rels, profile);
    }

    private void indexDown(String rpid, boolean onlyUpdate) throws Exception {
        if (Indexer.indexed_cache.contains(rpid)) {
            logger.log(Level.INFO, "Pid {0} already processed", rpid);
            
            return;
        }
        Indexer.indexed_cache.add(rpid);
        ds_cache = new HashMap<String, Document>();
        
        
        Document rels = fa.getRelsExt(rpid);
        ds_cache.put("RELS-EXT", rels);
        indexOne(rpid, onlyUpdate, rels);
        
        expr = xpath.compile("rdf:RDF/rdf:Description/node()");
        NodeList nodes = (NodeList) expr.evaluate(rels, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (predicates.contains(node.getLocalName())) {
                String pid = node.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1];
                try {
                    if (rpid.equals(pid)) {
                        if (config.getBoolean("k5indexer.continueOnError", false)) {
                            // continuing
                            logger.log(Level.WARNING, "Self reference on {0}", pid);
                            Indexer.warnings++;
                        } else {
                            logger.log(Level.SEVERE, "Self reference on {0}", pid);
                            Indexer.errors++;
                            throw new Exception("Self reference on " + pid);
                        }

                    } else {
                        indexDown(pid, onlyUpdate);
                    }
                } catch (Exception ex) {
                    if (config.getBoolean("k5indexer.continueOnError", false)) {
                        // continuing
                        logger.log(Level.WARNING, "Error indexing document " + pid + ". Continuing.", ex);
                        Indexer.errors++;
                    } else {
                        logger.log(Level.SEVERE, "Error indexing document " + pid, ex);
                        throw new Exception(ex);
                    }

                }
            }
        }
    }

    /* Index all fedora documents following kramerius logic structure.
     Finds in RELS-EXT the child dokument to index
     Propagates down some info, like parent title, dates, path
     */
    public void indexDown(String pid) throws Exception {
        indexDown(pid, false);
        commiter.commit();
    }

    /* Index all fedora documents following kramerius logic structure.
     Documents are indexed only if fedora modified date is newer than 
     index date. When document in index is up-to-date, recursion down ends
     */
    public void updateDown(String pid) throws Exception {
        indexDown(pid, true);
        commiter.commit();
    }

    /* Index fedora document.
     */
    public void indexOne(String pid) throws Exception {
        Document rels = fa.getRelsExt(pid);
        ds_cache = new HashMap<String, Document>();
        ds_cache.put("RELS-EXT", rels);
        indexOne(pid, false, rels);
        commiter.commit();
    }

    /* Index fedora document.
     Document is indexed only if fedora modified date is newer than 
     index date. When document in index is up-to-date, recursion down ends
     */
    public void updateOne(String pid) throws Exception {
        Document rels = fa.getRelsExt(pid);
        ds_cache = new HashMap<String, Document>();
        ds_cache.put("RELS-EXT", rels);
        indexOne(pid, true, rels);
        commiter.commit();
    }
}
