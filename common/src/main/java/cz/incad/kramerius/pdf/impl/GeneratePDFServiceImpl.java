package cz.incad.kramerius.pdf.impl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import cz.incad.kramerius.*;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.*;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.PDFContext;
import cz.incad.kramerius.pdf.commands.ITextCommands;
import cz.incad.kramerius.pdf.commands.render.RenderPDF;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.knav.pdf.PdfTextUnderImage;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.util.List;

public class GeneratePDFServiceImpl extends AbstractPDFRenderSupport implements
        GeneratePDFService {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(GeneratePDFServiceImpl.class.getName());

    private FedoraAccess fedoraAccess;
    private Provider<Locale> localeProvider;
    private TextsService textsService;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;
    private DocumentService documentService;

    private Provider<File> fontDirectory;

    @Inject
    public GeneratePDFServiceImpl(
            @Named("securedFedoraAccess") FedoraAccess fedoraAccess,
            SolrAccess solrAccess, KConfiguration configuration,
            Provider<Locale> localeProvider, TextsService textsService,
            ResourceBundleService resourceBundleService,
            DocumentService documentService,
            @Named("fontsDir") Provider<File> fontDirectoryProvider) {
        super();
        this.fedoraAccess = fedoraAccess;
        this.localeProvider = localeProvider;
        this.textsService = textsService;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;
        this.documentService = documentService;
        this.fontDirectory = fontDirectoryProvider;

        this.fontRegistration();
    }

    private void fontRegistration() {
        PdfTextUnderImage.registerFontDirectories(Arrays
                .asList(this.fontDirectory.get().getAbsolutePath()));
    }

    public void init() throws IOException {
        String[] texts = { 
                "k5security_fail", "k5security_fail_CZ_cs",
                "security_fail", "security_fail_CZ_cs",
                // TODO: Move to another position
                "logininfo", "logininfo_CZ_cs", "k5info","clienthelp" };

        IOUtils.copyBundledResources(this.getClass(), texts, "res/",
                this.textsService.textsFolder());

        String[] xlsts = { "template_static_export.0.1.xslt" };
        IOUtils.copyBundledResources(this.getClass(), xlsts, "templates/",
                this.templatesFolder());

        // copy fonts to fonts folder
        String[] fonts = { "ext_ontheflypdf_ArialCE.ttf", "GentiumPlus-I.ttf",
                "GentiumPlus-R.ttf" };
        IOUtils.copyBundledResources(this.getClass(), fonts, "res/",
                this.fontsFolder());

        for (String fontName : fonts) {
            try {
                InputStream is = this.getClass().getResourceAsStream(
                        "res/" + fontName);
                java.awt.Font font = java.awt.Font.createFont(
                        java.awt.Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(
                        font);
            } catch (FontFormatException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public PreparedDocument generateCustomPDF(
            PreparedDocument rdoc, OutputStream os, Break brk,
            FontMap fmap, String djvUrl, String i18nUrl, ImageFetcher fetcher)
            throws IOException {
        try {
            String brokenPage = null;

            PDFContext pdfContext = new PDFContext(fmap, djvUrl, i18nUrl);

            Document doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            doc.newPage();
            int pocetStranek = 0;
            List<AbstractPage> pages = new ArrayList<AbstractPage>(
                    rdoc.getPages());
            while (!pages.isEmpty()) {
                pocetStranek += 1;
                AbstractPage page = pages.remove(0);
                doc.newPage();
                if (page instanceof ImagePage) {
                    ImagePage iPage = (ImagePage) page;
                    insertOutlinedImagePage(iPage, writer, doc, pdfContext,
                            fetcher);
                } else {
                    TextPage tPage = (TextPage) page;
                    insertOutlinedTextPage(tPage, writer, doc,
                            rdoc.getDocumentTitle(), pdfContext);
                }
                os.flush();
                if (brk.broken(page.getUuid())) {
                    brokenPage = page.getUuid();
                    rdoc.removePagesTill(page.getUuid());
                    break;
                }
            }

            if (brokenPage == null) {
                rdoc.removePages();
            }

            OutlineItem root = rdoc.getOutlineItemRoot();
            if (brokenPage != null) {
                OutlineItem right = rdoc.getOutlineItemRoot().copy();
                OutlineItem left = rdoc.getOutlineItemRoot().copy();
                rdoc.divide(left, right, brokenPage);
                root = left;
                rdoc.setOutlineItemRoot(right);
            }

            PdfContentByte cb = writer.getDirectContent();
            PdfOutline pdfRoot = cb.getRootOutline();
            StringBuffer buffer = new StringBuffer();
            root.debugInformations(buffer, 1);
            fillOutline(pdfRoot, root);
            doc.close();
            os.flush();

        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }
        return rdoc;
    }

    @Override
    public void generateCustomPDF(PreparedDocument rdoc,
            OutputStream os, FontMap fmap, String imgServletUrl,
            String i18nUrl, ImageFetcher fetcher) throws IOException {
        try {

            PDFContext pdfContext = new PDFContext(fmap, imgServletUrl, i18nUrl);

            Document doc = DocumentUtils.createDocument(rdoc);
            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            doc.newPage();
            for (AbstractPage page : rdoc.getPages()) {
                doc.newPage();
                if (page instanceof ImagePage) {
                    ImagePage iPage = (ImagePage) page;
                    insertImage(iPage.getUuid(), writer, doc, (float) 1.0,
                            imgServletUrl, fetcher, pdfContext.getFontMap()
                                    .getRegistredFont("normal"));
                } else {
                    TextPage tPage = (TextPage) page;
                    if (tPage.getOutlineTitle().trim().equals(""))
                        throw new IllegalArgumentException(page.getUuid());
                    insertOutlinedTextPage(tPage, writer, doc,
                            rdoc.getDocumentTitle(), pdfContext);
                }
            }

            PdfContentByte cb = writer.getDirectContent();
            PdfOutline pdfRoot = cb.getRootOutline();
            OutlineItem rDocRoot = rdoc.getOutlineItemRoot();
            StringBuffer buffer = new StringBuffer();
            rDocRoot.debugInformations(buffer, 1);
            fillOutline(pdfRoot, rDocRoot);

            doc.close();
            doc.close();
            os.flush();

        } catch (DocumentException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e.getMessage());
        }

    }

    private void fillOutline(PdfOutline pdfRoot, OutlineItem rDocRoot) {
        OutlineItem[] children = rDocRoot.getChildren();
        for (OutlineItem outlineItem : children) {
            PdfOutline pdfOutline = new PdfOutline(
                    pdfRoot,
                    PdfAction.gotoLocalPage(outlineItem.getDestination(), false),
                    outlineItem.getTitle());
            fillOutline(pdfOutline, outlineItem);
        }
    }

    @Override
    public void generateParent(String requestedPid, int numberOfPages,
            String titlePage, OutputStream os, String imgServletUrl,
            String i18nUrl, int[] rect) throws IOException,
            ProcessSubtreeException {
        try {
            ObjectPidsPath[] paths = solrAccess.getPath(requestedPid);
            final ObjectPidsPath path = selectOnePath(requestedPid, paths);
            generateCustomPDF(this.documentService.buildDocumentAsFlat(path,
                    path.getLeaf(), numberOfPages, rect), os, null, imgServletUrl,
                    i18nUrl, ImageFetcher.WEB);
        } catch (OutOfRangeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ObjectPidsPath selectOnePath(String requestedPid,
            ObjectPidsPath[] paths) {
        ObjectPidsPath path;
        if (paths.length > 0) {
            path = paths[0];
        } else {
            path = new ObjectPidsPath(requestedPid);
        }
        return path;
    }

    @Override
    public void fullPDFExport(ObjectPidsPath path, OutputStreams streams,
            Break brk, String djvuUrl, String i18nUrl, int[] rect)
            throws IOException, ProcessSubtreeException, DocumentException {

        PreparedDocument restOfDoc = documentService
                .buildDocumentAsTree(path, path.getLeaf(), rect);
        OutputStream os = null;
        boolean konec = false;
        while (!konec) {
            if (!restOfDoc.getPages().isEmpty()) {
                os = streams.newOutputStream();
                // ImageFetcher process = ImageFetcher.PROCESS;
                ImageFetcher fetcher = ImageFetcher.WEB; // no security in the
                                                         // full export process
                restOfDoc = generateCustomPDF(restOfDoc, os, brk, new FontMap(
                        this.fontsFolder()), djvuUrl, i18nUrl, fetcher);

                StringBuffer buffer = new StringBuffer();
                restOfDoc.getOutlineItemRoot().debugInformations(buffer, 1);
                os.close();
            } else {
                konec = true;
                break;
            }
        }
    }

    // private void lineInFirstPage(PdfWriter pdfWriter, Document document,
    // float y) {
    // PdfContentByte cb = pdfWriter.getDirectContent();
    //
    // cb.moveTo(5f, y - 10);
    // cb.lineTo(document.getPageSize().getWidth() - 10, y - 10);
    // cb.stroke();
    // }

    private File prepareXSLStyleSheet(Locale locale, String i18nUrl,
            String title, String modelName, String pid) throws IOException {
        File tmpFile = File.createTempFile("temporary", "stylesheet");
        tmpFile.deleteOnExit();
        FileOutputStream fos = null;
        try {
            String localizedXslt = STUtils.localizedXslt(locale, i18nUrl,
                    templatesFolder(), title, modelName, pid);
            fos = new FileOutputStream(tmpFile);
            fos.write(localizedXslt.getBytes(Charset.forName("UTF-8")));
            fos.close();
        } finally {
            if (fos != null)
                fos.close();
        }
        return tmpFile;
    }

    public String xslt(FedoraAccess fa, File styleSheet, String uuid)
            throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf
                .newTransformer(new StreamSource(styleSheet));
        org.w3c.dom.Document biblioMods = fa.getBiblioMods(uuid);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(biblioMods), new StreamResult(bos));
        return new String(bos.toByteArray(), Charset.forName("UTF-8"));
    }

    public void insertOutlinedTextPage(TextPage page, PdfWriter pdfWriter,
            Document document, String title, PDFContext pdfContext)
            throws XPathExpressionException, IOException, DocumentException,
            TransformerException, InstantiationException,
            IllegalAccessException, ParserConfigurationException, SAXException {
        File styleSheet = prepareXSLStyleSheet(localeProvider.get(),
                pdfContext.getI18nUrl(), title, page.getModel(), page.getUuid());
        String text = xslt(this.fedoraAccess, styleSheet, page.getUuid());

        ITextCommands cmnds = new ITextCommands();
        cmnds.load(
                XMLUtils.parseDocument(
                        new ByteArrayInputStream(text.getBytes(Charset
                                .forName("UTF-8"))), true).getDocumentElement(),
                cmnds);

        RenderPDF render = new RenderPDF(pdfContext.getFontMap(), fedoraAccess);
        render.render(document, pdfWriter, cmnds);

    }

    public void insertOutlinedImagePage(ImagePage page, PdfWriter pdfWriter,
            Document document, PDFContext pdfContext, ImageFetcher fetcher)
            throws XPathExpressionException, IOException, DocumentException {
        String pageNumber = page.getPageNumber();
        insertImage(page.getUuid(), pdfWriter, document, 0.7f,
                pdfContext.getDjvuUrl(), fetcher, pdfContext.getFontMap()
                        .getRegistredFont(FontMap.NORMAL_FONT));

        // Font font = createFont();
        Font font = pdfContext.getFontMap().getRegistredFont(
                FontMap.NORMAL_FONT);
        Chunk chunk = new Chunk(pageNumber);
        chunk.setLocalDestination(page.getOutlineDestination());
        float fontSize = chunk.getFont().getCalculatedSize();
        float chwidth = chunk.getWidthPoint();
        int choffsetx = (int) ((document.getPageSize().getWidth() - chwidth) / 2);
        int choffsety = (int) (10 - fontSize);

        PdfContentByte cb = pdfWriter.getDirectContent();
        cb.saveState();
        cb.beginText();
        cb.localDestination(page.getOutlineDestination(), new PdfDestination(
                PdfDestination.FIT));
        pdfWriter.setOpenAction(page.getOutlineDestination());

        cb.setFontAndSize(font.getBaseFont(), 14f);
        cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, pageNumber,
                choffsetx, choffsety + 10, 0);
        cb.endText();
        cb.restoreState();
    }

    public void insertTitleImage(PdfPTable pdfPTable,
            PreparedDocument model, String djvuUrl, ImageFetcher fetcher)
            throws IOException, BadElementException, XPathExpressionException {
        try {
            String uuidToFirstPage = null;
            if ((model.getUuidTitlePage() != null)
                    && (fedoraAccess.isImageFULLAvailable(model
                            .getUuidTitlePage()))) {
                uuidToFirstPage = model.getUuidTitlePage();
            }
            if ((uuidToFirstPage == null)
                    && (model.getUuidFrontCover() != null)
                    && (fedoraAccess.isImageFULLAvailable(model
                            .getUuidFrontCover()))) {
                uuidToFirstPage = model.getUuidFrontCover();

            }
            if ((uuidToFirstPage == null)
                    && (model.getFirstPage() != null)
                    && (fedoraAccess.isImageFULLAvailable(model.getFirstPage()))) {
                uuidToFirstPage = model.getFirstPage();

            }
            if (uuidToFirstPage != null) {
                String mimetypeString = fedoraAccess
                        .getImageFULLMimeType(uuidToFirstPage);
                ImageMimeType mimetype = ImageMimeType
                        .loadFromMimeType(mimetypeString);
                if (mimetype != null) {
                    float smallImage = 0.2f;
                    BufferedImage javaImg = fetcher.fetch(uuidToFirstPage,
                            djvuUrl, mimetype, this.fedoraAccess);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    writeImageToStream(javaImg, "jpeg", bos);

                    com.lowagie.text.Image img = com.lowagie.text.Image
                            .getInstance(bos.toByteArray());

                    img.scaleAbsoluteHeight(smallImage * img.getHeight());
                    img.scaleAbsoluteWidth(smallImage * img.getWidth());
                    pdfPTable.addCell(img);
                }
            } else {
                pdfPTable.addCell(" - ");
            }
        } catch (cz.incad.kramerius.security.SecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            pdfPTable.addCell(" - ");
        }
    }

    public void insertImage(String uuid, PdfWriter pdfWriter,
            Document document, float percentage, String imgServletUrl,
            ImageFetcher fetcher, Font font) throws XPathExpressionException,
            IOException, DocumentException {
        try {
            if (fedoraAccess.isImageFULLAvailable(uuid)) {
                // bypass
                // String imgUrl = createIMGFULL(uuid, imgServletUrl);
                // kdyz je pdf, musi
                String mimetypeString = fedoraAccess.getImageFULLMimeType(uuid);
                ImageMimeType mimetype = ImageMimeType
                        .loadFromMimeType(mimetypeString);
                if (mimetype != null && (!ImageMimeType.PDF.equals(mimetype))) {
                    BufferedImage javaImg = fetcher.fetch(uuid, imgServletUrl,
                            mimetype, this.fedoraAccess);
                    boolean textocr = this.fedoraAccess.isStreamAvailable(uuid,
                            FedoraUtils.ALTO_STREAM);
                    boolean useAlto = KConfiguration.getInstance()
                            .getConfiguration()
                            .getBoolean("pdfQueue.useAlto", true);
                    if (textocr && useAlto) {
                        try {
                            org.w3c.dom.Document alto = XMLUtils
                                    .parseDocument(this.fedoraAccess
                                            .getDataStream(uuid,
                                                    FedoraUtils.ALTO_STREAM));
                            insertJavaImageWithOCR(document, percentage,
                                    pdfWriter, alto, javaImg);
                        } catch (ParserConfigurationException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                            insertJavaImage(document, percentage, javaImg);
                        } catch (SAXException e) {
                            LOGGER.log(Level.SEVERE, e.getMessage(), e);
                            insertJavaImage(document, percentage, javaImg);
                        }

                    } else {
                        insertJavaImage(document, percentage, javaImg);
                    }
                } else {
                    String text = textsService.getText("image_not_available",
                            localeProvider.get());
                    text = text != null ? text : "image_not_available";
                    Chunk chunk = new Chunk(text, font);
                    Paragraph na = new Paragraph();
                    na.add(chunk);
                    document.add(na);
                }
            } else {
                String text = textsService.getText("image_not_available",
                        localeProvider.get());
                text = text != null ? text : "image_not_available";
                Chunk chunk = new Chunk(text, font);
                Paragraph na = new Paragraph();
                na.add(chunk);
                document.add(na);
            }
        } catch (cz.incad.kramerius.security.SecurityException e) {
            LOGGER.log(Level.INFO, e.getMessage());
            Chunk chunk = new Chunk(textsService.getText("security_fail",
                    localeProvider.get()), font);
            Paragraph na = new Paragraph(chunk);
            document.add(na);
        }
    }

    /**
     * Bypass url
     * 
     * @param objectId
     * @return
     */
    private String createIMGFULL(String objectId, String imgServletUrl) {
        String imgUrl = imgServletUrl + "?uuid=" + objectId
                + "&action=GETRAW&stream="
                + ImageStreams.IMG_FULL.getStreamName();
        return imgUrl;
    }

    @Override
    public File templatesFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "templates";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs)
                throw new RuntimeException("cannot create folder '"
                        + dir.getAbsolutePath() + "'");
        }
        return dir;
    }

    public File fontsFolder() {
        File dir = this.fontDirectory.get();
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs)
                throw new RuntimeException("cannot create folder '"
                        + dir.getAbsolutePath() + "'");
        }
        return dir;
    }

}
