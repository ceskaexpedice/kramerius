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

public class FontMap {
    
    // different headers' fonts
    public static final String LOGO_FONT = "logo";
    public static final String HEADER4_FONT = "header4";

    // normal texts' fonts
    public static final String STRONG_FONT = "strong";
    public static final String NORMAL_FONT = "normal";
    public static final String SMALLER_FONT = "smaller";
    public static final String SMALL_FONT = "small";
    

    public enum TYPE {
        EMBEDED_TTF,  NOT_EMBEDED;
    }

    private Map<String, Font> map = new HashMap<String, Font>();

    public void registerFont(String fid, Font font) {
        map.put(fid, font);
    }

    public Font getRegistredFont(String fid) {
        return map.get(fid);
    }

    public static FontMap createFontMap() throws DocumentException, IOException {
        
        Font logoFont = createFont();
        logoFont.setSize(48f);

        Font normalFont = createFont();
        normalFont.setSize(14f);

        Font strongFont = createFont();
        strongFont.setSize(14f);
        strongFont.setStyle(Font.BOLD);

        Font header4Font = createFont();
        header4Font.setSize(16f);
        header4Font.setStyle(Font.BOLD);

        Font smallerFont = createFont();
        smallerFont.setSize(12f);

        Font smallFont = createFont();
        smallFont.setSize(10f);
        
        FontMap fmap = new FontMap();
        fmap.registerFont(NORMAL_FONT, normalFont);
        fmap.registerFont(STRONG_FONT, strongFont);
        fmap.registerFont(LOGO_FONT, logoFont);
        fmap.registerFont(HEADER4_FONT, header4Font);
        fmap.registerFont(SMALLER_FONT, smallerFont);
        fmap.registerFont(SMALL_FONT, smallFont);
        return fmap;
    }

    
    

    public static Font createFont() throws DocumentException, IOException {
        String workingDir = Constants.WORKING_DIR;
        File fontFile = new File(workingDir + File.separator + "fonts" + File.separator + "GentiumPlus-R.ttf");
        BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250, true);
        return new Font(bf);
    }

}
