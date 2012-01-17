package cz.incad.kramerius.pdf.impl;

import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.readImage;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.pdf.draw.VerticalPositionMark;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.document.model.ImagePage;
import cz.incad.kramerius.document.model.OutlineItem;
import cz.incad.kramerius.document.model.TextPage;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.PDFContext;
import cz.incad.kramerius.pdf.utils.pdf.DocumentUtils;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;

public class GeneratePDFServiceImpl extends AbstractPDFRenderSupport implements GeneratePDFService {

    
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GeneratePDFServiceImpl.class.getName());

    private FedoraAccess fedoraAccess;
    private Provider<Locale> localeProvider;
    private TextsService textsService;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;
    private DocumentService documentService;

    @Inject
    public GeneratePDFServiceImpl(@Named("securedFedoraAccess") FedoraAccess fedoraAccess, SolrAccess solrAccess, KConfiguration configuration, Provider<Locale> localeProvider, TextsService textsService, ResourceBundleService resourceBundleService, DocumentService documentService) {
        super();
        this.fedoraAccess = fedoraAccess;
        this.localeProvider = localeProvider;
        this.textsService = textsService;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;
        this.documentService = documentService;

    }

    public void init() throws IOException {
        String[] texts = { "first_page", "first_page_CZ_cs", "first_page_html", "first_page_html_CZ_cs",
        "first_page_xml", "first_page_xml_CZ_cs",
        "security_fail", "security_fail_CZ_cs",
        // TODO: Move to another position
        "logininfo", "logininfo_CZ_cs"

        };

        IOUtils.copyBundledResources(this.getClass(), texts, "res/", this.textsService.textsFolder());
        String[] xlsts = { "template.xslt" };
        IOUtils.copyBundledResources(this.getClass(), xlsts, "templates/", this.templatesFolder());

        String[] fonts = { "ext_ontheflypdf_ArialCE.ttf", "GentiumPlus-I.ttf", "GentiumPlus-R.ttf" };
        IOUtils.copyBundledResources(this.getClass(), fonts, "res/", this.fontsFolder());
    }

    @Override
    public AbstractRenderedDocument generateCustomPDF(AbstractRenderedDocument rdoc, OutputStream os, Break brk, String djvUrl, String i18nUrl, ImageFetcher fetcher) throws IOException {
        try {
            String brokenPage = null;

            PDFContext pdfContext = new PDFContext(FontMap.createFontMap(null), djvUrl, i18nUrl);

            Document doc = DocumentUtils.createDocument(rdoc);

            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();


            doc.newPage();
            int pocetStranek = 0;
            List<AbstractPage> pages = new ArrayList<AbstractPage>(rdoc.getPages());
            while (!pages.isEmpty()) {
                pocetStranek += 1;
                AbstractPage page = pages.remove(0);
                doc.newPage();
                if (page instanceof ImagePage) {
                    ImagePage iPage = (ImagePage) page;
                    insertOutlinedImagePage(iPage, writer, doc, pdfContext, fetcher);
                } else {
                    TextPage tPage = (TextPage) page;
                    insertOutlinedTextPage(tPage, writer, doc, rdoc.getDocumentTitle(), pdfContext);
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
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return rdoc;
    }

    @Override
    public void generateCustomPDF(AbstractRenderedDocument rdoc/*
                                                                * , String
                                                                * parentUUID
                                                                */, OutputStream os, String imgServletUrl, String i18nUrl, ImageFetcher fetcher) throws IOException {
        try {

            PDFContext pdfContext = new PDFContext(FontMap.createFontMap(null), imgServletUrl, i18nUrl);

            Document doc = DocumentUtils.createDocument(rdoc);
            PdfWriter writer = PdfWriter.getInstance(doc, os);
            doc.open();

            doc.newPage();
            for (AbstractPage page : rdoc.getPages()) {
                doc.newPage();
                if (page instanceof ImagePage) {
                    ImagePage iPage = (ImagePage) page;
                    insertImage(iPage.getUuid(), writer, doc, (float) 1.0, imgServletUrl, fetcher, pdfContext.getFontMap().getRegistredFont("normal"));
                } else {
                    TextPage tPage = (TextPage) page;
                    if (tPage.getOutlineTitle().trim().equals(""))
                        throw new IllegalArgumentException(page.getUuid());
                    insertOutlinedTextPage(tPage, writer, doc, rdoc.getDocumentTitle(), pdfContext);
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
        } catch (XPathExpressionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private void fillOutline(PdfOutline pdfRoot, OutlineItem rDocRoot) {
        OutlineItem[] children = rDocRoot.getChildren();
        for (OutlineItem outlineItem : children) {
            PdfOutline pdfOutline = new PdfOutline(pdfRoot, PdfAction.gotoLocalPage(outlineItem.getDestination(), false), outlineItem.getTitle());
            fillOutline(pdfOutline, outlineItem);
        }
    }

    @Override
    public void generateImagesSelection(String[] imagePids, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl, int[] rect) throws IOException, ProcessSubtreeException {
        generateCustomPDF(this.documentService.buildDocumentFromSelection(imagePids, rect), os, imgServletUrl, i18nUrl, ImageFetcher.WEB);
    }

    
    @Override
    public void generateParent(String requestedPid, int numberOfPages, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl, int[] rect) throws IOException, ProcessSubtreeException {
        ObjectPidsPath[] paths = solrAccess.getPath(requestedPid);
        final ObjectPidsPath path = selectOnePath(requestedPid, paths);
        generateCustomPDF(this.documentService.buildDocumentAsFlat(path, path.getLeaf(), numberOfPages, rect), os, imgServletUrl, i18nUrl, ImageFetcher.WEB);
    }


    public static ObjectPidsPath selectOnePath(String requestedPid, ObjectPidsPath[] paths) {
        ObjectPidsPath path;
        if (paths.length > 0) {
            path = paths[0];
        } else {
            path = new ObjectPidsPath(requestedPid);
        }
        return path;
    }

    

    @Override
    public void fullPDFExport(ObjectPidsPath path, OutputStreams streams, Break brk, String djvuUrl, String i18nUrl, int[] rect) throws IOException, ProcessSubtreeException {

        AbstractRenderedDocument restOfDoc = documentService.buildDocumentAsTree(path, path.getLeaf(), rect);
        OutputStream os = null;
        boolean konec = false;
        while (!konec) {
            if (!restOfDoc.getPages().isEmpty()) {
                os = streams.newOutputStream();
                restOfDoc = generateCustomPDF(restOfDoc, os, brk, djvuUrl, i18nUrl, ImageFetcher.PROCESS);

                StringBuffer buffer = new StringBuffer();
                restOfDoc.getOutlineItemRoot().debugInformations(buffer, 1);
                os.close();
            } else {
                konec = true;
                break;
            }
        }
    }

    private void lineInFirstPage(PdfWriter pdfWriter, Document document, float y) {
        PdfContentByte cb = pdfWriter.getDirectContent();

        cb.moveTo(5f, y - 10);
        cb.lineTo(document.getPageSize().getWidth() - 10, y - 10);
        cb.stroke();
    }

    private File prepareXSLStyleSheet(Locale locale, String i18nUrl, String title, String modelName) throws IOException {
        File tmpFile = File.createTempFile("temporary", "stylesheet");
        tmpFile.deleteOnExit();
        FileOutputStream fos = null;
        try {
            String localizedXslt = STUtils.localizedXslt(locale, i18nUrl, templatesFolder(), title, modelName);
            fos = new FileOutputStream(tmpFile);
            fos.write(localizedXslt.getBytes(Charset.forName("UTF-8")));
            fos.close();
        } finally {
            if (fos != null)
                fos.close();
        }
        return tmpFile;
    }

    public String xslt(FedoraAccess fa, File styleSheet, String uuid) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(styleSheet));
        org.w3c.dom.Document biblioMods = fa.getBiblioMods(uuid);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(biblioMods), new StreamResult(bos));
        return new String(bos.toByteArray(), Charset.forName("UTF-8"));
    }

    public void insertOutlinedTextPage(TextPage page, PdfWriter pdfWriter, Document document, String title, PDFContext pdfContext) throws XPathExpressionException, IOException, DocumentException, TransformerException {
        File styleSheet = prepareXSLStyleSheet(localeProvider.get(), pdfContext.getI18nUrl(), title, page.getModel());
        String text = xslt(this.fedoraAccess, styleSheet, page.getUuid());

        BufferedReader strReader = new BufferedReader(new StringReader(text));
        StringBuffer oneChunkBuffer = new StringBuffer();
        String line = null;
        while ((line = strReader.readLine()) != null) {
            SimpleTextCommands command = SimpleTextCommands.findCommand(line);
            if (command == SimpleTextCommands.LINE) {
                insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), pdfContext.getFontMap().getRegistredFont("normal"));
                document.add(new Paragraph(" "));
                VerticalPositionMark vsep = new LineSeparator();
                document.add(vsep);
                document.add(new Paragraph(" "));

                oneChunkBuffer = new StringBuffer();

            } else if (command == SimpleTextCommands.PARA) {
                insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), pdfContext.getFontMap().getRegistredFont("normal"));
                oneChunkBuffer = new StringBuffer();
            } else if (command == SimpleTextCommands.FONT) {
                oneChunkBuffer.append(line);
                Font font = pdfContext.getFontMap().getRegistredFont("normal");
                String oneChunkText = oneChunkBuffer.toString();
                Map<String, String> parameters = command.parameters(oneChunkText);
                if (parameters.containsKey("size")) {
                    font.setSize(Integer.parseInt(parameters.get("size")));
                }
                insertPara(page, pdfWriter, document, oneChunkText.substring(0, command.indexStart(oneChunkText)), font);
                oneChunkBuffer = new StringBuffer();
            } else {
                oneChunkBuffer.append(line).append('\n');
            }
        }

        if (oneChunkBuffer.length() > 0) {
            insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), pdfContext.getFontMap().getRegistredFont("normal"));
        }
    }

    private void insertPara(TextPage page, PdfWriter pdfWriter, Document document, String text, Font font) throws DocumentException, IOException {

        text = text.trim().replace('\t', ' ');

        Chunk chunk = new Chunk(text, font);
        chunk.setLocalDestination(page.getOutlineDestination());
        pdfWriter.setOpenAction(page.getOutlineDestination());
        
        Paragraph para = new Paragraph(chunk);
        document.add(para);
    }

    public void insertOutlinedImagePage(ImagePage page, PdfWriter pdfWriter, Document document, /*
                                                                                                 * String
                                                                                                 * djvuUrl
                                                                                                 * ,
                                                                                                 */PDFContext pdfContext, ImageFetcher fetcher) throws XPathExpressionException, IOException, DocumentException {
        String pageNumber = page.getPageNumber();
        insertImage(page.getUuid(), pdfWriter, document, 0.7f, pdfContext.getDjvuUrl(), fetcher, pdfContext.getFontMap().getRegistredFont(FontMap.NORMAL_FONT));
        

        // Font font = createFont();
        Font font = pdfContext.getFontMap().getRegistredFont(FontMap.NORMAL_FONT);
        Chunk chunk = new Chunk(pageNumber);
        chunk.setLocalDestination(page.getOutlineDestination());
        float fontSize = chunk.getFont().getCalculatedSize();
        float chwidth = chunk.getWidthPoint();
        int choffsetx = (int) ((document.getPageSize().getWidth() - chwidth) / 2);
        int choffsety = (int) (10 - fontSize);

        PdfContentByte cb = pdfWriter.getDirectContent();
        cb.saveState();
        cb.beginText();
        cb.localDestination(page.getOutlineDestination(), new PdfDestination(PdfDestination.FIT));
        pdfWriter.setOpenAction(page.getOutlineDestination());

        cb.setFontAndSize(font.getBaseFont(), 14f);
        cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, pageNumber, choffsetx, choffsety + 10, 0);
        cb.endText();
        cb.restoreState();
    }


    public static Font createFont(FontMap.TYPE type) throws DocumentException, IOException {
        switch (type) {
        case EMBEDED_TTF: {
            String workingDir = Constants.WORKING_DIR;
            File fontFile = new File(workingDir + File.separator + "fonts" + File.separator + "GentiumPlus-R.ttf");
            BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250, true);
            return new Font(bf);
        }
        case NOT_EMBEDED: {
            BaseFont bf = BaseFont.createFont("Helvetica", BaseFont.CP1250, BaseFont.NOT_EMBEDDED);
            return new Font(bf);
        }
        default:
            throw new IllegalStateException("");
        }
    }


    public void insertTitleImage(PdfPTable pdfPTable, AbstractRenderedDocument model, String djvuUrl, ImageFetcher fetcher) throws IOException, BadElementException, XPathExpressionException {
        try {
            String uuidToFirstPage = null;
            if ((model.getUuidTitlePage() != null) && (fedoraAccess.isImageFULLAvailable(model.getUuidTitlePage()))) {
                uuidToFirstPage = model.getUuidTitlePage();
            }
            if ((uuidToFirstPage == null) && (model.getUuidFrontCover() != null) && (fedoraAccess.isImageFULLAvailable(model.getUuidFrontCover()))) {
                uuidToFirstPage = model.getUuidFrontCover();

            }
            if ((uuidToFirstPage == null) && (model.getFirstPage() != null) && (fedoraAccess.isImageFULLAvailable(model.getFirstPage()))) {
                uuidToFirstPage = model.getFirstPage();

            }
            if (uuidToFirstPage != null) {
                //BufferedImage bufferedImage = KrameriusImageSupport.readImage(uuidToFirstPage, ImageStreams.IMG_FULL.name(), this.fedoraAccess, 0);
                
                // for static exports
                //String imgUrl = createIMGFULL(uuidToFirstPage, djvuUrl);
                String mimetypeString = fedoraAccess.getImageFULLMimeType(uuidToFirstPage);
                ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
                if (mimetype != null) {
                    float smallImage = 0.2f;
                    BufferedImage javaImg = fetcher.fetch(uuidToFirstPage, djvuUrl, mimetype, this.fedoraAccess);
                    //BufferedImage javaImg = readImage(new URL(imgUrl), mimetype, 0);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    writeImageToStream(javaImg, "jpeg", bos);

                    com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bos.toByteArray());

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

    public void insertImage(String uuid, PdfWriter pdfWriter, Document document, float percentage, String imgServletUrl, ImageFetcher fetcher, Font font) throws XPathExpressionException, IOException, DocumentException {
        try {
            if (fedoraAccess.isImageFULLAvailable(uuid)) {
                // bypass
                //String imgUrl = createIMGFULL(uuid, imgServletUrl);
                String mimetypeString = fedoraAccess.getImageFULLMimeType(uuid);
                ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
                if (mimetype != null) {
                    BufferedImage javaImg = fetcher.fetch(uuid, imgServletUrl,mimetype, this.fedoraAccess);
                    insertJavaImage(document, percentage, javaImg);
                }
            } else {
                Chunk chunk = new Chunk(textsService.getText("image_not_available", localeProvider.get()), font);
                Paragraph na = new Paragraph();
                na.add(chunk);
                document.add(na);
            }
        } catch (cz.incad.kramerius.security.SecurityException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            Chunk chunk = new Chunk(textsService.getText("security_fail", localeProvider.get()), font);
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
        String imgUrl = imgServletUrl + "?uuid=" + objectId + "&action=GETRAW&stream=" + ImageStreams.IMG_FULL.getStreamName();
        return imgUrl;
    }

    @Override
    public File templatesFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "templates";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs)
                throw new RuntimeException("cannot create folder '" + dir.getAbsolutePath() + "'");
        }
        return dir;
    }

    public File fontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        File dir = new File(dirName);
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs)
                throw new RuntimeException("cannot create folder '" + dir.getAbsolutePath() + "'");
        }
        return dir;
    }



}
