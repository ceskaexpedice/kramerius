package cz.incad.kramerius.indexer;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.indexer.coordinates.ParsingCoordinates;
import cz.incad.kramerius.security.impl.criteria.mw.DateLexer;
import cz.incad.kramerius.security.impl.criteria.mw.DatesParser;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


//----------------------------------------
import org.apache.commons.io.IOUtils;
import org.w3c.dom.NodeList;

import java.io.File;

/**
 *
 * @author Alberto
 * Handles and manages the fields not directly present in doc FOXML
 *
 */
public class ExtendedFields {

    private static final Logger logger = Logger.getLogger(ExtendedFields.class.getName());
    private String root_title;
    private ArrayList<Integer> rels_ext_indexes;
    private ArrayList<String> pid_paths;
    private ArrayList<String> model_paths;
    private FedoraOperations fo;
    FedoraAccess fa;
    HashMap<String, String> models_cache;
    HashMap<String, String> dateStrCache;
    HashMap<String, String> dateBeginCache;
    HashMap<String, String> dateEndCache;
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
    private List<String> dateXPaths;
    DateFormat df;
    DateFormat solrDateFormat;

    // geo coordinates range
    private List<String> coordinates;


    public ExtendedFields(FedoraOperations fo) throws IOException {
        this.fo = fo;
        KConfiguration config = KConfiguration.getInstance();
        this.fa = new FedoraAccessImpl(config,null);
        models_cache = new HashMap<String, String>();
        dateStrCache = new HashMap<String, String>();
        dateBeginCache = new HashMap<String, String>();
        dateEndCache = new HashMap<String, String>();
        root_title_cache = new HashMap<String, String>();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        df = new SimpleDateFormat(config.getProperty("mods.date.format", "dd.MM.yyyy"));
        df.setLenient(false);
        solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");

        dateXPaths = Arrays.asList("mods:part/mods:date",
                "mods:originInfo[@transliteration='publisher']/mods:dateIssued",
                "mods:originInfo/mods:dateIssued");
    }

    public void clearCache() {
        models_cache.clear();
        dateStrCache.clear();
        dateBeginCache.clear();
        dateEndCache.clear();
        root_title_cache.clear();

    }

    public void setFields(String pid) throws Exception {
        pid_paths = new ArrayList<String>();
        pid_paths = fo.getPidPaths(pid);
        rels_ext_indexes = fo.getRelsIndexByPath(pid_paths);
        model_paths = new ArrayList<String>();
        for (String s : pid_paths) {
            model_paths.add(getModelPath(s));
        }
        Document biblioMods = fa.getBiblioMods(pid);
        setRootTitle();
        setDate(biblioMods);
        // coordinates
        this.coordinates = ParsingCoordinates.processBibloModsCoordinates(biblioMods, this.factory);
    }
    PDDocument pdDoc = null;
    String pdfPid = "";

    public void setPDFDocument(String pid) throws Exception {
        if (!pdfPid.equals(pid)) {
            InputStream is = null;
            try {
                pdfPid = "";
                closePDFDocument();
                is = fa.getDataStream(pid, "IMG_FULL");

                //File pdfImg = new File("/usr/local/tomcat/temp/"+pid+".tmp");

                File pdfImg = File.createTempFile(pid,null);
                pdfImg.deleteOnExit();
                FileUtils.copyInputStreamToFile(is, pdfImg);


                if (KConfiguration.getInstance().getConfiguration().getBoolean("convert.pdf.loadNonSeq", false)){
                    pdDoc = PDDocument.load(pdfImg, KConfiguration.getInstance().getConfiguration().getString("convert.pdfPassword"));
                }else{
                    pdDoc = PDDocument.load(pdfImg, KConfiguration.getInstance().getConfiguration().getString("convert.pdfPassword"));
                }
                pdfPid = pid;


            } catch (Exception ex) {
                closePDFDocument();
                IOUtils.closeQuietly(is);
                logger.log(Level.WARNING, "Cannot parse PDF document", ex);
            }

        }

    }

    public void closePDFDocument() throws IOException {
        pdfPid = "";
        if (pdDoc != null) {
            pdDoc.close();
        }
    }

    public int getPDFPagesCount() {
        if (pdDoc != null) {
            return pdDoc.getNumberOfPages();
        } else {
            return 0;
        }
    }

