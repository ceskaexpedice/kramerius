package cz.incad.Kramerius.statistics.formatters.report.pids;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.Kramerius.statistics.formatters.report.model.ModelXMLFormatter;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.ModelStatisticReport;
import cz.incad.kramerius.statistics.impl.PidsReport;

public class PidsXMLFormatter implements StatisticsReportFormatter{


    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ModelXMLFormatter.class.getName());
    
    private OutputStream os;
    
    @Override
    public String getMimeType() {
        return XML_MIME_TYPE;
    }

    @Override
    public String getFormat() {
        return XML_FORMAT;
    }

    @Override
    public void beforeProcess(HttpServletResponse response) throws IOException {
        this.os = response.getOutputStream();
        this.os.write("<records>\n".getBytes("UTF-8"));
    }

    @Override
    public void afterProcess(HttpServletResponse response) throws IOException {
        this.os.write("\n</records>".getBytes("UTF-8"));
        this.os = null;
     }

    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {
            StringBuilder builder = new StringBuilder("<record>\n");

            builder.append("\t<count>").append(record.get("count")).append("</count>\n");
            builder.append("\t<pid>").append(StringUtils.nullify((String)record.get("pid"))).append("</pid>\n");
            builder.append("\t<model>").append(StringUtils.nullify((String)record.get("model"))).append("</model>\n");
            builder.append("\t<rights>").append(StringUtils.nullify((String)record.get("rights"))).append("</rights>\n");
            builder.append("</record>\n");
            
            this.os.write(builder.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public String getReportId() {
        return PidsReport.REPORT_ID;
    }

}
