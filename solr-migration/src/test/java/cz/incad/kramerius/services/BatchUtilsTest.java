package cz.incad.kramerius.services;

import cz.incad.kramerius.service.MigrateSolrIndexException;
import cz.incad.kramerius.utils.XMLUtils;
import junit.framework.TestCase;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;

import static cz.incad.kramerius.services.BatchUtils.*;

public class BatchUtilsTest extends TestCase {

    public void testDefaultTransform() throws IOException, SAXException, ParserConfigurationException, TransformerException, MigrateSolrIndexException {
        // nekopirovat text, spravne
        Document feedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element add = feedDocument.createElement("add");
        Element destDoc = feedDocument.createElement("doc");
        add.appendChild(destDoc);
        feedDocument.appendChild(add);

        InputStream resourceAsStream = BatchUtilsTest.class.getResourceAsStream("solr.xml");
        Assert.assertNotNull(resourceAsStream);

        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element doc = XMLUtils.findElement(parsed.getDocumentElement(), "doc");
        transform(doc, feedDocument,destDoc);

        // must be defined as copiied field
        Element text = BatchUtils.findByAttribute(destDoc, "text");
        Assert.assertNull(text);

        Element text_lemmatized = BatchUtils.findByAttribute(destDoc, "text_lemmatized");
        Assert.assertNull(text_lemmatized);

        Element text_lemmatized_ascii = BatchUtils.findByAttribute(destDoc, "text_lemmatized_ascii");
        Assert.assertNull(text_lemmatized_ascii);

        Element text_lemmatized_nostopwords = BatchUtils.findByAttribute(destDoc, "text_lemmatized_nostopwords");
        Assert.assertNull(text_lemmatized_nostopwords);
    }

    public void testPdfNoTextOcrTransform() throws IOException, SAXException, ParserConfigurationException, TransformerException, MigrateSolrIndexException {
        Document feedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element add = feedDocument.createElement("add");
        Element destDoc = feedDocument.createElement("doc");
        add.appendChild(destDoc);
        feedDocument.appendChild(add);

        InputStream resourceAsStream = BatchUtilsTest.class.getResourceAsStream("pdfsolr_no_text_ocr.xml");
        Assert.assertNotNull(resourceAsStream);

        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element doc = XMLUtils.findElement(parsed.getDocumentElement(), "doc");
        transform(doc, feedDocument,destDoc);

        Element text = BatchUtils.findByAttribute(destDoc, "text");
        Assert.assertNotNull(text);

        Element text_lemmatized = BatchUtils.findByAttribute(destDoc, "text_lemmatized");
        Assert.assertNotNull(text_lemmatized);

        Element text_lemmatized_ascii = BatchUtils.findByAttribute(destDoc, "text_lemmatized_ascii");
        Assert.assertNotNull(text_lemmatized_ascii);

        Element text_lemmatized_nostopwords = BatchUtils.findByAttribute(destDoc, "text_lemmatized_nostopwords");
        Assert.assertNotNull(text_lemmatized_nostopwords);

    }

    public void testPdfTextOcrTransform() throws IOException, SAXException, ParserConfigurationException, TransformerException, MigrateSolrIndexException {
        Document feedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element add = feedDocument.createElement("add");
        Element destDoc = feedDocument.createElement("doc");
        add.appendChild(destDoc);
        feedDocument.appendChild(add);

        InputStream resourceAsStream = BatchUtilsTest.class.getResourceAsStream("pdfsolr_text_ocr.xml");
        Assert.assertNotNull(resourceAsStream);

        Document parsed = XMLUtils.parseDocument(resourceAsStream);
        Element doc = XMLUtils.findElement(parsed.getDocumentElement(), "doc");
        transform(doc, feedDocument,destDoc);


        Element text = BatchUtils.findByAttribute(destDoc, "text");
        Assert.assertNull(text);

        Element text_lemmatized = BatchUtils.findByAttribute(destDoc, "text_lemmatized");
        Assert.assertNull(text_lemmatized);

        Element text_lemmatized_ascii = BatchUtils.findByAttribute(destDoc, "text_lemmatized_ascii");
        Assert.assertNull(text_lemmatized_ascii);

        Element text_lemmatized_nostopwords = BatchUtils.findByAttribute(destDoc, "text_lemmatized_nostopwords");
        Assert.assertNull(text_lemmatized_nostopwords);

        Element text_ocr = BatchUtils.findByAttribute(destDoc, "text_ocr");
        Assert.assertNotNull(text_ocr);

        Element text_ocr_lemmatized = BatchUtils.findByAttribute(destDoc, "text_ocr_lemmatized");
        Assert.assertNull(text_ocr_lemmatized);

    }

}
