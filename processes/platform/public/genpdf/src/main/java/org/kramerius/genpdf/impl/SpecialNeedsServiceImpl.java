package org.kramerius.genpdf.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.PreparedDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.kramerius.genpdf.SpecialNeedsService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SpecialNeedsServiceImpl implements SpecialNeedsService {

    @Inject
    @Named("SPECIALNEEDS")
    FirstPagePDFService textFirstPage;


    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    DocumentService documentService;

    @Inject
    GeneratePDFService deprectedService;

    @Inject
    SimplePDFService simplePdfService;



    public File generate(String pid, String user) throws DocumentException, IOException, OutOfRangeException {
        FontMap fmap = new FontMap(deprectedService.fontsFolder());

        ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
        final ObjectPidsPath path = paths.length> 0 ? paths[0] : new ObjectPidsPath(pid);
        // NOT page size
        Rectangle rect = PageSize.A4;

        File parentFile = null;
        File firstPageFile = null;
        try {
            // TODO: Do it better
            System.setProperty("user.uid", user);
            PreparedDocument rdoc = this.documentService.buildDocumentAsFlat(path, pid, 1000, new int[] {(int)rect.getWidth(), (int)rect.getHeight()});

            parentFile = File.createTempFile("body", "pdf");
            FileOutputStream bodyTmpFos = new FileOutputStream(parentFile);

            firstPageFile = File.createTempFile("head", "pdf");
            FileOutputStream fpageFos = new FileOutputStream(firstPageFile);
            this.textFirstPage.parent(rdoc, fpageFos, path, fmap);

            this.simplePdfService.pdf(rdoc, bodyTmpFos, fmap);

            File generatedPDF = File.createTempFile("rendered", ".pdf");
            FileOutputStream fos = new FileOutputStream(generatedPDF);

            mergeToOutput(fos, parentFile, firstPageFile);
            return generatedPDF;
        } catch (OutOfRangeException  e) {
            throw new RuntimeException(e);
        }
    }

    static void mergeToOutput(OutputStream fos, File bodyFile, File firstPageFile) throws IOException {
        PDFMergerUtility utility = new PDFMergerUtility();
        utility.addSource(firstPageFile);
        utility.addSource(bodyFile);
        utility.setDestinationStream(fos);
        utility.mergeDocuments();
    }

}
