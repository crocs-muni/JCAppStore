package cz.muni.crocs.appletstore.card.command;

import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.LocalizedCardException;
import cz.muni.crocs.appletstore.util.InformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

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
public class Install extends GPCommand<Void> {
    private static final Logger logger = LoggerFactory.getLogger(Install.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private final CAPFile file;
    private InstallOpts data;
    private AID instanceAID;

    public Install(CAPFile f, InstallOpts data) {
        this.file = f;
        this.data = data;
    }

    public AID getAppletAID() {
        return instanceAID;
    }

    @Override
    public boolean execute() throws LocalizedCardException, GPException {
        logger.info("Installing params: " + (data == null ? "no advanced settings." : data.toString()));
        GPRegistry registry = null;
        try {
            registry = context.getRegistry();
        } catch (IOException e) {
            //todo
            e.printStackTrace();
            throw new LocalizedCardException("");
        }

        if (data == null) {
            data = new InstallOpts(null, 0, false, new byte[0]);
        }

        if (data.getAppletIdx() >= file.getAppletAIDs().size())
            //todo
            throw new LocalizedCardException("");

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
        if (file.getAppletAIDs().size() <= 1) {
            try {
                //we do not support installing under custom SD
                //todo ask about third arg
                context.loadCapFile(file, null);
                logger.info("CAP file loaded.");
            } catch (GPException e) {
                if (e.sw == 0x00) {
                    throw new GPException(textSrc.getString("E_pkg_present"));
                }
                throw e;
            } catch (IOException e) {
                //todo
                e.printStackTrace();
            }
        }

        final AID appletAID = file.getAppletAIDs().get(data.getAppletIdx());
        instanceAID = data.getAID() == null || data.getAID().isEmpty() ? appletAID : AID.fromString(data.getAID());

        GPRegistryEntry.Privileges privs = new GPRegistryEntry.Privileges();
        //todo ask petr which privileges should be provided
        //privs.add(GPRegistryEntry.Privilege.CardReset)

        if (data.isForce() && (registry.getDefaultSelectedAID().isPresent() && privs.has(GPRegistryEntry.Privilege.CardReset))) {
            try {
                context.deleteAID(registry.getDefaultSelectedAID().get(), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (registry.allAppletAIDs().contains(instanceAID)) {
            InformerFactory.getInformer().showInfo(textSrc.getString("E_aid_present_on_card") + instanceAID);
        }

        try {
            context.installAndMakeSelectable(
                    file.getPackageAID(),
                    appletAID,
                    instanceAID,
                    privs,
                    data.getInstallParams());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

//    private static void calculateDapPropertiesAndLoadCap(OptionSet args, GPSession gp, CAPFile capFile) throws GPException, IOException {
//        try {
//            DAPProperties dap = new DAPProperties(args, gp);
//            loadCapAccordingToDapRequirement(args, gp, dap.getTargetDomain(), dap.getDapDomain(), dap.isRequired(), capFile);
//            System.out.println("CAP loaded");
//        } catch (GPException e) {
//            switch (e.sw) {
//                case 0x6A80:
//                    System.err.println("Applet loading failed. Are you sure the card can handle it?");
//                    break;
//                case 0x6985:
//                    System.err.println("Applet loading not allowed. Are you sure the domain can accept it?");
//                    break;
//                default:
//                    // Do nothing. Here for findbugs
//            }
//            throw e;
//        }
//    }
//
//    private static void loadCapAccordingToDapRequirement(OptionSet args, GPSession gp, AID targetDomain, AID dapDomain, boolean dapRequired, CAPFile cap) throws IOException, GPException {
//        // XXX: figure out right signature type in a better way
//        if (dapRequired) {
//            byte[] dap = args.has(OPT_SHA256) ? cap.getMetaInfEntry(CAPFile.DAP_RSA_V1_SHA256_FILE) : cap.getMetaInfEntry(CAPFile.DAP_RSA_V1_SHA1_FILE);
//            gp.loadCapFile(cap, targetDomain, dapDomain == null ? targetDomain : dapDomain, dap, args.has(OPT_SHA256) ? "SHA-256" : "SHA-1");
//        } else {
//            gp.loadCapFile(cap, targetDomain, args.has(OPT_SHA256) ? "SHA-256" : "SHA-1");
//        }
//    }
}
