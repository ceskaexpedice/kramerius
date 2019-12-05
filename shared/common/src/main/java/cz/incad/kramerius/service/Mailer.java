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
package cz.incad.kramerius.service;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;


/**
 * Simple mailing service
 * @author pavels
 */
public interface Mailer {
    
    
    /**
     * Returns mail session
     * @param name User name
     * @param pswd Password
     * @return Mail session
     */
    public Session getSession(String name,String pswd);

    /**
     * Simple authenticator
     * @author pavels
     */
    public class SMTPAuthenticator extends javax.mail.Authenticator {

        private String name;
        private String pass;

        public SMTPAuthenticator(String name, String pass) {
            super();
            this.name = name;
            this.pass = pass;
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(name, pass);
        }
    }

}
