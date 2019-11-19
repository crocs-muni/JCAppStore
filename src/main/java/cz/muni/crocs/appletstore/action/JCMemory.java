package cz.muni.crocs.appletstore.action;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.File;
import java.io.IOException;

import static cz.muni.crocs.appletstore.Config.S;

public class JCMemory {

    public static final int LIMITED_BY_API = 0x7FFF;

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

    public static int getJCSystemVersion(byte[] response) {
        return fromArray(response, 0, 2);
    }

    public static boolean isObjectDeletionSupported(byte[] response) {
        return fromArray(response, 2, 1) == 1;
    }

    public static int getPersistentMemory(byte[] response) {
        return fromArray(response, 3, 2);
    }

    public static int getTransientResetMemory(byte[] response) {
        return fromArray(response, 5, 2);
    }

    public static int getTransientDeselectMemory(byte[] response) {
        return fromArray(response, 7, 2);
    }

    public static int getMaxCommitCapacity(byte[] response) {
        return fromArray(response, 9, 2);
    }

    public static int getInBlockSize(byte[] response) {
        return fromArray(response, 11, 2);
    }

    public static int getOutBlockSize(byte[] response) {
        return fromArray(response, 13, 2);
    }

    public static int getProtocol(byte[] response) {
        return fromArray(response, 15, 1);
    }

    public static int getNAD(byte[] response) {
        return fromArray(response, 16, 1);
    }

    public static byte[] getSystemInfo() throws LocalizedCardException, UnknownKeyException {
        byte[] result;
        final CardManager manager = CardManagerFactory.getManager();

        ResponseAPDU response = null;
        try {
            response = manager.sendApdu(JCMemory.getAID(), JCMemory.getAPDU());
        } catch (LocalizedCardException e) {
            //ignore
            e.printStackTrace();
        }
        result = getData(response);
        if (result != null) {
            uninstallIfNotKeep(manager, false);
            return result;
        }

        try {
            manager.install(JCMemory.getSource(), JCMemory.getInstallOptions());
        } catch (IOException e) {
            return null;
        }

        try {
            response = manager.sendApdu(JCMemory.getAID(), JCMemory.getAPDU());
        } catch (LocalizedCardException e) {
            //ignore
            e.printStackTrace();
        }
        uninstallIfNotKeep(manager, true);
        return getData(response);
    }

    private static int fromArray(byte[] array, int offset, int length) {
        int result = 0;
        int newOffset = offset;
        while (newOffset < offset + length) {
            result = (result << 8) + toUnsigned(array[newOffset++]);
        }
        return result;
    }

    private static int toUnsigned(byte b) {
        if ((b & 0x80) == 0x80) {
            return 128 + (b & 0x7F);
        }
        return b;
    }

    private static byte[] getData(ResponseAPDU responseAPDU) throws LocalizedCardException {
        if (responseAPDU != null && responseAPDU.getSW() == 0x9000) {
            return responseAPDU.getBytes();
        }
        return null;
    }

    private static void uninstallIfNotKeep(CardManager manager, boolean refresh) throws LocalizedCardException, UnknownKeyException {
        if (!OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY)) {
            manager.uninstall(JCMemory.getPackageInfo(), true);
        } else if (refresh) {
            manager.loadCard();
        }
    }
}
