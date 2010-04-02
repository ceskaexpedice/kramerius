package cz.incad.Kramerius.backend.pdf.impl;

import static cz.incad.kramerius.FedoraNamespaces.RDF_NAMESPACE_URI;
import static cz.incad.kramerius.utils.imgs.KrameriusImageSupport.*;

import java.awt.Image;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import javax.imageio.ImageIO;
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
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfCell;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfWriter;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Page;
import com.qbizm.kramerius.ext.ontheflypdf.edi.PageLayer;
import com.qbizm.kramerius.ext.ontheflypdf.edi.TextBlock;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.DefaultImageFormatDisector;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageSuck;

import cz.incad.Kramerius.backend.impl.FedoraAccessImpl;
import cz.incad.Kramerius.backend.pdf.GeneratePDFService;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
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
	public void generatePDFOutlined(String parentUUID, List<String> uuids,String titlePage,
			OutputStream os) throws IOException {
		try {
			if (!uuids.isEmpty()) {
				Document doc = createDocument();
				PdfWriter writer = PdfWriter.getInstance(doc, os);
				doc.open();
				
				Map<String, String> mappingsJumpsToTitles = new HashMap<String, String>();
				insertFirstPage(parentUUID, titlePage == null ? uuids.get(0) : uuids.get(0), writer, doc);
				doc.newPage();
				for (String uuid : uuids) { 
					doc.newPage();
					Map<String, String> map = insertOutlinedPage(uuid,writer, doc);
					mappingsJumpsToTitles.putAll(map);
				}
				
				PdfContentByte cb = writer.getDirectContent();
				PdfOutline pdfRoot = cb.getRootOutline();
				for (String uuid : uuids) {
					String title = mappingsJumpsToTitles.get(uuid);
					PdfOutline pdfOutline = new PdfOutline(pdfRoot, PdfAction.gotoLocalPage(uuid, false),title);
				}
				
				
				
				doc.close();
				doc.close();
				os.flush();
			}
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (DocumentException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}
		
	}



	@Override
	public void generatePDFOutlined(String parentUUID, OutputStream os) throws IOException {
		org.w3c.dom.Document relsExt = this.fedoraAccess.getRelsExt(parentUUID);
		final List<String> images = new ArrayList<String>();
		final List<String> titlePages = new ArrayList<String>();

		fedoraAccess.processRelsExt(relsExt, new RelsExtHandler() {
				@Override
				public void handle(Element elm, FedoraRelationship relation,Stack<Element> processingStack) {
					try {
						String pid = elm.getAttributeNS(RDF_NAMESPACE_URI, "resource");
						PIDParser pidParse = new PIDParser(pid);
						pidParse.disseminationURI();
						String objectId = pidParse.getObjectId();
						if (relation.equals(FedoraRelationship.hasPage)) {
							images.add(0,objectId);
							if (titlePages.isEmpty()) {
								org.w3c.dom.Document biblioMods = fedoraAccess.getBiblioMods(objectId);
								Element part = XMLUtils.findElement(biblioMods.getDocumentElement(), "part", FedoraNamespaces.BIBILO_MODS_URI);
								String attribute = part.getAttribute("type");
								if ("TitlePage".equals(attribute)) {
									titlePages.add(objectId);
								}
							}
						}
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
				@Override
				public boolean accept(FedoraRelationship relation) {
					return relation.name().startsWith("has");
				}
			});
			
			generatePDFOutlined(parentUUID, images, titlePages.isEmpty() ? images.get(0) : titlePages.get(0),os);
			
	}
	


	private static Document createDocument() {
		Document doc = new Document(new Rectangle(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		return doc;
	}
	

	public void insertFirstPage(String parentUuid, String titlePageUuid, PdfWriter pdfWriter , Document document) throws IOException, DocumentException {
		try {
			PdfPTable pdfPTable = new PdfPTable(new float[] {0.2f, 0.8f});
			pdfPTable.setSpacingBefore(3f);

			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.TOP);
			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.LEFT);
			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.RIGHT);
			pdfPTable.getDefaultCell().disableBorderSide(PdfPCell.BOTTOM);
			pdfPTable.getDefaultCell().setBorderWidth(15f);

			Paragraph parMetadata = new Paragraph(TemplatesUtils.metadata(fedoraAccess, parentUuid), getFont());
			insertTitleImage(pdfPTable, titlePageUuid);
			pdfPTable.addCell(parMetadata);
			final float[] mheights = new float[2];
			pdfPTable.setTableEvent(new PdfPTableEvent() {
				@Override
				public void tableLayout(PdfPTable arg0, float[][] widths, float[] heights, int arg3, int rowStart, PdfContentByte[] arg5) {
					mheights[0] = heights[0];
					mheights[1] = heights[1];
				}
			});
			document.add(pdfPTable);
			
			lineInFirstPage(pdfWriter, document, mheights[1]);

			document.add(new Paragraph(" "));
			document.add(new Paragraph(" "));
			
			Paragraph parDesc = new Paragraph(TemplatesUtils.description(), getFont());		
			document.add(parDesc);
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private  void lineInFirstPage(PdfWriter pdfWriter, Document document,
			float y) {
		PdfContentByte cb = pdfWriter.getDirectContent();

		cb.moveTo(5f, y-10);
		cb.lineTo(document.getPageSize().width()-10, y - 10);
		cb.stroke();
	}
	

	public Map<String, String> insertOutlinedPage(String uuid, PdfWriter pdfWriter, Document document) throws XPathExpressionException, IOException, DocumentException {
		org.w3c.dom.Document dc = fedoraAccess.getDC(uuid);
		String title = DCUtils.titleFromDC(dc);
		Map<String, String> mappingJumpToTitle = new HashMap<String, String>();
		mappingJumpToTitle.put(uuid, title);
		insertImage(uuid, pdfWriter, document, 0.7f);
		
		Font font = getFont();
		Chunk chunk = new Chunk(title);
		chunk.setLocalDestination(uuid);
		float fontSize = chunk.font().getCalculatedSize();
		float chwidth = chunk.getWidthPoint();
		int choffsetx = (int) ((document.getPageSize().width() - chwidth) / 2);
		int choffsety = (int) ( 10 - fontSize);

		PdfContentByte cb = pdfWriter.getDirectContent();
		cb.saveState();
		cb.beginText();
		cb.localDestination(uuid, new PdfDestination(PdfDestination.FIT));
		pdfWriter.setOpenAction(uuid);
		
		cb.setFontAndSize(font.getBaseFont(), 14f);
		cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, title,choffsetx, choffsety + 10, 0);
		cb.endText();
		cb.restoreState();
		
		return mappingJumpToTitle;
	}

	private Font getFont() throws DocumentException, IOException {
		BaseFont bf = BaseFont.createFont("Helvetica", BaseFont.CP1250,BaseFont.NOT_EMBEDDED);
		return new Font(bf);
	}

	
	public void insertImagePage(String uuid, PdfWriter pdfWriter, Document document) throws XPathExpressionException, IOException, DocumentException { 
		insertImage(uuid, pdfWriter, document, 1.0f);
	}
	
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
				
				img.scaleAbsoluteHeight(smallImage * img.height());
				img.scaleAbsoluteWidth(smallImage * img.width());
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

				Float wratio = document.getPageSize().width()/ javaImg.getWidth(null);
				Float hratio = document.getPageSize().height()/ javaImg.getHeight(null);
				Float ratio = Math.min(wratio, hratio);
				if (percentage != 1.0) { ratio = ratio * percentage; }
				
				int fitToPageWidth = (int) (javaImg.getWidth(null) * ratio);
				int fitToPageHeight = (int) (javaImg.getHeight(null) * ratio);
				
				int offsetX = ((int)document.getPageSize().width() - fitToPageWidth) / 2;
				int offsetY = ((int)document.getPageSize().height() - fitToPageHeight) / 2;

				img.scaleAbsoluteHeight(ratio * img.height());
				
				img.scaleAbsoluteWidth(ratio * img.width());
				img.setAbsolutePosition((offsetX), document.getPageSize().height() - offsetY - (ratio * img.height()));
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
