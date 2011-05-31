package cz.incad.kramerius.rights.server;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public abstract class Mailer {

    public abstract Session getSession(String name, String pass);

    public class SMTPAuthenticator extends javax.mail.Authenticator {

        private String name;
        private String pass;

        public SMTPAuthenticator(String name, String pass) {
            super();
            this.name = name;
            this.pass = pass;
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(name, pass);
        }
    }

}
