package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.InstallOpts;
import cz.muni.crocs.appletstore.card.KeysPresence;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.File;

import static cz.muni.crocs.appletstore.Config.S;

public class JCMemory {

    public static AppletInfo getInfo() {
        return new AppletInfo("JCMemory", "jcmem.png", "1.0", "CRoCS", "2.2.2",
                getAID(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.Application);
    }

    public static AppletInfo getPackageInfo() {
        return new AppletInfo("JCMemory", "", "1.0", "CRoCS", "2.2.2",
                getPackageAID(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.ExecutableLoadFile, AID.fromString(getAID()));
    }

    public static InstallOpts getInstallOptions() {
        return new InstallOpts(getAID(), getInfo(), true, new byte[0]);
    }

    public static String getAID() {
        return "4A43416C675465737531";
    }

    public static String getPackageAID() {
        return "4A43416C6754657375";
    }

    public static String getAPDU() {
        //the command is ignored, only do not start 0x00 as this is CLA for domain
        return "B0000000";
    }

    public static File getSource() {
        return new File(Config.APP_STORE_CAPS_DIR + S + "JCMemory", "JCMemory_v1.0_sdk2.2.2.cap");
    }
}
