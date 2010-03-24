package cz.incad.kramerius.pdf.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Cell;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Image;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Line;
import com.qbizm.kramerius.ext.ontheflypdf.edi.PdfElement;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Table;
import com.qbizm.kramerius.ext.ontheflypdf.edi.TextBlock;
import com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontFamily;
import com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontStyle;
import com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontWeight;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.DefaultImageFormatDisector;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageFormatDisector;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageSuck;

import cz.incad.kramerius.pdf.PdfDocumentGeneratorPatched;
import cz.incad.kramerius.pdf.model.OutlinedImage;
import cz.incad.kramerius.pdf.model.Outline;
import cz.incad.kramerius.pdf.model.OutlineItem;

public class PDFGeneratorHelper {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(PDFGeneratorHelper.class.getName());

	private PDFGeneratorHelper() {
	}

	/**
	 * inserts a line
	 * 
	 * @param lineElem
	 *            the line to insert
	 */
	public static void insertLine(Line lineElem, PdfWriter iTextPDFWriter) {
		PdfContentByte cb = iTextPDFWriter.getDirectContent();
		int[] from = lineElem.getFromPosition();
		int[] to = lineElem.getToPosition();
		cb.moveTo(from[0], from[1]);
		cb.lineTo(to[0], to[1]);
		cb.stroke();
	}

	
	
