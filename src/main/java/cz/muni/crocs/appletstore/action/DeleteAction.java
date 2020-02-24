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
import java.util.ArrayList;

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

        AppletInfo packageInfo = null;
        if (info.getKind() == Kind.Application) {
            //implicit mode deletes package too
            if (OptionsFactory.getOptions().is(Options.KEY_DELETE_IMPLICIT)) {
                packageInfo = getPackageOf(info);
                logger.info("Implicit delete package too: " +
                        (packageInfo != null ? packageInfo.toString() : "ERROR: no package found!"));
            }
        } else { //executable because of the very first if clause
            //simple use deletes applet when deleting package
            //todo debug does force mode is enough for the package to uninstall its applets?
            if (!willForce && OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE)) {
                CardInstance card = manager.getCard();
                for (AID mod : info.getModules()) {
                    if (card != null && card.getInstalledApplets().stream().anyMatch(a -> a.getAid().equals(mod))) {
                        willForce = true;
                        break;
                    }
                }
            }
        }

        if (willForce || info.getKind() != Kind.ExecutableLoadFile) { //display notice if an applet instance is deleted
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
        final AppletInfo finalPackage = packageInfo;
        execute(() -> {
            manager.uninstall(info, finalWillForce);
            if (finalPackage != null) { //todo force package delete should suffice..?
                manager.uninstall(finalPackage, true);
            }
        }, "Failed to uninstall applet: ", textSrc.getString("delete_failed"));
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
