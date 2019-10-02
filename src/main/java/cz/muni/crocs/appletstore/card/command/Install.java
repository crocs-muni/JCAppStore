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
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class Install extends GPCommand<Void> {
    private static final Logger logger = LoggerFactory.getLogger(Install.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private final CAPFile file;
    private InstallOpts data;

    public Install(CAPFile f, InstallOpts data) {
        this.file = f;
        this.data = data;
    }

    @Override
    public boolean execute() throws LocalizedCardException, GPException {
        logger.info("Installing params: " + (data == null ? "no advanced settings." : data.toString()));
        if (data == null) {
            //todo
            throw new LocalizedCardException("");
        }

        GPRegistry registry;
        try {
            registry = context.getRegistry();
        } catch (IOException e) {
            //todo
            e.printStackTrace();
            throw new LocalizedCardException("");
        }

        // Remove existing default app
        if (data.isForce() && registry.allPackageAIDs().contains(file.getPackageAID())) {
            try {
                context.deleteAID(file.getPackageAID(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load
//        if (file.getAppletAIDs().size() <= 1) {
//            calculateDapPropertiesAndLoadCap(args, gp, instcap);
//        }
        //todo mail from martin
            try {
                //we do not support installing under custom SD
                //todo ask about third arg
                context.loadCapFile(file, null);
                logger.info("CAP file loaded.");
            } catch (GPException e) {
                //todo localized
                if (e.sw == 0x00) {
                    throw new GPException(textSrc.getString("E_pkg_present"));
                }
                throw e;
            } catch (IOException e) {
                //todo
                e.printStackTrace();
            }


        final AID appletAID = data.getAID();
        AID customAID = data.getCustomAID() == null ? null : AID.fromString(data.getCustomAID());

        GPRegistryEntry.Privileges privs = new GPRegistryEntry.Privileges();

        if (data.isForce() && (registry.getDefaultSelectedAID().isPresent() && privs.has(GPRegistryEntry.Privilege.CardReset))) {
            try {
                context.deleteAID(registry.getDefaultSelectedAID().get(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        if (registry.allAppletAIDs().contains(customAID)) {
//            InformerFactory.getInformer().showInfo(textSrc.getString("E_aid_present_on_card") + customAID);
//        }

        try {
            context.installAndMakeSelectable(
                    file.getPackageAID(),
                    appletAID,
                    customAID,
                    privs,
                    data.getInstallParams());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
