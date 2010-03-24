package cz.incad.kramerius.pdf;

import static cz.incad.kramerius.pdf.utils.PDFGeneratorHelper.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.qbizm.kramerius.ext.ontheflypdf.edi.EdiException;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Element;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Image;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Line;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Page;
import com.qbizm.kramerius.ext.ontheflypdf.edi.PageLayer;
import com.qbizm.kramerius.ext.ontheflypdf.edi.PdfElement;
import com.qbizm.kramerius.ext.ontheflypdf.edi.Table;
import com.qbizm.kramerius.ext.ontheflypdf.edi.TextBlock;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageFormatDisector;
import com.qbizm.kramerius.ext.ontheflypdf.edi.generating.ImageSuck;

import cz.incad.kramerius.pdf.model.OutlinedImage;
import cz.incad.kramerius.pdf.model.Outline;
import cz.incad.kramerius.pdf.utils.PDFGeneratorHelper;

/**
 * This class is copy of previous PdfDocumentGenerator with some small changes:
 * <ul>
 * <li>changed commons logger to standard JDK logger</li>
 * <li>support plugguable image format disecting</li>
 * <li>outline support
 * <li>
 * </ul>
 * 
 * Old comment: class for exporting {@link cz.muni.fi.xgrabovs.edi.Document} to
 * PDF, uses iText for PDF generating, and javaDjVu for DjVu -&gt;
 * java.awt.Image conversion
 * 
 * @see <a href='http://www.lowagie.com/iText/'>iText</a>
 * @see <a href='http://javadjvu.foxtrottechnologies.com/'>JavaDjVu</a>
 * 
 * @author Miroslav Gaborsky
 * @author Pavel Stastny
 */
public class PdfDocumentGeneratorPatched {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(PdfDocumentGeneratorPatched.class.getName());

	private static final String DEFAULT_ENCODING = "ISO-8859-1";


	private PdfWriter iTextPDFWriter;
	private Document iTextDocument;

	private String encoding;
	private Float jpegQuality;
	private com.qbizm.kramerius.ext.ontheflypdf.edi.Document documentModel;
	private ImageFormatDisector disector;

	/**
	 * constructor
	 * 
	 * @param doc
	 */
	public PdfDocumentGeneratorPatched(
			com.qbizm.kramerius.ext.ontheflypdf.edi.Document doc) {
		this.documentModel = doc;
		setEncoding(DEFAULT_ENCODING);
	}

	/**
	 * transforms the document into a pdf output with specified encoding and
	 * jpeg quality (jpegQuality is used only if there are some DjVu images)
	 * 
	 * @param out
	 *            the output stream
	 * @param encoding
	 *            demanded encoding
	 * @param jpegQuality
	 *            demanded jpeg quality
	 */
	public void generateDocument(java.io.OutputStream out, String encoding,
			float jpegQuality) throws EdiException {
		setEncoding(encoding);
		setJpegQuality(new Float(jpegQuality));
		generateDocument(out);
	}

