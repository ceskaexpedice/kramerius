package cz.incad.Kramerius;


import java.io.IOException;

import javax.servlet.ServletException;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.security.SecurityException;
import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.rmi.ServerException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FeedbackServlet extends GuiceServlet {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(MimeTypeServlet.class.getName());
    
    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedoraAccess;
	
    @Inject
    KConfiguration configuration;

    @Inject
    protected ResourceBundleService resourceBundleService;
    @Inject
    protected Provider<HttpServletRequest> requestProvider;
    @Inject
    protected Provider<HttpServletResponse> responseProvider;

    @Inject
    protected Provider<Locale> localesProvider;
    @Inject
    Mailer mailer;

    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String pid = req.getParameter("pid");
            String from = req.getParameter("from");
            String content = req.getParameter("content");
            ResourceBundle resourceBundle = this.resourceBundleService.getResourceBundle("labels", this.localesProvider.get());

            HttpServletRequest request = this.requestProvider.get();
            javax.mail.Session sess = mailer.getSession(null, null);
            Message msg = new MimeMessage(sess);
            msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
            
            String mailto = configuration.getProperty("administrator.email");
            msg.addRecipient(RecipientType.TO, new InternetAddress(mailto));
            msg.setSubject(resourceBundle.getString("feedback.mail.subject"));
            String formatted = MessageFormat.format(resourceBundle.getString("feedback.mail.message"),
                    from+"\n", pid+"\n", "\n"+content+"\n");
            msg.setText(formatted);
            Transport.send(msg);    
                
            
        } catch(SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }catch (NoSuchProviderException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new ServerException(e.toString());
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new ServerException(e.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new ServerException(e.toString());
        }
    }
}
