package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private static Logger logger = LoggerFactory.getLogger(InstallDialogWindow.class);
    //applet info and setting GUI components
    private JTextField name = new JTextField(50);
    private JTextField author = new JTextField(10);
    private JTextField version = new JTextField(10);
    private JTextField sdk = new JTextField(10);
    private JTextField installParams = new JTextField(50);
    private JCheckBox forceInstall = new JCheckBox();
    private JCheckBox hasKeys = new JCheckBox();
    private JTextField[] customAIDs;
    private JPanel advanced = new JPanel();
    private ButtonGroup selectedAID = new ButtonGroup();
    //applet install info variables
    private AppletInfo info;
    private CAPFile src;
    private boolean isInstalled;

    private boolean initialized;
    private File customSignatureFile;
    private Color wrong = new Color(0xA3383D);
    public static final Pattern HEXA_PATTERN = Pattern.compile("[0-9a-fA-F]*");

    public InstallDialogWindow(CAPFile file, AppletInfo info, boolean isInstalled, String verifyMsg) {
        this.info = info;
        this.src = file;
        this.isInstalled = isInstalled;
        build(verifyMsg);
    }

    /**
     * Build advanced section of the dialog, if not called only metadata section shown
     * @param parent parent window to resize on switch
     */
    public void buildAdvanced(Window parent) {
        if (initialized) return;
        initAdvanced(parent);
        buildAdvanced();
    }

    public void buildAdvancedAndCustomSigned(Window parent) {
        if (initialized) return;
        initAdvanced(parent);
        buildAdvanced();
        buildCustomSigned();
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
                    sdk.getText(), aid, KeysPresence.UNKNOWN, GPRegistryEntry.Kind.Application);
        } else {
            details = info;
            info.setAID(aid);
        }

        if (isAdvanced())
            return new InstallOpts(getCustomAppletName(aid), details, forceInstall.isSelected(), installParams.getText());
        else return new InstallOpts(getCustomAppletName(aid), details, isInstalled, new byte[0]);
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

    public boolean validCustomAID() {
        return validAID(getCustomAppletName(getSelectedAID()));
    }

    public File getCustomSignatureFile() {
        logger.info("Custom signature file selected: " + customSignatureFile.getAbsolutePath());
        return customSignatureFile;
    }

    private void build(String verifiedMsg) {
        setLayout(new MigLayout("width 250px"));
        add(new HtmlText("<p width=\"600\">" + verifiedMsg + "</p>"), "wrap, span 5, gapbottom 10");

        add(new HtmlText("<p width=\"600\">" + textSrc.getString("W_do_not_unplug") + "</p>"),
                "wrap, span 5, gapbottom 10");

        add(new JLabel(textSrc.getString("pkg_id")), "span 2");
        add(new JLabel(src.getPackageAID().toString()), "span 3, wrap");
        buildMetaDataSection();
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

    private boolean isAdvanced() {
        return advanced.isVisible();
    }

    private void initAdvanced(Window parent) {
        if (parent == null)
            return;

        JLabel more = new JLabel(textSrc.getString("advanced_settings"), new ImageIcon(Config.IMAGE_DIR + "arrow_small.png"), JLabel.LEFT);
        more.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 12f));
        more.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(more, "gaptop 22, span 4, wrap");

        more.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (advanced.isVisible()) {
                    advanced.setVisible(false);
                    remove(advanced);
                } else {
                    advanced.setVisible(true);
                    add(advanced, "span 5, wrap");
                }
                parent.pack();
            }
        });
        advanced.setLayout(new MigLayout());
        advanced.setVisible(false);
        initialized = true;
    }

    private void buildAdvanced() {
        advanced.add(getHint("H_advanced_syntax", "300"), "span 5, wrap");
        advanced.add(new JLabel(textSrc.getString("applet_ids")), "span 2");

        addAllAppletCustomAIDSFields(advanced, src.getAppletAIDs());

        advanced.add(getHint("H_default_aid", "600"), "span 5, wrap");

        advanced.add(new JLabel(textSrc.getString("install_params")), "span 2");
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
        advanced.add(installParams, "span 3, wrap");
        advanced.add(getHint("H_install_params", "600"), "span 5, wrap");

        forceInstall.setSelected(isInstalled);
        advanced.add(forceInstall);

        advanced.add(new JLabel(textSrc.getString("force_install")), "span 4, wrap");
        advanced.add(getHint("H_force_install", "600"), "span 5, wrap");
        if (isInstalled) {
            advanced.add(new HtmlText("<p width=\"600\">" + textSrc.getString("W_installed") + "</p>"),
                    "wrap, span 5, gapbottom 10");
        }
    }

    private void buildCustomSigned() {
        JButton specifyCustomSignature = new JButton(textSrc.getString("custom_sign_button"));
        specifyCustomSignature.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getShaderFileChoser(FileSystemView.getFileSystemView().getDefaultDirectory());
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    customSignatureFile = fileChooser.getSelectedFile();
                    specifyCustomSignature.setText(customSignatureFile.getName());
                }
            }
        });
        advanced.add(specifyCustomSignature, "span 5, wrap");
        advanced.add(getHint("H_custom_sign", "600"), "span 5, wrap");
    }

    private JFileChooser getShaderFileChoser(File defaultFolder) {
        JFileChooser fileChooser = new JFileChooser(defaultFolder);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }

    /**
     * List all applets to possibly install
     * @param applets list to install
     */
    private void addAllAppletCustomAIDSFields(JPanel to, List<AID> applets) {
        customAIDs = new JTextField[applets.size()];
        int i = 0;
        for (AID applet : applets) {
            JRadioButton button = new JRadioButton();
            button.setActionCommand(applet.toString());
            selectedAID.add(button);

            JTextField f = new JTextField(applet.toString(), 50);
            f.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    f.setForeground(validAID(f) ? Color.BLACK : wrong);
                    button.setToolTipText(f.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    f.setForeground(validAID(f) ? Color.BLACK : wrong);
                    button.setToolTipText(f.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    f.setForeground(validAID(f) ? Color.BLACK : wrong);
                    button.setToolTipText(f.getText());
                }
            });
            to.add(f, "span 2");
            to.add(button, "wrap");
            to.add(new JLabel(), "span 2"); //empty label to align
            customAIDs[i++] = f;
        }
        to.add(new JLabel(), "wrap"); //cut line
        //set first selected
        Enumeration elements = selectedAID.getElements();
        if (elements.hasMoreElements()) {
            AbstractButton button = (AbstractButton) elements.nextElement();
            button.setSelected(true);
        }
    }

    private String valueOrDefault(String data, String defaultValue) {
        if (data == null || data.isEmpty()) return defaultValue;
        return data;
    }

    private String getCustomAppletName(String defaultOpt) {
        Enumeration elements = selectedAID.getElements();
        while (elements.hasMoreElements()) {
            AbstractButton button = (AbstractButton)elements.nextElement();
            if (button.isSelected()) {
                return valueOrDefault(button.getToolTipText(), defaultOpt);
            }
        }
        return defaultOpt;
    }

    private String getSelectedAID() {
        if (selectedAID.getSelection() == null) {
            List<AID> aids = src.getAppletAIDs();
            return (aids.size() > 0) ? src.getAppletAIDs().get(0).toString() : null;
        }
        return selectedAID.getSelection().getActionCommand();
    }

    private void enableAll(boolean enable) {
        for (JTextField f : customAIDs) {
            f.setEnabled(enable);
        }
    }

    private JLabel getHint(String langKey, String width) {
        JLabel hint = new HtmlText("<p width=\"" + width + "\">" + textSrc.getString(langKey) + "</p>", 10f);
        hint.setForeground(Color.DARK_GRAY);
        return hint;
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
