package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.ErrDisplay;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(Delete.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final boolean force;
    private final AppletInfo toDelete;

    public Delete(AppletInfo nfo, boolean force) {
        if (nfo == null)
            throw new IllegalArgumentException(textSrc.getString("E_delete_invalid_data"));
        this.toDelete = nfo;
        this.force = force;
    }

    @Override
    public boolean execute() throws GPException, LocalizedCardException, IOException {
        AID aid = toDelete.getAid();
        GPRegistry reg = context.getRegistry();
        logger.info("About to uninstall an instance: " + toDelete.getKind() + ", with id: " + aid);

        if (!reg.allAIDs().contains(aid)) {
            logger.warn("Applet not present when deletion requested.");
            throw new LocalizedCardException("Could not delete AID because not present on card: " + aid, "E_no_aid_on_card", ErrDisplay.POPUP);
        }

        try {
            context.deleteAID(aid, force);
            logger.info("Applet " + aid + " successfully deleted.");
        } catch (GPException e) {
            logger.error("Failed to uninstall applet: " + e.sw, e);
            if (e.sw == 0x6985) {
                throw new LocalizedCardException("Deletion not allowed. Some app still active?", "E_delete_not_allowed", e, ErrDisplay.POPUP);
            } else {
                throw e;
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Delete method: delete applet " + toDelete + ", using force: " + force;
    }
}
