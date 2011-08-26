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
package cz.incad.kramerius.pdf.impl;

import java.util.ResourceBundle;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.draw.LineSeparator;

import cz.incad.kramerius.pdf.FirstPageRenderer;

public abstract class AbstractFirstPageRenderer implements FirstPageRenderer {

    public void logo(Document pdfDoc, ResourceBundle resBundle, Font bigFont) throws DocumentException {
        bigFont.setSize(48f);
        // TODO: Change in text
        pdfDoc.add(new Paragraph(resBundle.getString("pdf.firstpage.title"), bigFont));
    }

    public void digitalLibrary(Document pdfDoc, ResourceBundle resBundle, Font smallerFont) throws DocumentException {
        // TODO: Change in text
        pdfDoc.add(new Paragraph(resBundle.getString("pdf.firstpage.library"), smallerFont));
        pdfDoc.add(new Paragraph(" \n"));
        pdfDoc.add(new LineSeparator());
        pdfDoc.add(new Paragraph(" \n"));
    }

}
