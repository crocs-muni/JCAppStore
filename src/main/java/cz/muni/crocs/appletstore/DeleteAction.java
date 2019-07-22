package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.gp.GPRegistryEntry.Kind;

import javax.smartcardio.CardException;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Class to add to button as listener target to perform applet deletion
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteAction extends MouseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAction.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

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
        if (!(info.getKind() == Kind.ExecutableLoadFile ||
                info.getKind() == Kind.Application)) {
            return;
        }

        logger.info("Delete applet: " + info.toString());

        DeleteDialogWindow opts = new DeleteDialogWindow(info.getAid().toString(), info.getKind(), info.hasKeys());
        switch (showDialog(textSrc.getString("CAP_delete_applet"), opts, "delete.png", "delete")) {
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
            case JOptionPane.YES_OPTION:
                break;
        }

        String msg = opts.confirm();
        if (msg != null) {
            switch (showDialog(textSrc.getString("W_"), msg, "error.png", "delete_anyway")) {
                case JOptionPane.NO_OPTION:
                case JOptionPane.CLOSED_OPTION:
                    return;
                case JOptionPane.YES_OPTION:
                    break;
            }
        }
        call.onStart();

        new Thread(() ->  {
            try {
                CardManagerFactory.getManager().uninstall(info, opts.willForce());
            } catch (CardException ex) {
                ex.printStackTrace();
                logger.warn("Failed to uninstall applet: " + ex.getMessage());
                SwingUtilities.invokeLater(() -> {
                    showFailed(textSrc.getString("delete_failed"), ex.getMessage());
                });
                SwingUtilities.invokeLater(call::onFail);
            }
            SwingUtilities.invokeLater(call::onFinish);
        }).start();
    }

    private static int showDialog(String title, Object msg, String imgname, String confirmBtnKey) {
        return JOptionPane.showOptionDialog(
                null,
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + imgname),
                new String[]{textSrc.getString(confirmBtnKey), textSrc.getString("cancel")},
                "error");
    }

    private static void showFailed(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message, title, JOptionPane.ERROR_MESSAGE, new ImageIcon(Config.IMAGE_DIR + "error.png"));
    }
}
