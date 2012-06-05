package cz.incad.kramerius.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocumentException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.impl.OutputStreams;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;


/**
 * Service for generating PDF
 * @author pavels
 */
public interface GeneratePDFService {
	

    /**
     * Full PDF export 
     * @param path Path of exported pid
     * @param outputs Outputs
     * @param brk Break for dividing pages into more pdfs
     * @param djvuUrl Image serlvet URL
     * @param i18Url I18N servlet URL
     * @param rect Page sizes
     */
    public void fullPDFExport(ObjectPidsPath path, OutputStreams outputs, Break brk, String djvuUrl, String i18Url, int[] rect) throws IOException, ProcessSubtreeException, DocumentException;
	
    
    
	public void generateParent(String requestedPid, int numberOfPages, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl, int[] rect) throws IOException, ProcessSubtreeException;
	
	
	public void generateCustomPDF(AbstractRenderedDocument doc, /*String parentUUID,*/ OutputStream os, FontMap fmap, String djvuUrl, String i18nUrl, ImageFetcher fetcher) throws IOException;

	
	public AbstractRenderedDocument generateCustomPDF( AbstractRenderedDocument doc,  OutputStream os, Break brk,   FontMap fmap, String djvuUrl, String i18nUrl, ImageFetcher fetcher) throws IOException;

	
	
	/**
	 * Folder for templates
	 * @return
	 */
	public File templatesFolder();

	/**
	 * Folder for fonts
	 * @return
	 */
	public File fontsFolder();

	//TODO: move
	public void init() throws IOException;
}