	/**
	 * transforms the document into a pdf output with specified encoding
	 * 
	 * @param out
	 *            the output stream
	 * @param encoding
	 *            demanded encoding
	 */
	public void generateDocument(java.io.OutputStream out, String encoding)
			throws EdiException {
		setEncoding(encoding);
		generateDocument(out);
	}

	
	public void generateOutlinedDocument(OutputStream out) throws EdiException {
		try {
			iTextPDFWriter = PdfWriter.getInstance(iTextDocument, out);
			this.documentHeader(iTextDocument, documentModel);
			iTextDocument.open();
			Element elem;
			for (int i = 0; i < documentModel.getPagesCount(); i++) {
				PageLayer layer = documentModel.getPage(i).getPageLayer(Page.DEFAULT_LAYER_INDEX);
				iTextDocument.newPage();
				for (int j = 0; j < layer.getElementsCount(); j++) {
					elem = layer.getElement(j);
					fillStandardElements(elem);
					//fillOutlineElements(elem);
				}
			}
			iTextDocument.close();
			iTextPDFWriter.close();
			out.flush();
		} catch (DocumentException e) {
			throw new EdiException(e.getMessage());
		} catch (IOException ex) {
			throw new EdiException(ex.getMessage());
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}



	/**
	 * transforms the document to a pdf output
	 * @param out the output stream
	 */
	public void generateDocument(java.io.OutputStream out) throws EdiException {
		initialization();
		try {
			iTextPDFWriter = PdfWriter.getInstance(iTextDocument, out);
			this.documentHeader(iTextDocument, documentModel);
			iTextDocument.open();
			Element elem;
			for (int i = 0; i < documentModel.getPagesCount(); i++) {
				PageLayer layer = documentModel.getPage(i).getPageLayer(
						Page.DEFAULT_LAYER_INDEX);
				iTextDocument.newPage();
				for (int j = 0; j < layer.getElementsCount(); j++) {
					elem = layer.getElement(j);
					fillStandardElements(elem);
				}

				/*
				 * // hidden text - sublayer - not supported yet ...
				 * if(doc.getPage(i).getPageLayersCount() > 1) { layer =
				 * doc.getPage(i).getPageLayer(1); //sublayer - hidden text
				 * PdfContentByte cbu = pdfWriter.getDirectContentUnder();
				 * cbu.beginText(); BaseFont bf =
				 * BaseFont.createFont(BaseFont.HELVETICA, getEncoding(),
				 * BaseFont.EMBEDDED); cbu.setFontAndSize(bf, 12);
				 * cbu.setColorFill(this.hiddenTextColor); for(int j = 0; j <
				 * layer.getElementsCount(); j++){ elementClass =
				 * layer.getElement(j).getClass().getName();
				 * if(elementClass.equals(TextBlock.class.getName())) { textElem
				 * = (TextBlock) layer.getElement(j); msg = textElem.getText();
				 * cbu.setTextMatrix(10, 400); cbu.showText(msg); } }
				 * cbu.endText(); }
				 */
			}

			iTextDocument.close();
			iTextPDFWriter.close();

			out.flush();

		} catch (DocumentException e) {
			throw new EdiException(e.getMessage());
		} catch (IOException ex) {
			throw new EdiException(ex.getMessage());
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
	}



	private void fillStandardElements(Element elem) throws DocumentException,
			IOException, MalformedURLException {
		if (elem instanceof TextBlock) {
			// text:
			insertText((TextBlock) elem, iTextPDFWriter, iTextDocument, getEncoding());
		} else if (elem instanceof Image) {
			// image:
			insertImage((Image) elem, this.iTextPDFWriter, this.disector, getJpegQuality(), documentModel, iTextDocument);
		} else if (elem instanceof Table) {
			// table:
			insertTable((Table) elem,encoding, iTextPDFWriter, iTextDocument);
		} else if (elem instanceof Line) {
			// line:
			insertLine((Line) elem, iTextPDFWriter);
		} else if (elem instanceof PdfElement) {
			// external pdf:
			insertPdf((PdfElement) elem, iTextPDFWriter, this.documentModel);
		} else	if (elem instanceof OutlinedImage) {
			// img with desc
			insertImageWithDescription((OutlinedImage)elem, this.iTextPDFWriter, this.disector, getJpegQuality(), this.documentModel, this.iTextDocument);
		} else if (elem instanceof Outline) {
			insertPDFOutline((Outline) elem, this.iTextPDFWriter);
		}
	}

	private void initialization() {
		PageLayer layer = null;

		// X server independence
		System.setProperty("java.awt.headless", "true");

		// init iText Document object:
		iTextDocument = new Document(new Rectangle(documentModel.getWidth(), documentModel.getHeight()));
	}

	/**
	 * set meta information
	 * 
	 * @param document
	 *            the result document
	 * @param doc
	 *            the source document
	 */
	private void documentHeader(Document document,com.qbizm.kramerius.ext.ontheflypdf.edi.Document doc) {
		document.addAuthor(doc.getAuthor());
		document.addCreator(doc.getCreator());
		document.addCreationDate();
		document.addTitle(doc.getTitle());
		document.addKeywords(doc.getKeywords());
		document.addSubject(doc.getSubject());
	}

	/**
	 * get encoding
	 * 
	 * @return encoding of the document
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * set encoding of the document
	 * 
	 * @param encoding
	 *            the encoding
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * set jpeg quality for transforming a DjVu image (see
	 * {@link ImageSuck.getImage})
	 * 
	 * @param jpegQuality
	 *            the quality (0..1)
	 */
	private void setJpegQuality(Float jpegQuality) {
		this.jpegQuality = jpegQuality;
	}

	/**
	 * get jpeg quality used in DjVu transformation
	 * 
	 * @return the value
	 */
	private Float getJpegQuality() {
		return jpegQuality;
	}

	/**
	 * Returns current image format disector
	 * 
	 * @return
	 */
	public ImageFormatDisector getDisector() {
		return disector;
	}

	/**
	 * Sets new image format disector
	 * 
	 * @param disector
	 */
	public void setDisector(ImageFormatDisector disector) {
		this.disector = disector;
	}
}