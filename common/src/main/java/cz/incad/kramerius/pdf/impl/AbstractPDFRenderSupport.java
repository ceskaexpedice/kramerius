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

import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.readImage;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;

import cz.incad.kramerius.utils.imgs.ImageMimeType;

public class AbstractPDFRenderSupport {

    public void insertImageFromURL(Document document, float percentage, String imgUrl, ImageMimeType mimetype) throws IOException, MalformedURLException, BadElementException, DocumentException {
        BufferedImage javaImg = readImage(new URL(imgUrl), mimetype, 0);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeImageToStream(javaImg, "jpeg", bos);
    
        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bos.toByteArray());
    
        Float wratio = document.getPageSize().getWidth() / javaImg.getWidth(null);
        Float hratio = document.getPageSize().getHeight() / javaImg.getHeight(null);
        Float ratio = Math.min(wratio, hratio);
        if (percentage != 1.0) {
            ratio = ratio * percentage;
        }
    
        int fitToPageWidth = (int) (javaImg.getWidth(null) * ratio);
        int fitToPageHeight = (int) (javaImg.getHeight(null) * ratio);
    
        int offsetX = ((int) document.getPageSize().getWidth() - fitToPageWidth) / 2;
        int offsetY = ((int) document.getPageSize().getHeight() - fitToPageHeight) / 2;
    
        img.scaleAbsoluteHeight(ratio * img.getHeight());
    
        img.scaleAbsoluteWidth(ratio * img.getWidth());
        img.setAbsolutePosition((offsetX), document.getPageSize().getHeight() - offsetY - (ratio * img.getHeight()));
        document.add(img);
    }

}
