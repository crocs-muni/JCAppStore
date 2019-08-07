package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;

import javax.smartcardio.CardException;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class Delete extends GPCommand<Void> {
    private static final Logger logger = LoggerFactory.getLogger(Install.class);
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
    public boolean execute() throws CardException, GPException, LocalizedCardException, IOException {
        AID aid = toDelete.getAid();
        GPRegistry reg = context.getRegistry();

        try {
            context.deleteAID(aid, reg.allPackageAIDs().contains(aid) || force);
        } catch (GPException e) {
            e.printStackTrace();

            if (!context.getRegistry().allAIDs().contains(aid)) {
                throw new LocalizedCardException("Could not delete AID because not present on card: " + aid, "E_no_aid_on_card");
            } else {
                if (e.sw == 0x6985) {
                    throw new LocalizedCardException("Deletion not allowed. Some app still active?", "E_delete_not_allowed");
                } else {
                    throw e;
                }
            }
        }
        return true;
    }
}
