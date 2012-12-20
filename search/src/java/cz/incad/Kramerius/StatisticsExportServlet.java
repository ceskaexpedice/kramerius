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
package cz.incad.Kramerius;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.imaging.utils.FileNameUtils;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.statistics.StatisticsAccessLogSupport;

/**
 * @author pavels
 *
 */
public class StatisticsExportServlet extends GuiceServlet {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(StatisticsExportServlet.class.getName());
    
    @Inject
    StatisticsAccessLog statisticAccessLog;
    

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String format = req.getParameter("format");
        Format enumFormat = Format.valueOf(format);
        resp.setContentType(enumFormat.getMimeType());
        resp.setHeader("Content-disposition", "attachment; filename=export."+(format.toLowerCase()) );
        enumFormat.render(this.statisticAccessLog, resp.getOutputStream());
    }
    
    enum Format {
        XML{

            @Override
            public void render(StatisticsAccessLog statisticAccessLog,OutputStream os) {
                try {
                    os.write("<records>\n".getBytes("UTF-8"));
                    _XMLSupport xSup = new _XMLSupport(os);
                    statisticAccessLog.processAccessLog(xSup);
                    if (xSup.shouldRenderEndTag()) {
                        StringBuilder builder = new StringBuilder("\t</details>\n</record>");
                        os.write(builder.toString().getBytes("UTF-8"));
                    }
                    os.write("\n</records>".getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }

            @Override
            public String getMimeType() {
                return "application/xml";
            }
            
            
        }, 
        CSV {

            @Override
            public void render(StatisticsAccessLog statisticAccessLog, OutputStream os) {
                try {
                    statisticAccessLog.processAccessLog(new _CSVSupport(os));
                } catch (UnsupportedEncodingException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
            }

            @Override
            public String getMimeType() {
                return "text/csv";
            }
            
        };
        public abstract void render(StatisticsAccessLog statisticAccessLog,OutputStream os);
        public abstract String getMimeType();
    }
    
    static class _XMLSupport implements StatisticsAccessLogSupport {
        protected OutputStream os;

        
        
        public _XMLSupport(OutputStream os) {
            super();
            this.os = os;
        }

        boolean shouldRenderEndTag = false;
        @Override
        public void processMainRecord(Map<String, Object> record) {
            try {
                if (shouldRenderEndTag)  {
                    StringBuilder builder = new StringBuilder("\t</details>\n</record>");
                    this.os.write(builder.toString().getBytes("UTF-8"));
                }

                StringBuilder builder = new StringBuilder("<record>");
                builder.append("\t<pid>").append(nullify((String)record.get("pid"))).append("</pid>\n");
                builder.append("\t<date>").append(nullify(record.get("date").toString())).append("</date>\n");
                builder.append("\t<user>").append(nullify((String)record.get("user"))).append("</user>\n");
                builder.append("\t<remote_ip_address>").append(nullify((String)record.get("remote_ip_address"))).append("</remote_ip_address>\n");
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
                builder.append("\t<model>").append(nullify((String)detail.get("model"))).append("</model>\n");
                builder.append("\t<pid>").append(nullify((String)detail.get("pid"))).append("</pid>\n");
                builder.append("\t<issued_date>").append(nullify((String)detail.get("issued_date"))).append("</issued_date>\n");
                builder.append("\t<rights>").append(nullify((String)detail.get("rights"))).append("</rights>\n");
                builder.append("\t<lang>").append(nullify((String)detail.get("lang"))).append("</lang>\n");
                builder.append("\t<title>").append(nullify((String)detail.get("title"))).append("</title>\n");
                builder.append("</detail>");

                this.os.write(builder.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }
    
    static class _CSVSupport implements StatisticsAccessLogSupport {
        
        protected OutputStream os;
        private boolean firstLine = true;
        
        public _CSVSupport(OutputStream os) throws UnsupportedEncodingException, IOException {
            super();
            this.os = os;
            this.printHeader();
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

            this.os.write(builder.toString().getBytes("UTF-8"));
        }
        @Override
        public void processMainRecord(Map<String, Object> record) {
            try {
                if (!firstLine) os.write("\n".getBytes());
                StringBuilder builder = new StringBuilder();
                builder.append(nullify((String)record.get("pid"))).append(',');
                builder.append(nullify((String)record.get("date").toString())).append(',');
                builder.append(nullify((String)record.get("remote_ip_address"))).append(',');
                builder.append(nullify((String)record.get("user")));

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
                builder.append(nullify((String)detail.get("model"))).append(',');
                builder.append(nullify((String)detail.get("pid"))).append(',');
                builder.append(nullify((String)detail.get("issued_date"))).append(',');
                builder.append(nullify((String)detail.get("rights"))).append(',');
                builder.append(nullify((String)detail.get("lang"))).append(',');
                builder.append('"').append(escapeNewLine((String)detail.get("title"))).append('"');
                
                os.write(builder.toString().getBytes());
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
        }
    }
        

    public static String nullify(String str) {
        return str != null  ? str : "";
    }

}

