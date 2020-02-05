/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package cz.incad.Kramerius.statistics.formatters.main.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.statistics.formatters.main.StatisticsExportMainLogFormatter;
import static cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter.DEFAULT_ENCODING;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;

/**
 * @author pavels
 *
 */
public class CSVFormatter implements StatisticsExportMainLogFormatter {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CSVFormatter.class.getName());
    
    protected OutputStream os;
    private boolean firstLine = true;
    
    public CSVFormatter() throws UnsupportedEncodingException, IOException {
        super();
    }

    
    
    @Override
    public void beforeProcess(HttpServletResponse response) throws IOException {
        this.firstLine = true;
        this.printHeader();
    }



    @Override
    public void afterProcess(HttpServletResponse response) throws IOException {
        this.os = null;
        this.firstLine = true;
    }


    public void printHeader() throws UnsupportedEncodingException, IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("pid").append(',');
        builder.append("date").append(',');
        builder.append("remote_ip_address").append(',');
        builder.append("user").append(",");

        builder.append("model").append(',');
        builder.append("detail pid").append(',');
        builder.append("issued_date").append(',');
        builder.append("rights").append(',');
        builder.append("lang").append(',');
        builder.append("title");
        builder.append("\n");
        
        this.os.write(builder.toString().getBytes("UTF-8"));
    }
    @Override
    public void processMainRecord(Map<String, Object> record) {
        try {
            if (!firstLine) os.write("\n".getBytes());
            StringBuilder builder = new StringBuilder();
            builder.append(StringUtils.nullify((String)record.get("pid"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("date"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("remote_ip_address"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("user")));
            builder.append(StringUtils.nullify((String)record.get("stat_action")));

            os.write(builder.toString().getBytes());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        firstLine = false;
    }


    public String escapeNewLine(String str) {
        StringBuilder builder = new StringBuilder();
        char[] chrs = str.toCharArray();
        for (char c : chrs) {
            if (c=='\n') builder.append(' ');
            else builder.append(c);
        }
        return builder.toString();
    }
    @Override
    public void processDetailRecord(Map<String, Object> detail) {
        try {
            StringBuilder builder = new StringBuilder(",");
            builder.append(StringUtils.nullify((String)detail.get("model"))).append(',');
            builder.append(StringUtils.nullify((String)detail.get("pid"))).append(',');
            builder.append(StringUtils.nullify((String)detail.get("issued_date"))).append(',');
            builder.append(StringUtils.nullify((String)detail.get("rights"))).append(',');
            builder.append(StringUtils.nullify((String)detail.get("lang"))).append(',');
            builder.append('"').append(escapeNewLine(StringUtils.nullify((String)detail.get("title")))).append('"');
            //builder.append('\n');
            
            os.write(builder.toString().getBytes());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }



    @Override
    public String getFormat() {
        return CSV_FORMAT;
    }



    @Override
    public String getMimeType() {
        return CSV_MIME_TYPE;
    }

    @Override
    public void addInfo(HttpServletResponse response, String info) throws IOException {
        this.os = response.getOutputStream();
        String comment = "# " + "CSV export"+ "\n";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));
    }
    
}
    
