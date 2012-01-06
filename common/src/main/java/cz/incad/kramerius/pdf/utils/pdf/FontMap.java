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
import cz.incad.kramerius.pdf.PDFFontConfigBean;

public class FontMap {

    public static final String BIG_FONT = "big";
    public static final String NORMAL_FONT = "normal";


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

    public static FontMap createFontMap(PDFFontConfigBean conf) throws DocumentException, IOException {
        
        Font bigFont = createFont();
        if (conf != null) {
            setFontAttrs(conf, FontMap.BIG_FONT, bigFont);
        } else {
            bigFont.setSize(48f);
        }

        Font normalFont = createFont();
        if (conf != null) {
            setFontAttrs(conf, FontMap.NORMAL_FONT, normalFont);
        } else {
            normalFont.setSize(14f);
        }

        FontMap fmap = new FontMap();
        fmap.registerFont(NORMAL_FONT, normalFont);
        fmap.registerFont(BIG_FONT, bigFont);
        return fmap;
    }

    public static void setFontAttrs(PDFFontConfigBean conf, String mapName, Font font) {
        int fontFlag = conf.getFontStyle(mapName);
        switch(fontFlag) {
            case PDFFontConfigBean.BOLD: {
                font.setStyle(Font.BOLD);
            }
            break;
            case PDFFontConfigBean.ITALIC: {
                font.setStyle(Font.ITALIC);
            }
            break;
            case PDFFontConfigBean.BOLDITALIC: {
                font.setStyle(Font.BOLDITALIC);
            }
            break;
        }
        font.setSize(conf.getFontSize(mapName));
    }
    
    

    public static Font createFont() throws DocumentException, IOException {
        String workingDir = Constants.WORKING_DIR;
        File fontFile = new File(workingDir + File.separator + "fonts" + File.separator + "GentiumPlus-R.ttf");
        BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250, true);
        return new Font(bf);
    }

}
