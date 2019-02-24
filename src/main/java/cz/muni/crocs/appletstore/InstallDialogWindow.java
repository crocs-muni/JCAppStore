package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallDialogWindow extends JPanel {

    private JTextField installParams = new JTextField(50);
    private JCheckBox forceInstall = new JCheckBox();
    private JTextField customAID = new JTextField(50);

    private Color wrong = new Color(0xC01628);

    public boolean hasAdditionalInfo() {
       return installParams.isEnabled();
    }

    public InstallDialogWindow() {
        setLayout(new MigLayout());
        add(new JLabel("<html><p width=\"600\">" + Config.translation.get(131) + "</p></html>"),
                "wrap, span 5, gapbottom 20");

        JLabel more = new JLabel(Config.translation.get(132));
        more.setFont(CustomFont.plain.deriveFont(Font.BOLD, 12f));
        add(more, "span 4");

        JCheckBox advanced = new JCheckBox();
        advanced.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                installParams.setEnabled(advanced.isSelected());
                forceInstall.setEnabled(advanced.isSelected());
                customAID.setEnabled(advanced.isSelected());
            }
        });
        add(advanced, "wrap");

        add(new JLabel(Config.translation.get(133)), "span 2");
        installParams.setEnabled(false);
        installParams.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                installParams.setForeground(validInstallParams() ? Color.BLACK : wrong);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                installParams.setForeground(validInstallParams() ? Color.BLACK : wrong);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                installParams.setForeground(validInstallParams() ? Color.BLACK : wrong);
            }
        });
        add(installParams, "span 3, wrap");
        add(getHint(134), "span 5, wrap");

        add(new JLabel(Config.translation.get(137)), "span 2");
        customAID.setEnabled(false);
        customAID.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                customAID.setForeground(validAID() ? Color.BLACK : wrong);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                customAID.setForeground(validAID() ? Color.BLACK : wrong);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                customAID.setForeground(validAID() ? Color.BLACK : wrong);
            }
        });
        add(customAID, "span 3, wrap");
        add(getHint(138), "span 5, wrap");

        add(forceInstall);
        forceInstall.setEnabled(false);
        add(new JLabel(Config.translation.get(135)), "span 4, wrap");
        add(getHint(136), "span 5, wrap");
    }

    private JLabel getHint(int translationId) {
        JLabel hint = new JLabel("<html><p width=\"600\">" + Config.translation.get(translationId) + "</p></html>");
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    public String[] getAdditionalInfo() {
        return new String[]{ installParams.getText(), customAID.getText(), (forceInstall.isSelected() ? "yes" : "no") };
    }

    public boolean validInstallParams() {
        return installParams.getText().length() % 2 == 0;
    }

    public boolean validAID() {
        String text = customAID.getText();
        int length = text.length();
        for (int i = 0; i < length; i++){
            char c = text.charAt(i);
            if ("0123456789ABCDEFabcdef".indexOf(c) < 0) {
                return false;
            }
        }
        return length % 2 == 0 && length < 22;
    }
}
