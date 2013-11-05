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
package cz.incad.kramerius.pdf.utils.pdf;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;

import cz.incad.kramerius.Constants;

/**
 * Mapping between logical name and real fonts
 * @author pavels
 */
public class FontMap {
    
    // different headers' fonts
    /** Logo font */
    public static final String LOGO_FONT = "logo";
    /** Header 4 font */
    public static final String HEADER4_FONT = "header4";

    // normal texts' fonts
    /** Strong font */
    public static final String STRONG_FONT = "strong";
    /** Plain text font */
    public static final String NORMAL_FONT = "normal";
    /** Smaller font */
    public static final String SMALLER_FONT = "smaller";
    /** Small font */
    public static final String SMALL_FONT = "small";

    private File fontDirectory; 
    private Map<String, Font> map = new HashMap<String, Font>();

    
    public FontMap(File fontDirectory) throws DocumentException, IOException {
        super();
        this.fontDirectory = fontDirectory;
    
        Font logoFont = createGentiumFont(this.fontDirectory);
        logoFont.setSize(48f);

        Font normalFont = createGentiumFont(this.fontDirectory);
        normalFont.setSize(14f);

        Font strongFont = createGentiumFont(this.fontDirectory);
        strongFont.setSize(14f);
        strongFont.setStyle(Font.BOLD);

        Font header4Font = createGentiumFont(this.fontDirectory);
        header4Font.setSize(16f);
        header4Font.setStyle(Font.BOLD);

        Font smallerFont = createGentiumFont(this.fontDirectory);
        smallerFont.setSize(12f);

        Font smallFont = createGentiumFont(this.fontDirectory);
        smallFont.setSize(10f);
        
        this.registerFont(NORMAL_FONT, normalFont);
        this.registerFont(STRONG_FONT, strongFont);
        this.registerFont(LOGO_FONT, logoFont);
        this.registerFont(HEADER4_FONT, header4Font);
        this.registerFont(SMALLER_FONT, smallerFont);
        this.registerFont(SMALL_FONT, smallFont);

    }

    /**
     * Register new font 
     * @param fid Font identificator
     * @param font Font object
     */
    private void registerFont(String fid, Font font) {
        map.put(fid, font);
    }

    /**
     * Returns font associated with given ident
     * @param fid Font identificator
     * @return Font object
     */
    public Font getRegistredFont(String fid) {
        return map.get(fid);
    }

    
    

    private Font createArialFont(File fontDirectory) throws DocumentException, IOException {
        File fontFile = new File(fontDirectory.getAbsolutePath(), "ext_ontheflypdf_ArialCE.ttf");
        BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250, true);
        return new Font(bf);
    }
    
    private Font createGentiumFont(File fontDirectory) throws DocumentException, IOException {
        File fontFile = new File(fontDirectory.getAbsolutePath(), "GentiumPlus-R.ttf");
        BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250, true);
        return new Font(bf);
    }

}
