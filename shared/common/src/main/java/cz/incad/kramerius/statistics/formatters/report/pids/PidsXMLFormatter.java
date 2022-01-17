/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.statistics.formatters.report.pids;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.PidsReport;

/**
 *
 * @author Gabriela Melingerová
 */
public class PidsXMLFormatter implements StatisticsReportFormatter{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PidsXMLFormatter.class.getName());
    
    private OutputStream os;

    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {

            StringBuilder builder = new StringBuilder("<record>\n");
            builder.append("\t<count>").append(record.get("count")).append("</count>\n");
            builder.append("\t<pid>").append(StringUtils.nullify((String)record.get("pid"))).append("</pid>\n");
            builder.append("\t<title>").append(StringUtils.nullify((String)record.get("title"))).append("</title>\n");
            builder.append("</record>\n");

            this.os.write(builder.toString().getBytes(DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

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
//
//
//    @Override
//    public void afterProcess(HttpServletResponse response) throws IOException {
//        this.os.write("\n</records>".getBytes(DEFAULT_ENCODING));
//        this.os = null;
//    }

    @Override
    public String getReportId() {
        return PidsReport.REPORT_ID;
    }

//    @Override
//    public void addInfo(HttpServletResponse response, String info) throws IOException {
//        this.os = response.getOutputStream();
//        String text = "Report dle pidů, ";
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
        String text = "Report dle pidů, ";
        String comment = "<!-- " + text + info + " -->";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));

    }
}
