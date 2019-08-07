package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class InstallOpts {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private String AID;
    private int appletIdx;
    private boolean force;
    private byte[] installParams;

    public InstallOpts(String AID, int appletIdx, boolean force, String installParams) {
        if (installParams == null) installParams = "";
        if (installParams.length() % 2 != 0 || installParams.length() > 512)
            throw new InvalidParameterException(textSrc.getString("E_invalid_install_params"));
        this.AID = AID;
        this.appletIdx = appletIdx;
        this.force = force;
        this.installParams = HexUtils.stringToBin(installParams);
    }

    public InstallOpts(String AID, int appletIdx, boolean force, byte[] installParams) {
        this.AID = AID;
        this.appletIdx = appletIdx;
        this.force = force;
        this.installParams = installParams;
    }

    public String getAID() {
        return AID;
    }

    public int getAppletIdx() {
        return appletIdx;
    }

    public boolean isForce() {
        return force;
    }

    public byte[] getInstallParams() {
        return installParams;
    }

    @Override
    public String toString() {
        return "Applet AID " + AID +
                ", index in capfile: " + appletIdx +
                ", forceInstall: " + force +
                ", params: " + Arrays.toString(installParams);
    }
}
