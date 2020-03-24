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
package cz.incad.Kramerius.statistics.formatters.report.lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.statistics.formatters.report.StatisticsReportFormatter;
import cz.incad.Kramerius.statistics.formatters.utils.StringUtils;
import cz.incad.kramerius.statistics.impl.LangReport;

/**
 * @author pavels
 *
 */
public class LangXMLFormatter implements StatisticsReportFormatter {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LangXMLFormatter.class.getName());
    
    private OutputStream os;
    
    @Override
    public String getFormat() {
        return XML_FORMAT;
    }

    
    
    @Override
    public void processReportRecord(Map<String, Object> record) {
        try {
            StringBuilder builder = new StringBuilder("<record>\n");
            builder.append("\t<count>").append(record.get("count")).append("</count>\n");
            builder.append("\t<lang>").append(StringUtils.nullify((String)record.get("lang"))).append("</lang>\n");
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
        return LangReport.REPORT_ID;
    }


    @Override
    public String getMimeType() {
        return XML_MIME_TYPE;
    }


    @Override
    public void beforeProcess(HttpServletResponse response) throws IOException {
        this.os.write("<records>\n".getBytes(DEFAULT_ENCODING));
    }


    @Override
    public void afterProcess(HttpServletResponse response) throws IOException {
        this.os.write("\n</records>".getBytes(DEFAULT_ENCODING));
        this.os = null;
    }

    @Override
    public void addInfo(HttpServletResponse response, String info) throws IOException {
        this.os = response.getOutputStream();
        String text = "Report dle jazyků, ";
        String comment = "<!-- " + text + info + " -->";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));
    }
}
