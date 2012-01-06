/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.pdf;

import java.util.HashMap;
import java.util.Map;


import cz.incad.kramerius.pdf.utils.pdf.FontMap;

/**
 * Represents configuration bean for fonts used in pdf generation process
 * @author pavels
 */
public class PDFFontConfigBean {

    public static final int STANDARD = 0x0;
    public static final int ITALIC = 0x1;
    public static final int BOLD = 0x2;
    public static final int BOLDITALIC = BOLD | ITALIC;
    
    private Map<String, FontAttrs> attributes = new HashMap<String, FontAttrs>();


    public PDFFontConfigBean() {
        super();
        attributes.put(FontMap.BIG_FONT, new FontAttrs(BOLD, 48));
        attributes.put(FontMap.NORMAL_FONT, new FontAttrs(STANDARD, 22));
    }


    public int getFontSize(String fname) {
        FontAttrs attrs = this.attributes.get(fname);
        if (attrs != null) {
            return attrs.getSize();
        } else return -1;
    }
    
    public synchronized void setFontSize(String fname, int fsize) {
        FontAttrs attrs = this.attributes.get(fname);
        if (attrs == null) {
            attrs = new FontAttrs(STANDARD, fsize);
        } else {
            attrs.setSize(fsize);
        }
    }
    
    public int getFontStyle(String fname) {
        FontAttrs attrs = this.attributes.get(fname);
        if (attrs != null) {
            return attrs.getFlag();
        } else return STANDARD;
    }
    
    public synchronized void setFontStyle(String fname, int style) {
        FontAttrs attrs = this.attributes.get(fname);
        if (attrs == null) {
            attrs = new FontAttrs(style, 22);
        } else {
            attrs.setFlag(style);
        }
    }
    
    
    private class FontAttrs {

        private int flag = STANDARD;
        private int fsize = 22;
        
        public FontAttrs(int flag, int fsize) {
            super();
            this.flag = flag;
            this.fsize = fsize;
        }
        
        public int getFlag() {
            return flag;
        }
        
        public int getSize() {
            return fsize;
        }
        public void setFlag(int flag) {
            this.flag = flag;
        }
        
        public void setSize(int fsize) {
            this.fsize = fsize;
        }
    }
}
