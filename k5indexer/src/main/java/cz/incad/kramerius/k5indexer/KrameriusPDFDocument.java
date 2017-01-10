/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.utils.conf.KConfiguration;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 *
 * @author alberto
 */
public class KrameriusPDFDocument {

    private static final Logger logger = Logger.getLogger(KrameriusPDFDocument.class.getName());
    private final String pid;
    private final InputStream stream;

    PDDocument pdDoc = null;

    public KrameriusPDFDocument(String pid, InputStream stream) throws Exception {
        this.pid = pid;
        this.stream = stream;
        setDocument();
    }

    private void setDocument() throws Exception {
        try {
            closeDocument();
            PDDocument pdDocument = PDDocument.load(stream, KConfiguration.getInstance().getConfiguration().getString("convert.pdfPassword"));
        } catch (Exception ex) {
            closeDocument();
            logger.log(Level.WARNING, "Cannot parse PDF document", ex);
        }

    }

    public void closeDocument() throws IOException {
        if (pdDoc != null) {
            pdDoc.close();
        }
    }

    public int getPagesCount() {
        if (pdDoc != null) {
            return pdDoc.getNumberOfPages();
        } else {
            return 0;
        }
    }

    public String getPage(int page) throws Exception {
        logger.log(Level.INFO, "Getting page {0}", page);
        try {
            PDFTextStripper stripper = new PDFTextStripper(/*"UTF-8"*/);
            if (page != -1) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
            }

            return StringEscapeUtils.escapeXml(stripper.getText(pdDoc));
        } catch (Exception ex) {
            return "";
        }
    }
}
