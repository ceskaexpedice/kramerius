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
public class PidsCSVFormatter implements StatisticsReportFormatter {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PidsCSVFormatter.class.getName());
    
    private OutputStream os;
    private boolean firstLine = false;
    
    public void printHeader() throws UnsupportedEncodingException, IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("count").append(',');
        builder.append("pid").append(',');
        builder.append("title");
        builder.append("\n");

        this.os.write(builder.toString().getBytes(DEFAULT_ENCODING));
    }

    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {
            if (!firstLine) os.write("\n".getBytes());
            StringBuilder builder = new StringBuilder();
            builder.append(record.get("count")).append(',');
            builder.append(StringUtils.nullify((String)record.get("pid"))).append(',');
            builder.append('"').append(StringUtils.escapeNewLine(StringUtils.nullify((String)record.get("title")))).append('"');

            os.write(builder.toString().getBytes(DEFAULT_ENCODING));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        
    }

    @Override
    public String getMimeType() {
        return CSV_MIME_TYPE;
    }

    @Override
    public String getFormat() {
        return CSV_FORMAT;
    }

//    @Override
//    public void beforeProcess(HttpServletResponse response) throws IOException {
//        this.printHeader();
//    }
//
//    @Override
//    public void afterProcess(HttpServletResponse response) throws IOException {
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
//        String comment = "# " + text + info + "\n";
//        this.os.write(comment.getBytes(DEFAULT_ENCODING));
//    }

    @Override
    public void beforeProcess(OutputStream os) throws IOException {
        this.printHeader();
    }

    @Override
    public void afterProcess(OutputStream os) throws IOException {
        this.os = null;

    }

    @Override
    public void addInfo(OutputStream os, String info) throws IOException {
        this.os = os;
        String text = "Report dle pidů, ";
        String comment = "# " + text + info + "\n";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));
    }
}