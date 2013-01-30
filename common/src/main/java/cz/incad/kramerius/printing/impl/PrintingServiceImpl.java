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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.JobOriginatingUserName;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.PrinterResolution;
import javax.print.attribute.standard.RequestingUserName;
import javax.print.attribute.standard.Sides;
import javax.print.attribute.standard.MediaSize.ISO;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;

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
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.utils.TitlesUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.printing.PrintingService;
import cz.incad.kramerius.printing.utils.Utils;
import cz.incad.kramerius.processes.impl.ProcessStarter;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;

public class PrintingServiceImpl implements PrintingService {

    public static final int MAX_PAGES = 1000;

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PrintingServiceImpl.class.getName());

    private FedoraAccess fedoraAccess;
    private KConfiguration configuration;

    private SolrAccess solrAccess;

    private DocumentService documentService;
    private GeneratePDFService pdfService;
    
    private Provider<User> userProvider;
    private Provider<Locale> localesProvider;
    
    @Inject
    public PrintingServiceImpl(@Named("securedFedoraAccess") FedoraAccess fedoraAccess, SolrAccess solrAccess, KConfiguration configuration, Provider<Locale> localeProvider, TextsService textsService, ResourceBundleService resourceBundleService, DocumentService documentService, GeneratePDFService pdfService, Provider<User> userProvider) {
        super();
        this.fedoraAccess = fedoraAccess;
        this.configuration = configuration;
        this.configuration = configuration;
        this.solrAccess = solrAccess;
        this.documentService = documentService;
        this.pdfService = pdfService;
        this.userProvider = userProvider;
        this.localesProvider = localeProvider;
        try {
            this.init();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void init() throws IOException {
    }

    @Override
    public void printMaster(String pidFrom, String imgUrl, String i18nUrl) throws IOException, ProcessSubtreeException, PrinterException, PrintException {

        try {
            ObjectPidsPath[] paths = this.solrAccess.getPath(pidFrom);
            ObjectPidsPath selectedPath = selectOnePath(pidFrom, paths);

            AbstractRenderedDocument documentAsFlat = this.documentService.buildDocumentAsFlat(selectedPath, pidFrom, MAX_PAGES, null /*
                                                                                                                                       * used
                                                                                                                                       * default
                                                                                                                                       * values
                                                                                                                                       */);

            renderToPDFandPrint(imgUrl, i18nUrl, documentAsFlat);
        } catch (DocumentException e) {
            throw new PrintException(e);
        }
    }

    public void renderToPDFandPrint(String imgUrl, String i18nUrl, AbstractRenderedDocument document) throws IOException, FileNotFoundException, PrintException, DocumentException {
        FontMap fontMap = new FontMap(this.pdfService.fontsFolder());
        File pdfFile = File.createTempFile("pdf", "rendered");
        pdfFile.deleteOnExit();

        this.pdfService.generateCustomPDF(document, new FileOutputStream(pdfFile), fontMap, imgUrl, i18nUrl, ImageFetcher.WEB);

        String print = KConfiguration.getInstance().getConfiguration().getString("print.implementation", "javax");
        if (print.equals("javax")) {
            javaxprint(pdfFile);
        } else {
            lprprint(pdfFile);
        }
    }

    void lprprint(File pdfFile) throws IOException, PrintException {
        try {
            Map<String, String> mapping = new HashMap<String, String>();
            {
                mapping.put("ONE_SIDE", "one-sided");
                mapping.put("DUPLEX", "one-sided");
                mapping.put("TWO_SIDED_LONG_EDGE", "two-sided-long-edge");
                mapping.put("TWO_SIDED_SHORT_EDGE", "two-sided-short-edge");
            }

            String side = KConfiguration.getInstance().getConfiguration().getString("print.sided", "ONE_SIDE");
            
            List<String> command = new ArrayList<String>();
            command.add("lpr");
            command.add("-#"+KConfiguration.getInstance().getConfiguration().getInt("print.copies", 1));
            command.add("-o sides="+mapping.get(side));
            command.add("-U "+this.userProvider.get().getLoginname());
            command.add(pdfFile.getAbsolutePath());
            
            LOGGER.info(command.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            int code = process.waitFor();
            LOGGER.info("command exit with "+code);
            InputStream errS = process.getErrorStream();
            InputStream outS = process.getInputStream();
            
            ByteArrayOutputStream errBos = new ByteArrayOutputStream();
            IOUtils.copyStreams(errS, errBos);
            System.out.println(new String(errBos.toByteArray()));
            
            ByteArrayOutputStream outBos = new ByteArrayOutputStream();
            IOUtils.copyStreams(outS, outBos);
            System.out.println(new String(outBos.toByteArray()));
            
            
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        
    }
    
    void javaxprint(File pdfFile) throws FileNotFoundException, PrintException {
        PrintService lps = PrintServiceLookup.lookupDefaultPrintService();
        
        DocPrintJob printJob = lps.createPrintJob();
        Doc doc = new SimpleDoc(new FileInputStream(pdfFile), DocFlavor.INPUT_STREAM.PDF, null);

        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        aset.add(new Copies(KConfiguration.getInstance().getConfiguration().getInt("print.copies", 1)));
        aset.add(resolveSidesConfiguration());
        aset.add(new RequestingUserName(this.userProvider.get().getLoginname(), localesProvider.get()));
        
        printJob.print(doc, aset);
    }

    
    public Sides resolveSidesConfiguration() {
        Map<String, Sides> mapping = new HashMap<String, Sides>();
        {
            mapping.put("ONE_SIDE", Sides.ONE_SIDED);
            mapping.put("DUPLEX", Sides.DUPLEX);
            mapping.put("TWO_SIDED_LONG_EDGE", Sides.TWO_SIDED_LONG_EDGE);
            mapping.put("TWO_SIDED_SHORT_EDGE", Sides.TWO_SIDED_SHORT_EDGE);
        }

        String side = KConfiguration.getInstance().getConfiguration().getString("print.sided", "ONE_SIDE");
        if (mapping.containsKey(side)) {
            return mapping.get(side);
        } else
            return Sides.ONE_SIDED;

    }

    public ObjectPidsPath selectOnePath(String requestedPid, ObjectPidsPath[] paths) {
        ObjectPidsPath path;
        if (paths.length > 0) {
            path = paths[0];
        } else {
            path = new ObjectPidsPath(requestedPid);
        }
        return path;
    }

    @Override
    public void printSelection(String[] selection, String imgUrl, String i18nUrl) throws IOException, ProcessSubtreeException, PrinterException, PrintException {
        try {
            AbstractRenderedDocument document = this.documentService.buildDocumentFromSelection(selection, null /*
                                                                                                                 * use
                                                                                                                 * default
                                                                                                                 * values
                                                                                                                 */);
            renderToPDFandPrint(imgUrl, i18nUrl, document);
        } catch (DocumentException e) {
            throw new PrintException(e);
        }
    }

    public static class PrintableDoc implements Printable {

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
            String imgUrl = imgServletUrl + "?uuid=" + objectId + "&action=GETRAW&stream=" + ImageStreams.IMG_FULL.getStreamName();
            return imgUrl;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            try {

                List<AbstractPage> pages = this.document.getPages();
                if (pageIndex < pages.size()) {
                    AbstractPage page = pages.get(pageIndex);

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    if (page instanceof TextPage) {
                        // skip..
                    } else {
                        ImagePage ipage = (ImagePage) page;
                        String pid = ipage.getUuid();

                        String imgUrl = createIMGFULL(pid, imgServletUrl);
                        String mimetypeString = fedoraAccess.getImageFULLMimeType(pid);
                        ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
                        if (mimetype != null) {
                            BufferedImage javaImg = readImage(new URL(imgUrl), mimetype, 0);

                            double imgWidth = javaImg.getWidth();
                            double imgHeight = javaImg.getHeight();

                            double pageWidth = pf.getImageableWidth();
                            double pageHeight = pf.getImageableHeight();

                            if ((imgHeight > pageHeight) || (imgWidth > pageWidth)) {
                                // scaling..
                                double hscale = (pageHeight / imgHeight);
                                double wscale = (pageWidth / imgWidth);

                                double scale = Math.max(hscale, wscale);

                                BufferedImage scaled = ImageUtils.scaleByPercent(javaImg, new Rectangle(javaImg.getWidth(), javaImg.getHeight()), scale, null);
                                g2d.drawImage(scaled, 0, 0, null);

                            } else {
                                g2d.drawImage(javaImg, 5, 5, null);
                            }
                        }

                    }
                    return PAGE_EXISTS;
                } else
                    return NO_SUCH_PAGE;
            } catch (XPathExpressionException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return NO_SUCH_PAGE;
            } catch (MalformedURLException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return NO_SUCH_PAGE;
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                return NO_SUCH_PAGE;
            }
        }

    }

    public static void main(String[] args) {

        PrintService lps = PrintServiceLookup.lookupDefaultPrintService();
        lps.getAttributes().add(new JobOriginatingUserName("troubelin", Locale.getDefault()));
    }
}
