package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.DeleteDialogWindow;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Sources;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteAction extends MouseAdapter {

    private AppletInfo info;
    private final OnEventCallBack<Void, Void, Void> call;

    public DeleteAction(AppletInfo info, OnEventCallBack<Void, Void, Void> call) {
        this.call = call;
        this.info = info;
    }

    public void setInfo(AppletInfo info) {
        this.info = info;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!(info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile ||
                info.getKind() == GPRegistryEntry.Kind.Application)) {
            return;
        }

        DeleteDialogWindow opts = new DeleteDialogWindow(info.getAid().toString(), info.getKind(), info.hasKeys());
        switch (showDialog(Sources.language.get("CAP_delete_applet"), opts, "delete.png", "delete")) {
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
            case JOptionPane.YES_OPTION: //continue
        }

        String msg = opts.confirm();
        if (msg != null) {
            switch (showDialog(Sources.language.get("W_"), msg, "error.png", "delete_anyway")) {
                case JOptionPane.NO_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    return;
                case JOptionPane.YES_OPTION: //continue
            }
        }
        SwingUtilities.invokeLater(call::onStart);

        try {
            Sources.manager.uninstall(info, opts.willForce());

        } catch (CardException ex) {
            ex.printStackTrace();
            //todo log and notify try to get better result
            showFailed(Sources.language.get("delete_failed"),
                    Sources.language.get("E_generic") + ex.getMessage());

            SwingUtilities.invokeLater(call::onFail);
        }
        SwingUtilities.invokeLater(call::onFinish);

    }

    private int showDialog(String title, Object msg, String imgname, String confirmBtnKey) {
        return JOptionPane.showOptionDialog(
                null,
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + imgname),
                new String[]{Sources.language.get(confirmBtnKey), Sources.language.get("cancel")},
                "error");
    }

    private void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message, title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }
}
