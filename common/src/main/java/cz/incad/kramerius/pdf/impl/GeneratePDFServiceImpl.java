package cz.incad.kramerius.pdf.impl;

import static cz.incad.kramerius.utils.BiblioModsUtils.getPageNumber;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.readImage;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.lowagie.text.pdf.draw.VerticalPositionMark;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AbstractPage;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.document.model.ImagePage;
import cz.incad.kramerius.document.model.OutlineItem;
import cz.incad.kramerius.document.model.RenderedDocument;
import cz.incad.kramerius.document.model.TextPage;
import cz.incad.kramerius.imaging.ImageStreams;
import cz.incad.kramerius.impl.AbstractTreeNodeProcessorAdapter;
import cz.incad.kramerius.pdf.Break;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.utils.TitlesUtils;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.BiblioModsUtils;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class GeneratePDFServiceImpl implements GeneratePDFService {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GeneratePDFServiceImpl.class.getName());

    public static final int DEFAULT_WIDTH = 595;
    public static final int DEFAULT_HEIGHT = 842;

	public static com.lowagie.text.Image DEFAULT_LOGO_IMAGE;
	static {
		try {
			DEFAULT_LOGO_IMAGE = com.lowagie.text.Image.getInstance(GeneratePDFServiceImpl.class.getResource("res/kramerius_logo.png"));
		} catch (BadElementException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private FedoraAccess fedoraAccess;
	private KConfiguration configuration;
	private Provider<Locale> localeProvider;
	private TextsService textsService;
	private ResourceBundleService resourceBundleService;
	private SolrAccess solrAccess;
	private DocumentService documentService;
	
	
	@Inject
	public GeneratePDFServiceImpl(@Named("securedFedoraAccess") FedoraAccess fedoraAccess, SolrAccess solrAccess, KConfiguration configuration, Provider<Locale> localeProvider, TextsService textsService, ResourceBundleService resourceBundleService, DocumentService documentService) {
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
		String[] texts = 
		{
        "first_page",
		"first_page_CZ_cs",
		"first_page_html",
		"first_page_html_CZ_cs",
		"security_fail",
		"security_fail_CZ_cs",
		
		//TODO: Move to another position
		"logininfo",
        "logininfo_CZ_cs"
		
		};

		IOUtils.copyBundledResources(this.getClass(),texts,"res/", this.textsService.textsFolder());
		String[] xlsts = 
		{"template.xslt"};
		IOUtils.copyBundledResources(this.getClass(),xlsts,"templates/", this.templatesFolder());

		String[] fonts = 
		{"ext_ontheflypdf_ArialCE.ttf",
        "GentiumPlus-I.ttf",
        "GentiumPlus-R.ttf"};
		IOUtils.copyBundledResources(this.getClass(), fonts,"res/", this.fontsFolder());
	}

	@Override
	public AbstractRenderedDocument generateCustomPDF(AbstractRenderedDocument rdoc, String parentUUID, OutputStream os, Break brk, String djvUrl, String i18nUrl) throws IOException {
		try {
			String brokenPage = null;
			Document doc = createDocument();
			PdfWriter writer = PdfWriter.getInstance(doc, os);
			doc.open();
			insertFirstPage(rdoc, parentUUID, rdoc.getUuidTitlePage(), writer, doc, djvUrl);
			doc.newPage();
			int pocetStranek = 0;
			List<AbstractPage> pages = new ArrayList<AbstractPage>(rdoc.getPages());
			while(!pages.isEmpty()) {
				pocetStranek += 1;
				AbstractPage page = pages.remove(0);
				doc.newPage();
				if (page instanceof ImagePage) {
					ImagePage iPage = (ImagePage) page;
					//insertImage(iPage.getUuid(), writer, doc, (float)1.0, djvUrl);
					insertOutlinedImagePage(iPage, writer, doc, djvUrl);
				} else {
					TextPage tPage = (TextPage) page;
					insertOutlinedTextPage(tPage, writer, doc, rdoc.getDocumentTitle(), i18nUrl);
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
	public void generateCustomPDF(AbstractRenderedDocument rdoc, String parentUUID, OutputStream os, String imgServletUrl, String i18nUrl) throws IOException {
		try {
			Document doc = createDocument();
			PdfWriter writer = PdfWriter.getInstance(doc, os);
			doc.open();
			
			insertFirstPage(rdoc, parentUUID, rdoc.getUuidTitlePage(), writer, doc, imgServletUrl);

			doc.newPage();
			for (AbstractPage page : rdoc.getPages()) {
				doc.newPage();
				if (page instanceof ImagePage) {
					ImagePage iPage = (ImagePage) page;
//					insertOutlinedImagePage(iPage, writer, doc, djvuUrl);
                    insertImage(iPage.getUuid(), writer, doc, (float)1.0, imgServletUrl);
				} else {
					TextPage tPage = (TextPage) page;
					if (tPage.getOutlineTitle().trim().equals("")) throw new IllegalArgumentException(page.getUuid());
					insertOutlinedTextPage(tPage, writer, doc, rdoc.getDocumentTitle(), i18nUrl);
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
			PdfOutline pdfOutline = new PdfOutline(pdfRoot, PdfAction.gotoLocalPage(outlineItem.getDestination(), false),outlineItem.getTitle());
			fillOutline(pdfOutline, outlineItem);
		}
	}


	

	
	

	@Override
    public void dynamicPDFExport(String requestinguuid, String uuidFrom, int numberOfPage, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl) throws IOException, ProcessSubtreeException {
//	    
//        ObjectPidsPath[] paths = solrAccess.getPath(uuidFrom);
//        String[] pathFromRootToLeaf = paths[0].getPathFromRootToLeaf();
//        
//        org.w3c.dom.Document relsExt = this.fedoraAccess.getRelsExt(uuidFrom);
//        String modelName = this.fedoraAccess.getKrameriusModelName(relsExt);
//        
//        final AbstractRenderedDocument renderedDocument = new RenderedDocument(modelName, uuidFrom);
//        renderedDocument.setDocumentTitle(TitlesUtils.title(requestinguuid, this.solrAccess, this.fedoraAccess));
//        renderedDocument.setUuidTitlePage(titlePage);
//        renderedDocument.setUuidMainTitle(pathFromRootToLeaf[0]);
//        
//        buildRenderingDocumentAsFlat(renderedDocument, uuidFrom, numberOfPage);
//        generateCustomPDF(renderedDocument, uuidFrom,os, imgServletUrl,i18nUrl);
	    
    }

    @Override
	public void dynamicPDFExport(List<String> path, String uuidFrom, String uuidTo, String titlePage, OutputStream os, String imgUrl, String i18nUrl) throws IOException, ProcessSubtreeException {
        throw new UnsupportedOperationException("");
//		LOGGER.info("current locale is "+localeProvider.get());
//		
//		
//		if (!path.isEmpty()) {
//			String lastUuid = path.get(path.size() -1);
//
//			org.w3c.dom.Document relsExt = this.fedoraAccess.getRelsExt(lastUuid);
//			String modelName = this.fedoraAccess.getKrameriusModelName(relsExt);
//			
//			final AbstractRenderedDocument renderedDocument = new RenderedDocument(modelName, lastUuid);
//			renderedDocument.setDocumentTitle(TitlesUtils.title(lastUuid, this.solrAccess, this.fedoraAccess));
//			renderedDocument.setUuidTitlePage(titlePage);
//			renderedDocument.setUuidMainTitle(path.get(0));
//			
//			buildRenderingDocumentAsFlat(relsExt, lastUuid, renderedDocument, uuidFrom, uuidTo);
//			generateCustomPDF(renderedDocument, lastUuid,os, imgUrl,i18nUrl);
//		}
	}


	@Override
	public void fullPDFExport(ObjectPidsPath path, OutputStreams streams, Break brk, String djvuUrl, String i18nUrl) throws IOException, ProcessSubtreeException {
	    
	    
	    
		
		AbstractRenderedDocument restOfDoc = documentService.buildDocumentAsTree(path, path.getLeaf());
		OutputStream os = null;
		boolean konec = false;
		while(!konec) {
			if (!restOfDoc.getPages().isEmpty()) {
				os = streams.newOutputStream();
				restOfDoc = generateCustomPDF(restOfDoc, path.getLeaf(), os, brk, djvuUrl,i18nUrl);
				
				StringBuffer buffer = new StringBuffer();
				restOfDoc.getOutlineItemRoot().debugInformations(buffer, 1);
				os.close();
			} else {
				konec = true;
				break;
			}
		}
	}


	
	



	private static Document createDocument() {
		Document doc = new Document(new Rectangle(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		return doc;
	}

	public PdfPTable insertTitleAndAuthors(AbstractRenderedDocument document) throws DocumentException, IOException {
		PdfPTable pdfPTable = new PdfPTable(new float[] {1.0f});
		
		pdfPTable.setSpacingBefore(3f);
		pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.TOP);
		pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
		pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
		pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
		pdfPTable.getDefaultCell().setBorderWidth(15f);

		Font bigFont = createFont();
		bigFont.setSize(20f);
		Chunk titleChunk = new Chunk(document.getDocumentTitle(), bigFont);


		Font smallFont = createFont();
		smallFont.setSize(12f);
		StringBuffer buffer = new StringBuffer();
		String[] creatorsFromDC = DCUtils.creatorsFromDC(fedoraAccess.getDC(document.getUuidMainTitle()));
		for (String string : creatorsFromDC) {
			buffer.append(string).append('\n');
		}
		Chunk creatorsChunk = new Chunk(buffer.toString(), smallFont);
		pdfPTable.addCell(new Paragraph(titleChunk));
		pdfPTable.addCell(new Paragraph(creatorsChunk));
		
		return pdfPTable;
	}

	public void insertFirstPage(AbstractRenderedDocument model, String parentUuid, String titlePageUuid , PdfWriter pdfWriter, Document pdfDoc, String djvuUrl) throws IOException, DocumentException {
		try {
            ResourceBundle resBundle = this.resourceBundleService.getResourceBundle("base", this.localeProvider.get());
			
			//paragraph.add(DEFAULT_LOGO_IMAGE);
	        Font bigFont = createFont();
	        bigFont.setSize(48f);
	        //TODO: Change in text
	        pdfDoc.add(new Paragraph( resBundle.getString("pdf.firstpage.title"),bigFont));

            Font smallerFont = createFont();
            smallerFont.setSize(20f);
            //TODO: Change in text
            pdfDoc.add(new Paragraph( resBundle.getString("pdf.firstpage.library"),smallerFont));
            pdfDoc.add(new Paragraph(" \n"));
            pdfDoc.add(new LineSeparator());
            pdfDoc.add(new Paragraph(" \n"));
			
	        pdfDoc.add(new Paragraph(model.getDocumentTitle(), smallerFont));
            Font smallFont = createFont();
            smallFont.setSize(12f);
            StringBuffer buffer = new StringBuffer();
            String[] creatorsFromDC = DCUtils.creatorsFromDC(fedoraAccess.getDC(model.getUuidMainTitle()));
            for (String string : creatorsFromDC) {
                buffer.append(string).append('\n');
            }

            pdfDoc.add(new Paragraph(buffer.toString(), smallerFont));
            
            pdfDoc.add(new Paragraph(" \n"));
            pdfDoc.add(new Paragraph(" \n"));

	        
//			PdfPTable pdfPTable = new PdfPTable(new float[] {0.2f, 0.8f});
//			pdfPTable.setSpacingBefore(3f);
//
//			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.TOP);
//			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
//			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
//			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
//			pdfPTable.getDefaultCell().setBorderWidth(15f);
//
//			
//			insertTitleImage(pdfPTable,model, djvuUrl);
//			pdfPTable.addCell(insertTitleAndAuthors(model));
//			
//			
//			final float[] mheights = new float[2];
//			pdfPTable.setTableEvent(new PdfPTableEvent() {
//				@Override
//				public void tableLayout(PdfPTable arg0, float[][] widths, float[] heights, int arg3, int rowStart, PdfContentByte[] arg5) {
//					mheights[0] = heights[0];
//					mheights[1] = heights[1];
//				}
//			});
//			pdfDoc.add(pdfPTable);
//			
//			lineInFirstPage(pdfWriter, pdfDoc, mheights[1]);
//
//			pdfDoc.add(new Paragraph(" "));
//			pdfDoc.add(new Paragraph(" "));
			
			Paragraph parDesc = new Paragraph(this.textsService.getText("first_page", localeProvider.get()), createFont());		
			pdfDoc.add(parDesc);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	

	private  void lineInFirstPage(PdfWriter pdfWriter, Document document,
			float y) {
		PdfContentByte cb = pdfWriter.getDirectContent();

		cb.moveTo(5f, y-10);
		cb.lineTo(document.getPageSize().getWidth()-10, y - 10);
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
			if (fos != null) fos.close();
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
	
	
	
	public void insertOutlinedTextPage(TextPage page, PdfWriter pdfWriter, Document document, String title, String i18nUrl) throws XPathExpressionException, IOException, DocumentException, TransformerException {
		File styleSheet = prepareXSLStyleSheet(localeProvider.get(), i18nUrl, title, page.getModel());
		String text = xslt(this.fedoraAccess, styleSheet, page.getUuid());
		
		
		BufferedReader strReader = new BufferedReader(new StringReader(text));
		StringBuffer oneChunkBuffer = new StringBuffer();
		String line = null;
		while((line = strReader.readLine()) != null) {
			SimpleTextCommands command = SimpleTextCommands.findCommand(line);
			if (command == SimpleTextCommands.LINE) {
				insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), createFont());
				document.add(new Paragraph(" "));
				VerticalPositionMark vsep = new LineSeparator();
				document.add(vsep);
				document.add(new Paragraph(" "));
				
				
				oneChunkBuffer = new StringBuffer();
				
			} else if (command == SimpleTextCommands.PARA) {
				insertPara(page, pdfWriter, document, oneChunkBuffer.toString(),createFont());
				oneChunkBuffer = new StringBuffer();
			} else if (command == SimpleTextCommands.FONT) {
				oneChunkBuffer.append(line);
				Font font = createFont();
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
		
		if (oneChunkBuffer.length() > 0 ) {
			insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), createFont());
		}
	}


	private void insertPara(TextPage page, PdfWriter pdfWriter,
			Document document, String text, Font font)
			throws DocumentException, IOException {

		text = text.trim().replace('\t',' ');
		
		Chunk chunk = new Chunk(text, font);
		chunk.setLocalDestination(page.getOutlineDestination());
		pdfWriter.setOpenAction(page.getOutlineDestination());

		Paragraph para = new Paragraph(chunk);
		document.add(para);
	}

	public void insertOutlinedImagePage(ImagePage page, PdfWriter pdfWriter, Document document, String djvuUrl) throws XPathExpressionException, IOException, DocumentException {
		String pageNumber = page.getPageNumber();
		insertImage(page.getUuid(), pdfWriter, document, 0.7f,djvuUrl);
		
		Font font = createFont();
		Chunk chunk = new Chunk(pageNumber);
		chunk.setLocalDestination(page.getOutlineDestination());
		float fontSize = chunk.getFont().getCalculatedSize();
		float chwidth = chunk.getWidthPoint();
		int choffsetx = (int) ((document.getPageSize().getWidth() - chwidth) / 2);
		int choffsety = (int) ( 10 - fontSize);

		PdfContentByte cb = pdfWriter.getDirectContent();
		cb.saveState();
		cb.beginText();
		cb.localDestination(page.getOutlineDestination(), new PdfDestination(PdfDestination.FIT));
		pdfWriter.setOpenAction(page.getOutlineDestination());
		
		cb.setFontAndSize(font.getBaseFont(), 14f);
		cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, pageNumber,choffsetx, choffsety + 10, 0);
		cb.endText();
		cb.restoreState();
	}

	private Font createFont() throws DocumentException, IOException {
		String workingDir = Constants.WORKING_DIR;
		File fontFile = new File(workingDir+File.separator+"fonts"+File.separator+"GentiumPlus-R.ttf");
		if (fontFile.exists()) {
			BaseFont bf = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.CP1250,true);
			return new Font(bf);
		} else {
			BaseFont bf = BaseFont.createFont("Helvetica", BaseFont.CP1250,true);
			return new Font(bf);
		}
		
	}

	
//	public void insertImagePage(String uuid, PdfWriter pdfWriter, Document document) throws XPathExpressionException, IOException, DocumentException { 
//		insertImage(uuid, pdfWriter, document, 1.0f);
//	}
	
	public void insertTitleImage(PdfPTable pdfPTable, AbstractRenderedDocument model, String djvuUrl) throws IOException, BadElementException, XPathExpressionException {
		try {
			String uuidToFirstPage = null;
			if ((model.getUuidTitlePage() != null) && (fedoraAccess.isImageFULLAvailable(model.getUuidTitlePage()))) {
				uuidToFirstPage = model.getUuidTitlePage();
			}
			if ((uuidToFirstPage == null) && (model.getUuidFrontCover()!=null) && (fedoraAccess.isImageFULLAvailable(model.getUuidFrontCover()))) {
				uuidToFirstPage = model.getUuidFrontCover();
				
			}
			if ((uuidToFirstPage == null) && (model.getFirstPage()!=null) && (fedoraAccess.isImageFULLAvailable(model.getFirstPage()))) {
				uuidToFirstPage = model.getFirstPage();
				
			}
			if (uuidToFirstPage != null) {
		        String imgUrl = createIMGFULL(uuidToFirstPage, djvuUrl);
				String mimetypeString = fedoraAccess.getImageFULLMimeType(uuidToFirstPage);
				ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
				if (mimetype != null) {
					float smallImage = 0.2f;
					BufferedImage javaImg = readImage(new URL(imgUrl), mimetype,0);
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
	
	public void insertImage(String uuid, PdfWriter pdfWriter , Document document, float percentage, String imgServletUrl) throws XPathExpressionException, IOException, DocumentException {
		try {
			if (fedoraAccess.isImageFULLAvailable(uuid)) {
				//bypass 
				String imgUrl = createIMGFULL(uuid, imgServletUrl);
				String mimetypeString = fedoraAccess.getImageFULLMimeType(uuid);
				ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
				if (mimetype != null) {
					BufferedImage javaImg = readImage(new URL(imgUrl), mimetype,0);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					writeImageToStream(javaImg, "jpeg", bos);

					com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bos.toByteArray());

					Float wratio = document.getPageSize().getWidth()/ javaImg.getWidth(null);
					Float hratio = document.getPageSize().getHeight()/ javaImg.getHeight(null);
					Float ratio = Math.min(wratio, hratio);
					if (percentage != 1.0) { ratio = ratio * percentage; }
					
					int fitToPageWidth = (int) (javaImg.getWidth(null) * ratio);
					int fitToPageHeight = (int) (javaImg.getHeight(null) * ratio);
					
					int offsetX = ((int)document.getPageSize().getWidth() - fitToPageWidth) / 2;
					int offsetY = ((int)document.getPageSize().getHeight() - fitToPageHeight) / 2;

					img.scaleAbsoluteHeight(ratio * img.getHeight());
					
					img.scaleAbsoluteWidth(ratio * img.getWidth());
					img.setAbsolutePosition((offsetX), document.getPageSize().getHeight() - offsetY - (ratio * img.getHeight()));
					document.add(img);
				}
			} else {
				Paragraph na = new Paragraph(textsService.getText("image_not_available", localeProvider.get()));
				document.add(na);
			}
		} catch (cz.incad.kramerius.security.SecurityException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			Paragraph na = new Paragraph(textsService.getText("security_fail", localeProvider.get()));
			document.add(na);
		}
	}
	


	
	/**
	 * Bypass url
	 * @param objectId
	 * @return
	 */
	private String createIMGFULL(String objectId, String imgServletUrl) {
	    String imgUrl = imgServletUrl +"?uuid="+objectId+"&action=GETRAW&stream="+ImageStreams.IMG_FULL.getStreamName();
		return imgUrl;
	}

	@Override
	public File templatesFolder() {
		String dirName = Constants.WORKING_DIR + File.separator + "templates";
		File dir = new File(dirName);
		if (!dir.exists()) { 
			boolean mkdirs = dir.mkdirs();
			if (!mkdirs) throw new RuntimeException("cannot create folder '"+dir.getAbsolutePath()+"'");
		}
		return dir;
	}

	public File fontsFolder() {
		String dirName = Constants.WORKING_DIR + File.separator + "fonts";
		File dir = new File(dirName);
		if (!dir.exists()) { 
			boolean mkdirs = dir.mkdirs();
			if (!mkdirs) throw new RuntimeException("cannot create folder '"+dir.getAbsolutePath()+"'");
		}
		return dir;
	}
	
}
