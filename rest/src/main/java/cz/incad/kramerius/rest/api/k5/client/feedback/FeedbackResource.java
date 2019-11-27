package cz.incad.kramerius.rest.api.k5.client.feedback;

import java.io.IOException;
import java.rmi.ServerException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Created by Jan Holman on 22.5.15.
 */

@Path("/v5.0/feedback")
public class FeedbackResource {

    private static Logger LOGGER = Logger.getLogger(FeedbackResource.class.getName());

    @Inject
    private KConfiguration configuration;
    @Inject
    private ResourceBundleService resourceBundleService;
    @Inject
    private Provider<Locale> localesProvider;
    @Inject
    private Mailer mailer;

    @POST
    @Produces({MediaType.TEXT_PLAIN+";charset=utf-8"})
    @Consumes({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response sendFeedback(JSONObject data) {
        try {
            String pid = "";
            String url = "";
            String text = "";
            String email = "";
            if (data.has("pid")) {
                pid = data.getString("pid");
            }
            if (data.has("url")) {
                url = data.getString("url");
            }
            if (data.has("text")) {
                text = data.getString("text");
            }
            if (data.has("email")) {
                email = data.getString("email");
            }

            sendMail(email, pid, url, text);
            return Response.status(201).entity("Feedback sent to administrator").build();

        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return Response.status(422).entity("Malformed request").build();
        } catch (ServerException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            return Response.status(500).build();
        }
    }

    private void sendMail(String from, String pid, String url, String text) throws ServerException {
        try {
            ResourceBundle resourceBundle = resourceBundleService.getResourceBundle("labels", this.localesProvider.get());

            javax.mail.Session sess = mailer.getSession(null, null);
            Message msg = new MimeMessage(sess);
            msg.setHeader("Content-Type", "text/plain; charset=UTF-8");
            String mailto = configuration.getProperty("administrator.email");
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(mailto));
            msg.setSubject(resourceBundle.getString("feedback.mail.subject"));
            String content = text + "\n\n" + url;
            String formatted = MessageFormat.format(resourceBundle.getString("feedback.mail.message"),
                    from + "\n", pid + "\n", "\n" + content + "\n");
            msg.setText(formatted);
            Transport.send(msg);

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new ServerException(e.toString());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new ServerException(e.toString());
        }
    }
}
