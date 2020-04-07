package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.TextField;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.URLAdapter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CrashReporter {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final String system =
            System.getProperty("os.name") + "_" +
                    System.getProperty("os.arch") + "_" +
                    System.getProperty("os.version");

    /**
     * Create a crash reporter
     * @param title title to display on window
     * @param description error description
     * @param parent parent component
     */
    public CrashReporter(String title, String description, Component parent) {
        if (title.isEmpty()) title = "JCAppStore reporter";
        if (description == null)
            description = textSrc.getString("unknown_error");

        final String message = description + "<br><br>Running on " + system + ", java "
                + System.getProperty("java.version") + " by " + System.getProperty("java.vendor");
        final String header = title;

            JOptionPane.showMessageDialog(
                    parent,
                    new FeedbackConfirmPane(message),
                    header,
                    JOptionPane.ERROR_MESSAGE,
                    new ImageIcon(Config.IMAGE_DIR + "bug.png"));

    }

    /**
     * GUI error-feedback form class
     */
    private static class FeedbackConfirmPane extends JPanel {
        FeedbackConfirmPane(String message) {
            super(new MigLayout());
            SwingUtilities.invokeLater(() -> build(message));
        }

        private void build(String message) {
            add(new HtmlText("<div width=\"400\">" + textSrc.getString("attachment_desc") + "</div>" +
                    "<div>&emsp;</div>"), "wrap");

            JTextPane error = TextField.getTextField("<div width=\"400\"><b>" + textSrc.getString("error_desc") +
                    "</b><br><br>" + message + "</div>");
            error.setComponentPopupMenu(TextField.getCopyMenu());
            error.setPreferredSize(new Dimension(400, 150));
            error.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLACK),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            add(error, "wrap");

            JLabel url = new HtmlText("<div style=\"margin: 5px;\">" + textSrc.getString("issues_url") + "</div>",
                    new ImageIcon(Config.IMAGE_DIR + "web.png"), Font.BOLD, 15f, SwingConstants.CENTER);
            url.setCursor(new Cursor(Cursor.HAND_CURSOR));
            url.addMouseListener(new URLAdapter(Config.REPO_ISSUES));
            add(url, " wrap");

            add(new HtmlText("<div width=\"400\">" + textSrc.getString("how_to_fill") + "</div>"), "wrap");

            JButton button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Desktop.getDesktop().open(Config.APP_LOG_DIR);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, textSrc.getString("e_open_folder"),
                                textSrc.getString("e_open_folder_title"), JOptionPane.ERROR_MESSAGE,
                                new ImageIcon(Config.IMAGE_DIR + "error.png"));
                    }
                }
            });
            button.setText("<html><div style=\"margin: 1px 10px;\">" +
                    textSrc.getString("log_folder") + "</div></html>");
            button.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 16f));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            button.setFocusPainted(false);
            add(button, "align right, wrap");

            SwingUtilities.getWindowAncestor(this).pack();
            SwingUtilities.getWindowAncestor(this).setLocationRelativeTo(null);
        }
    }
}
