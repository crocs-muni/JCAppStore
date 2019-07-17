package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.util.Sources;
import pro.javacard.AID;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;

import javax.smartcardio.CardException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class Delete extends GPCommand<Void> {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private boolean force;
    private AppletInfo toDelete;

    public Delete(AppletInfo nfo, boolean force) {
        if (nfo == null)
            throw new IllegalArgumentException(textSrc.getString("E_delete_invalid_data"));
        this.toDelete = nfo;
        this.force = force;
    }

    @Override
    public boolean execute() throws CardException, GPException {
        AID aid = toDelete.getAid();
        GPRegistry reg = context.getRegistry();

        try {
            context.deleteAID(aid, reg.allPackageAIDs().contains(aid) || force);
        } catch (GPException e) {
            if (!context.getRegistry().allAIDs().contains(aid)) {
                System.err.println("Could not delete AID (not present on card): " + aid);
            } else {
                if (e.sw == 0x6985) {
                    System.err.println("Deletion not allowed. Some app still active?");
                } else {
                    throw e;
                }
            }
        }
        return true;
    }
}
