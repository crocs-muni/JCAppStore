package cz.muni.crocs.appletstore;

import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.activation.FileDataSource;
import javax.swing.*;
import java.awt.*;
import java.io.File;



/**
 * //TODO implement or remove
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class FeedbackFatalError {

    private final String system =
            System.getProperty("os.name") + "_" +
                    System.getProperty("os.arch") + "_" +
                    System.getProperty("os.version");

    //TODO send us some info
    public FeedbackFatalError(String title, String message, String description,
                              boolean notifyUs, final int messageType, Component parent) {

        if (notifyUs) {
            FeedbackConfirmPane content = new FeedbackConfirmPane(message + description);

            int result = JOptionPane.showOptionDialog(
                    parent,
                    content,
                    title,
                    JOptionPane.YES_NO_OPTION, messageType,
                    new ImageIcon("src/main/resources/img/mail.png"),
                    new String[]{"Send", "Don't send"},
                    "error");

            if (result == 0) {
                sendMail(content.getUserText(), content.hasAttachment());
            } else {
                System.exit(result);
            }
        } else {
            JOptionPane.showMessageDialog(null, message, title, messageType);
        }
    }

    private void sendMail(String msg, boolean attachLog) {
        //todo security check
        EmailBuilder builder = new EmailBuilder().from("user", "unknown")
                .to("J Horák", "horakj7@gmail.com").subject("JCAppStore Failure Report")
                .text(msg);

        if (attachLog)
            builder.addAttachment(system + ".log",
                    new FileDataSource(new File("log/jcAppStore.log")));

        //misuse google
        new Mailer("smtp.gmail.com", 25, "horakj7@gmail.com", "xfc68c49",
                TransportStrategy.SMTP_TLS).sendMail(builder.build());
    }

    private class FeedbackConfirmPane extends JPanel {
        private JCheckBox attachment = new JCheckBox("<html><div width=\"350px\">" +
                "attach log info to help us identify the problem" +
                "</div></html>");
        private JTextArea area = new JTextArea(5, 30);

        FeedbackConfirmPane(String message) {
            setLayout(new BorderLayout());
            area.setText(message);
            area.setBorder(BorderFactory.createCompoundBorder(
                    area.getBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            add(area, BorderLayout.CENTER);
            add(attachment, BorderLayout.SOUTH);
        }

        boolean hasAttachment() {
            return attachment.isSelected();
        }

        String getUserText() {
            return area.getText();
        }
    }
}
