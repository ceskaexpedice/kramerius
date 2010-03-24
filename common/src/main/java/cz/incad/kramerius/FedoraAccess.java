package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This service allows to access to fedora through REST-API
 * @author pavels
 */
public interface FedoraAccess {

	
	/**
	 * Returns parsed rels-ext
	 * @param uuid Object uuid
	 * @return
	 * @throws IOException 
	 */
	public Document getRelsExt(String uuid) throws IOException;

	/**
	 * Recursive processing fedora objects
	 * @param uuid UUID of top level object
	 * @param handler handler fo handling events
	 * @throws IOException
	 */
	public void processRelsExt(String uuid, RelsExtHandler handler) throws IOException;

	/**
	 * Recursive processing fedora objects
	 * @param relsExtDocument Document of top level object
	 * @param handler handler fo handling events
	 * @throws IOException
	 */
	public void processRelsExt(Document relsExtDocument, RelsExtHandler handler) throws IOException;
	

	/**
	 * Return parsed biblio mods stream
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	public Document getBiblioMods(String uuid) throws IOException;
	
	/**
	 * Returns DC stream 
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	public Document getDC(String uuid) throws IOException;
	
	
	/**
	 * Parse, find and returns all  pages 
	 * @param uuid  UUID of object
	 * @return
	 */
	public List<Element> getPages(String uuid, boolean deep) throws IOException;

	/**
	 * Find and returns all pages
	 * @param uuid UUID of object
	 * @param rootElementOfRelsExt Root element of RelsExt
	 * @return
	 * @throws IOException
	 */
	public List<Element> getPages(String uuid, Element rootElementOfRelsExt) throws IOException;


	/**
	 * Returns input stream of thumbnail
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	public InputStream getThumbnail(String uuid) throws IOException;

	Document getThumbnailProfile(String uuid) throws IOException;
	public String getThumbnailMimeType(String uuid) throws IOException, XPathExpressionException;

	/**
	 * Returns djvu image of the object
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	public InputStream getImageFULL(String uuid) throws IOException;

	public Document getImageFULLProfile(String uuid) throws IOException;

	public String getImageFULLMimeType(String uuid) throws IOException, XPathExpressionException;

	


}
