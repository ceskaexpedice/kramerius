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

    private File fontDirectory; 
    private Map<String, Font> map = new HashMap<String, Font>();

    
    public FontMap(File fontDirectory) throws DocumentException, IOException {
        super();
        this.fontDirectory = fontDirectory;
    
        Font logoFont = createFont(this.fontDirectory);
        logoFont.setSize(48f);

        Font normalFont = createFont(this.fontDirectory);
        normalFont.setSize(14f);

        Font strongFont = createFont(this.fontDirectory);
        strongFont.setSize(14f);
        strongFont.setStyle(Font.BOLD);

        Font header4Font = createFont(this.fontDirectory);
        header4Font.setSize(16f);
        header4Font.setStyle(Font.BOLD);

        Font smallerFont = createFont(this.fontDirectory);
        smallerFont.setSize(12f);

        Font smallFont = createFont(this.fontDirectory);
        smallFont.setSize(10f);
        
        this.registerFont(NORMAL_FONT, normalFont);
        this.registerFont(STRONG_FONT, strongFont);
        this.registerFont(LOGO_FONT, logoFont);
        this.registerFont(HEADER4_FONT, header4Font);
        this.registerFont(SMALLER_FONT, smallerFont);
        this.registerFont(SMALL_FONT, smallFont);
    }

    private void registerFont(String fid, Font font) {
        map.put(fid, font);
    }

    public Font getRegistredFont(String fid) {
        return map.get(fid);
    }

    
    

    private Font createFont(File fontDirectory) throws DocumentException, IOException {
//        String workingDir = Constants.WORKING_DIR;
//        File fontFile = new File(workingDir + File.separator + "fonts" + File.separator + "GentiumPlus-R.ttf");

        File fontFile = new File(fontDirectory.getAbsolutePath(), "GentiumPlus-R.ttf");
        BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250, true);
        return new Font(bf);
    }

}
