package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlLabel;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.activation.FileDataSource;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class FeedbackFatalError {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private final String system =
            System.getProperty("os.name") + "_" +
                    System.getProperty("os.arch") + "_" +
                    System.getProperty("os.version");

    //TODO send us some info
    public FeedbackFatalError(String title, String description,
                              boolean notifyUs, final int messageType, Component parent) {

        if (description == null)
            description = textSrc.getString("unknown_error");

        if (notifyUs) {
            FeedbackConfirmPane content = new FeedbackConfirmPane(description);

            int result = JOptionPane.showOptionDialog(
                    parent,
                    content,
                    title,
                    JOptionPane.YES_NO_OPTION, messageType,
                    new ImageIcon("src/main/resources/img/bug.png"),
                    new String[]{textSrc.getString("send"), textSrc.getString("send_not")},
                    "error");

            if (result == 0) {
                sendMail(content.getUserText(), content.hasAttachment());
            } else {
                System.exit(result);
            }
        } else {
            JOptionPane.showMessageDialog(null, description, title, messageType);
        }
    }

    private void sendMail(String msg, boolean attachLog) {
        //todo security check
        //todo learn the mail sending now it cannot wotk without pwd which i wont paste
        EmailBuilder builder = new EmailBuilder().from("user", "unknown")
                .to("J Horák", "horakj7@gmail.com").subject("JCAppStore Failure Report")
                .text(msg);

        if (attachLog)
            builder.addAttachment(system + ".log",
                    new FileDataSource(new File("log/jcAppStore.log")));

        //misuse google
        new Mailer("smtp.gmail.com", 25, "horakj7@gmail.com", "",
                TransportStrategy.SMTP_TLS).sendMail(builder.build());
    }

    private class FeedbackConfirmPane extends JPanel {
        private JCheckBox attachment = new JCheckBox("<html><div width=\"350\">" +
                textSrc.getString("attachment") + "</div></html>");
        private JTextArea area = new JTextArea(8, 12);

        FeedbackConfirmPane(String message) {
            super(new BorderLayout());
            add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "bug.png")));
            add(new HtmlLabel("<div width=\"400\">" + textSrc.getString("attachment_desc") + "</div>" +
                    "<div>&emsp;</div>"), BorderLayout.NORTH);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setText(message);
            area.setFont(Font.decode(Font.SANS_SERIF).deriveFont(12.f));
            area.setBorder(BorderFactory.createCompoundBorder(
                    area.getBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            add(area, BorderLayout.CENTER);
            add(attachment, BorderLayout.SOUTH);
            attachment.setSelected(true);
        }

        boolean hasAttachment() {
            return attachment.isSelected();
        }

        String getUserText() {
            return area.getText();
        }
    }
}
