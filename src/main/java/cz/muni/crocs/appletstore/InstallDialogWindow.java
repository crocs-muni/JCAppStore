package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallDialogWindow extends JPanel {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private JTextField installParams = new JTextField(50);
    private JCheckBox forceInstall = new JCheckBox();
    private JTextField[] customAIDs;
    private JCheckBox advanced = new JCheckBox();
    private ButtonGroup selectedAID = new ButtonGroup();

    private Color wrong = new Color(0xC01628);

    public InstallDialogWindow(CAPFile file) {

        setLayout(new MigLayout("width 250px"));
        add(new JLabel("<html><p width=\"600\">" + textSrc.getString("W_do_not_unplug") +
                        "</p></html>"), "wrap, span 5, gapbottom 10");

        add(new JLabel("<html><p width=\"600\">" + textSrc.getString("pkg_id") +
                file.getPackageAID().toString() + "</p></html>"),
                "wrap, span 5, gapbottom 20");

        JLabel more = new JLabel(textSrc.getString("advanced_settings"));
        more.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(Font.BOLD, 12f));
        add(more, "span 2");

        advanced.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                installParams.setEnabled(advanced.isSelected());
                forceInstall.setEnabled(advanced.isSelected());
                enableAll(advanced.isSelected());
            }
        });
        add(advanced);

        add(getHint("H_advanced_syntax", "300"), "wrap");

        add(new JLabel(textSrc.getString("install_params")), "span 2");
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
        add(getHint("H_install_params", "600"), "span 5, wrap");

        add(new JLabel(textSrc.getString("applet_ids")), "span 2");

        addAllAppletCustomAIDSFields(file.getAppletAIDs());

        add(getHint("H_default_aid", "600"), "span 5, wrap");

        add(forceInstall);
        forceInstall.setEnabled(false);
        add(new JLabel(textSrc.getString("force_install")), "span 4, wrap");
        add(getHint("H_force_install", "600"), "span 5, wrap");
    }

    private void addAllAppletCustomAIDSFields(List<AID> applets) {
        customAIDs = new JTextField[applets.size()];
        int i = 0;
        for (AID applet : applets) {
            JTextField f = new JTextField(applet.toString(), 50);
            f.setEnabled(false);
            f.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    f.setForeground(validAID(f) ? Color.BLACK : wrong);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    f.setForeground(validAID(f) ? Color.BLACK : wrong);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    f.setForeground(validAID(f) ? Color.BLACK : wrong);
                }
            });
            add(f, "span 2");

            JRadioButton button = new JRadioButton();
            button.setActionCommand(applet.toString());
            selectedAID.add(button);
            add(button, "wrap");
            add(new JLabel(), "span 2"); //empty label to align
            customAIDs[i++] = f;
        }
        add(new JLabel(), "wrap"); //cut line
        //set first selected
        Enumeration elements = selectedAID.getElements();
        if (elements.hasMoreElements()) {
            AbstractButton button = (AbstractButton) elements.nextElement();
            button.setSelected(true);
        }
    }

    private String getSelectedAID() {
        if (selectedAID.getSelection() == null) return null;
        return selectedAID.getSelection().getActionCommand();
    }

    private void enableAll(boolean enable) {
        for (JTextField f : customAIDs) {
            f.setEnabled(enable);
        }
    }

    private JLabel getHint(String langKey, String width) {
        JLabel hint = new JLabel("<html><p width=\"" +
                width + "\">" + textSrc.getString(langKey) + "</p></html>");
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    public String[] getAdditionalInfo() {
        if (advanced.isSelected())
            return new String[]{installParams.getText(),
                    forceInstall.isSelected() ? "yes" : "no", getSelectedAID()};
        return null;
    }

    public boolean validInstallParams() {
        return installParams.getText().length() % 2 == 0;
    }

    public boolean validAID() {
        String aid = getSelectedAID();
        if (aid == null)
            return false;
        return validAID(aid);
    }

    private static boolean validAID(JTextField field) {
        return validAID(field.getText());
    }

    private static boolean validAID(String aid) {
        int length = aid.length();
        for (int i = 0; i < length; i++){
            char c = aid.charAt(i);
            if ("0123456789ABCDEFabcdef".indexOf(c) < 0) {
                return false;
            }
        }
        return length % 2 == 0 && length < 32;
    }
}
