/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Authenticator;
import javax.mail.Session;

import cz.incad.kramerius.service.Mailer;

public class MailerImpl implements Mailer {

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(MailerImpl.class.getName());
    
    //TODO: Change it
    private static final String MAIL_PROPS_PATH = System.getProperty("user.home") + File.separator + ".kramerius4" + File.separator + "mail.properties";

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
