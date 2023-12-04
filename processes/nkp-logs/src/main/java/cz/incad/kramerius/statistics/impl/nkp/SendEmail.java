package cz.incad.kramerius.statistics.impl.nkp;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class SendEmail {

    public static void main(String[] args) {

        // Nastavení e-mailového účtu Gmail
        String username = "k4system@gmail.com";
        //String username = "pavel.stastny@inovatika.cz";
        
        //String password = "veje vbng xuhv goyq";
        String password = "cast wosb orpa jyny";
        
        // Nastavení vlastností pro spojení s Gmail SMTP serverem
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Vytvoření Session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Vytvoření zprávy
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("pavel.stastny@gmail.com"));
            message.setSubject("Předmět zprávy");
            message.setText("Toto je text zprávy.");

            // Odeslání zprávy
            Transport.send(message);

            System.out.println("Zpráva byla úspěšně odeslána.");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Chyba při odesílání zprávy: " + e.getMessage());
        }
    }
}
