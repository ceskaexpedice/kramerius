/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.service;

import java.io.IOException;
import java.io.File;

/**
 *
 * @author Alberto
 */
public interface XSLService {
public boolean isAvailable(String name);

	/**
	 * Return localized texts
	 * @param name Name of text
	 * @return
	 * @throws IOException
	 */
	public String getXSL(String name) throws IOException;

	public String transform(String xml, String xsltName) throws Exception;



	/**
	 * Folder for user defined xsls
	 * @return
	 */
	public File xslsFolder();


}
