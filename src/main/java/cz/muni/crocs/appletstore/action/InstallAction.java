package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.Informer;
import cz.muni.crocs.appletstore.InstallDialogWindow;
import cz.muni.crocs.appletstore.card.CapFileChooser;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Sources;
import pro.javacard.CAPFile;

import javax.smartcardio.CardException;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallAction extends MouseAdapter {

    private File capfile = null;
    private final OnEventCallBack<Void, Void, Void> call;

    public InstallAction(OnEventCallBack<Void, Void, Void> call) {
        this.call = call;
    }

    public InstallAction(File capfile, OnEventCallBack<Void, Void, Void> call) {
        this(call);
        this.capfile = capfile;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        CAPFile file;
        if (capfile == null) {
            file = CapFileChooser.chooseCapFile();
        } else {
            file = CapFileChooser.getCapFile(capfile);
        }

        //if the user did not selected any file
        if (file == null)
            return;

        InstallDialogWindow opts = new InstallDialogWindow(file);

        int result = JOptionPane.showOptionDialog(null, opts,
                Sources.language.get("CAP_install_applet"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "error.png"),
                new String[]{Sources.language.get("install"), Sources.language.get("cancel")}, "error");

        switch (result) {
            case JOptionPane.YES_OPTION:
                if (!opts.validAID() || !opts.validInstallParams()) {
                    Informer.getInstance().showInfo("E_install_invalid_data");
                    return;
                }
                break;
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
        }

        SwingUtilities.invokeLater(call::onStart);

        new Thread(() -> {
            try {
                String[] additionalInfo = opts.getAdditionalInfo();
                Sources.manager.install(file, additionalInfo);
            } catch (CardException ex) {
                ex.printStackTrace();
                //todo setup (on failed maybe) or get better detailed info...
                showFailed(Sources.language.get("install_failed"),
                        Sources.language.get("E_generic") + ex.getMessage());
                SwingUtilities.invokeLater(call::onFail);
            }

            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    private void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message, title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }
}
