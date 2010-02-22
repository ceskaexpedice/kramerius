package cz.incad.kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
	 * Parse, find and returns all  pages 
	 * @param uuid  UUID of object
	 * @return
	 */
	public List<Element> getPages(String uuid) throws IOException;

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

	/**
	 * Returns djvu image of the object
	 * @param uuid
	 * @return
	 * @throws IOException
	 */
	public InputStream getDJVU(String uuid) throws IOException;

}
