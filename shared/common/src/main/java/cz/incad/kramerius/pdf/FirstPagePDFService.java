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
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;

/**
 * Service for generating first page
 */
public interface FirstPagePDFService {

    /**
     * Type of page
     */
    public enum FirstPageType {
        // Textova prvni stranka
        TEXT, 
        // Vyrendrovany obrazek
        IMAGE
    };

    /**
     * Generate first pdf page for selection
     * @param rdoc Generating document model
     * @param os Outputstream
     * @param pids PIDs selection
     * @param imgServlet IMG servlet 
     * @param i18nServlet I18N servlet 
     * @param fontMap Prepared FontMap object
     */
    public void selection(PreparedDocument rdoc, OutputStream os, String[] pids,  FontMap fontMap);
    
    /**
     * Generate first pdf page for title 
     * @param rdoc Generating document model
     * @param os Outputstream
     * @param path Path for generting object
     * @param imgServlet IMG servlet 
     * @param i18nServlet I18N servlet 
     * @param fontMap Prepared FontMap object
     */
    public void parent(PreparedDocument rdoc, OutputStream os, ObjectPidsPath path, FontMap fontMap);
    
}
