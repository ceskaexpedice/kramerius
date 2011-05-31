package cz.incad.kramerius.rights.server.impl;

import javax.mail.Session;

import cz.incad.kramerius.rights.server.Mailer;

public class JNDIMailer extends Mailer {

    @Override
    public Session getSession(String name, String pass) {
        // session from jndi
        return null;
    }

}
