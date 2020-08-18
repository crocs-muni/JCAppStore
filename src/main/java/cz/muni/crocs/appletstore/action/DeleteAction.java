package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.DeleteDialogWindow;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.CardInstance;
import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;
import pro.javacard.gp.GPRegistryEntry.Kind;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class to add to button as listener target to perform applet deletion
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteAction extends CardAbstractAction<Void, Void> {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAction.class);

    private AppletInfo info;

    public DeleteAction(AppletInfo info, OnEventCallBack<Void, Void> call) {
        super(call);
        this.info = info;
    }

    /**
     * Set info of the applet to delete
     * @param info AppletInfo info to delete, must contain AID
     */
    public void setInfo(AppletInfo info) {
        this.info = info;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        final CardManager manager = CardManagerFactory.getManager();
        if (!(info.getKind() == Kind.ExecutableLoadFile ||
                info.getKind() == Kind.Application) || manager.getCard() == null) {
            return;
        }

        DeleteDialogWindow opts = showDeletionDialog();
        if (opts == null) return;
        boolean willForce = opts.willForce();
        AppletInfo packageInfo = getPackageIfShouldBeUninstalled();
        String applets = findCollisions(willForce, manager);

        //if found dependencies and  we do not try to uninstall package as a dependency from simple mode
        if (!applets.isEmpty() && packageInfo == null) {
            logger.info("Found applet collisions: " + applets);
            ConfirmDeletion confirm = new ConfirmDeletion(applets);
            if (showDialog(textSrc.getString("W_"), confirm, "error.png", "delete_anyway")
                    != JOptionPane.YES_OPTION)  return;
            willForce = confirm.agreed();
            if (!willForce) return; //did not agreed
        } else {
            //do not uninstall package dependency if more applets are still on the card
            if (!applets.isEmpty()) {
                packageInfo = null;
                logger.info("Package not implicitly deleted: other applets still active.");
            }
            willForce = false;
        }
        if (!confirmForceInstallWarnDialog(willForce, opts)) return;

        final boolean finalWillForce = willForce;
        final AppletInfo finalPackage = packageInfo;
         execute(() -> {
            manager.uninstall(info, finalWillForce);
            if (finalPackage != null) {
                manager.uninstall(finalPackage, true);
            }
            return null;
        }, "Failed to uninstall applet: ", textSrc.getString("delete_failed"), 3, TimeUnit.MINUTES);
    }

    private DeleteDialogWindow showDeletionDialog() {
        DeleteDialogWindow opts = new DeleteDialogWindow(info.getAid().toString(), info.getKind(), info.hasKeys());
        switch (showDialog(textSrc.getString("CAP_delete_applet"), opts, "delete.png", "delete")) {
            case JOptionPane.NO_OPTION:
            case JOptionPane.CLOSED_OPTION:
                return null;
            case JOptionPane.YES_OPTION:
                break;
        }
        logger.info("Delete applet: " + info.toString());
        return opts;
    }

    private String findCollisions(boolean isForce, CardManager manager) {
        //ask user whether to delete applet too
        StringBuilder appletsToDelete = new StringBuilder();

        if (!isForce && OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE)) {
            for (AID mod : info.getModules()) {
                for (AppletInfo nfo : manager.getCard().getCardMetadata().getApplets()) {
                    if (nfo.getKind() == Kind.Application && nfo.getAid().equals(mod) &&
                            //do not notify about the applet we are removing now
                            !(info.getKind() == Kind.Application && info.getAid().equals(nfo.getAid()))) {
                        appletsToDelete.append("<br>").append(info.getName());
                    }
                }
            }
        }
        return appletsToDelete.toString();
    }

    private AppletInfo getPackageIfShouldBeUninstalled() {
        if ((info.getKind() == Kind.Application) && (OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE))) {
            //implicit mode deletes package too
            AppletInfo packageInfo = getPackageOf(info);
            logger.info("Implicit delete package too: " +
                    (packageInfo != null ? packageInfo.toString() : "ERROR: no package found!"));
            return packageInfo;
        }
        return null;
    }

    private boolean confirmForceInstallWarnDialog(boolean isForce, DeleteDialogWindow options) {
        if (isForce || info.getKind() != Kind.ExecutableLoadFile) { //display notice if an applet instance is deleted
            String msg = options.confirm();
            if (msg != null) {
                switch (showDialog(textSrc.getString("W_"), msg, "error.png", "delete_anyway")) {
                    case JOptionPane.NO_OPTION:
                    case JOptionPane.CLOSED_OPTION:
                        return false;
                    case JOptionPane.YES_OPTION:
                        return true;
                }
            }
        }
        return true;
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
        AtomicReference<AppletInfo> result = new AtomicReference<>(null);
        CardInstance card = CardManagerFactory.getManager().getCard();
        if (card == null) return null;
        card.foreachAppletOf(GPRegistryEntry.Kind.ExecutableLoadFile, info -> {
            for (AID instance : info.getModules()) {
                if (instance.equals(applet.getAid())) {
                    result.set(info);
                    return false;
                }
            }
            return true;
        });
        return result.get();
    }

    private static class ConfirmDeletion extends JPanel {
        private final JCheckBox verify = new JCheckBox();
        public ConfirmDeletion(String appletName) {
            super(new MigLayout());
            add(new HtmlText("<div width=\"300\">"+textSrc.getString("W_applet_deletion1")+appletName+"</div>"), "wrap");
            add(new HtmlText("<div width=\"300\">"+textSrc.getString("W_applet_deletion2")+"</div>"), "wrap");
            verify.setText("<html><div width=\"300\">" + textSrc.getString("W_confirm_applet_delete") + "</div></html>");
            add(verify);
        }

        public boolean agreed() {
            return verify.isSelected();
        }
    }
}
