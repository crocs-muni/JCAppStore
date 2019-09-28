package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import pro.javacard.AID;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class InstallOpts {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private int aidIdx;
    private AppletInfo info;
    private boolean force;
    private byte[] installParams;

    /**
     * Install options for applet
     * @param aidIdx index of real applet AID from cap file
     * @param nfo applet file info, either full info from store or provided from user
     * @param force if install by force
     * @param installParams installation parameters
     */
    public InstallOpts(int aidIdx, AppletInfo nfo, boolean force, String installParams) {
        if (installParams == null) installParams = "";
        if (installParams.length() % 2 != 0 || installParams.length() > 512)
            throw new InvalidParameterException(textSrc.getString("E_invalid_install_params"));
        this.info = nfo;
        this.force = force;
        this.installParams = HexUtils.stringToBin(installParams);
    }

    public InstallOpts(int aidIdx, AppletInfo nfo, boolean force, byte[] installParams) {
        this.info = nfo;
        this.force = force;
        this.installParams = installParams;
    }

    public String getName() {
        return verifyValue(info.getName());
    }

    public String getAuthor() {
        return verifyValue(info.getAuthor());
    }

    public String getVersion() {
        return verifyValue(info.getVersion());
    }

    public String getSdkVersion() {
        return verifyValue(info.getSdk());
    }

    public AppletInfo getInfo() {
        return info;
    }

    private String verifyValue(String value) {
        if (value == null || value.isEmpty() || value.equals(textSrc.getString("unknown")))
            return null;
        return value;
    }

    public AID getAID() {
        return info.getAid();
    }

    public boolean isForce() {
        return force;
    }

    public byte[] getInstallParams() {
        return installParams;
    }

    @Override
    public String toString() {
        return "Applet AID " + info.getAid().toString() +
                ", forceInstall: " + force +
                ", params: " + Arrays.toString(installParams);
    }
}
