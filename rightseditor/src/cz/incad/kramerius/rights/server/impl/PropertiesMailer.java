package cz.incad.kramerius.rights.server.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Authenticator;
import javax.mail.Session;

import cz.incad.kramerius.rights.server.Mailer;

public class PropertiesMailer extends Mailer {

    private static final String MAIL_PROPS_PATH = System.getProperty("user.home") + File.separator + ".kramerius4" + File.separator + "mail.properties";
    private static final Logger LOGGER = Logger.getLogger(PropertiesMailer.class.getName());

    @Override
    public Session getSession(String name, String pass) {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(MAIL_PROPS_PATH)));
            if (name == null) {
                name = properties.getProperty("mail.smtp.user");
            }
            if (pass == null) {
                pass = properties.getProperty("mail.smtp.pass");
            }
            Authenticator auth = new SMTPAuthenticator(name, pass);
            Session session = Session.getInstance(properties, auth);
            return session;
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

}
