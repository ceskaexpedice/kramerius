package cz.incad.kramerius.statistics.formatters.report.nkp;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectModelsPath;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.statistics.accesslogs.dnnt.DNNTStatisticsAccessLogImpl;
import cz.incad.kramerius.statistics.accesslogs.utils.SElemUtils;
import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.NKPLogReport;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NKPXMLFormatter implements StatisticsReportFormatter {

    public static final Logger LOGGER = Logger.getLogger(NKPXMLFormatter.class.getName());

    private OutputStream os;

    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedoraAccess;

    @Inject
    SolrAccess solrAccess;

    @Override
    public String getMimeType() {
        return XML_MIME_TYPE;
    }

    @Override
    public String getFormat() {
        return XML_FORMAT;
    }



//    @Override
//    public void beforeProcess(HttpServletResponse response) throws IOException {
//        this.os.write("<records>\n".getBytes(DEFAULT_ENCODING));
//    }

//    @Override
//    public void afterProcess(HttpServletResponse response) throws IOException {
//        this.os.write("\n</records>".getBytes(DEFAULT_ENCODING));
//        this.os = null;
//    }

    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {

            String pid = (String) record.get("pid");

            Document solrDoc = this.solrAccess.getSolrDataDocument(pid);

            ObjectPidsPath[] paths = this.solrAccess.getPath(null, solrDoc);
            ObjectModelsPath[] mpaths = this.solrAccess.getPathOfModels(solrDoc);

            String rootTitle  = SElemUtils.selem("str", "root_title", solrDoc);
            String rootPid  = SElemUtils.selem("str", "root_pid", solrDoc);
            String dctitle = SElemUtils.selem("str", "dc.title", solrDoc);
            String solrDate = SElemUtils.selem("str", "datum_str", solrDoc);
            String dnnt = SElemUtils.selem("bool", "dnnt", solrDoc);
            String policy = SElemUtils.selem("str", "dostupnost", solrDoc);

            List<String> sAuthors = DNNTStatisticsAccessLogImpl.solrAuthors(rootPid, solrAccess);
            List<String> dcPublishers = DNNTStatisticsAccessLogImpl.dcPublishers(paths, fedoraAccess);

            StringBuilder builder = new StringBuilder("<record>\n");

            builder.append("\t<pid>").append(record.get("pid")).append("</pid>\n");
            builder.append("\t<date>").append(record.get("date")).append("</date>\n");
            builder.append("\t<username>").append(record.get("user")).append("</username>\n");

            builder.append("\t<dnnt>").append(record.get("dnnt")).append("</dnnt>\n");
            builder.append("\t<providedByDnnt>").append(record.get("providedByDnnt")).append("</providedByDnnt>\n");
            builder.append("\t<model>").append(StringUtils.nullify((String)record.get("model"))).append("</model>\n");
            builder.append("\t<rights>").append(StringUtils.nullify((String)record.get("rights"))).append("</rights>\n");


            builder.append("</record>\n");


            this.os.write(builder.toString().getBytes(DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public String getReportId() {
        return NKPLogReport.REPORT_ID;
    }

//    @Override
//    public void addInfo(HttpServletResponse response, String info) throws IOException {
//        this.os = response.getOutputStream();
//        String text = "Report pro nkp ";
//        String comment = "<!-- " + text + info + " -->";
//        this.os.write(comment.getBytes(DEFAULT_ENCODING));
//    }

    @Override
    public void beforeProcess(OutputStream os) throws IOException {
        this.os.write("<records>\n".getBytes(DEFAULT_ENCODING));
    }

    @Override
    public void afterProcess(OutputStream os) throws IOException {
        this.os.write("\n</records>".getBytes(DEFAULT_ENCODING));
        this.os = null;
    }

    @Override
    public void addInfo(OutputStream os, String info) throws IOException {
        this.os = os;
        String text = "Report pro nkp ";
        String comment = "<!-- " + text + info + " -->";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));
    }
}
