/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.service;

import java.io.IOException;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import org.w3c.dom.Document;

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

    public String transform(String xml, String xsltName, Locale locale) throws Exception;
    
    public String transform(String xml, String xsltName, Locale locale, Map<String, String> params) throws Exception;

    public String transform(Document xml, String xsltName) throws Exception;

    public String transform(Document xml, String xsltName, Locale locale) throws Exception;
    
    public String transform(Document xml, String xsltName, Locale locale, Map<String, String> params) throws Exception;

        public String serialize(Document xmldoc) throws Exception;

    /**
     * Folder for user defined xsls
     * @return
     */
    public File xslsFolder();

    public File xslFile(String name);


}
