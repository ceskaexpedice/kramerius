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
package cz.incad.Kramerius.security.userscommands.post;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
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
import javax.servlet.http.HttpSession;

import nl.captcha.Captcha;

import com.google.inject.Inject;

import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.impl.RoleImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.service.Mailer;
import cz.incad.kramerius.utils.ApplicationURL;

public class RegisterPublicUser extends AbstractPostUser{

    static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(CreateRole.class.getName());
    
    @Inject
    Mailer mailer;
    
    
    @Override
    public void doCommand() throws IOException {
        try {
            HttpServletRequest req = this.requestProvider.get();
            String loginName = req.getParameter(LOGIN_NAME);
            String name = req.getParameter(NAME);
            String email = req.getParameter(EMAIL);
            String pswd = req.getParameter(PASSWORD);
            String captcha = req.getParameter(CAPTCHA);
            String firstName = name;
            String surName = "";
            
            
            if (requestProvider.get().getSession().getAttribute(Captcha.NAME) != null) {
                Captcha expected = (Captcha) requestProvider.get().getSession().getAttribute(Captcha.NAME);
                
                if (expected.getAnswer().equals(captcha)) {
                    StringTokenizer tokenizer = new StringTokenizer(name," ");
                    firstName = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : name;
                    surName  = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
                    
                    UserImpl user = new UserImpl(-1, firstName, surName, loginName, -1);
                    user.setEmail(email);
                    this.userManager.insertUser(user,pswd);
                    
                    sendMail(user);
                } else {
                    HttpServletResponse response = this.responseProvider.get();
                    response.setContentType("application/json");
                    PrintWriter writer = response.getWriter();
                    writer.write("{'error':'bad_captcha'}");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
                
            } else { 
                HttpServletResponse response = this.responseProvider.get();
                response.setCharacterEncoding("application/json");
                PrintWriter writer = response.getWriter();
                writer.write("{'error':'no_captcha'}");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            this.responseProvider.get().sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            this.responseProvider.get().sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }



    public void sendMail(UserImpl user) {
        try {
            ResourceBundle resourceBundle = this.resourceBundleService.getResourceBundle("labels", this.localesProvider.get());

            HttpServletRequest request = this.requestProvider.get();
            String key = this.notActivatedUsersSingleton.addNotActivatedUser(user);
            javax.mail.Session sess = mailer.getSession(null, null);
            Message msg = new MimeMessage(sess);
            msg.addRecipient(RecipientType.TO, new InternetAddress(user.getEmail()));
            msg.setSubject(resourceBundle.getString("registeruser.mail.subject"));
            String formatted = MessageFormat.format(resourceBundle.getString("registeruser.mail.message"),user.getFirstName()+" "+user.getSurname(), user.getLoginname(), ApplicationURL.applicationURL(request)+"/users?action=activation&key="+key);
            msg.setText(formatted);
            String from = sess.getProperty("mail.from");
            if (from == null) {
                from = sess.getProperty("mail.smtp.user");
            }
            if (from == null) {
                // default value
                from = user.getEmail();
            }
            InternetAddress mailFrom = new InternetAddress(from);
            msg.setFrom(mailFrom);

            Transport.send(msg);
        } catch (NoSuchProviderException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }
}