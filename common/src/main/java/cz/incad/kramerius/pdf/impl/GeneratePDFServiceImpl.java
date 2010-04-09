package cz.incad.kramerius.pdf.impl;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;
import static cz.incad.kramerius.utils.BiblioModsUtils.getPageNumber;
import static cz.incad.kramerius.utils.BiblioModsUtils.getTitle;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.readImage;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.writeImageToStream;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import com.google.inject.Inject;
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

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.pdfpages.AbstractPage;
import cz.incad.kramerius.pdf.pdfpages.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.pdfpages.ImagePage;
import cz.incad.kramerius.pdf.pdfpages.OutlineItem;
import cz.incad.kramerius.pdf.pdfpages.RenderedDocument;
import cz.incad.kramerius.pdf.pdfpages.TextPage;
import cz.incad.kramerius.utils.DCUtils;
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

	@Inject
	FedoraAccess fedoraAccess;
	@Inject
	KConfiguration configuration;
	
	
	@Override
	public void generateCustomPDF(AbstractRenderedDocument rdoc, String parentUUID, OutputStream os) throws IOException {
		try {
			Document doc = createDocument();
			PdfWriter writer = PdfWriter.getInstance(doc, os);
			doc.open();
			insertFirstPage(rdoc, parentUUID, rdoc.getUuidTitlePage(), writer, doc);
			doc.newPage();
			for (AbstractPage page : rdoc.getPages()) {
				doc.newPage();
				if (page instanceof ImagePage) {
					ImagePage iPage = (ImagePage) page;
					insertOutlinedImagePage(iPage, writer, doc);
				} else {
					TextPage tPage = (TextPage) page;
					insertOutlinedTextPage(tPage, writer, doc, rdoc.getDocumentTitle());
				}
			}

			PdfContentByte cb = writer.getDirectContent();
			PdfOutline pdfRoot = cb.getRootOutline();
			OutlineItem rDocRoot = rdoc.getOutlineItemRoot();
			fillOutline(pdfRoot, rDocRoot);

			doc.close();
			doc.close();
			os.flush();

		} catch (DocumentException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (XPathExpressionException e) {
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
	public void dynamicPDFExport(List<String> path, String uuidFrom, String uuidTo, String titlePage, OutputStream os) throws IOException {
		if (!path.isEmpty()) {
			String lastUuid = path.get(path.size() -1);

			org.w3c.dom.Document relsExt = this.fedoraAccess.getRelsExt(lastUuid);
			KrameriusModels model = this.fedoraAccess.getKrameriusModel(relsExt);
			
			final AbstractRenderedDocument renderedDocument = new RenderedDocument(model, lastUuid);
			renderedDocument.setDocumentTitle(DCUtils.titleFromDC(this.fedoraAccess.getDC(lastUuid)));
			renderedDocument.setUuidTitlePage(titlePage);
			renderedDocument.setUuidMainTitle(path.get(0));
			
			buildRenderingDocumentAsFlat(relsExt, renderedDocument, uuidFrom, uuidTo);
			generateCustomPDF(renderedDocument, lastUuid,os);

		}
	}


	@Override
	public void fullPDFExport(String parentUUID, OutputStream os) throws IOException {
		org.w3c.dom.Document relsExt = this.fedoraAccess.getRelsExt(parentUUID);
		KrameriusModels model = this.fedoraAccess.getKrameriusModel(relsExt);
		
		final AbstractRenderedDocument renderedDocument = new RenderedDocument(model, parentUUID);
		renderedDocument.setDocumentTitle(DCUtils.titleFromDC(this.fedoraAccess.getDC(parentUUID)));
		renderedDocument.setUuidMainTitle(parentUUID);
		
		TextPage dpage = new TextPage(model, parentUUID);
		dpage.setOutlineDestination("desc");
		dpage.setOutlineTitle("Popis");
		renderedDocument.addPage(dpage);
		OutlineItem item = new OutlineItem();
		item.setLevel(1); item.setParent(renderedDocument.getOutlineItemRoot()); 
		item.setTitle("Popis"); item.setDestination("desc");
		renderedDocument.getOutlineItemRoot().addChild(item);
		
		buildRenderingDocumentAsTree(relsExt, renderedDocument);
		generateCustomPDF(renderedDocument, parentUUID,os);
	}


	
	private void buildRenderingDocumentAsFlat(org.w3c.dom.Document relsExt, final AbstractRenderedDocument renderedDocument, final String uuidFrom, final String uuidTo ) throws IOException {
		fedoraAccess.processRelsExt(relsExt, new RelsExtHandler() {
	
			private boolean acceptingState = false;
			
			@Override
			public boolean accept(FedoraRelationship relation) {
				return relation == FedoraRelationship.hasPage;
			}

			@Override
			public void handle(Element elm, FedoraRelationship relation, int level) {
				if (relation == FedoraRelationship.hasPage) {
					try {
						String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
						PIDParser pidParse = new PIDParser(pid);
						pidParse.disseminationURI();
						String objectId = pidParse.getObjectId();
						if (!acceptingState) {
							if (objectId.equals(uuidFrom)) {
								acceptingState = true;
								renderedDocument.addPage(createPage(renderedDocument, elm, relation));
							}
						} else {
							if (objectId.equals(uuidTo)) {
								acceptingState = false;
							}
							renderedDocument.addPage(createPage(renderedDocument, elm, relation));
						}
						
					} catch (LexerException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
			
		});
	}
	
	private void buildRenderingDocumentAsTree(org.w3c.dom.Document relsExt,
			final AbstractRenderedDocument renderedDocument ) throws IOException {
		fedoraAccess.processRelsExt(relsExt, new RelsExtHandler() {
			
				private int previousLevel = -1;
				private OutlineItem currOutline = null;
				
				@Override
				public void handle(Element elm, FedoraRelationship relation, int level) {
					try {
						AbstractPage page = createPage(renderedDocument, elm, relation);
						renderedDocument.addPage(page);
						if (previousLevel == -1) {
							// first
							this.currOutline = createOutlineItem(renderedDocument.getOutlineItemRoot(), page.getOutlineDestination(), page.getOutlineTitle(), level);
							StringBuffer buffer = new StringBuffer();
							this.currOutline.debugInformations(buffer, 0);
						} else if (previousLevel == level) {
							this.currOutline = this.currOutline.getParent();
							this.currOutline = createOutlineItem(this.currOutline, page.getOutlineDestination(), page.getOutlineTitle(), level);

							StringBuffer buffer = new StringBuffer();
							this.currOutline.debugInformations(buffer, 0);

						} else if (previousLevel < level) {
							// dolu
							this.currOutline = createOutlineItem(this.currOutline, page.getOutlineDestination(), page.getOutlineTitle(), level);

							StringBuffer buffer = new StringBuffer();
							this.currOutline.debugInformations(buffer, 0);

						} else if (previousLevel > level) {
							// nahoru // za poslednim smerem nahoru
							this.currOutline = this.currOutline.getParent();
							
							StringBuffer buffer = new StringBuffer();
							this.currOutline.debugInformations(buffer, 0);
							
							this.currOutline = this.currOutline.getParent();
							this.currOutline = createOutlineItem(this.currOutline, page.getOutlineDestination(), page.getOutlineTitle(), level);
							
						}

						previousLevel = level;
					} catch (DOMException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
						throw new RuntimeException(e);
					} catch (LexerException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
						throw new RuntimeException(e);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
						throw new RuntimeException(e);
					}
				}


				private OutlineItem createOutlineItem(OutlineItem parent, String objectId, String biblioModsTitle, int level) {
					OutlineItem item = new OutlineItem();
					item.setDestination(objectId);
					item.setTitle(biblioModsTitle);
					
					parent.addChild(item);
					item.setParent(parent);
					item.setLevel(level);
					return item;
				}
				
				@Override
				public boolean accept(FedoraRelationship relation) {
					return relation.name().startsWith("has");
				}
			});
	}
	

	protected AbstractPage createPage(
			final AbstractRenderedDocument renderedDocument,
			Element elm, FedoraRelationship relation)
			throws LexerException, IOException {
		String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
		PIDParser pidParse = new PIDParser(pid);
		pidParse.disseminationURI();
		String objectId = pidParse.getObjectId();
		
		org.w3c.dom.Document biblioMods = fedoraAccess.getBiblioMods(objectId);
		AbstractPage page = null;
		if (relation.equals(FedoraRelationship.hasPage)) {
			
			page = new ImagePage(KrameriusModels.PAGE, objectId);
			page.setOutlineDestination(objectId);
			String pageNumber = getPageNumber(biblioMods);
			if (pageNumber.trim().equals("")) {
				throw new IllegalStateException(objectId);
			}
			page.setOutlineTitle(pageNumber);
			//renderedDocument.addPage(page);
			
			if (renderedDocument.getUuidTitlePage() == null) {
				Element part = XMLUtils.findElement(biblioMods.getDocumentElement(), "part", FedoraNamespaces.BIBILO_MODS_URI);
				String attribute = part.getAttribute("type");
				if ("TitlePage".equals(attribute)) {
					renderedDocument.setUuidTitlePage(objectId);
				}
			}
			
		} else {
			page = new TextPage(relation.getPointingModel(), objectId);
			page.setOutlineDestination(objectId);
			page.setOutlineTitle(getTitle(biblioMods));
			//renderedDocument.addPage(page);
		}
		return page;
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

		Font bigFont = getFont();
		bigFont.setSize(20f);
		Chunk titleChunk = new Chunk(document.getDocumentTitle(), bigFont);


		Font smallFont = getFont();
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

	public void insertFirstPage(AbstractRenderedDocument model, String parentUuid, String titlePageUuid , PdfWriter pdfWriter, Document pdfDoc) throws IOException, DocumentException {
		try {
			PdfPTable pdfPTable = new PdfPTable(new float[] {0.2f, 0.8f});
			pdfPTable.setSpacingBefore(3f);

			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.TOP);
			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
			pdfPTable.getDefaultCell().setBorderWidth(15f);

			
			insertTitleImage(pdfPTable, titlePageUuid);
			pdfPTable.addCell(insertTitleAndAuthors(model));
			
			final float[] mheights = new float[2];
			pdfPTable.setTableEvent(new PdfPTableEvent() {
				@Override
				public void tableLayout(PdfPTable arg0, float[][] widths, float[] heights, int arg3, int rowStart, PdfContentByte[] arg5) {
					mheights[0] = heights[0];
					mheights[1] = heights[1];
				}
			});
			pdfDoc.add(pdfPTable);
			
			lineInFirstPage(pdfWriter, pdfDoc, mheights[1]);

			pdfDoc.add(new Paragraph(" "));
			pdfDoc.add(new Paragraph(" "));
			
			Paragraph parDesc = new Paragraph(TemplatesUtils.description(), getFont());		
			pdfDoc.add(parDesc);
		} catch (XPathExpressionException e) {
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
	
	public void insertOutlinedTextPage(TextPage page, PdfWriter pdfWriter, Document document, String title) throws XPathExpressionException, IOException, DocumentException {
		String text = TemplatesUtils.textPage(this.fedoraAccess, page.getUuid(), page.getModel(), title);
		BufferedReader strReader = new BufferedReader(new StringReader(text));
		
		StringBuffer oneChunkBuffer = new StringBuffer();
		String line = null;
		while((line = strReader.readLine()) != null) {
			SimpleTextCommands command = SimpleTextCommands.findCommand(line);
			if (command == SimpleTextCommands.LINE) {
				insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), getFont());
				document.add(new Paragraph(" "));
				VerticalPositionMark vsep = new LineSeparator();
				document.add(vsep);
				document.add(new Paragraph(" "));
				
				
				oneChunkBuffer = new StringBuffer();
				
			} else if (command == SimpleTextCommands.PARA) {
				insertPara(page, pdfWriter, document, oneChunkBuffer.toString(),getFont());
				oneChunkBuffer = new StringBuffer();
			} else if (command == SimpleTextCommands.FONT) {
				oneChunkBuffer.append(line);
				Font font = getFont();
				String oneChunkText = oneChunkBuffer.toString();
				Map<String, String> parameters = command.parameters(oneChunkText);
				if (parameters.containsKey("size")) {
					font.setSize(Integer.parseInt(parameters.get("size")));
				}
				insertPara(page, pdfWriter, document, oneChunkText.substring(0, command.indexStart(oneChunkText)), font);
				oneChunkBuffer = new StringBuffer();
			} else {
				oneChunkBuffer.append(line).append("\n");
			}
		}
		
		if (oneChunkBuffer.length() > 0 ) {
			insertPara(page, pdfWriter, document, oneChunkBuffer.toString(), getFont());
		}
	}


	private void insertPara(TextPage page, PdfWriter pdfWriter,
			Document document, String text, Font font)
			throws DocumentException, IOException {

		System.out.println("Emmiting text '"+text+"'");
		
		Chunk chunk = new Chunk(text, font);
		chunk.setLocalDestination(page.getOutlineDestination());
		pdfWriter.setOpenAction(page.getOutlineDestination());

		Paragraph para = new Paragraph(chunk);
		document.add(para);
	}

	public void insertOutlinedImagePage(ImagePage page, PdfWriter pdfWriter, Document document) throws XPathExpressionException, IOException, DocumentException {
		String title = page.getOutlineTitle();
		insertImage(page.getUuid(), pdfWriter, document, 0.7f);
		
		Font font = getFont();
		Chunk chunk = new Chunk(title);
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
		cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, title,choffsetx, choffsety + 10, 0);
		cb.endText();
		cb.restoreState();
	}

	private Font getFont() throws DocumentException, IOException {
		BaseFont bf = BaseFont.createFont("Helvetica", BaseFont.CP1250,BaseFont.NOT_EMBEDDED);
		return new Font(bf);
	}

	
//	public void insertImagePage(String uuid, PdfWriter pdfWriter, Document document) throws XPathExpressionException, IOException, DocumentException { 
//		insertImage(uuid, pdfWriter, document, 1.0f);
//	}
	
	public void insertTitleImage(PdfPTable pdfPTable, String uuid) throws IOException, BadElementException, XPathExpressionException {
		String imgUrl = createIMGFULL(uuid);
		if (fedoraAccess.isImageFULLAvailable(uuid)) {
			String mimetypeString = fedoraAccess.getImageFULLMimeType(uuid);
			ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
			if (mimetype != null) {
				float smallImage = 0.2f;
				Image javaImg = readImage(new URL(imgUrl), mimetype);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				writeImageToStream(javaImg, "jpeg", bos);

				com.lowagie.text.Image img = com.lowagie.text.Image.getInstance(bos.toByteArray());
				System.out.println(img.getClass().getResource("Image.class"));
				
				img.scaleAbsoluteHeight(smallImage * img.getHeight());
				img.scaleAbsoluteWidth(smallImage * img.getWidth());
				pdfPTable.addCell(img);
			}
		} else {
			pdfPTable.addCell(" - ");
		}
	}
	
	public void insertImage(String uuid, PdfWriter pdfWriter , Document document, float percentage) throws XPathExpressionException, IOException, DocumentException {
		if (fedoraAccess.isImageFULLAvailable(uuid)) {
			//bypass 
			String imgUrl = createIMGFULL(uuid);
			String mimetypeString = fedoraAccess.getImageFULLMimeType(uuid);
			ImageMimeType mimetype = ImageMimeType.loadFromMimeType(mimetypeString);
			if (mimetype != null) {
				Image javaImg = readImage(new URL(imgUrl), mimetype);
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
			Paragraph na = new Paragraph("NA");
			document.add(na);
		}
	}
	

	private boolean containsDJVU(String uuid) throws IOException {
		try {
			InputStream djvu = null;
			try {
				djvu = fedoraAccess.getImageFULL(uuid);
				return djvu != null;
			}finally {
				if (djvu != null) { djvu.close(); }
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

	
	/**
	 * Bypass url
	 * @param objectId
	 * @return
	 */
	private String createIMGFULL(String objectId) {
		String imgUrl = this.configuration.getDJVUServletUrl() +"?uuid="+objectId+"&outputFormat=RAW";
		return imgUrl;
	}
	
	
}
