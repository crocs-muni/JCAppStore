package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.CapFileChooser;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.smartcardio.CardException;
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
    private String titleBar = "";
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
        CAPFile file;
        if (capfile == null) file = CapFileChooser.chooseCapFile(Config.APP_LOCAL_DIR);
        else file = CapFileChooser.getCapFile(capfile);

        if (file == null)
            return;

        installed = checkIfCountains(file);
        InstallDialogWindow opts = new InstallDialogWindow(file, installed);
        if (!showInstallDialog(opts))
            return;

        logger.info("Install fired.");
        call.onStart();
        new Thread(() -> {
            try {
                InstallOpts intallOpts = opts.getInstallOpts();
                if (info == null)
                    CardManagerFactory.getManager().install(file, intallOpts);
                else
                    CardManagerFactory.getManager().install(file, intallOpts, info);

            } catch (LocalizedCardException ex) {
                ex.printStackTrace();
                logger.warn("Failed to install applet: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    showFailed(textSrc.getString("install_failed"), ex.getLocalizedMessage());
                });
                SwingUtilities.invokeLater(call::onFail);
            }
            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    private boolean showInstallDialog(InstallDialogWindow opts) {
        int result = JOptionPane.showOptionDialog(null, opts,
                textSrc.getString("CAP_install_applet") + titleBar,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "error.png"),
                new String[]{textSrc.getString("install"), textSrc.getString("cancel")}, "error");

        switch (result) {
            case JOptionPane.YES_OPTION:
                if (!opts.validAID() || !opts.validInstallParams()) {
                    InformerFactory.getInformer().showInfo(textSrc.getString("E_install_invalid_data"));
                    return false;
                }
                break;
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return false;
        }
        return true;
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
