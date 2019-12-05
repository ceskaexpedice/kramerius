package cz.incad.kramerius.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocumentException;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.impl.ImageFetcher;
import cz.incad.kramerius.pdf.impl.OutputStreams;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;


/**
 * Service for generating PDF
 * @author pavels
 * 
 * This will be redesigned in future release
 */
@Deprecated
public interface GeneratePDFService {

    /**
     * Full PDF export 
     * @param path Path of exported pid
     * @param outputs Outputs
     * @param brk Break for dividing pages into more pdfs
     * @param djvuUrl Image serlvet URL
     * @param i18Url I18N servlet URL
     * @param rect Page size
     * @throws IOException IO error has been occurred
     * @throws ProcessSubtreeException algorithm cannot traverse over tree
     * @throws DocumentException Error has been occurred in PDF generation
     */
    public void fullPDFExport(ObjectPidsPath path, OutputStreams outputs, Break brk, String djvuUrl, String i18Url, int[] rect) throws IOException, ProcessSubtreeException, DocumentException;
	
    
    /**
     * Generate PDF for title 
     * @param requestedPid Requested PID
     * @param numberOfPages Number of pages
     * @param titlePage Title page
     * @param os OutputStream
     * @param imgServletUrl IMG servlet 
     * @param i18nUrl i18N servlet 
     * @param rect Page size
     * @throws IOException IO error has been occured
     * @throws ProcessSubtreeException algorithm cannot traverse over tree
     */
	public void generateParent(String requestedPid, int numberOfPages, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl, int[] rect) throws IOException, ProcessSubtreeException;
	
	
	public void generateCustomPDF(PreparedDocument doc, /*String parentUUID,*/ OutputStream os, FontMap fmap, String djvuUrl, String i18nUrl, ImageFetcher fetcher) throws IOException;

	
	public PreparedDocument generateCustomPDF( PreparedDocument doc,  OutputStream os, Break brk,   FontMap fmap, String djvuUrl, String i18nUrl, ImageFetcher fetcher) throws IOException;

	
	
	/**
	 * Folder for templates
	 * @return templates folder 
	 */
	public File templatesFolder();

	/**
	 * Folder for fonts
	 * @return fonts folder
	 */
	public File fontsFolder();

	//TODO: move
	/**
	 * Initialization method
	 */
	public void init() throws IOException;
}