    private String getPDFPage(int page) throws Exception {
        try {
            PDFTextStripper stripper = new PDFTextStripper(/*"UTF-8"*/);
            if (page != -1) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
            }

            return StringEscapeUtils.escapeXml10(stripper.getText(pdDoc));
        } catch (Exception ex) {
            return "";
        }
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
        return model_path.deleteCharAt(model_path.length() - 1).toString();
    }

    public HashMap<String, String> toArray() {
        HashMap<String, String> paramsMap = new HashMap<String, String>();
        return paramsMap;
    }

    public String toXmlString(int pageNum) throws Exception {
        Set<String> texts = new HashSet<>();

        StringBuilder sb = new StringBuilder();
        for (String s : pid_paths) {
            sb.append("<field name=\"pid_path\">").append(s).append(pageNum == 0 ? "" : "/@" + pageNum).append("</field>");
            String[] pids = s.split("/");
            if (pageNum != 0) {
                sb.append("<field name=\"parent_pid\">").append(pids[pids.length - 1]).append("</field>");
                // TODO: Do it better. For now, i have to use any field which is aldrady defined
                // in the future consider of interoducing field text_pdf
                String pdfText = getPDFPage(pageNum);
                texts.add(pdfText);

            } else {
                if (pids.length == 1) {
                    sb.append("<field name=\"parent_pid\">").append(pids[0]).append("</field>");
                } else {
                    sb.append("<field name=\"parent_pid\">").append(pids[pids.length - 2]).append("</field>");
                }
            }
        }

        if (!texts.isEmpty()) {
            StringBuilder textsBuilder = new StringBuilder();
            texts.stream().forEach(item -> textsBuilder.append(item));
            sb.append("<field name=\"text_ocr\">").append(textsBuilder).append("</field>");
        }


        for (Integer i : rels_ext_indexes) {
            sb.append("<field name=\"rels_ext_index\">").append(i).append("</field>");
        }
        int level = pid_paths.get(0).split("/").length - 1;
        if (pageNum != 0) {
            level++;

        }
        for (String s : model_paths) {
            if (pageNum != 0) {
                sb.append("<field name=\"model_path\">").append(s).append("/page").append("</field>");
            } else {
                sb.append("<field name=\"model_path\">").append(s).append("</field>");
            }
        }
        sb.append("<field name=\"root_title\">").append(root_title).append("</field>");
        sb.append("<field name=\"root_pid\">").append(pid_paths.get(0).split("/")[0]).append("</field>");
        sb.append("<field name=\"level\">").append(level).append("</field>");
        sb.append("<field name=\"datum_str\">").append(datum_str).append("</field>");
        if (datum != null) {
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


        if (this.coordinates != null) {
            coordinates.stream().forEach((loc)->{
                sb.append(loc);
            });
        }

        return sb.toString();
    }

    private void setRootTitle() throws Exception {
        String root_pid = pid_paths.get(0).split("/")[0];
        if (root_title_cache.containsKey(root_pid)) {
            root_title = root_title_cache.get(root_pid);
        } else {
            Document doc = fa.getDC(root_pid);
            xPathStr = "//dc:title/text()";
            expr = xpath.compile(xPathStr);
            Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
            if (node != null) {
                root_title = StringEscapeUtils.escapeXml(node.getNodeValue());
                root_title_cache.put(root_pid, root_title);
            }
        }
    }

    private void setDate(Document biblioMods) throws XPathExpressionException {
        datum_str = "";
        rok = "";
        datum_begin = "";
        datum_end = "";
        datum = null;

        for (int j = 0; j < pid_paths.size(); j++) {
            String[] pid_path = pid_paths.get(j).split("/");
            for (int i = pid_path.length - 1; i > -1; i--) {
                String pid = pid_path[i];

                if (dateStrCache.containsKey(pid)) {
                    datum_str = dateStrCache.get(pid);
                    datum_begin = dateBeginCache.get(pid);
                    datum_end = dateEndCache.get(pid);
                    setRokAndDatum(datum_str);
                    return;
                }

                boolean dateFound = extractDates(biblioMods);

                if (dateFound) {
                    dateStrCache.put(pid, datum_str);
                    dateBeginCache.put(pid, datum_begin);
                    dateEndCache.put(pid, datum_end);
                    return;
                }
            }
        }
    }

    private boolean extractDates(Document biblioMods) throws XPathExpressionException {
        boolean dateFound = false;
        for (String dateXPath : dateXPaths) {
            xPathStr = prefix + dateXPath;
            expr = xpath.compile(xPathStr);
            /* check all dataIssued nodes */
            NodeList nodes = (NodeList) expr.evaluate(biblioMods, XPathConstants.NODESET);
            if (nodes.getLength() > 0) {
                dateFound = true;
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    parseDates(node);
                }
                break;
            }
        }

        if (datum_begin.equals("") || datum_end.equals("")) {
            setBeginAndEnd(datum_str);
        }

        setRokAndDatum(datum_str);

        return dateFound;
    }

    private void parseDates(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        String nodeTextContent = node.getTextContent();

        if (attributes == null || attributes.getLength() == 0 ||
                attributes.getNamedItem("point") == null) {
            datum_str = nodeTextContent;
        } else {
            Node point = attributes.getNamedItem("point");
            if ("start".equals(point.getNodeValue())) {
                datum_begin = replaceNonDigit(nodeTextContent);
            } else {
                datum_end = replaceNonDigit(nodeTextContent);
            }
        }
    }

    private void setBeginAndEnd(String datumStr) {
        if (datumStr.matches("[0-9]{4}")) {  // 1946
            datum_begin = datumStr;
            datum_end = datum_begin;
        } else if (datumStr.matches("[0-9]{4}[\\s]*-[\\s]*[0-9]{4}")) { // 1999-2000
            String[] dateParts = datumStr.split("-");
            datum_begin = dateParts[0];
            datum_end = dateParts[1];
        } else if(datumStr.matches("mezi [0-9]{4} a [0-9]{4}")) { // mezi 1898 a 1900
            String[] dateParts = datumStr.split("a");
            datum_begin = dateParts[0];
            datum_end = dateParts[1];
        } else if (datumStr.matches("[0-9]{2}..")) { // 18--
            datum_begin = datumStr.substring(0, 2) + "00";
            datum_end = datumStr.substring(0, 2) + "99";
        } else if (datumStr.matches("[0-9]{3}.")) { // 187-
            datum_begin = datumStr.substring(0, 3) + "0";
            datum_end = datumStr.substring(0, 3) + "9";
        } else if (datumStr.matches("[0-9]{2}-[0-9]{2}\\.[0-9]{4}")) {   // 11-12.1946
            datum_begin = datumStr.split("\\.")[1].trim();
            datum_end = datum_begin;
        } else if (datumStr.matches("[0-9]{2}\\.-[0-9]{2}\\.[0-9]{2}\\.[0-9]{4}")) {  // 19.-20.03.1890
            String[] dateParts = datumStr.split(".");
            datum_begin = dateParts[dateParts.length-1];
            datum_end = datum_begin;
        } else {
            datum_begin = replaceNonDigit(datumStr);
            datum_end = datum_begin;
        }
    }

    private void setRokAndDatum(String datumStr) {
        rok = datum_end;
        try {
            datum = df.parse(datumStr);
        } catch (Exception e) {
            if (datumStr.matches("\\d\\d\\.-\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d")) {  // 19.-20.03.1890
                String end = datumStr.split("-")[1].trim();
                try{
                    datum = df.parse(end);
                }catch (Exception ex) {
                    logger.log(Level.FINE, "Cant parse date "+datumStr);
                }
            } else {
                try {
                    DatesParser p = new DatesParser(new DateLexer(new StringReader(datumStr)));
                    datum = p.dates();
                } catch (RecognitionException ex) {
                    logger.log(Level.FINE, "Cant parse date "+datumStr);
                } catch (TokenStreamException ex) {
                    logger.log(Level.FINE, "Cant parse date "+datumStr);
                }
            }
        }
    }

    private String replaceNonDigit(String dateStr) {
        String result;
        result = dateStr.replaceAll("/\\^/{4}", "9"); // ^^^^ -> 9999
        result = result.replaceAll("- | /\\^/", "0");
        result = result.replaceAll("[^\\d.]", "");
        return result;
    }
}
