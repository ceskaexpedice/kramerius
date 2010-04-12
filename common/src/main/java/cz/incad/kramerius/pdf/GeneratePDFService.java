package cz.incad.kramerius.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import cz.incad.kramerius.pdf.pdfpages.AbstractRenderedDocument;


/**
 * Service for generating PDF
 * @author pavels
 */
public interface GeneratePDFService {
	
	/**
	 * Vygeneruje cely obsah do jednoho outputstreamu
	 * @param parentUUID
	 * @param os
	 * @throws IOException
	 */
	public void fullPDFExport(String parentUUID, OutputStream os) throws IOException;

	/**
	 * Vygeneruje jenom zadane stranky
	 * @param path TODO
	 * @param uuidFrom
	 * @param uuidTo
	 * @param titlePage
	 * @param os
	 * @throws IOException
	 */
	public void dynamicPDFExport(List<String> path,String uuidFrom, String uuidTo, String titlePage, OutputStream os) throws IOException;
	
	/**
	 * Vygeneruje vlastni strukturu
	 * @param doc
	 * @param parentUUID
	 * @param os
	 * @throws IOException
	 */
	public void generateCustomPDF(AbstractRenderedDocument doc, String parentUUID, OutputStream os) throws IOException;

	public void generateCustomPDF(AbstractRenderedDocument doc, String parentUUID, OutputStream os, Break brk) throws IOException;
}

