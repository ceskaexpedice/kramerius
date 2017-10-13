//$Id: TransformerToText.java 7837 2008-11-21 11:39:09Z gertsp $
/*
 * <p><b>License and Copyright: </b>The contents of this file is subject to the
 * same open source license as the Fedora Repository System at www.fedora-commons.org
 * Copyright &copy; 2006, 2007, 2008 by The Technical University of Denmark.
 * All rights reserved.</p>
 */
package dk.defxws.fedoragsearch.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.stream.StreamSource;

import org.apache.lucene.demo.html.HTMLParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import cz.incad.kramerius.utils.UnicodeUtil;

/**
 * performs transformations from formatted documents to text
 *
 * @author  gsp@dtv.dk
 * @version
 */
public class TransformerToText {

    private static final Logger logger =
            Logger.getLogger(TransformerToText.class.getName());

    public TransformerToText() {
    }

    public StringBuffer getText(byte[] doc, String mimetype, String page)
            throws Exception {
        if (mimetype.equals("text/plain")) {
            return getTextFromText(doc);
        } else if (mimetype.equals("plain/text")) {
            return getTextFromText(doc);
        } else if (mimetype.equals("text/xml")) {
            return getTextFromXML(doc);
        } else if (mimetype.equals("text/html")) {
            return getTextFromHTML(doc);
        } else if (mimetype.equals("application/pdf")) {
            return getTextFromPDF(doc, page);
        } else if (mimetype.equals("application/ps")) {
            return new StringBuffer();
        } else if (mimetype.equals("application/msword")) {
            return new StringBuffer();
        } else {
            logger.log(Level.FINE, "unsupported mimetype: {0}", mimetype);
            return new StringBuffer();
        }
    }

    /**
     *
     *
     * @throws Exception.
     */
    private StringBuffer getTextFromText(byte[] doc)
            throws Exception {
        StringBuffer docText = new StringBuffer();
        try {
            String enc = UnicodeUtil.getEncoding(doc);
            //enc = "UTF-8";
            InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(doc), enc);
            int c = isr.read();
            while (c > -1) {
                docText.append((char) c);
                c = isr.read();
            }
        } catch (Exception e) {
            throw new Exception(e.toString());

        }
        return docText;
    }

    /**
     *
     *
     * @throws Exception.
     */
    private StringBuffer getTextFromXML(byte[] doc)
            throws Exception, Exception {
        InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(doc));
        StringBuffer docText = (new GTransformer()).transform(
                "config/textFromXml",
                new StreamSource(isr));
        docText.delete(0, docText.indexOf(">") + 1);
        return docText;
    }

    /**
     *
     *
     * @throws Exception.
     */
    private StringBuffer getTextFromHTML(byte[] doc)
            throws Exception {
        StringBuffer docText = new StringBuffer();
        HTMLParser htmlParser = new HTMLParser(new ByteArrayInputStream(doc));
        try {
            InputStreamReader isr = (InputStreamReader) htmlParser.getReader();
            int c = isr.read();
            while (c > -1) {
                docText.append((char) c);
                c = isr.read();
            }
        } catch (IOException e) {
            throw new Exception(e.toString());
        }
        return docText;
    }


    public int getPdfPagesCount_(byte[] doc) throws Exception{
        String password = "";
        PDDocument pdDoc = null;
        // extract PDF document's textual content
        try {
            pdDoc = PDDocument.load(new ByteArrayInputStream(doc),password);
            return pdDoc.getNumberOfPages();
        } catch (Exception e) {
            throw new Exception(
                    "Cannot parse PDF document", e);
        } finally {
            closePDDocument(pdDoc);
        }
    }

    public static StringBuffer getTextFromPDF(PDDocument pdDoc, String pageNum)
            throws Exception {
        StringBuffer docText = new StringBuffer();
        String password = "";
        // extract PDF document's textual content
        try {
            PDFTextStripper stripper = new PDFTextStripper(/*"UTF-8"*/);
            int page = Integer.parseInt(pageNum);
            if(page!=-1){
                stripper.setStartPage(page);
                stripper.setEndPage(page);
            }
            docText = new StringBuffer(stripper.getText(pdDoc));
        } catch (IOException e) {
            throw new Exception(
                    "Cannot parse PDF document", e);
        }
        return docText;
    }

    /**
     *
     *
     * @throws Exception.
     */
    private StringBuffer getTextFromPDF(byte[] doc, String pageNum)
            throws Exception {
        StringBuffer docText = new StringBuffer();
        PDDocument pdDoc = null;
        String password = "";

        // extract PDF document's textual content
        try {
            PDFTextStripper stripper = new PDFTextStripper(/*"UTF-8"*/);
            int page = Integer.parseInt(pageNum);
            if(page!=-1){
                stripper.setStartPage(page);
                stripper.setEndPage(page);
            }
            //password
            pdDoc = PDDocument.load(new ByteArrayInputStream(doc),password); // new PDDocument(cosDoc);
            docText = new StringBuffer(stripper.getText(pdDoc));
        } catch (IOException e) {
            throw new Exception(
                    "Cannot parse PDF document", e);
        } finally {
            closePDDocument(pdDoc);
        }
        return docText;
    }


    private void closePDDocument(PDDocument pdDoc) {
        if (pdDoc != null) {
            try {
                pdDoc.close();
            } catch (IOException e) {
            }
        }
    }
}