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
import pro.javacard.gp.GPSession;

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
        if (data == null) {
            logger.info("Installing params are missnig");
            throw new LocalizedCardException("No install data.", textSrc.getString("E_notify_us"));
        }
        logger.info("Installing params: " + data.toString());

        GPRegistry registry;
        try {
            registry = context.getRegistry();
        } catch (IOException e) {
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

        if (file.getAppletAIDs().size() <= 1) {
            try {
                calculateDapPropertiesAndLoadCap(context, file);
                //context.loadCapFile(file, null);
                logger.info("CAP file loaded.");
            } catch (GPException e) {
                //todo localized
                if (e.sw == 0x00) {
                    throw new LocalizedCardException("Package already present", textSrc.getString("E_pkg_present"), e);
                }
                throw e;
            } catch (IOException e) {
                //todo
                e.printStackTrace();
            }
        }

        //no applets dont install
        if (file.getAppletAIDs().size() == 0) return true;

        final AID appletAID = data.getAID();
        AID customAID = data.getCustomAID() == null ? appletAID : AID.fromString(data.getCustomAID());

        GPRegistryEntry.Privileges privs = new GPRegistryEntry.Privileges();
        privs.add(GPRegistryEntry.Privilege.CardReset);

        if (data.isForce() && (registry.getDefaultSelectedAID().isPresent() && privs.has(GPRegistryEntry.Privilege.CardReset))) {
            try {
                context.deleteAID(registry.getDefaultSelectedAID().get(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (data.isForce() && (registry.getDefaultSelectedAID().isPresent() && privs.has(GPRegistryEntry.Privilege.CardReset))) {
            try {
                context.deleteAID(registry.getDefaultSelectedAID().get(), false);
            } catch (IOException e) {
                e.printStackTrace();
                //todo
                throw new LocalizedCardException("");
            }
        }

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

    private static void calculateDapPropertiesAndLoadCap(GPSession gp, CAPFile capFile) throws GPException, IOException {
        try {
            DAPProperties dap = new DAPProperties(gp);
            loadCapAccordingToDapRequirement(gp, dap.getTargetDomain(), dap.getDapDomain(), dap.isRequired(), capFile);
            System.out.println("CAP loaded");
        } catch (GPException e) {
            switch (e.sw) {
                case 0x6A80:
                    System.err.println("Applet loading failed. Are you sure the card can handle it?");
                    break;
                case 0x6985:
                    System.err.println("Applet loading not allowed. Are you sure the domain can accept it?");
                    break;
                default:
                    // Do nothing. Here for findbugs
            }
            throw e;
        }
    }

    private static void loadCapAccordingToDapRequirement(GPSession gp, AID targetDomain, AID dapDomain, boolean dapRequired, CAPFile cap) throws IOException, GPException {
        // XXX: figure out right signature type in a better way
        if (dapRequired) {
            byte[] dap = cap.getMetaInfEntry(CAPFile.DAP_RSA_V1_SHA1_FILE);
            gp.loadCapFile(cap, targetDomain, dapDomain == null ? targetDomain : dapDomain, dap, "SHA-1");
        } else {
            gp.loadCapFile(cap, targetDomain, "SHA-1");
        }
    }
}
