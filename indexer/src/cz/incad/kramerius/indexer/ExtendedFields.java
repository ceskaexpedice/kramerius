package cz.incad.kramerius.indexer;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.indexer.coordinates.ParsingCoordinates;
import cz.incad.kramerius.indexer.dates.BiblioModsDateParser;
import cz.incad.kramerius.indexer.dates.DateQuintet;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


//----------------------------------------
import org.apache.commons.io.IOUtils;
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
    DateFormat df;
    DateFormat solrDateFormat;
    PDDocument pdDoc = null;
    String pdfPid = "";

    // geo coordinates range
    private List<String> coordinates;

    private BiblioModsDateParser dateParser;

    public ExtendedFields(FedoraOperations fo) throws IOException {
        this.fo = fo;
        KConfiguration config = KConfiguration.getInstance();
        this.fa = new FedoraAccessImpl(config,null);
        models_cache = new HashMap<String, String>();
        dates_cache = new HashMap<String, String>();
        root_title_cache = new HashMap<String, String>();
        xpath.setNamespaceContext(new FedoraNamespaceContext());
        df = new SimpleDateFormat(config.getProperty("mods.date.format", "dd.MM.yyyy"));
        df.setLenient(false);
        solrDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        dateParser = new BiblioModsDateParser();
    }

    public void clearCache() {
        models_cache.clear();
        dates_cache.clear();
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

    private void setDate(Document biblioMods) throws Exception {
        datum_str = "";
        rok = "";
        datum_begin = "";
        datum_end = "";
        datum = null;
        for (String pidPath : pid_paths) {
            String[] pid_path = pidPath.split("/");
            for (int i = pid_path.length - 1; i > -1; i--) {
                String pid = pid_path[i];

                DateQuintet dateQuintet = dateParser.checkInCache(pid);
                if (dateQuintet == null) {
                    dateQuintet = dateParser.extractYearsFromBiblioMods(biblioMods, pid);
                }

                if (dateQuintet != null) {
                    datum = dateQuintet.getDate();
                    rok = dateQuintet.getYear();
                    datum_str = dateQuintet.getDateStr();
                    datum_begin = dateQuintet.getYearBegin();
                    datum_end = dateQuintet.getYearEnd();
                    return;
                }
            }
        }
    }
}
