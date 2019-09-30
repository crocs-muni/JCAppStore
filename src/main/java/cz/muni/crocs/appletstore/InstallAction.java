package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.crypto.KeyBase;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.CapFileChooser;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class to add to button as listener target to perform applet installation
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallAction extends MouseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(InstallAction.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private boolean checked;
    private boolean installed;
    private File capfile;
    private AppletInfo info;
    private String titleBar;
    private final OnEventCallBack<Void, Void, Void> call;

    public InstallAction(String titleBar, AppletInfo info, File capfile, boolean installed,
                         OnEventCallBack<Void, Void, Void> call) {
        this.installed = installed;
        this.call = call;
        this.capfile = capfile;
        this.titleBar = titleBar;
        this.info = info;
        this.checked = true;
    }

    public InstallAction(OnEventCallBack<Void, Void, Void> call) {
        this("", null, null, false, call);
        this.checked = false;
    }

    public InstallAction(String titleBar, AppletInfo info, File capfile, OnEventCallBack<Void, Void, Void> call) {
        this(titleBar, info, capfile, false, call);
        this.checked = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (capfile == null) capfile = CapFileChooser.chooseCapFile(Config.APP_LOCAL_DIR);

        JOptionPane pane = new JOptionPane(textSrc.getString("H_keybase_loading"),
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION,
                new ImageIcon(Config.IMAGE_DIR + "verify_loading.png"),
                new Object[]{}, null);

        JDialog dialog = pane.createDialog(null, textSrc.getString("wait_sec"));
        dialog.setContentPane(pane);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        new SwingWorker<Void, Void>() {
            private Tuple<String, String> result;

            @Override
            public Void doInBackground() {
                try {
                    result = new KeyBase().verifySignature(capfile.getAbsolutePath());
                } catch (LocalizedSignatureException e) {
                    result = new Tuple<>("not_verified.png", textSrc.getString("H_verify_failed")
                            + e.getLocalizedMessageWithoutCause());
                }
                return null;
            }

            @Override
            protected void done() {
                dialog.dispose();
                showInstallDialog(result.second, result.first);
            }
        }.execute();
        dialog.setVisible(true);
    }

    private void showInstallDialog(String verifyResult, String imgIcon) {
        CAPFile file = CapFileChooser.getCapFile(capfile);
        if (file == null)
            return;

        InstallDialogWindow dialog = new InstallDialogWindow(file, info, installed, verifyResult);
        int result = JOptionPane.showOptionDialog(null, dialog,
                textSrc.getString("CAP_install_applet") + titleBar,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + imgIcon),
                new String[]{textSrc.getString("install"), textSrc.getString("cancel")}, "error");

        switch (result) {
            case JOptionPane.YES_OPTION:
                if (!dialog.validAID() || !dialog.validInstallParams()) {
                    InformerFactory.getInformer().showInfo(textSrc.getString("E_install_invalid_data"));
                    showInstallDialog(verifyResult, imgIcon);
                    return;
                }
                break;
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
        }

        final InstallOpts opts = dialog.getInstallOpts();
        logger.info("Install fired, list of AIDS: " + file.getApplets().toString());
        logger.info("Install AID: " + opts.getAID());
        call.onStart();
        new Thread(() -> {
            try {
                CardManager manager = CardManagerFactory.getManager();
                manager.install(file, opts);
                manager.setLastAppletInstalled(opts.getAID());
            } catch (LocalizedCardException ex) {
                ex.printStackTrace();
                logger.warn("Failed to install applet: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> showFailed(
                        textSrc.getString("install_failed"), ex.getLocalizedMessage()));
                SwingUtilities.invokeLater(call::onFail);
            }
            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    private void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message, title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }

    private boolean checkIfCountains(CAPFile file) {
        if (checked) return installed;
        List<AppletInfo> infos = CardManagerFactory.getManager().getInstalledApplets();
        for (AppletInfo nfo : infos) {
            for (AID aid : file.getAppletAIDs()) {
                if (nfo.getAid().equals(aid))
                    return true;
            }
        }
        return false;
    }
}
