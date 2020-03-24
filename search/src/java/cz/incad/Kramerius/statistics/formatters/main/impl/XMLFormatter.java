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
public class XMLFormatter implements StatisticsExportMainLogFormatter {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(XMLFormatter.class.getName());
    
    protected OutputStream os;
    
    public XMLFormatter() {
        super();
    }

    
    boolean shouldRenderEndTag = false;
    
    @Override
    public void beforeProcess(HttpServletResponse response) throws IOException {
        this.shouldRenderEndTag = false;
        StringBuilder builder = new StringBuilder("<records>\n");
        this.os.write(builder.toString().getBytes("UTF-8"));
    }


    @Override
    public void afterProcess(HttpServletResponse response) throws IOException {
        if (this.shouldRenderEndTag) {
            StringBuilder builder = new StringBuilder("\t</details>\n</record>");
            this.os.write(builder.toString().getBytes("UTF-8"));
        }
        StringBuilder builder = new StringBuilder("\n</records>");

        this.os.write(builder.toString().getBytes("UTF-8"));
        this.os = null;
        this.shouldRenderEndTag = false;
    }




    @Override
    public void processMainRecord(Map<String, Object> record) {
        try {
            if (shouldRenderEndTag)  {
                StringBuilder builder = new StringBuilder("\t</details>\n</record>");
                this.os.write(builder.toString().getBytes("UTF-8"));
            }

            StringBuilder builder = new StringBuilder("<record>");
            builder.append("\t<pid>").append(StringUtils.nullify((String)record.get("pid"))).append("</pid>\n");
            builder.append("\t<date>").append(StringUtils.nullify(record.get("date").toString())).append("</date>\n");
            builder.append("\t<user>").append(StringUtils.nullify((String)record.get("user"))).append("</user>\n");
            builder.append("\t<remote_ip_address>").append(StringUtils.nullify((String)record.get("remote_ip_address"))).append("</remote_ip_address>\n");
            builder.append("\t<stat_action>").append(StringUtils.nullify((String)record.get("stat_action"))).append("</stat_action>\n");
            builder.append("\t<session_id>").append(StringUtils.nullify((String)record.get("session_id"))).append("</session_id>\n");
            builder.append("<details>");

            this.os.write(builder.toString().getBytes("UTF-8"));
            this.shouldRenderEndTag = true;
            
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    
    public boolean shouldRenderEndTag() {
        return shouldRenderEndTag;
    }




    @Override
    public void processDetailRecord(Map<String, Object> detail) {

        try {
            StringBuilder builder = new StringBuilder("<detail>");
            builder.append("\t<model>").append(StringUtils.nullify((String)detail.get("model"))).append("</model>\n");
            builder.append("\t<pid>").append(StringUtils.nullify((String)detail.get("pid"))).append("</pid>\n");
            builder.append("\t<issued_date>").append(StringUtils.nullify((String)detail.get("issued_date"))).append("</issued_date>\n");
            builder.append("\t<rights>").append(StringUtils.nullify((String)detail.get("rights"))).append("</rights>\n");
            builder.append("\t<lang>").append(StringUtils.nullify((String)detail.get("lang"))).append("</lang>\n");
            builder.append("\t<title>").append(StringUtils.nullify((String)detail.get("title"))).append("</title>\n");
            builder.append("</detail>");

            this.os.write(builder.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }


    @Override
    public String getFormat() {
        return XML_FORMAT;
    }


    @Override
    public String getMimeType() {
        return XML_MIME_TYPE;
    }

    @Override
    public void addInfo(HttpServletResponse response, String info) throws IOException {
        this.os = response.getOutputStream();
        String comment = "<!-- " + "XML export" + " -->";
        this.os.write(comment.getBytes(DEFAULT_ENCODING));
    }
}
