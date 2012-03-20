package org.kramerius.importmets.utils;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.kramerius.importmets.valueobj.ServiceException;
import org.w3c.dom.Document;


public class XSLTransformer {

  public static Document transform(InputStream input, InputStream stylesheet)
    throws ServiceException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    factory.setNamespaceAware(false);
    factory.setValidating(false);
    try {
      // DocumentBuilder builder = factory.newDocumentBuilder();
      StreamSource source = new StreamSource(input);
      // Document document = builder.parse(input);

      // Use a Transformer for output
      TransformerFactory tFactory = TransformerFactory.newInstance();
      StreamSource stylesource = new StreamSource(stylesheet);
      Transformer transformer = tFactory.newTransformer(stylesource);

      // DOMSource source = new DOMSource(document);
      DOMResult result = new DOMResult();
      transformer.transform(source, result);

      return (Document) result.getNode();
    } catch (TransformerConfigurationException tce) {
      throw new ServiceException(tce);
    } catch (TransformerException te) {
      throw new ServiceException(te);
    }
  }

  /**
   * Prevede xml dokument na string
   * 
   * @param document
   * @return
   * @throws ServiceException
   */
  public static String documentToString(Document document) throws ServiceException {
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      StreamResult result = new StreamResult(new StringWriter());
      DOMSource source = new DOMSource(document);
      transformer.transform(source, result);

      String xmlString = result.getWriter().toString();
      return xmlString;
    } catch (Exception e) {
      throw new ServiceException(e);
    }
  }
}