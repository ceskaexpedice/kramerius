package cz.kramerius.searchIndex.indexerProcess;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see cz.incad.kramerius.indexer.ExtendedFields
 */
public class PdfExtractor {

    private static final Logger LOGGER = Logger.getLogger(PdfExtractor.class.getName());

    private final PDDocument pdDoc;

    public PdfExtractor(String pid, InputStream is) throws IOException {
        try {
            //tmp file for PDF
            File pdfImg = File.createTempFile(pid, ".pdf");
            pdfImg.deleteOnExit();
            FileUtils.copyInputStreamToFile(is, pdfImg);
            //load PDF
            this.pdDoc = PDDocument.load(pdfImg, KConfiguration.getInstance().getConfiguration().getString("convert.pdfPassword"));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error parsing PDF document from " + pid, ex);
            throw new IOException("Error parsing PDF document from " + pid, ex);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @return total number of pages in the PDF
     */
    public int getPagesCount() {
        return pdDoc.getNumberOfPages();
    }

    /**
     * @param pageNum page number starting with 0
     * @return text from the page
     */
    public String getPageText(int pageNum) throws IOException {
        try {
            PDFTextStripper stripper = new PDFTextStripper(/*"UTF-8"*/);
            if (pageNum != -1) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
            }
            return StringEscapeUtils.escapeXml10(stripper.getText(pdDoc));
        } catch (Exception ex) {
            throw new IOException("Error fetching page number " + pageNum, ex);
        }
    }

    public void finalize() {
        IOUtils.closeQuietly(pdDoc);
    }

}
