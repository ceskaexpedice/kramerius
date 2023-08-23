/*
 * Copyright (C) Jul 17, 2023 Pavel Stastny
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
package cz.incad.kramerius.rest.apiNew.client.v70.epub;

import java.util.Iterator;
import java.util.logging.Logger;

public enum EPubFileTypes {
    
    
    opf("application/oebps-package+xml"),
    ncx("application/x-dtbncx+xml"),
    htm("application/xhtml+xml"),
    html("application/xhtml+xml"),
    xhtml("application/xhtml+xml"),
    css("text/css"),
    jpg("image/jpeg"),
    jpeg("image/jpeg"),
    png("image/png"),
    gif("image/gif"),
    xml("application/xml"),
    ttf("application/x-font-ttf"),

    none("application/octet-stream");
    
    
    private EPubFileTypes(String mimeType) {
        this.mimeType = mimeType;
    }

    private String mimeType;
    
    public String getMimeType() {
        return mimeType;
    }
    
    public static String findMimetype(String name) {
        if (name.contains(".")) {
            String postfix = name.substring(name.lastIndexOf(".")+1);
            //System.out.println(postfix);
            EPubFileTypes[] values = EPubFileTypes.values(); // postfix.toLowerCase()
            for (int i = 0; i < values.length; i++) {
                EPubFileTypes ftype = values[i];
                if (ftype.name().equals(postfix.toLowerCase())) {
                    return ftype.getMimeType();
                }
            }
        }
        
        return EPubFileTypes.none.getMimeType();
    }
 
    public static final Logger LOGGER = Logger.getLogger(EPubFileTypes.class.getName());

    
}
