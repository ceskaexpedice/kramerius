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

import java.io.OutputStream;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;

/**
 * Sluzba pro generovani prvni stranky 
 */
public interface FirstPagePDFService {

    /**
     * Typ prvni stranky
     */
    public enum FirstPageType {
        // Textova prvni stranka
        TEXT, 
        // Vyrendrovany obrazek
        IMAGE
    };

    /**
     * Generovani prvni stranky pro vyber
     */
    public void generateFirstPageForSelection(AbstractRenderedDocument rdoc, OutputStream os, String[] pids, String imgServlet, String i18nServlet, FontMap fontMap);
    
    /**
     * Generovani prvni stranky pro parent titul
     */
    public void generateFirstPageForParent(AbstractRenderedDocument rdoc, OutputStream os, ObjectPidsPath path,String imgServlet, String i18nServlet, FontMap fontMap);
    
}
