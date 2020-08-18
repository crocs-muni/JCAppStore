package cz.muni.crocs.appletstore.action.applet;

import apdu4j.ResponseAPDU;
import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.File;

import static cz.muni.crocs.appletstore.Config.S;

/**
 * JCMemory class
 * can talk to JCMemory Applet, a .cap file in store is required
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class JCMemory extends AppletBase<byte[]> {
    private static final Logger logger = LoggerFactory.getLogger(JCMemory.class);
    //maximum memory that what can be measured below SDK 3.0.4 included (maximum short value)
    public static final int LIMITED_BY_API = 0x7FFF;

    JCMemory() {}

    @Override
    public String getAppletName() {
        return "JCMemory";
    }

    @Override
    public AppletInfo getInfo() {
        return new AppletInfo(getAppletName(), "jcmem.png", "1.0", "CRoCS", "2.2.2",
                getAID(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.Application);
    }

    @Override
    public AppletInfo getPackageInfo() {
        return new AppletInfo(getAppletName(), "", "1.0", "CRoCS", "2.2.2",
                getPackageAID(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.ExecutableLoadFile, AID.fromString(getAID()));
    }

    @Override
    public InstallOpts getInstallOptions() {
        return new InstallOpts(new String[]{getAID()}, new String[]{getAID()}, null, getInfo(), true, new byte[0]);
    }

    @Override
    public String getAID() {
        return "4A43416C675465737531";
    }

    @Override
    public String getPackageAID() {
        return "4A43416C6754657375";
    }

    @Override
    public File getSource() {
        return new File(Config.APP_STORE_CAPS_DIR + S + "JCMemory", "JCMemory_v1.0_sdk2.2.2.cap");
    }

    @Override
    protected byte[] executeAppletCommunication(CardManager manager) {
        try {
            ResponseAPDU response = manager.sendApdu(getAID(), "B0000000");
            return getDataFromResponseAPDU(response);
        } catch (LocalizedCardException e) {
            logger.info("Error sending APDU to JCMemory after successful installation...", e);
            return null;
        }
    }

    //////////////////////////////////////////////////
    /// METHODS TO PARSE DATA FROM JCMEMORY OUTPUT ///
    //////////////////////////////////////////////////

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
}
