package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.Informer;
import cz.muni.crocs.appletstore.InstallDialogWindow;
import cz.muni.crocs.appletstore.card.CapFileChooser;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.iface.IniParser;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.IniParserImpl;
import pro.javacard.CAPFile;

import javax.smartcardio.CardException;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallAction extends MouseAdapter {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private File capfile = null;
    private String appletName = null;
    private final OnEventCallBack<Void, Void, Void> call;

    public InstallAction(OnEventCallBack<Void, Void, Void> call) {
        this.call = call;
    }

    public InstallAction(String appletName, File capfile, OnEventCallBack<Void, Void, Void> call) {
        this(call);
        this.capfile = capfile;
        this.appletName = appletName;
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
                textSrc.getString("CAP_install_applet"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + "error.png"),
                new String[]{textSrc.getString("install"), textSrc.getString("cancel")}, "error");

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
                CardManagerFactory.getManager().install(file, additionalInfo);

                if (appletName != null)
                    storeAppletData(additionalInfo);

            } catch (CardException ex) {
                ex.printStackTrace();
                //todo setup (on failed maybe) or get better detailed info...
                showFailed(textSrc.getString("install_failed"),
                        textSrc.getString("E_generic") + ex.getMessage());
                SwingUtilities.invokeLater(call::onFail);
            }

            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    private void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message, title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }

    private void storeAppletData(String[] additionalInfo) {
        try {
            IniParser parser = new IniParserImpl(Config.INI_CARD_LIST, CardManagerFactory.getManager().getCard().getId());
            String cards = parser.getValue(Config.INI_INSTALLED);

            String[] aids = Arrays.copyOfRange(additionalInfo, 2, additionalInfo.length);
            cards += appletName + ",";
            StringBuilder builder = new StringBuilder();
            builder.append(cards).append(appletName).append(";");
            for (String aid : aids) {
                builder.append(aid).append(";");
            }
            parser.addValue(Config.INI_INSTALLED, builder.append("|").toString());
            parser.store();
        } catch (IOException e) {
            Informer.getInstance().showInfo(textSrc.getString("install_info_failed"));
        }
    }
}
