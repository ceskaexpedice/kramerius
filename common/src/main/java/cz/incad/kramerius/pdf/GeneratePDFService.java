package cz.incad.kramerius.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import cz.incad.kramerius.pdf.impl.OutputStreams;
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
	public void fullPDFExport(String parentUUID, OutputStreams outputs, Break brk, String djvuUrl, String i18Url) throws IOException;

	/**
	 * Vygeneruje jenom zadane stranky
	 * @param path TODO
	 * @param uuidFrom
	 * @param uuidTo
	 * @param titlePage
	 * @param os
	 * @throws IOException
	 */
	public void dynamicPDFExport(List<String> path,String uuidFrom, String uuidTo, String titlePage, OutputStream os, String djvuUrl, String i18nUrl) throws IOException;
	
	/**
	 * Vygeneruje vlastni strukturu
	 * @param doc
	 * @param parentUUID
	 * @param os
	 * @throws IOException
	 */
	public void generateCustomPDF(AbstractRenderedDocument doc, String parentUUID, OutputStream os, String djvuUrl, String i18nUrl) throws IOException;

	public AbstractRenderedDocument generateCustomPDF(AbstractRenderedDocument doc, String parentUUID, OutputStream os, Break brk, String djvuUrl, String i18nUrl) throws IOException;

	public File templatesFolder();
}

