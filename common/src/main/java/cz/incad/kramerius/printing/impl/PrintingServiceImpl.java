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
package cz.incad.kramerius.printing.impl;

import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.readImage;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.PageAttributes;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.print.PrintService;
import javax.print.attribute.standard.PrinterResolution;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.document.model.ImagePage;
import cz.incad.kramerius.document.model.PageVisitor;
import cz.incad.kramerius.document.model.RenderedDocument;
import cz.incad.kramerius.document.model.TextPage;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.imaging.utils.ImageUtils;
import cz.incad.kramerius.pdf.utils.TitlesUtils;
import cz.incad.kramerius.printing.PrintingService;
import cz.incad.kramerius.printing.utils.Utils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

public class PrintingServiceImpl implements PrintingService {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PrintingServiceImpl.class.getName());
    
    private FedoraAccess fedoraAccess;
    private KConfiguration configuration;
    private Provider<Locale> localeProvider;
    private TextsService textsService;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;

    private DocumentService documentService;
    
    //(210 × 297 mm)
    
    public int dpi = Utils.DEFAUTL_DPI;
    public Dimension page = Utils.A4;
    

    
    
    
    @Inject
    public PrintingServiceImpl(@Named("securedFedoraAccess") FedoraAccess fedoraAccess, SolrAccess solrAccess, KConfiguration configuration, Provider<Locale> localeProvider, TextsService textsService, ResourceBundleService resourceBundleService, DocumentService documentService) {
        super();
        this.fedoraAccess = fedoraAccess;
        this.configuration = configuration;
        this.localeProvider = localeProvider;
        this.textsService = textsService;
        this.configuration = configuration;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;
        this.documentService = documentService;
        try {
            this.init();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    
    private void init() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void print(ObjectPidsPath path, String pidFrom, int howMany, String imgUrl, String i18nUrl) throws IOException, ProcessSubtreeException {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        AbstractRenderedDocument documentAsFlat = this.documentService.buildDocumentAsFlat(path, pidFrom, howMany);
        printerJob.setPrintable(new PrintableDoc(this.fedoraAccess, documentAsFlat , imgUrl,Utils.A4, Utils.DEFAUTL_DPI));
        
        printerJob.setJobName("K4 print");
        
        try {
            
            printerJob.print();
        } catch (PrinterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }



    public static class PrintableDoc implements Printable{
        
        private AbstractRenderedDocument document;
        private String imgServletUrl;
        private FedoraAccess fedoraAccess;
        
        private Dimension page;
        private int dpi;
        
        public PrintableDoc(FedoraAccess fedoraAccess, AbstractRenderedDocument document, String imgServletUrl, Dimension page, int dpi) {
            super();
            this.fedoraAccess = fedoraAccess;
            this.document = document;
            this.imgServletUrl = imgServletUrl;
            
            this.page = page;
            this.dpi = dpi;
        }

        private String createIMGFULL(String objectId, String imgServletUrl) {
            String imgUrl = imgServletUrl +"?uuid="+objectId+"&action=GETRAW&stream="+ImageStreams.IMG_FULL.getStreamName();
            return imgUrl;
        }

        
        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            try {
                
                
                List<AbstractPage> pages = this.document.getPages();
                if (pageIndex < pages.size() -1) {
                    AbstractPage page = pages.get(pageIndex);
                    
                    Graphics2D g2d = (Graphics2D)g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    if (page instanceof TextPage) {
                        
                    } else {
                        ImagePage ipage = (ImagePage) page;
                        String pid = ipage.getUuid();
                        
                        String imgUrl = createIMGFULL(pid, imgServletUrl);
                        String mimetypeString = fedoraAccess.getImageFULLMimeType(pid);
                        ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
                        if (mimetype != null) {
                            BufferedImage javaImg = readImage(new URL(imgUrl), mimetype,0);
                            
                            double imgWidth = javaImg.getWidth();    
                            double imgHeight = javaImg.getHeight() ;
                            
                            double pageWidth = pf.getImageableWidth();
                            double pageHeight = pf.getImageableHeight();
                            
                            if ((imgHeight>pageHeight) || (imgWidth > pageWidth)) {
                                //scaling..
                                 double hscale =  (pageHeight / imgHeight);
                                 double wscale =  (pageWidth / imgWidth);
                                 System.out.println(hscale);
                                 System.out.println(wscale);
                                 
                                 double scale = Math.max(hscale, wscale);
                                 
                                 BufferedImage scaled = ImageUtils.scaleByPercent(javaImg, new Rectangle(javaImg.getWidth(), javaImg.getHeight()), scale, null);
                                 g2d.drawImage(scaled, 0,0,null);
                                 
                            } else {
                                g2d.drawImage(javaImg, 5,5,null);
                            }
                        }                    
                        
                    }
                    return PAGE_EXISTS;
                } else return NO_SUCH_PAGE;
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return NO_SUCH_PAGE;
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return NO_SUCH_PAGE;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return NO_SUCH_PAGE;
            }
        }

        
        
        
    }
    
}
