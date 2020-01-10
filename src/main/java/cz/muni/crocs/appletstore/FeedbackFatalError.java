package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import javax.activation.FileDataSource;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class FeedbackFatalError {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private String charset = "UTF-8";
    private String boundary = Long.toHexString(System.currentTimeMillis());
    private String CRLF = "\r\n";
    private String server = "https://www.fi.muni.cz/~xhorak8/jcappstore-reporter.php";

    private final String system =
            System.getProperty("os.name") + "_" +
                    System.getProperty("os.arch") + "_" +
                    System.getProperty("os.version");

    public FeedbackFatalError(String title, String description,
                              boolean notifyUs, final int messageType, Component parent) {

        if (title.isEmpty()) title = "JCAppStore reporter";
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

            if (result == JOptionPane.YES_OPTION) {
                try {
                    if (send(content.getUserText(), content.getUserMail(), content.hasAttachment())) {
                        notifyUser(title, textSrc.getString("request_sent"), JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        notifyUser(title, textSrc.getString("failed_send_report"), JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    notifyUser(title, textSrc.getString("failed_send_report"), JOptionPane.ERROR_MESSAGE);
                }
            } else {
                notifyUser(title, textSrc.getString("failed_send_report"), JOptionPane.ERROR_MESSAGE);
                System.exit(result);
            }
        } else {
            notifyUser(title, description, messageType);
        }
    }

    private void notifyUser(String title, String message, final int messageType) {
        JOptionPane.showMessageDialog(null, "<html><div width=\"350\">" + message +
                "</div></html>", title, messageType);
    }

    /**
     * Idea from https://stackoverflow.com/questions/2469451/upload-files-from-java-client-to-a-http-server
     * @param msg message to send
     * @param mail mail of the sender
     * @param attachLog log file with details
     * @throws IOException When IO fails - reading log file, sending POST request etc..
     */
    private boolean send(String msg, String mail, boolean attachLog) throws IOException {
        if (mail == null || mail.isEmpty()) mail = "user@unknown";

        URLConnection connection = new URL(server).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream ostream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(ostream, charset), true)) {
            sendParameter("mail", mail, writer);
            sendParameter("text", msg, writer);
            if (attachLog) {
                File log = new File("log/jcAppStore.log");
                if (log.exists()) sendFile("soubor", log, writer, ostream);
            }
            writer.append("--").append(boundary).append("--").append(CRLF).flush();
        }
        return ((HttpURLConnection) connection).getResponseCode() == 200;
    }

    private void sendParameter(String name, String value, PrintWriter writer) {
        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
        writer.append(CRLF).append(value).append(CRLF).flush();
    }

    private void sendFile(String name, File file, PrintWriter writer, OutputStream ostream) throws IOException {
        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"; filename=\"")
                .append(file.getName()).append("\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=").append(charset).append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(file.toPath(), ostream);
        ostream.flush();
        writer.append(CRLF).flush();
    }

    /**
     * GUI error-feedback form class
     */
    private class FeedbackConfirmPane extends JPanel {
        private JCheckBox attachment = new JCheckBox("<html><div width=\"350\">" +
                textSrc.getString("attachment") + "</div></html>");
        private JTextArea area = new JTextArea(8, 12);
        private JTextArea email = new JTextArea(1, 6);

        FeedbackConfirmPane(String message) {
            super(new BorderLayout());
            add(new JLabel(new ImageIcon(Config.IMAGE_DIR + "bug.png")));

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            panel.add(new HtmlText("<div width=\"400\">" + textSrc.getString("attachment_desc") + "</div>" +
                    "<div>&emsp;</div>"), BorderLayout.NORTH);

            JLabel yourmail = new HtmlText(textSrc.getString("yourmail"));
            panel.add(yourmail, BorderLayout.CENTER);
            setTextArea(email);
            panel.add(email, BorderLayout.SOUTH);
            add(panel, BorderLayout.NORTH);

            setTextArea(area);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setText(message);
            add(area, BorderLayout.CENTER);
            add(attachment, BorderLayout.SOUTH);
            attachment.setSelected(true);
        }

        private void setTextArea(JTextArea area) {
            area.setFont(Font.decode(Font.SANS_SERIF).deriveFont(12.f));
            area.setBorder(BorderFactory.createCompoundBorder(
                    area.getBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }

        boolean hasAttachment() {
            return attachment.isSelected();
        }

        String getUserText() {
            return area.getText();
        }

        String getUserMail() {
            return email.getText();
        }
    }
}
