package cz.muni.crocs.appletstore;

//import javax.mail.Message;
//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.Transport;
//import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.awt.*;
import java.util.Properties;

/**
 * //TODO implement or remove
 * @author Jiří Horák
 * @version 1.0
 */
public class FeedbackFatalError {
    //TODO send us some info
    public FeedbackFatalError(String title, String message, String descrpition,
                              boolean notifyUs, final int messageType, Component parent) {

        if (notifyUs) {
              System.out.println(JOptionPane.showConfirmDialog(parent, message));

              int result = JOptionPane.showConfirmDialog(parent, message);
              switch (result) {
                  case 0: //accepts
                  case 1: //rejects
                  case 2: //closes
                  default:
                      System.exit(result);
              }
            if (JOptionPane.showConfirmDialog(parent, message) == 0) {
                //mail(message + "\n\nDesctiprtion:\n" + descrpition);
                //TODO close on confirm
            }
        } else {
            JOptionPane.showMessageDialog(null, message, title, messageType);
        }
    }

//    /**
//     * FROM https://www.tutorialspoint.com/java/java_sending_email.htm
//     */
//    public void mail(String content) {
//        // Recipient's email ID needs to be mentioned.
//        String to = "horakj7@gmail.com";
//
//        // Sender's email ID needs to be mentioned
//        String from = "appletstore@gmail.com";
//
//        // Assuming you are sending email from localhost
//        String host = "localhost";
//
//        // Get system properties
//        Properties properties = System.getProperties();
//
//        // Setup mail server
//        properties.setProperty("mail.smtp.host", host);
//
//        // Get the default Session object.
//        Session session = Session.getDefaultInstance(properties);
//
//        try {
//            // Create a default MimeMessage object.
//            MimeMessage message = new MimeMessage(session);
//
//            // Set From: header field of the header.
//            message.setFrom(new InternetAddress(from));
//
//            // Set To: header field of the header.
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
//
//            // Set Subject: header field
//            message.setSubject("AppletStore report.");
//
//            // Now set the actual message
//            message.setText(content);
//
//            // Send message
//            Transport.send(message);
//            System.out.println("Sent message successfully....");
//        } catch (MessagingException mex) {
//            mex.printStackTrace();
//        }
//    }
}
