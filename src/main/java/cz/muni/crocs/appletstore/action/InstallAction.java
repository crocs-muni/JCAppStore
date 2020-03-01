package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.InstallDialogWindow;
import cz.muni.crocs.appletstore.ReinstallWarnPanel;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.crypto.Signature;
import cz.muni.crocs.appletstore.crypto.SignatureImpl;
import cz.muni.crocs.appletstore.ui.Notice;
import cz.muni.crocs.appletstore.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;

import static javax.swing.JOptionPane.*;
import static pro.javacard.gp.GPRegistryEntry.Kind;


/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallAction extends CardAbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(InstallAction.class);

    private InstallBundle data;
    private boolean installed;
    private String defaultSelected;
    private CAPFile code;

    private boolean fromCustomFile = false;

    /**
     * Create an install action
     *
     * @param installData data for install
     * @param installed  whether installed on the card already
     * @param call       callback that is called before action and after failure or after success
     */
    public InstallAction(InstallBundle installData, boolean installed, String defaultSelected, OnEventCallBack<Void, Void> call) {
        super(call);
        this.data = installData;
        this.installed = installed;
        this.defaultSelected = defaultSelected;
    }

    public InstallAction(OnEventCallBack<Void, Void> call) {
        this(InstallBundle.empty(), false, null, call);
        this.fromCustomFile = true;
    }

    public InstallAction(InstallBundle installData, OnEventCallBack<Void, Void> call) {
        this(installData, false, null, call);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!CardManagerFactory.getManager().isCard()) {
            InformerFactory.getInformer().showInfo(textSrc.getString("missing_card"),
                    Notice.Importance.SEVERE, Notice.CallBackIcon.CLOSE, null, 7000);
            return;
        }

        if (fromCustomFile) data.setCapfile(CapFileChooser.chooseCapFile(Config.APP_LOCAL_DIR));
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


    private void runCustomInstallationGuide() {
        showInstallDialog(textSrc.getString("custom_file"), "verify_no_pgp.png", true);
    }

    private void runStoreInstallationGuide() {
        verifySignatureRoutine(new Executable() {
            @Override
            void work() {
                final Signature signature = new SignatureImpl();
                try {
                    result = signature.verifyPGPAndReturnMessage("JCAppStore", data.getCapfile());
                    if (data.getSigner() != null && !data.getSigner().isEmpty()) {
                        Tuple<String, String> another =
                                signature.verifyPGPAndReturnMessage(data.getSigner(), data.getCapfile());
                        result = new Tuple<>(another.first,
                                "JCAppStore: " + result.second + "<br>" + data.getSigner() + ": " + another.second);
                    }
                } catch (LocalizedSignatureException e) {
                    logger.warn("Signature verification failed", e);
                    result = new Tuple<>("not_verified.png", e.getLocalizedMessage());
                }
            }

            @Override
            void after() {
                showInstallDialog(result.second, result.first, false);
            }
        });
    }

    private void showInstallDialog(String verifyResult, String imgIcon, final boolean isCustom) {
        //simple usage will always do force install
        boolean forceInstall = installed || OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE);
        InstallDialogWindow dialog = new InstallDialogWindow(code, data.getInfo(), forceInstall, verifyResult, isCustom, data.getAppletNames());
        String[] buttons = new String[]{textSrc.getString("install"), textSrc.getString("cancel")};
        CustomJOptionPane pane = new CustomJOptionPane(dialog, new ImageIcon(Config.IMAGE_DIR + imgIcon), buttons, "error");
        pane.setOnClose(() -> {
            int selectedValue = CustomJOptionPane.getSelectedValue(buttons, pane.getValue());
            switch (selectedValue) {
                case JOptionPane.YES_OPTION:
                    //invalid data
                    if (!dialog.validCustomAIDs() || !dialog.validInstallParams()) {
                        InformerFactory.getInformer().showMessage(textSrc.getString("E_install_invalid_data"));
                        showInstallDialog(verifyResult, imgIcon, isCustom);
                        return null;
                    } else if (!dialog.getInstallOpts().isForce()) { //check if custom AID is not conflicting
                        logger.info("No force install: check the applets");
                        if (someCustomAppletAIDsConflicts(dialog.getInstallOpts().getCustomAIDs())) {
                            InformerFactory.getInformer().showMessage(textSrc.getString("E_install_already_present"));
                            showInstallDialog(verifyResult, imgIcon, isCustom);
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
        JDialog window = pane.createDialog(textSrc.getString("CAP_install_applet") + data.getTitleBar());
        window.setModal(false);
        window.pack();
        window.setVisible(true);
    }

    private boolean someCustomAppletAIDsConflicts(String[] aids) {
        Set<AppletInfo> applets = CardManagerFactory.getManager().getCard().getInstalledApplets();
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

    private void verifyCustomInstallationAndInstall(final InstallDialogWindow resultDialog) {
        if (resultDialog == null) return;
        final File customSign = resultDialog.getCustomSignatureFile();
        if (customSign != null) {
            verifySignatureRoutine(new Executable() {
                @Override
                void work() {
                    final Signature signature = new SignatureImpl();
                    try {
                        result = signature.verifyPGPAndReturnMessage(null, data.getCapfile(), customSign);
                    } catch (LocalizedSignatureException e) {
                        logger.warn("Signature verification failed", e);
                        result = new Tuple<>("not_verified.png", e.getLocalizedMessage());
                    }
                }

                @Override
                void after() {
                    int choice = JOptionPane.showConfirmDialog(null,
                            "<html><div width=\"350\">" + result.second + "<br>" +
                                    textSrc.getString("install_ask") + "</div></html>",
                            textSrc.getString("signature_title_dialog"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            new ImageIcon(Config.IMAGE_DIR + result.first));
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
     * Perform various pre-install checks (memory available, force installs, warns) and fire install
     * @param opts install options from the install form
     */
    private void fireInstall(final InstallOpts opts) {
        final CardManager manager = CardManagerFactory.getManager();
        if (!manager.isCard()) {
            return;
        }
        logger.info("Install fired, list of AIDS: " + code.getApplets().toString());
        logger.info("Install AID: " + opts.getAIDs());

        //if easy mode && package already present
        if (OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE) && !opts.isForce()) {
            //if applet present dont change anything
            if (manager.getCard().getInstalledApplets().stream().noneMatch(a ->
                    a.getKind() != Kind.ExecutableLoadFile && a.getAid().equals(opts.getAnyAID()))) {
                if (manager.getCard().getInstalledApplets().stream().anyMatch(a ->
                        a.getKind() == Kind.ExecutableLoadFile && a.getAid().equals(code.getPackageAID()))) {
                    opts.setForce(true);
                }
            }
        }

        if (opts.isForce() && !userAcceptsForceInstallWarn()) {
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
                //if no reinstall and memory is not max and applet size + 1kB install space > remaining memory
                if (!installed && cardMemory < JCMemory.LIMITED_BY_API && size + 1024 > cardMemory) {
                    int res = JOptionPane.showConfirmDialog(null,
                            "<html>" + textSrc.getString("no_space_1") + (size + 1024) +
                                    textSrc.getString("no_space_2") + cardMemory +
                                    textSrc.getString("no_space_3") + "</html>");
                    if (res == YES_OPTION) {
                        doInstall(opts, manager);
                    } else {
                        call.onFinish();
                        return null;
                    }
                } else {
                    doInstall(opts, manager);
                }
                return null;
            }
        }).start();
    }

    private boolean userAcceptsForceInstallWarn() {
        if (OptionsFactory.getOptions().is(Options.KEY_WARN_FORCE_INSTALL)) {
            ReinstallWarnPanel warn = new ReinstallWarnPanel();
            switch(JOptionPane.showOptionDialog(null, warn,
                    textSrc.getString("reinstall_warn_title"), YES_NO_OPTION, QUESTION_MESSAGE,
                    new ImageIcon(Config.IMAGE_DIR + "reinstall_warn_title"),
                    new String[]{textSrc.getString("continue"), textSrc.getString("cancel")},
                    "error")) {
                case YES_OPTION:
                    OptionsFactory.getOptions().addOption(Options.KEY_WARN_FORCE_INSTALL,
                            "" + (!warn.userSelectedDontShowAgain()));
                    return true;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * Actual installation
     * @param opts options from the install form modified by fireInstall() method
     *             (e.g. simple use mode adds force install if package present)
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
            manager.install(code, opts);
            SwingUtilities.invokeLater(() ->
                    InformerFactory.getInformer().showInfo(textSrc.getString("installed"),
                            Notice.Importance.INFO, Notice.CallBackIcon.CLOSE, null, 4000));
            data.setCapfile(null);
        }, "Failed to install applet.", textSrc.getString("install_failed"));
    }

    private static void verifySignatureRoutine(Executable task) {
        JOptionPane pane = new JOptionPane(textSrc.getString("H_pgp_loading"),
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


    private abstract class Executable {
        Tuple<String, String> result;

        void setResult(Tuple<String, String> result) {
            this.result = result;
        }

        abstract void work();

        abstract void after();
    }

    //this class clontains copied-out code of JOptionPane, slightly modified because JOptionPane does not allow much
    //custom behaviour
    private static class CustomJOptionPane extends JOptionPane {

        private CallBack<Void> onClose;

        CustomJOptionPane(Object message, Icon icon, Object[] options, Object initialValue) {
            super(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, icon, options, initialValue);
        }

        void setOnClose(@Nonnull CallBack<Void> onClose) {
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
            final PropertyChangeListener listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
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
