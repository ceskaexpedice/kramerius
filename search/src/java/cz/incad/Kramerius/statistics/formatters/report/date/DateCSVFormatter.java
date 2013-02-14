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
package cz.incad.Kramerius.statistics.formatters.report.date;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.DateDurationReport;

/**
 * @author pavels
 *
 */
public class DateCSVFormatter implements StatisticsReportFormatter{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(DateCSVFormatter.class.getName());
    
    private OutputStream os;
    private boolean firstLine = false;

//    builder.append(record.get("count")).append(',');
//    builder.append(StringUtils.nullify((String)record.get("pid"))).append(',');
//    builder.append(StringUtils.nullify((String)record.get("model"))).append(',');
//    builder.append(StringUtils.nullify((String)record.get("issued_date"))).append(',');
//    builder.append(StringUtils.nullify((String)record.get("rights"))).append(',');
//    builder.append(StringUtils.nullify((String)record.get("lang"))).append(',');
//    builder.append(StringUtils.nullify((String)record.get("title")));

    public void printHeader() throws UnsupportedEncodingException, IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("count").append(',');
        builder.append("pid").append(',');
        builder.append("model").append(',');
        builder.append("issued_date").append(',');
        builder.append("rights").append(',');
        builder.append("lang").append(',');
        builder.append("title");

        this.os.write(builder.toString().getBytes("UTF-8"));
    }

    
    @Override
    public String getMimeType() {
        return CSV_MIME_TYPE;
    }

    @Override
    public String getFormat() {
        return CSV_FORMAT;
    }

    @Override
    public void beforeProcess(HttpServletResponse response) throws IOException {
        this.os= response.getOutputStream();
        this.printHeader();
        
    }

    @Override
    public void afterProcess(HttpServletResponse response) throws IOException {
        this.os = null;
    }

    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {
            if (!firstLine) os.write("\n".getBytes());
            StringBuilder builder = new StringBuilder();

            builder.append(record.get("count")).append(',');
            builder.append(StringUtils.nullify((String)record.get("pid"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("model"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("issued_date"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("rights"))).append(',');
            builder.append(StringUtils.nullify((String)record.get("lang"))).append(',');
            builder.append(StringUtils.nullify(StringUtils.escapeChars((String)record.get("title"), new char[] {','})));

            os.write(builder.toString().getBytes());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    @Override
    public String getReportId() {
        return DateDurationReport.REPORT_ID;
    }
}
