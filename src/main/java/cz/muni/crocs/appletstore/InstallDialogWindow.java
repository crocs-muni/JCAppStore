package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.crypto.KeyBase;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HtmlLabel;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;
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
import java.util.regex.Pattern;

/**
 * Dialog window for applet installation to show install options
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallDialogWindow extends JPanel {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private JTextField name = new JTextField(50);
    private JTextField author = new JTextField(10);
    private JTextField version = new JTextField(10);
    private JTextField sdk = new JTextField(10);
    private JTextField installParams = new JTextField(50);
    private JCheckBox forceInstall = new JCheckBox();
    private JCheckBox hasKeys = new JCheckBox();
    private JTextField[] customAIDs;
    private JCheckBox advanced = new JCheckBox();
    private ButtonGroup selectedAID = new ButtonGroup();
    private AppletInfo info;

    private Color wrong = new Color(0xA3383D);

    private static final Pattern HEXA_PATTERN = Pattern.compile("[0-9a-fA-F]*");

    public InstallDialogWindow(CAPFile file, AppletInfo info, boolean isInstalled, String verifyMsg) {
        this.info = info;
        build(file, isInstalled, verifyMsg);
    }

    private void build(CAPFile file, boolean installed, String verified) {
        setLayout(new MigLayout("width 250px"));
        add(new HtmlLabel("<p width=\"600\">" + verified + "</p>"),
                "wrap, span 5, gapbottom 10");
        if (installed) {
            add(new HtmlLabel("<p width=\"600\">" + textSrc.getString("W_installed") + "</p>"),
                    "wrap, span 5, gapbottom 10");
        }

        add(new HtmlLabel("<p width=\"600\">" + textSrc.getString("W_do_not_unplug") + "</p>"),
                "wrap, span 5, gapbottom 10");

        add(new JLabel(textSrc.getString("pkg_id")), "span 2");
        add(new JLabel(file.getPackageAID().toString()), "span 3, wrap");
        buildMetaDataSection();
        buildAdvanced(file, installed);
    }

    private void buildMetaDataSection() {
        add(new JLabel(textSrc.getString("applet_name")), "span 2");
        name.setText(info == null ? textSrc.getString("unknown") : info.getName());
        name.setEnabled(info == null);
        add(name, "span 3, wrap");

        add(new JLabel(textSrc.getString("author")), "span 2");
        author.setText(info == null ? textSrc.getString("unknown") : info.getAuthor());
        author.setEnabled(info == null);
        add(author, "span 3, wrap");

        add(new JLabel(textSrc.getString("custom_version") + ": "), "span 2");
        version.setText(info == null ? textSrc.getString("unknown") : info.getVersion());
        version.setEnabled(info == null);
        add(version, "span 3, wrap");

        add(new JLabel(textSrc.getString("sdk_version")), "span 2");
        sdk.setText(info == null ? textSrc.getString("unknown") : info.getSdk());
        sdk.setEnabled(info == null);
        add(sdk, "span 3, wrap");

        hasKeys.setEnabled(true);
        hasKeys.setSelected(info != null && info.hasKeys == KeysPresence.PRESENT);
        hasKeys.setEnabled(info == null);
        add(hasKeys);

        add(new JLabel(textSrc.getString("has_keys")), "span 4, wrap");
        add(getHint("H_has_keys", "600"), "span 5, wrap");
    }

    private void buildAdvanced(CAPFile file, boolean installed) {
        JLabel more = new JLabel(textSrc.getString("advanced_settings"));
        more.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 12f));
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

        add(new JLabel(textSrc.getString("applet_ids")), "span 2");

        addAllAppletCustomAIDSFields(file.getAppletAIDs());

        add(getHint("H_default_aid", "600"), "span 5, wrap");

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

        forceInstall.setEnabled(false);
        forceInstall.setSelected(installed);
        add(forceInstall);

        add(new JLabel(textSrc.getString("force_install")), "span 4, wrap");
        add(getHint("H_force_install", "600"), "span 5, wrap");
    }

    /**
     * List all applets to possibly install
     * @param applets list to install
     */
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



    private int getSelectedIdx() {
        int result = 0;
        Enumeration elements = selectedAID.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = (AbstractButton)elements.nextElement();
            if (button.isSelected()) {
                return result;
            }
            ++result;
        }
        return 0;
    }

    private String getSelectedAID() {
        if (selectedAID.getSelection() == null)
            return null;
        return selectedAID.getSelection().getActionCommand();
    }

    private void enableAll(boolean enable) {
        for (JTextField f : customAIDs) {
            f.setEnabled(enable);
        }
    }

    private JLabel getHint(String langKey, String width) {
        JLabel hint = new HtmlLabel("<p width=\"" +
                width + "\">" + textSrc.getString(langKey) + "</p>");
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    /**
     * Get applet install information
     * @return null if basic installation,
     * array with [installation arguments, force install, selected applet to install] values
     */
    public InstallOpts getInstallOpts() {
        String aid = getSelectedAID();
        AppletInfo details;
        if (info == null) {
            details = new AppletInfo(name.getText(), null, author.getText(), version.getText(),
                    sdk.getText(), aid, KeysPresence.UNKNOWN);
        } else {
            details = info;
            info.setAID(aid);
        }

        if (advanced.isSelected())
            return new InstallOpts(getSelectedIdx(), details, forceInstall.isSelected(), installParams.getText());
        else return new InstallOpts(getSelectedIdx(), details, forceInstall.isSelected(), new byte[0]);
    }

    public boolean validInstallParams() {
        String text = installParams.getText();
        return text == null || validHex(text);
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
        return validHex(aid) && aid.length() <= 32;
    }

    private static boolean validHex(String hex) {
        return hex.isEmpty() || (HEXA_PATTERN.matcher(hex.toLowerCase()).matches() && hex.length() % 2 == 0);
    }
}
