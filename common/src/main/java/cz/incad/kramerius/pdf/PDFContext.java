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

import cz.incad.kramerius.pdf.utils.pdf.FontMap;

/**
 * PDF rendering runtime context
 * @author pavels
 */
public class PDFContext {
    
    private cz.incad.kramerius.pdf.utils.pdf.FontMap fontMap;
    private String djvuUrl;
    private String i18nUrl;
    
    public PDFContext(FontMap fontMap , String djvuUrl, String i18nUrl) {
        super();
        this.fontMap = fontMap;
        this.djvuUrl = djvuUrl;
        this.i18nUrl = i18nUrl;
    }
    
    /**
     * Returns fonts used in pdf generation
     * @return fontmap 
     * @see FontMap
     */
    public FontMap getFontMap() {
        return fontMap;
    }
    
    public String getDjvuUrl() {
        return djvuUrl;
    }
    
    public String getI18nUrl() {
        return i18nUrl;
    }

}
