package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import java.io.IOException;


/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Install extends GPCommand<AID> {
    private static final Logger logger = LoggerFactory.getLogger(Install.class);

    private final CAPFile file;
    private final InstallOpts data;
    private final int index;
    private final boolean isDefaultSelected;

    public Install(CAPFile f, InstallOpts data, int index, boolean isDefaultSelected) {
        this.file = f;
        this.data = data;
        this.index = index;
        this.isDefaultSelected = isDefaultSelected;
    }

    @Override
    public boolean execute() throws LocalizedCardException, GPException {
        //todo maybe throw, user probably did not want to load only..?
        if (file.getAppletAIDs().size() == 0) return true;

        if (data.getOriginalAIDs() == null) return false;
        GPRegistryEntry.Privileges privs = new GPRegistryEntry.Privileges();

        AID appletAID = AID.fromString(data.getOriginalAIDs()[index]);
        String userDefinedAID = data.getCustomAIDs() == null ? null : data.getCustomAIDs()[index];
        AID customAID = userDefinedAID == null || userDefinedAID.isEmpty() ? appletAID
                : AID.fromString(userDefinedAID);

        if (isDefaultSelected) privs.add(GPRegistryEntry.Privilege.CardReset);

        logger.info("Installing applet: pkg " + file.getPackageAID() + ", aid " + appletAID + ", custom aid " + customAID);
        try {
            context.installAndMakeSelectable(
                    file.getPackageAID(),
                    appletAID,
                    customAID,
                    privs,
                    data.getInstallParams());
        } catch (IOException e) {
            logger.error("Unable to finish Install for install.");
            throw new LocalizedCardException("IOException when install for install", "E_installforinstall", e);
        }
        result = customAID;
        return true;
    }

    @Override
    public String getDescription() {
        return "Install procedure [install for install].";
    }
}
