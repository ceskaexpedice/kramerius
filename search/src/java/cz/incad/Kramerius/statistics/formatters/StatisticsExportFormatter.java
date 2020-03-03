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
package cz.incad.Kramerius.statistics.formatters;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;


/**
 * @author pavels
 *
 */
public interface StatisticsExportFormatter {

    public static String XML_FORMAT="XML";
    public static String CSV_FORMAT="CSV";

    public static String XML_MIME_TYPE="application/xml";
    public static String CSV_MIME_TYPE="text/csv; charset=UTF-8";
    
    
    public static String[] SUPPORTED_FORMATS = {XML_FORMAT,CSV_FORMAT};
    
    
    public String getMimeType();
    
    public String getFormat();
    
    public void beforeProcess(HttpServletResponse response) throws IOException;
    
    public void afterProcess(HttpServletResponse response) throws IOException;
    
    public void addInfo(HttpServletResponse response, String info) throws IOException;
}
