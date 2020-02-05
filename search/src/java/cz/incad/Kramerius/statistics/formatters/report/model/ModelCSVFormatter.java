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
package cz.incad.Kramerius.statistics.formatters.report.model;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.Kramerius.statistics.formatters.report.lang.LangCSVFormatter;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.LangReport;
import cz.incad.kramerius.statistics.impl.ModelStatisticReport;

/**
 * @author pavels
 *
 */
public class ModelCSVFormatter implements StatisticsReportFormatter{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LangCSVFormatter.class.getName());
    
    private OutputStream os;
    private boolean firstLine;

    public void printHeader() throws UnsupportedEncodingException, IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("count").append(',');
        builder.append("pid").append(',');
        builder.append("model").append(',');
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
            builder.append(StringUtils.nullify((String)record.get("model"))).append(',');
            builder.append('"').append(StringUtils.escapeNewLine(StringUtils.nullify((String)record.get("title")))).append('"');

            os.write(builder.toString().getBytes(DEFAULT_ENCODING));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
        firstLine = false;
    }

    @Override
    public String getFormat() {
        return CSV_FORMAT;
    }

    @Override
    public void beforeProcess(HttpServletResponse response) throws IOException {
        this.firstLine = true;
        this.printHeader();
    }

    @Override
    public void afterProcess(HttpServletResponse response) throws IOException {
        this.os = null;
        this.firstLine = false;
    }

    @Override
    public String getReportId() {
        return ModelStatisticReport.REPORT_ID;
    }

    @Override
    public String getMimeType() {
        return CSV_MIME_TYPE;
    }

    @Override
    public void addInfo(HttpServletResponse response, String info) throws IOException {
        this.os = response.getOutputStream();
        String text = "Report dle modelu: ";
        String comment = "# " + text + info + "\n";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));
    }

}
