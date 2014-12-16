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
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.knav.pdf.PdfTextUnderImage;

/**
 * Support for pdf rendering
 * 
 * @author pavels
 */
public class AbstractPDFRenderSupport {

    public static class ScaledImageOptions {

        private int xoffset;
        private int yoffset;
        private float scaleFactor;

        private int xdpi;
        private int ydpi;

        private int width;
        private int height;

        public int getXoffset() {
            return xoffset;
        }

        public void setXoffset(int xoffset) {
            this.xoffset = xoffset;
        }

        public int getYoffset() {
            return yoffset;
        }

        public void setYoffset(int yoffset) {
            this.yoffset = yoffset;
        }

        public float getScaleFactor() {
            return scaleFactor;
        }

        public void setScaleFactor(float scaleFactor) {
            this.scaleFactor = scaleFactor;
        }

        public int getXdpi() {
            return xdpi;
        }

        public void setXdpi(int xdpi) {
            this.xdpi = xdpi;
        }

        public int getYdpi() {
            return ydpi;
        }

        public void setYdpi(int ydpi) {
            this.ydpi = ydpi;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    public void insertImageFromURL(Document document, float percentage,
            String imgUrl, ImageMimeType mimetype) throws IOException,
            MalformedURLException, BadElementException, DocumentException {
        BufferedImage javaImg = readImage(new URL(imgUrl), mimetype, 0);
        insertJavaImage(document, percentage, javaImg);
    }

    public ScaledImageOptions insertJavaImageWithOCR(Document document,
            float percentage, PdfWriter pdfWriter, org.w3c.dom.Document alto,
            BufferedImage javaImg) throws IOException, BadElementException,
            MalformedURLException, DocumentException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeImageToStream(javaImg, "jpeg", bos);

        // com.lowagie.text.Image img =
        // com.lowagie.text.Image.getInstance(bos.toByteArray());

        PdfTextUnderImage textUnderImage = new PdfTextUnderImage();
        ScaledImageOptions options = insertJavaImage(document, percentage,
                javaImg);
        // textUnderImage.setDebug(true);

        textUnderImage.imageWithAlto(document, pdfWriter, alto, options);
        return options;
    }

    public static ScaledImageOptions insertJavaImage(Document document,
            float percentage, BufferedImage javaImg) throws IOException,
            BadElementException, MalformedURLException, DocumentException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeImageToStream(javaImg, "jpeg", bos);

        com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bos
                .toByteArray());

        Float ratio = ratio(document, percentage, javaImg);

        int fitToPageWidth = (int) (javaImg.getWidth(null) * ratio);
        int fitToPageHeight = (int) (javaImg.getHeight(null) * ratio);

        int offsetX = ((int) document.getPageSize().getWidth() - fitToPageWidth) / 2;
        int offsetY = ((int) document.getPageSize().getHeight() - fitToPageHeight) / 2;

        img.scaleAbsoluteHeight(ratio * img.getHeight());

        img.scaleAbsoluteWidth(ratio * img.getWidth());
        img.setAbsolutePosition((offsetX), document.getPageSize().getHeight()
                - offsetY - (ratio * img.getHeight()));

        document.add(img);

        ScaledImageOptions options = new ScaledImageOptions();
        options.setXdpi(img.getDpiX());
        options.setYdpi(img.getDpiY());

        options.setXoffset(offsetX);
        options.setYoffset(offsetY);

        options.setWidth(fitToPageWidth);
        options.setHeight(fitToPageHeight);
        options.setScaleFactor(ratio);

        return options;
    }

    public static Float ratio(Document document, float percentage,
            BufferedImage javaImg) {
        Float wratio = document.getPageSize().getWidth()
                / javaImg.getWidth(null);
        Float hratio = document.getPageSize().getHeight()
                / javaImg.getHeight(null);
        Float ratio = Math.min(wratio, hratio);
        if (percentage != 1.0) {
            ratio = ratio * percentage;
        }
        return ratio;
    }

}
