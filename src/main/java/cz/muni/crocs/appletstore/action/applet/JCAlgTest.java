package cz.muni.crocs.appletstore.action.applet;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.card.*;
import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.io.File;

import static cz.muni.crocs.appletstore.Config.S;

/**
 * JCMemory class
 * can talk to JCMemory Applet, a .cap file in store is required
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class JCAlgTest extends AppletBase<Void> {

    JCAlgTest() {}

    @Override
    public String getAppletName() {
        return "JCAlgTest";
    }

    /**
     * AppletInfo instance getter
     * @return AppletInfo instance to work with in actions
     */
    public AppletInfo getInfo() {
        return new AppletInfo(getAppletName(), "jcalgtest.png", "1.7.9", "CRoCS", "2.2.2",
                getAID(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.Application);
    }

    /**
     * AppletInfo package getter
     * @return AppletInfo package to work with in actions
     */
    public AppletInfo getPackageInfo() {
        return new AppletInfo(getAppletName(), "", "1.7.9", "CRoCS", "2.2.2",
                getPackageAID(), KeysPresence.NO_KEYS, GPRegistryEntry.Kind.ExecutableLoadFile, AID.fromString(getAID()));
    }

    /**
     * InstallOpts bundle getter
     * @return InstallOpts bundle for installation process
     */
    public InstallOpts getInstallOptions() {
        return new InstallOpts(new String[]{getAID()}, new String[]{getAID()}, null, getInfo(), true, new byte[0]);
    }

    /**
     * JCMemory AID
     * @return JCMemory AID
     */
    public String getAID() {
        return "4A43416C675465737431";
    }

    /**
     * JCMemory package ID
     * @return JCMemory package ID
     */
    public String getPackageAID() {
        return "4A43416C6754657374";
    }

    /**
     * JCMemory sources
     * @return File with .cap from the store
     */
    public File getSource() {
        return new File(Config.APP_STORE_CAPS_DIR + S + "JCAlgTest", "JCAlgTest_v1.7.9_sdk2.2.2.cap");
    }

    @Override
    protected Void executeAppletCommunication(CardManager manager) {
        throw new UnsupportedOperationException("Use non-default call with custom implementation instead.");
    }
}
