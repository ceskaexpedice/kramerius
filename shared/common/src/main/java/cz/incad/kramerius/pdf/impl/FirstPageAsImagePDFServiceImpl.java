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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.PDFContext;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class FirstPageAsImagePDFServiceImpl extends AbstractPDFRenderSupport implements FirstPagePDFService {
    
    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FirstPageAsImagePDFServiceImpl.class.getName());
    

    @Inject
    @Named("TEXT")
    FirstPagePDFService textPDFService;

    
    @Override
    public void selection(PreparedDocument rdoc, OutputStream os,String[] pids, FontMap fontMap) {
        try {


            File pdfFile = writeSelectionToPDF(rdoc, pids, fontMap);
            BufferedImage image = KrameriusImageSupport.readImage(pdfFile.toURI().toURL(), ImageMimeType.PDF, 0);
            LOGGER.fine("Original first page file :"+pdfFile.getAbsolutePath());
            
            File imageFile = File.createTempFile("image", ImageMimeType.PNG.getDefaultFileExtension());
            LOGGER.fine("Original first page file as image :"+imageFile.getAbsolutePath());
            ImageIO.write(image, ImageMimeType.PNG.getDefaultFileExtension(), imageFile);
            
            insertImage(rdoc, os, imageFile);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    public void insertImage(PreparedDocument rdoc, OutputStream os, File imageFile) throws DocumentException, IOException, MalformedURLException, BadElementException {
        Document doc = DocumentUtils.createDocument(rdoc);

        PdfWriter writer = PdfWriter.getInstance(doc, os);
        doc.open();

        insertImageFromURL(doc, 1.0f, imageFile.toURI().toURL().toString(), ImageMimeType.PNG);

        doc.close();
        os.flush();
    }

    File writeSelectionToPDF(PreparedDocument rdoc, String[] pids,  FontMap fontMap) throws IOException, FileNotFoundException {
        FileOutputStream pdfFos = null;
        try {
            File tmpFile = File.createTempFile("firstpage", "pdf");
            pdfFos = new FileOutputStream(tmpFile);
            this.textPDFService.selection(rdoc, pdfFos, pids, fontMap);
            return tmpFile;
        } finally {
            if (pdfFos != null) pdfFos.close();
        }
    }

    public File writeParentToPDF(PreparedDocument rdoc,ObjectPidsPath path,   FontMap fontMap) throws IOException, FileNotFoundException {
        FileOutputStream pdfFos = null;
        try {
            File tmpFile = File.createTempFile("firstpage", "pdf");
            pdfFos = new FileOutputStream(tmpFile);
            this.textPDFService.parent(rdoc, pdfFos,path, fontMap);
            return tmpFile;
        } finally {
            if (pdfFos != null) pdfFos.close();
        }
    }

    @Override
    public void parent(PreparedDocument rdoc, OutputStream os, ObjectPidsPath path,  FontMap fontMap) {
        try {
            File pdfFile =writeParentToPDF(rdoc, path, fontMap);
            BufferedImage image = KrameriusImageSupport.readImage(pdfFile.toURI().toURL(), ImageMimeType.PDF, 0);
            
            File imageFile = File.createTempFile("image", ImageMimeType.PNG.getDefaultFileExtension());
            ImageIO.write(image, ImageMimeType.PNG.getDefaultFileExtension(), imageFile);
            
            insertImage(rdoc, os, imageFile);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }
}
