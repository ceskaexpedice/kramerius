package cz.incad.kramerius.pdf;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.document.model.AbstractRenderedDocument;
import cz.incad.kramerius.pdf.impl.OutputStreams;


/**
 * Service for generating PDF
 * @author pavels
 */
public interface GeneratePDFService {
	
	/**
	 * Generate static export
	 * @param parentUUID  Starting point of static export
	 * @param outputs  OutputStreams
	 * @param brk Break
	 * @param djvuUrl URL for full images
	 * @param i18Url URL for translations
	 * @throws IOException
	 * @throws ProcessSubtreeException 
	 */
	public void fullPDFExport(ObjectPidsPath path, OutputStreams outputs, Break brk, String djvuUrl, String i18Url, int[] rect) throws IOException, ProcessSubtreeException;

	/**
	 * Generate dynamic export
	 * @param path Path from Master UUID to selected UUID,
	 * @param uuidFrom Starting page of export
	 * @param uuidTo Ending page of export
	 * @param titlePage Where is title page
	 * @param os OutputStreams
	 * @throws IOException 
	 * @throws ProcessSubtreeException 
	 */
	//public void dynamicPDFExport(List<String> path,String uuidFrom, String uuidTo, String titlePage, OutputStream os, String djvuUrl, String i18nUrl) throws IOException, ProcessSubtreeException;
	

	
	public void generateImagesSelection(String[] imagePids, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl, int[] rectangle) throws IOException, ProcessSubtreeException;

	
	public void generateParent(String requestedPid, int numberOfPages, String titlePage, OutputStream os, String imgServletUrl, String i18nUrl, int[] rect) throws IOException, ProcessSubtreeException;
	
	
	
	/**
	 * Generate custom pdf 
	 * @param doc Rendered document 
	 * @param parentUUID starting point of export
	 * @param os OutputStreams
	 * @throws IOException
	 */
	public void generateCustomPDF(AbstractRenderedDocument doc, /*String parentUUID,*/ OutputStream os, String djvuUrl, String i18nUrl, FirstPageRenderer firstPageListener) throws IOException;

	public AbstractRenderedDocument generateCustomPDF( AbstractRenderedDocument doc,  OutputStream os, Break brk, String djvuUrl, String i18nUrl, FirstPageRenderer firstPageListener) throws IOException;

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

