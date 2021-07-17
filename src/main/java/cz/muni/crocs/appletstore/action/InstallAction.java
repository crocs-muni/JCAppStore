package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.InstallDialogWindow;
import cz.muni.crocs.appletstore.ReinstallWarnPanel;
import cz.muni.crocs.appletstore.action.applet.JCMemory;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.crypto.Signature;
import cz.muni.crocs.appletstore.crypto.SignatureImpl;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Dialog;
import java.awt.HeadlessException;
import java.awt.Container;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static javax.swing.JOptionPane.*;
import static pro.javacard.gp.GPRegistryEntry.Kind;

/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallAction extends CardAbstractAction<Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(InstallAction.class);

    private final InstallBundle data;
    private final boolean installed;
    private String defaultSelected;
    private CAPFile code;

    private boolean fromCustomFile = false;

    /**
     * Create an install action
     *
     * @param installData data for install
     * @param installed   whether installed on the card already
     * @param call        callback that is called before action and after failure or after success
     */
    public InstallAction(InstallBundle installData, boolean installed, String defaultSelected, OnEventCallBack<Void, Void> call) {
        super(call);
        this.data = installData;
        this.installed = installed;
        this.defaultSelected = defaultSelected;
    }

    /**
     * Create install action from custom source
     * @param call callback on events start/fail/finish
     */
    public InstallAction(OnEventCallBack<Void, Void> call) {
        this(InstallBundle.empty(), false, null, call);
        this.fromCustomFile = true;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!CardManagerFactory.getManager().isCard()) {
            InformerFactory.getInformer().showInfo(textSrc.getString("missing_card"),
                    Notice.Importance.SEVERE, Notice.CallBackIcon.CLOSE, null, 7000);
            return;
        }

        if (fromCustomFile) data.setCapfile(CapFileChooser.chooseCapFile());
        code = CapFileChooser.getCapFile(data.getCapfile());
        if (code == null) {
            return;
        }

        if (fromCustomFile) {
            runCustomInstallationGuide();
        } else {
            runStoreInstallationGuide();
        }
    }

    //shows the dialog window without verifying the signature
    private void runCustomInstallationGuide() {
        showInstallDialog(textSrc.getString("custom_file"), "verify_no_pgp.png", null, null, true);
    }

    //shows the dialog window AFTER verifying the signature
    private void runStoreInstallationGuide() {
        doAsyncWorkRoutine(textSrc.getString("H_pgp_loading"), new Executable<Tuple<Integer, String>>() {
            @Override
            void work() {
                final Signature signature = new SignatureImpl();
                try {
                    result = signature.verifyPGPAndReturnMessage(data.getCapfile());
                    if (data.getSigner() != null && !data.getSigner().isEmpty()) {
                        Tuple<Integer, String> another = signature.verifyPGPAndReturnMessage(data.getFingerprint(),
                                data.getCapfile(), signature.getSignatureFileFromString(data.getSigner(), data.getCapfile().getAbsolutePath()));
                        result = new Tuple<>(another.first,
                                "JCAppStore: " + result.second + "<br>" + data.getSigner() + ": " + another.second);
                    }
                } catch (LocalizedSignatureException e) {
                    logger.warn("Signature verification failed", e);
                    result = new Tuple<>(2, e.getLocalizedMessage());
                }
            }

            @Override
            void after() {
                final Tuple<Integer, String> signResult = result;
                doAsyncWorkRoutine("Verify dependencies...", new Executable<Tuple<String, String>>() {
                    @Override
                    void work() {
                        this.result = checkDependencies();
                    }

                    @Override
                    void after() {
                        if (result == null) {
                            logger.info("Dependency file not found;");
                            showInstallDialog(signResult.second, Signature.getImageByErrorCode(signResult.first),
                                    null, null, false);
                        } else {
                            showInstallDialog(signResult.second, Signature.getImageByErrorCode(signResult.first),
                                    result.first, result.second, false);
                        }
                    }
                });

            }
        });
    }

    //display intallation dialog window
    private void showInstallDialog(String verifyResult, String imgIcon, String issueMsg, String issueDetails, final boolean isCustom) {
        final boolean forceInstall = installed; /*todo removed || OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE);*/
        InstallDialogWindow dialog = new InstallDialogWindow(code, data.getInfo(), forceInstall, verifyResult,
                issueMsg, issueDetails, isCustom, data.getAppletNames());
        String[] buttons = new String[]{textSrc.getString("install"), textSrc.getString("cancel")};
        CustomJOptionPane pane = new CustomJOptionPane(dialog, new ImageIcon(Config.IMAGE_DIR + imgIcon), buttons, "error");
        pane.setOnClose(() -> {
            int selectedValue = CustomJOptionPane.getSelectedValue(buttons, pane.getValue());
            switch (selectedValue) {
                case JOptionPane.YES_OPTION:
                    //invalid data
                    if (!dialog.validCustomAIDs() || !dialog.validInstallParams()) {
                        InformerFactory.getInformer().showInfoMessage(textSrc.getString("E_install_invalid_data"), "error.png");
                        showInstallDialog(pane);
                        return null;
                    } else if (!dialog.getInstallOpts().isForce()) { //check if custom AID is not conflicting
                        logger.info("No force install: check the applets");
                        if (someCustomAppletAIDsConflicts(dialog.getInstallOpts().getCustomAIDs())) {
                            InformerFactory.getInformer().showInfoMessage(textSrc.getString("E_install_already_present"), "warn.png");
                            showInstallDialog(pane);
                            return null;
                        }
                    }
                    break;
                case JOptionPane.NO_OPTION:
                case CLOSED_OPTION:
                    return null;
            }

            if (isCustom) {
                verifyCustomInstallationAndInstall(dialog);
            } else {
                fireInstall(dialog.getInstallOpts());
            }
            return null;
        });
        showInstallDialog(pane);
    }

    //modal dialog window displayer
    private void showInstallDialog(JOptionPane pane) {
        JDialog window = pane.createDialog(textSrc.getString("CAP_install_applet") + data.getTitleBar());
        window.setModal(false);
        window.pack();
        window.setVisible(true);
    }

    //collision finder
    private boolean someCustomAppletAIDsConflicts(String[] aids) {
        Set<AppletInfo> applets = CardManagerFactory.getManager().getCard().getCardMetadata().getApplets();
        for (AppletInfo applet : applets) {
            for (String customAID : aids) {
                if (applet.getAid().equals(AID.fromString(customAID))) {
                    logger.info("applet: " + applet.getAid() + ", with " + customAID);
                    return true;
                }
            }
        }
        return false;
    }

    //verifies signature of custom installation AFTER dialog window closed
    private void verifyCustomInstallationAndInstall(final InstallDialogWindow resultDialog) {
        if (resultDialog == null) return;
        final File customSign = resultDialog.getCustomSignatureFile();
        if (customSign != null) {
            doAsyncWorkRoutine(textSrc.getString("H_pgp_loading"), new Executable<Tuple<Integer, String>>() {
                @Override
                void work() {
                    final Signature signature = new SignatureImpl();
                    try {
                        result = signature.verifyPGPAndReturnMessage(null, data.getCapfile(), customSign);
                    } catch (LocalizedSignatureException e) {
                        logger.warn("Signature verification failed", e);
                        result = new Tuple<>(2, e.getLocalizedMessage());
                    }
                }

                @Override
                void after() {
                    //confirm install only if signature failed
                    int choice = (result.first == 0) ? YES_OPTION :
                            JOptionPane.showConfirmDialog(null,
                            "<html><div width=\"350\">" + result.second + "<br>" +
                                    textSrc.getString("install_ask") + "</div></html>",
                            textSrc.getString("signature_title_dialog"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon(Config.IMAGE_DIR + Signature.getImageByErrorCode(result.first)));
                    if (choice == YES_OPTION) {
                        fireInstall(resultDialog.getInstallOpts());
                    }
                }
            });
        } else {
            fireInstall(resultDialog.getInstallOpts());
        }
    }

    /**
     * Perform various pre-install checks (memory available, force installs, warns) and fires install
     * @param opts install options from the install form
     */
    private void fireInstall(final InstallOpts opts) {
        final CardManager manager = CardManagerFactory.getManager();
        if (!manager.isCard()) {
            return;
        }
        logger.info("Install fired, list of AIDS: " + code.getApplets().toString());
        logger.info("Install AID: " + opts.getAIDs());

        if (opts.isForce() && !userAcceptsForceInstallWarn(manager.getCard())) {
            return;
        }

        new FreeMemoryAction(new OnEventCallBack<Void, byte[]>() {
            @Override
            public void onStart() {
                call.onStart();
            }

            @Override
            public void onFail() {
                call.onFail();
            }

            @Override
            public Void onFinish() {
                call.onFinish();
                return null;
            }

            @Override
            public Void onFinish(byte[] value) {
                if (value == null) {
                    doInstall(opts, manager);
                    return null;
                }
                int cardMemory = JCMemory.getPersistentMemory(value);
                long size;
                try {
                    size = data.getCapfile().length();
                } catch (SecurityException e) {
                    logger.warn("Failed to obtain the capfile size", e);
                    e.printStackTrace();
                    size = 0; //pretend nothing happened
                }
                //do not add instance install size to the limit, the actual zip file is bigger than on card
                //todo JCALgTEST is NOT detected as its installation size exceeds JCMemory.LIMITED_BY_API
                if (!installed && cardMemory < JCMemory.LIMITED_BY_API) {
                    int installationSize = Math.max((int)(size / 6), 1024);

                    if (manager.getCard().getCardMetadata().isPackagePresent(code.getPackageAID()) && cardMemory > 1024) { //do not include pkg size if already present
                        doInstall(opts, manager);
                    } else if (cardMemory <= (int)(size + installationSize)) {
                        int res = showConfirmDialog(null,
                                "<html><p style='width: 350px;'>" + textSrc.getString("no_space_1") +
                                        size + ", " + textSrc.getString("no_space_2") +
                                        cardMemory + ". " + textSrc.getString("no_space_3") + "</p></html>",
                                textSrc.getString("ifaq_low_memory_title"), YES_NO_OPTION, INFORMATION_MESSAGE,
                                new ImageIcon(Config.IMAGE_DIR + "error.png"));
                        if (res == YES_OPTION) {
                            doInstall(opts, manager);
                        } else {
                            call.onFinish();
                            return null;
                        }
                    } else doInstall(opts, manager);
                } else doInstall(opts, manager);
                return null;
            }
        }).start();
    }

    private boolean userAcceptsForceInstallWarn(CardInstance card) {
        //for (AppletInfo info: card.getCardMetadata().getApplets()) {
            //todo recognize applet has onlyone package and do not bother the user!!!!
        //}

        if (OptionsFactory.getOptions().is(Options.KEY_WARN_FORCE_INSTALL)) {
            ReinstallWarnPanel warn = new ReinstallWarnPanel();
            if (showOptionDialog(null, warn,
                    textSrc.getString("reinstall_warn_title"), YES_NO_OPTION, QUESTION_MESSAGE,
                    new ImageIcon(Config.IMAGE_DIR + "announcement.png"),
                    new String[]{textSrc.getString("continue"), textSrc.getString("cancel")},
                    "error") == YES_OPTION) {
                OptionsFactory.getOptions().addOption(Options.KEY_WARN_FORCE_INSTALL,
                        "" + (!warn.userSelectedDontShowAgain()));
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Actual installation
     *
     * @param opts    options from the install form modified by fireInstall() method
     *                (e.g. simple use mode adds force install if package present)
     * @param manager card manager instance
     */
    private void doInstall(final InstallOpts opts, final CardManager manager) {
        if (!manager.isCard()) {
            return;
        }

        if (defaultSelected != null && !defaultSelected.isEmpty()) {
            //custom applet never reaches this section
            AID selected = manager.getCard().getDefaultSelected();
            AppletInfo info = manager.getCard().getInfoOf(selected);
            if (info != null && info.getKind() != Kind.IssuerSecurityDomain && info.getKind() != Kind.SecurityDomain) {
                int result = JOptionPane.showOptionDialog(null,
                        "<html><div width=\"600\">" + textSrc.getString("default_selected_ask1") +
                                info.getName() + "<br>" + textSrc.getString("default_selected_ask2") +
                                opts.getName() + "</div></html>",
                        textSrc.getString("default_selected_ask_title"),
                        YES_NO_OPTION, PLAIN_MESSAGE,
                        new ImageIcon("src/main/resources/img/info.png"),
                        new String[]{textSrc.getString("default_selected_yes"),
                                textSrc.getString("default_selected_no")},
                        textSrc.getString("default_selected_yes"));
                if (result != YES_OPTION) defaultSelected = null;
            } // else defaultSelected == true -> silently set as default selected
        }
        opts.setDefalutSelected(defaultSelected);
        execute(() -> {
            try {
                manager.install(code, opts);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        InformerFactory.getInformer().showInfoToClose(textSrc.getString("install_failed"),
                                Notice.Importance.SEVERE, 4000));
                throw e;
            }

            SwingUtilities.invokeLater(() ->
                    InformerFactory.getInformer().showInfoToClose(textSrc.getString("installed"),
                            Notice.Importance.INFO, 4000));
            data.setCapfile(null);
            return null;

        }, "Failed to install applet.", textSrc.getString("install_failed"), 5, TimeUnit.MINUTES);
    }

    /**
     * Find closest file to version available and verify dependencies
     * @return tuple: 1st string is a short message, second contains more detailed information on the problem
     *         null: no compatibility issues found
     */
    private Tuple<String, String> checkDependencies() {
        CardInstance card = CardManagerFactory.getManager().getCard();
        HashMap<String, HashMap<String, String>> capabilities = card.getCardMetadata().getJCData();
        if (capabilities == null) return new Tuple<>(textSrc.getString("not_found"), null);

        String[] versions = JsonParser.jsonArrayToStringArray(data.getDataSet().getAsJsonArray(JsonParser.TAG_VERSION));
        Arrays.sort(versions, Comparator.reverseOrder());

        StringBuilder unmetRequirements = new StringBuilder();

        for (String version : versions) {
            File file = new File(data.getStoreFolder() + Config.S + "requirements_" + version + ".txt");
            if (file.exists()) {
                try (BufferedReader input = new BufferedReader(new FileReader(file))) {
                    HashMap<String, String> category = null;
                    String line;
                    while ((line = input.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            if (category == null) {
                                category = capabilities.get(line);
                            }
                            else {
                                String required = category.get(line);
                                if (required == null || !required.equals("yes")) {
                                    unmetRequirements.append(line).append(", ");
                                }
                            }
                        } else category = null;
                    }
                } catch (IOException e) {
                    logger.error("Could not read file: " + file.getAbsolutePath(), e);
                    return new Tuple<>("jcfile_failure", null);
                }
                break;
            } //else //not applicable, missing file = no dependencies
        }

        String result = unmetRequirements.toString();
        String sdkVersion = null;
        HashMap<String, String> jcsupportVersion = capabilities.get("JavaCard support version");
        if (jcsupportVersion != null) sdkVersion = jcsupportVersion.get("JavaCard support version");

        StringBuilder sdk = new StringBuilder();
        if (sdkVersion == null) {
            //todo do not bother the user if SDK version not found?
            //sdk.append("<br><p style='width:350px;'>").append(textSrc.getString("jcdia_nosdk")).append("</p>");
        } else if (!sdkVersion.equals(data.getInfo().getSdk())) {
            sdk.append("<br><p>").append(textSrc.getString("your_sdk")).append(data.getInfo().getSdk())
                    .append("</p><p>").append(textSrc.getString("applet_sdk")).append(sdkVersion).append("</p>");
        } else if (result.isEmpty()) return null;

        return new Tuple<>(textSrc.getString((result.isEmpty() ? "not_found_sdk" : "unmet_requirements")),
                getReport(capabilities.get("Header"), sdk.toString(), result));
    }

    private String getReport(HashMap<String, String> header, String sdkReport, String requirementsReport) {
        if (requirementsReport.isEmpty() && sdkReport.isEmpty()) return null;

        StringBuilder result = new StringBuilder();
        result.append("<div style='text-align:right;'>").append(textSrc.getString("test_date"))
                .append(header.get("Execution date/time")).append("</div><h3>")
                .append(header.get("Card name")).append("</h3><p>")
                .append(textSrc.getString("reader_user")).append(header.get("Used reader")).append("</p><br>");
        result.append(sdkReport);
        if (!requirementsReport.isEmpty()) {
            result.append("<br><p>").append(textSrc.getString("not_supported_requirements_list"))
                    .append("</p><p style='width:350px;'>")
                    .append(requirementsReport, 0, requirementsReport.length() - 2).append("</p>");
        }
        result.append("<br><p style='width:350px;'>").append(textSrc.getString("jc_test_note")).append("</p>");
        return result.toString();
    }

    //routine used to verify the signatures
    private static void doAsyncWorkRoutine(String msg, Executable<?> task) {
        JOptionPane pane = new JOptionPane(msg,
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION,
                new ImageIcon(Config.IMAGE_DIR + "verify_loading.png"),
                new Object[]{}, null);

        JDialog dialog = pane.createDialog(null, textSrc.getString("wait_sec"));
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                task.work();
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                task.after();
            }
        }.execute();
        dialog.setVisible(true);
    }

    //executable abstraction callback for signature verification worker routine
    private abstract static class Executable<T> {
        T result;

        abstract void work();
        abstract void after();
    }

    /**
     * Copied and modified code from JOptionPane Swing class
     * enables custom JOptionPane behaviour
     */
    private static class CustomJOptionPane extends JOptionPane {

        private CallBack<Void> onClose;

        CustomJOptionPane(Object message, Icon icon, Object[] options, Object initialValue) {
            super(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon, options, initialValue);
        }

        void setOnClose(CallBack<Void> onClose) {
            this.onClose = onClose;
        }

        static int getSelectedValue(Object[] options, Object selectedValue) {
            Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (fo != null && fo.isShowing()) {
                fo.requestFocus();
            }
            if (selectedValue == null) {
                return CLOSED_OPTION;
            }
            if (options == null) {
                if (selectedValue instanceof Integer) {
                    return (Integer) selectedValue;
                }
                return CLOSED_OPTION;
            }
            for (int counter = 0, maxCounter = options.length;
                 counter < maxCounter; counter++) {
                if (options[counter].equals(selectedValue)) {
                    return counter;
                }
            }
            return CLOSED_OPTION;
        }

        @Override
        public JDialog createDialog(String title) throws HeadlessException {
            JDialog dialog = new JDialog((Dialog) null, title, true);
            initDialog(dialog);
            return dialog;
        }

        private void initDialog(final JDialog dialog) {
            dialog.setComponentOrientation(this.getComponentOrientation());
            Container contentPane = dialog.getContentPane();

            contentPane.setLayout(new BorderLayout());
            contentPane.add(this, BorderLayout.CENTER);
            dialog.setResizable(false);
            if (JDialog.isDefaultLookAndFeelDecorated()) {
                boolean supportsWindowDecorations =
                        UIManager.getLookAndFeel().getSupportsWindowDecorations();
                if (supportsWindowDecorations) {
                    dialog.setUndecorated(true);
                    getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
                }
            }
            dialog.pack();
            dialog.setLocationRelativeTo(null);

            final CustomJOptionPane self = this;
            final PropertyChangeListener listener = event -> {
                // Let the defaultCloseOperation handle the closing
                // if the user closed the window without selecting a button
                // (newValue = null in that case).  Otherwise, close the dialog.
                if (dialog.isVisible() && event.getSource() == self &&
                        (event.getPropertyName().equals(VALUE_PROPERTY)) &&
                        event.getNewValue() != null &&
                        event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    dialog.setVisible(false);
                    onClose.callBack();
                }
            };

            WindowAdapter adapter = new WindowAdapter() {
                private boolean gotFocus = false;

                public void windowClosing(WindowEvent we) {
                    setValue(null);
                }

                public void windowClosed(WindowEvent e) {
                    removePropertyChangeListener(listener);
                    dialog.getContentPane().removeAll();
                }

                public void windowGainedFocus(WindowEvent we) {
                    // Once window gets focus, set initial focus
                    if (!gotFocus) {
                        selectInitialValue();
                        gotFocus = true;
                    }
                }
            };
            dialog.addWindowListener(adapter);
            dialog.addWindowFocusListener(adapter);
            dialog.addComponentListener(new ComponentAdapter() {
                public void componentShown(ComponentEvent ce) {
                    // reset value to ensure closing works properly
                    setValue(JOptionPane.UNINITIALIZED_VALUE);
                }
            });

            addPropertyChangeListener(listener);
        }
    }
}
