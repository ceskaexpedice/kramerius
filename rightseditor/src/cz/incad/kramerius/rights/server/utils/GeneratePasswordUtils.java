package cz.incad.kramerius.rights.server.utils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import cz.incad.kramerius.rights.server.Mailer;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class GeneratePasswordUtils {

	static char[] CHARS={
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','R','S','T','U','V','W','X','Y','Z',
	   	'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','r','s','t','u','v','w','x','y','z',
	   	'0','1','2','3','4','5','6','7','8','9'
	};

	
	public static String generatePswd()  {
		StringBuffer generated = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < GeneratePasswordUtils.PASSWORD_LENGTH; i++) {
			int randomIndex = random.nextInt(CHARS.length);
			generated.append(CHARS[randomIndex]);
		}
		return generated.toString();
	}


	public static final int PASSWORD_LENGTH=8;


	public static  void sendGeneratedPasswordToMail(String emailAddres,
			String generated, Mailer mailer) throws MessagingException, AddressException {
		Session session = mailer.getSession(null, null);
		MimeMessage msg = new MimeMessage(session);
		msg.setText("Vygenerovane heslo je :"+generated);
		msg.setSubject("Vygenerovane heslo");
		// mail.from
		//msg.setFrom(new InternetAddress(d_email));
		msg.addRecipient(Message.RecipientType.TO,
				new InternetAddress(emailAddres));
		Transport.send(msg);
	}

}
