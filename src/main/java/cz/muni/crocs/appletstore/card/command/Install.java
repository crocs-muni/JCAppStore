package cz.muni.crocs.appletstore.card.command;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.Informer;
import cz.muni.crocs.appletstore.util.Sources;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPException;
import pro.javacard.gp.GPRegistry;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Modified install process from GPPro
 *
 * @author Jiří Horák & Martin Paljak
 * @version 1.0
 */
public class Install extends GPCommand<Void> {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private final CAPFile file;
    private String[] data;
    private AID finalAID;

    public Install(CAPFile f, String[] data) {
        if (data != null && data.length != 3)
            throw new IllegalArgumentException(textSrc.getString("E_install_invalid_data"));
        this.file = f;
        this.data = data;
    }

    public AID getAppletAID() {
        return finalAID;
    }

    @Override
    public boolean execute() throws CardException, GPException {
        //todo possibly third param in data null
        GPRegistry registry = context.getRegistry();

        byte[] installParams;
        boolean force;
        String[] aids;
        if (data == null) {
            installParams = new byte[0];
            force = false;
        } else {
            installParams = HexUtils.stringToBin(data[0]);
            force = data[1].equals("yes");
            //todo only one?
            aids = Arrays.copyOfRange(data, 2, data.length);
        }

        if (force && registry.allPackageAIDs().contains(file.getPackageAID())) {
            context.deleteAID(file.getPackageAID(), true);
        }

        //todo delete
        String customId = (data == null) ? null : data[1];
        //load onto card

        //todo ??? why not load when more applets available??
        if (file.getAppletAIDs().size() <= 1) {
            try {
                AID target = null;
//                if (args.has(OPT_TO)) todo install to specified security domain, not supported i think
//                    target = AID.fromString(args.valueOf(OPT_TO));
                context.loadCapFile(file, target);

                System.out.println("CAP loaded");
            } catch (GPException e) {
                if (e.sw == 0x00) {
                    //to translate message
                    throw new GPException(textSrc.getString("E_pkg_present"));
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

        GPRegistryEntry.Privileges privs = new GPRegistryEntry.Privileges();
        privs.add(GPRegistryEntry.Privilege.CardReset);

        // Remove existing default app
        if (force && (registry.getDefaultSelectedAID() != null && privs.has(GPRegistryEntry.Privilege.CardReset))) {
            context.deleteAID(registry.getDefaultSelectedAID(), false);
        }

        if (context.getRegistry().allAppletAIDs().contains(finalAID)) {
            Informer.getInstance().showInfo("WARNING: Applet " + finalAID + " already present on card");
        }

        context.installAndMakeSelectable(file.getPackageAID(), appaid, finalAID, privs, installParams, null);
        return true;
    }
}
