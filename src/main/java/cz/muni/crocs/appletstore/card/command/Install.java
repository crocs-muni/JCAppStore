package cz.muni.crocs.appletstore.card.command;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.Informer;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import java.io.IOException;

/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class Install extends GPCommand<Void> {

    private final CAPFile file;
    private String[] data;
    private AID finalAID;

    public Install(CAPFile f, String[] data) {
        if (data != null && data.length != 3)
            throw new IllegalArgumentException(Config.translation.get(153));
        this.file = f;
        this.data = data;
    }

    public AID getAppletAID() {
        return finalAID;
    }

    @Override
    public boolean execute() throws CardException, GPException {
        //todo install parameters
        GPRegistry registry = context.getRegistry();
        if (registry.allPackageAIDs().contains(file.getPackageAID())) {
            context.deleteAID(file.getPackageAID(), true);
        }

        byte[] installParams = (data == null) ? new byte[0] : HexUtils.stringToBin(data[0]);
        String customId = (data == null) ? null : data[1];
        boolean force = (data != null) && data[2].equals("yes");

        //load onto card
        if (force && file.getAppletAIDs().size() <= 1) {
            try {
                AID target = null;
//                if (args.has(OPT_TO)) todo install to specified security domain, not supported i think
//                    target = AID.fromString(args.valueOf(OPT_TO));
                context.loadCapFile(file, target);
                System.out.println("CAP loaded");
            } catch (GPException e) {
                if (e.sw == 0x6985 || e.sw == 0x6A80) {
                    System.err.println("Loading failed. Are you sure the CAP file (JC version, packages, sizes) is compatible with your card?");
                }
                throw e;
            }
        }

        final AID appaid;
        if (file.getAppletAIDs().size() == 0) {
            return false;
        } else if (file.getAppletAIDs().size() > 1) {
            if (customId == null) {
                Informer.getInstance().showInfo("CAP contains more than one applet, before install, choose different applet ID");
                return false;
            } else {
                appaid = AID.fromString(customId);
            }
        } else {
            appaid = file.getAppletAIDs().get(0);
        }
        finalAID = appaid;
        // override todo create? what does it do
//        if (args.has(OPT_CREATE)) {
//            instanceaid = AID.fromString(args.valueOf(OPT_CREATE));
//        } else {
//        }

        //todo what privileges?
        GPRegistryEntry.Privileges privs = new GPRegistryEntry.Privileges();
        privs.add(GPRegistryEntry.Privilege.CardReset);
//        if (args.has(OPT_TERMINATE)) {
        privs.add(GPRegistryEntry.Privilege.CardLock);
        privs.add(GPRegistryEntry.Privilege.CardTerminate);

        // Remove existing default app
        if (force && (registry.getDefaultSelectedAID() != null && privs.has(GPRegistryEntry.Privilege.CardReset))) {
            context.deleteAID(registry.getDefaultSelectedAID(), false);
        }

        // warn
        if (context.getRegistry().allAppletAIDs().contains(finalAID)) {
            Informer.getInstance().showInfo("WARNING: Applet " + finalAID + " already present on card");
        }

        context.installAndMakeSelectable(file.getPackageAID(), appaid, finalAID, privs, installParams, null);
        return true;
    }
}
