package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.DeleteDialogWindow;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;
import pro.javacard.gp.GPRegistryEntry.Kind;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * Class to add to button as listener target to perform applet deletion
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteAction extends CardAbstractAction {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAction.class);

    private AppletInfo info;

    public DeleteAction(AppletInfo info, OnEventCallBack<Void, Void> call) {
        super(call);
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

        if (OptionsFactory.getOptions().is(Options.KEY_DELETE_IMPLICIT)) {
            AppletInfo pkg = getPackageOf(info);
            if (pkg != null) info = pkg;
        }

        DeleteDialogWindow opts = new DeleteDialogWindow(info.getAid().toString(), info.getKind(), info.hasKeys());
        switch (showDialog(textSrc.getString("CAP_delete_applet"), opts, "delete.png", "delete")) {
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return;
            case JOptionPane.YES_OPTION:
                break;
        }
        logger.info("Delete applet: " + info.toString());

        final CardManager manager = CardManagerFactory.getManager();
        boolean willForce = opts.willForce() || OptionsFactory.getOptions().is(Options.KEY_DELETE_IMPLICIT);
        //if easy mode, and uninstalling package, then uninstall applet too and show notice uninstalling applet
        if (!OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE) && info.getKind() == Kind.ExecutableLoadFile) {
            for (AID mod : info.getModules()) {
                CardInstance card = manager.getCard();
                if (card != null && card.getInstalledApplets().stream().anyMatch(a -> a.getAid().equals(mod))) {
                    willForce = true;
                    break;
                }
            }
        }

        if (willForce || info.getKind() != Kind.ExecutableLoadFile) {
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
        }

        final boolean finalWillForce = willForce;
        execute(() -> manager.uninstall(info, finalWillForce),
                "Failed to uninstall applet: ", textSrc.getString("delete_failed"));
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

    private AppletInfo getPackageOf(AppletInfo applet) {
        CardInstance card = CardManagerFactory.getManager().getCard();
        if (card == null) return null;
        for (AppletInfo info : card.getInstalledApplets()) {
            if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile) {
                for (AID instance : info.getModules()) {
                    if (instance.equals(applet.getAid())) {
                        return info;
                    }
                }
            }
        }
        return null;
    }
}