	/**
	 * inserts an image (uses ImageSuck to get the image)
	 * 
	 * @param imgElem
	 *            the image element
	 */
	public static void insertImage(Image imgElem, PdfWriter iTextPDFWriter,
			ImageFormatDisector disector, Float jpegQuality,
			com.qbizm.kramerius.ext.ontheflypdf.edi.Document documentModel,
			Document iTextDocument) throws DocumentException {
		com.lowagie.text.Image img = null;
		// ratio for image-size decision
		float docRatio;
		float imgRatio;
		float ratio = 1;
		int positionX = 0;
		int positionY = 0;
		int x = 0;
		int y = 0;

		if (imgElem.isPositioned()) {
			positionX = imgElem.getPosition()[0];
			positionY = imgElem.getPosition()[1];
		}

		try {
			ImageSuck suck = new ImageSuck(imgElem.getHref(), imgElem
					.getWidth(), imgElem.getHeight(), iTextPDFWriter,
					disector != null ? disector
							: new DefaultImageFormatDisector());
			if (jpegQuality != null)
				suck.setJpegQuality(jpegQuality.floatValue());

			LOGGER.info("suck: " + suck.getImageWidth() + "x"
					+ suck.getImageHeight());

			if (needScale(documentModel, suck)) {
				ratio = new Float(documentModel.getHeight()).floatValue()
						/ suck.getImageHeight();
				positionX = new Float((documentModel.getWidth() - (suck
						.getImageWidth() * ratio)) / 2).intValue();
				positionY = new Float((documentModel.getHeight() - (suck
						.getImageHeight() * ratio)) / 2).intValue();

				ratio = ratio * 0.85f;
			}

			int scaledImageWidth = (int) (suck.getImageWidth() * ratio);
			int scaledImageHeight = (int) (suck.getImageHeight() * ratio);
			int offsetX = (documentModel.getWidth() - scaledImageWidth) / 2;
			int offsetY = (documentModel.getHeight() - scaledImageHeight) / 2;
			int i = 0;
			while (suck.hasImage()) {
				x = new Float((ratio * suck.getActualX())).intValue() + offsetX;
				y = new Float((ratio * suck.getActualY())).intValue() + offsetY;
				System.out.println("x and y of current part x = " + x
						+ " and y = " + y);
				img = suck.getImage();
				img.scaleAbsoluteHeight(ratio * img.height());
				img.scaleAbsoluteWidth(ratio * img.width());
				img.setAbsolutePosition((positionX + x), documentModel.getHeight() - (positionY + y) - (ratio * img.height()));
				iTextDocument.add(img);
			}


		} catch (MalformedURLException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	
	
	public static void insertImageWithDescription(OutlinedImage imgElem,
			PdfWriter iTextPDFWriter, 
			ImageFormatDisector disector,
			Float jpegQuality,
			com.qbizm.kramerius.ext.ontheflypdf.edi.Document documentModel,
			Document iTextDocument) throws DocumentException {
		com.lowagie.text.Image img = null;
		// ratio for image-size decision
		float docRatio;
		float imgRatio;
		float ratio = 1;
		int positionX = 0;
		int positionY = 0;
		int x = 0;
		int y = 0;

		if (imgElem.isPositioned()) {
			positionX = imgElem.getPosition()[0];
			positionY = imgElem.getPosition()[1];
		}

		try {
			ImageSuck suck = new ImageSuck(imgElem.getHref(), imgElem
					.getWidth(), imgElem.getHeight(), iTextPDFWriter,
					disector != null ? disector
							: new DefaultImageFormatDisector());
			if (jpegQuality != null)
				suck.setJpegQuality(jpegQuality.floatValue());

			LOGGER.info("suck: " + suck.getImageWidth() + "x"
					+ suck.getImageHeight());

			if (needScale(documentModel, suck)) {
				ratio = new Float(documentModel.getHeight()).floatValue()
						/ suck.getImageHeight();
				positionX = new Float((documentModel.getWidth() - (suck
						.getImageWidth() * ratio)) / 2).intValue();
				positionY = new Float((documentModel.getHeight() - (suck
						.getImageHeight() * ratio)) / 2).intValue();

				ratio = ratio * 0.85f;
			}

			int scaledImageWidth = (int) (suck.getImageWidth() * ratio);
			int scaledImageHeight = (int) (suck.getImageHeight() * ratio);
			int offsetX = (documentModel.getWidth() - scaledImageWidth) / 2;
			int offsetY = (documentModel.getHeight() - scaledImageHeight) / 2;
			int i = 0;
			while (suck.hasImage()) {
				x = new Float((ratio * suck.getActualX())).intValue() + offsetX;
				y = new Float((ratio * suck.getActualY())).intValue() + offsetY;
				img = suck.getImage();
				img.scaleAbsoluteHeight(ratio * img.height());
				img.scaleAbsoluteWidth(ratio * img.width());
				img.setAbsolutePosition((positionX + x), documentModel
						.getHeight()
						- (positionY + y) - (ratio * img.height()));
				iTextDocument.add(img);
			}

			Chunk chunk = new Chunk(imgElem.getDest());
			//chunk.setLocalDestination(arg0);
			float fontSize = chunk.font().getCalculatedSize();
			float chwidth = chunk.getWidthPoint();
			int choffsetx = (int) ((documentModel.getWidth() - chwidth) / 2);
			int choffsety = (int) (documentModel.getHeight() - 10 - fontSize);
			// chunk.set

			com.lowagie.text.Font font = PDFGeneratorHelper.convertToITextFont(getDefaultDocumentFont(), "ISO-8859-1");
			PdfContentByte cb = iTextPDFWriter.getDirectContent();
			cb.saveState();
			cb.beginText();
			cb.localDestination(imgElem.getDest(), new PdfDestination(PdfDestination.FIT));
			iTextPDFWriter.setOpenAction(imgElem.getDest());
			
			cb.setFontAndSize(font.getBaseFont(), font.getCalculatedSize());

			
			cb.showTextAligned(com.lowagie.text.Element.ALIGN_LEFT, imgElem.getDest(),choffsetx, choffsety + 10, 0);
			cb.endText();
			cb.restoreState();

			
			
		} catch (MalformedURLException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	/**
	 * inserts external pdf
	 * 
	 * @param pdfElement
	 */
	public static void insertPdf(PdfElement pdfElement,
			PdfWriter iTextPDFWriter,
			com.qbizm.kramerius.ext.ontheflypdf.edi.Document documentModel) {
		PdfReader reader;
		try {
			reader = new PdfReader(pdfElement.getHref());
			PdfImportedPage page1 = iTextPDFWriter.getImportedPage(reader, 1);
			// place it in the middle of the page
			iTextPDFWriter.getDirectContent().addTemplate(page1,
					(documentModel.getWidth() - page1.getWidth()) / 2,
					(documentModel.getHeight() - page1.getHeight()) / 2);
		} catch (IOException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	private static com.qbizm.kramerius.ext.ontheflypdf.edi.font.Font getDefaultDocumentFont() {
		String fontFile = "resources" + "/" + "ext_ontheflypdf_ArialCE.ttf";

		String fontFilePath = PDFGeneratorHelper.class.getClassLoader()
				.getResource(fontFile).getPath();
		return new com.qbizm.kramerius.ext.ontheflypdf.edi.font.Font(
				fontFilePath);
	}

	/**
	 * inserts text into the pdf output
	 * 
	 * @param textElem
	 *            the text element to insert
	 */
	public static void insertText(TextBlock textElem, PdfWriter iTextPDFWriter,
			Document iTextDocument, String encoding) throws DocumentException,
			java.io.IOException {
		String msg = textElem.getText();
		com.lowagie.text.Font font = PDFGeneratorHelper.convertToITextFont(
				textElem.getFont(), encoding);
		if (textElem.isPositioned()) {
			PdfContentByte cb = iTextPDFWriter.getDirectContent();
			cb.beginText();
			cb.setFontAndSize(font.getBaseFont(), font.getCalculatedSize());
			cb.setTextMatrix(textElem.getPosition()[0],
					textElem.getPosition()[1]);
			cb.showText(msg);
			cb.endText();
		} else {
			Phrase phrase = new Phrase(msg, font);
			iTextDocument.add(phrase);
		}
	}

	/**
	 * inserts table i`nto the pdf document (recursively inserts all the inner
	 * elements found in its cells)
	 * 
	 * @param tableElem
	 *            the table element to insert
	 */
	public static void insertTable(Table tableElem, String encoding,
			PdfWriter iTextPDFWriter, Document iTextDocument)
			throws DocumentException, MalformedURLException, IOException {
		// load the table
		PdfPTable table = PDFGeneratorHelper.loadTable(tableElem, encoding,
				iTextPDFWriter);

		if (tableElem.getBorderColor() != null)
			table.getDefaultCell().setBorderColor(tableElem.getBorderColor());
		if (tableElem.getBorderWidth() != null)
			table.getDefaultCell().setBorderWidth(
					tableElem.getBorderWidth().floatValue());

		// insert the table
		if (tableElem.isPositioned()) {
			table.setTotalWidth(tableElem.getWidth().floatValue());
			table.writeSelectedRows(0, -1, tableElem.getPosition()[0],
					tableElem.getPosition()[1], iTextPDFWriter
							.getDirectContent());
		} else {
			iTextDocument.add(table);
		}
	}

	/**
	 * transforms a Table object (see {@link cz.muni.fi.xgrabovs.edi.Table})
	 * into a PdfPTable object (see {@link com.lowagie.text.pdf.PdfPTable})
	 * 
	 * @param imgElem
	 *            the table element
	 */
	public static PdfPTable loadTable(Table tableElem, String encoding,
			PdfWriter iTextPDFWriter) throws DocumentException,
			MalformedURLException, IOException {
		Font font;
		int x;
		int y;
		PdfPTable table = new PdfPTable(tableElem.getColumns());
		if (tableElem.getCellWidths() != null) {
			table.setWidths(tableElem.getCellWidths());
		}

		// permits row splitting (end of page)
		table.setSplitLate(false);

		// absolute width of the table
		if (tableElem.getWidth() != null) {
			table.setTotalWidth(tableElem.getWidth().floatValue());
			table.setLockedWidth(true);
		}

		// border properties
		if (tableElem.getBorderColor() != null)
			table.getDefaultCell().setBorderColor(tableElem.getBorderColor());
		if (tableElem.getBorderWidth() != null)
			table.getDefaultCell().setBorderWidth(
					tableElem.getBorderWidth().floatValue());

		Element elem;
		PdfPCell cell = null;

		for (int i = 0; i < tableElem.getCellCount(); i++) {
			elem = tableElem.getCell(i).getContent();
			Cell tableElemCell = tableElem.getCell(i);
			String elementClass = elem.getClass().getName();

			if (elementClass.equals(TextBlock.class.getName())) {
				// text:
				font = PDFGeneratorHelper.convertToITextFont(((TextBlock) elem)
						.getFont(), encoding);
				cell = new PdfPCell(new Phrase(((TextBlock) elem).getText(),
						font));
			} else if (elementClass.equals(Image.class.getName())) {
				throw new UnsupportedOperationException(
						"Images in table are not supported !");
				// image:
				// Images in table is not supported
				// cell = new PdfPCell();
				//
				// ImageSuck suck = new ImageSuck(((Image) elem).getHref(),
				// ((Image) elem).getWidth(), ((Image) elem).getHeight(),
				// pdfWriter, this.disector != null ? this.disector
				// : new DefaultImageFormatDisector());
				//
				// if (getJpegQuality() != null)
				// suck.setJpegQuality(getJpegQuality().floatValue());
				//
				// while (suck.hasImage()) {
				// x = suck.getActualX();
				// y = suck.getActualY();
				// img = suck.getImage();
				// img.setAbsolutePosition(x, doc.getHeight() - y
				// - (img.height()));
				// cell.addElement(img);
				// }
			} else if (elementClass.equals(Table.class.getName())) {
				// table:
				cell = new PdfPCell(loadTable((Table) elem, encoding,
						iTextPDFWriter));
			}

			if (tableElemCell.getBorderColor() != null)
				cell.setBorderColor(tableElemCell.getBorderColor());
			else if (tableElem.getBorderColor() != null)
				cell.setBorderColor(tableElem.getBorderColor());

			if (tableElemCell.getBorderWidth() != null)
				cell
						.setBorderWidth(tableElemCell.getBorderWidth()
								.floatValue());
			else if (tableElem.getBorderWidth() != null)
				cell.setBorderWidth(tableElem.getBorderWidth().floatValue());

			cell.setColspan(tableElemCell.getColspan());
			table.addCell(cell);
		}

		return table;
	}

	/**
	 * conversion: cz.muni.fi.xgrabovs.generating.font.Font -&gt;
	 * com.lowagie.text.Font
	 * 
	 * @param font
	 *            the font to tranform
	 * @return Font object
	 */
	public static Font convertToITextFont(
			com.qbizm.kramerius.ext.ontheflypdf.edi.font.Font font,
			String encoding) {
		Font retFont = null;
		BaseFont bf;

		try {
			// not external font file specified
			if (font.getFontFile() == null) {
				bf = BaseFont.createFont(font.getFamily().toString(), encoding,
						BaseFont.EMBEDDED);
			} else if (new File(font.getFontFile()).exists()) {
				// an external font file specified
				try {
					bf = BaseFont.createFont(font.getFontFile(), encoding,
							BaseFont.EMBEDDED);
				} catch (DocumentException e) {
					PdfDocumentGeneratorPatched.LOGGER.log(Level.SEVERE, e
							.getMessage(), e);
					// a rescue font
					bf = BaseFont
							.createFont(
									com.qbizm.kramerius.ext.ontheflypdf.edi.font.Font.DEFAULT_FONT_FAMILY
											.toString(), BaseFont.CP1250,
									BaseFont.NOT_EMBEDDED);
				}
			} else {
				// a rescue font
				PdfDocumentGeneratorPatched.LOGGER.severe("file not found: "
						+ font.getFontFile());
				bf = BaseFont
						.createFont(
								com.qbizm.kramerius.ext.ontheflypdf.edi.font.Font.DEFAULT_FONT_FAMILY
										.toString(), BaseFont.CP1250,
								BaseFont.NOT_EMBEDDED);
			}

			retFont = new Font(bf);
		} catch (DocumentException ex) {
			PdfDocumentGeneratorPatched.LOGGER.log(Level.SEVERE, ex
					.getMessage(), ex);
		} catch (java.io.IOException e) {
			PdfDocumentGeneratorPatched.LOGGER.log(Level.SEVERE,
					e.getMessage(), e);
		}

		/* FontFamily conversion */
		if (font.getFamily() == com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontFamily.COURIER) {
			retFont.setFamily("Courier");
		} else if (font.getFamily() == com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontFamily.HELVETICA) {
			retFont.setFamily("Helvetica");
		}/*
		 * else if(font.getFamily() ==
		 * cz.muni.fi.xgrabovs.generating.font.FontFamily.TIMES_ROMAN) {
		 * retFont.setFamily("Times Roman"); }
		 */

		/* FontStyle coversion */
		if (font.getStyle() == com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontStyle.NORMAL) {
			retFont.setStyle("normal");
		} else if (font.getStyle() == com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontStyle.ITALICA) {
			retFont.setStyle("italica");
		}

		/* FontWeight coversion */
		if (font.getWeight() == com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontWeight.NORMAL) {
			retFont.setStyle("normal");
		} else if (font.getWeight() == com.qbizm.kramerius.ext.ontheflypdf.edi.font.FontWeight.BOLD) {
			retFont.setStyle("bold");
		}

		/* size conversion */
		retFont.setSize(font.getSize());

		/* color conversion */
		retFont.setColor(font.getColor());

		return retFont;
	}

	public static boolean needScale(
			com.qbizm.kramerius.ext.ontheflypdf.edi.Document documentModel,
			ImageSuck suck) {
		return suck.getImageWidth() > documentModel.getWidth()
				|| suck.getImageHeight() > documentModel.getHeight();
	}

	public static float documentRatio(
			com.qbizm.kramerius.ext.ontheflypdf.edi.Document documentModel) {
		return new Float(documentModel.getHeight()).floatValue()
				/ documentModel.getWidth();
	}

	public static float imageRatio(ImageSuck suck) {
		return (new Float(suck.getImageHeight()).floatValue() / suck
				.getImageWidth());
	}

	public static void insertPDFOutline(Outline elem, PdfWriter iTextPDFWriter) {
		PdfContentByte cb = iTextPDFWriter.getDirectContent();
		PdfOutline pdfRoot = cb.getRootOutline();
		OutlineItem docRoot = elem.getRoot();
		RecursionPair recPair = new RecursionPair(docRoot, pdfRoot);
		Stack<RecursionPair> stack = new Stack<RecursionPair>();
		stack.push(recPair);
		while(!stack.isEmpty()) {
			RecursionPair poped = stack.pop();
			OutlineItem docOutline = poped.getDocOutline();
			OutlineItem[] chitems = docOutline.getItems();
			for (OutlineItem chitem : chitems) {
				PdfOutline pdfOutline = new PdfOutline(poped.getPdfOutline(), PdfAction.gotoLocalPage(chitem.getDestination(), false),chitem.getTitle());
				RecursionPair childRecPair = new RecursionPair(chitem, pdfOutline);
				stack.push(childRecPair);
			}
		}
	}

	
	static class RecursionPair {

		OutlineItem outline;
		PdfOutline pdfOutline;
		
		public RecursionPair(OutlineItem outline, PdfOutline pdfOutline) {
			super();
			this.outline = outline;
			this.pdfOutline = pdfOutline;
		}

		public OutlineItem getDocOutline() {
			return outline;
		}

		public PdfOutline getPdfOutline() {
			return pdfOutline;
		}
	}
//	public PdfOutline newItem(PdfOutline pdfRoot, OutlineItem docRoot) {
//		PdfOutline pdfOutline = new PdfOutline(pdfRoot, PdfAction.gotoLocalPage(docRoot.getDestination(), false),docRoot.getTitle());
//		OutlineItem[] chItems = docRoot.getItems();
//		for (OutlineItem chItem : chItems) {
//			PdfOutline pdfChroot = new 
//		}
//		return pdfOutline;
//	}
}
