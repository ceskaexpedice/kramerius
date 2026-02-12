package org.kramerius.genpdf;

import com.lowagie.text.DocumentException;
import cz.incad.kramerius.pdf.OutOfRangeException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

/** --  */
public interface GenerateFullPDFService {

    public String generate(String pid, String user, String providedByLicense) throws DocumentException, IOException, OutOfRangeException;


    public void sendEmailNotification(String emailFrom, List<Object> recipients, String subject, String text) throws MessagingException;
}