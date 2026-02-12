package org.kramerius.genpdf.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.DocumentService;
import cz.incad.kramerius.document.model.AkubraDocument;
import cz.incad.kramerius.pdf.FirstPagePDFService;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.OutOfRangeException;
import cz.incad.kramerius.pdf.SimplePDFService;
import cz.incad.kramerius.pdf.utils.pdf.FontMap;
import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.impl.MailerImpl;
import cz.inovatika.dochub.DocumentType;
import cz.inovatika.dochub.PermanentContentSpace;
import cz.inovatika.dochub.UserContentSpace;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.kramerius.genpdf.GenerateFullPDFService;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class GenerateFullPDFServiceImpl implements GenerateFullPDFService {

    public static final Logger LOGGER = Logger.getLogger(GenerateFullPDFServiceImpl.class.getName());



    @Inject
    @Named("USERPROCESS")
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

    @Inject
    PermanentContentSpace permanentContentSpace;

    @Inject
    UserContentSpace userContentSpace;



    public String generate(String pid, String user, String providedByLicense) throws DocumentException, IOException, OutOfRangeException {
        FontMap fmap = new FontMap(deprectedService.fontsFolder());

        ObjectPidsPath[] paths = solrAccess.getPidPaths(pid);
        final ObjectPidsPath path = paths.length> 0 ? paths[0] : new ObjectPidsPath(pid);
        // NOT page size
        Rectangle rect = PageSize.A4;

        File firstPageFile = null;
        try {
            AkubraDocument rdoc =  null;
            String token = userContentSpace.getToken(pid, user);
            if (userContentSpace.exists(token)) {
                userContentSpace.deleteBundle(token);
            }

            if (permanentContentSpace.exists(pid, DocumentType.PDF)) {
                rdoc = this.documentService.buildDocumentAsFlat(path, pid, 1, new float[] {rect.getWidth(), rect.getHeight()});
                rdoc.pageDimensionFromFirstPage();
                LOGGER.info("Found existing pdf content for pid "+pid);
            } else {
                rdoc = this.documentService.buildDocumentAsFlat(path, pid, 1000, new float[] {rect.getWidth(), rect.getHeight()});
                rdoc.pageDimensionFromFirstPage();

                try (OutputStream os = permanentContentSpace.createOutputStream(pid, DocumentType.PDF)) {
                    this.simplePdfService.pdf(rdoc, os, fmap);
                }
            }

            firstPageFile = File.createTempFile("head", ".pdf");
            FileOutputStream fpageFos = new FileOutputStream(firstPageFile);
            this.textFirstPage.parent(rdoc, fpageFos, path, fmap, providedByLicense);

            File generatedPDF = File.createTempFile("rendered", ".pdf");
            FileOutputStream fos = new FileOutputStream(generatedPDF);

            if (permanentContentSpace.exists(pid, DocumentType.PDF)) {
                mergeToOutput(fos, permanentContentSpace.getContent(pid, DocumentType.PDF), firstPageFile);
                userContentSpace.storeBundle(new FileInputStream(generatedPDF), user, pid, DocumentType.PDF, "{audit}");
                return userContentSpace.getToken(pid, user);
            } else throw new RuntimeException("Persistent store doesn't contain data for pid '"+pid+"'");
        } catch (OutOfRangeException e) {
            throw new RuntimeException(e);
        }
    }

    static void mergeToOutput(OutputStream fos, InputStream bodyStream, File firstPageFile) throws IOException {
        PDFMergerUtility utility = new PDFMergerUtility();
        utility.addSource(firstPageFile);
        utility.addSource(bodyStream);
        utility.setDestinationStream(fos);
        utility.mergeDocuments();
    }


    public void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException {
        Mailer mailer = new MailerImpl();
        javax.mail.Session sess = mailer.getSession(null, null);
        MimeMessage msg = new MimeMessage(sess);

        msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
        msg.setFrom(new InternetAddress(emailFrom));
        for (Object recp : recipients) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recp.toString()));
        }
        msg.setSubject(subject, "UTF-8");
        msg.setText(text, "UTF-8");
        Transport.send(msg);
    }

}
