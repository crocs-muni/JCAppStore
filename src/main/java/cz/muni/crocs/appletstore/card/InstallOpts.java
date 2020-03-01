package cz.muni.crocs.appletstore.card;

import apdu4j.HexUtils;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import pro.javacard.AID;
import pro.javacard.CAPFile;

import javax.annotation.Nonnull;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class InstallOpts {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private String[] customAIDs;
    private String[] originalAIDs;
    private String[] appletNames;
    private AppletInfo info;
    private boolean force;
    private byte[] installParams;
    private String defalutSelected;

    /**
     * Install options for applet
     * @param customAIDs custom applet AIDs, can be null or empty if not provided, can be empty on certain position to
     *                   signalize the original value should be used
     * @param originalAIDs subset of all applet AIDs from CAP file, to be installed
     * @param nfo applet file info, either full info from store or provided from user
     * @param force if install by force
     * @param installParams installation parameters
     */
    public InstallOpts(String[] customAIDs, @Nonnull String[] originalAIDs, String[] appletNames, AppletInfo nfo, boolean force, String installParams) {
        if (installParams == null) installParams = "";
        if (installParams.length() % 2 != 0 || installParams.length() > 512)
            throw new InvalidParameterException(textSrc.getString("E_invalid_install_params"));
        this.customAIDs = customAIDs;
        this.originalAIDs = originalAIDs;
        this.appletNames = appletNames;
        this.info = nfo;
        this.force = force;
        this.installParams = HexUtils.stringToBin(installParams);
        this.defalutSelected = null;
    }

    public InstallOpts(String[] customAIDs, String[] originalAIDs, String[] appletNames, AppletInfo nfo, boolean force, byte[] installParams) {
        if (installParams.length % 2 != 0 || installParams.length > 512)
            throw new InvalidParameterException(textSrc.getString("E_invalid_install_params"));
        this.customAIDs = customAIDs;
        this.originalAIDs = originalAIDs;
        this.appletNames = appletNames;
        this.info = nfo;
        this.force = force;
        this.installParams = installParams;
        this.defalutSelected = null;
    }

    public String getDefalutSelected() {
        return defalutSelected;
    }

    public void setDefalutSelected(String defalutSelected) {
        this.defalutSelected = defalutSelected;
    }

    public String[] getCustomAIDs() {
        return customAIDs;
    }

    public String[] getOriginalAIDs() {
        return originalAIDs;
    }

    public String[] getAppletNames() {
        return appletNames;
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

    public String getAIDs() {
        return Arrays.toString(originalAIDs);
    }

    public AID getAnyAID() {
        return originalAIDs.length > 0 ? AID.fromString(originalAIDs[0]) : null;
    }

    public String[] getAppletAIDsAsInstalled() {
        String[] result = originalAIDs.clone();
        if (customAIDs == null) return result;
        for (int idx = 0; idx < customAIDs.length; idx++) {
            String custom = customAIDs[idx];
            if (custom != null && !custom.isEmpty()) {
                result[idx] = custom;
            }
        }
        return result;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean isForce) {
        this.force = isForce;
    }

    public byte[] getInstallParams() {
        return installParams;
    }

    @Override
    public String toString() {
        return "Applet AID " + info.getAid() + ", forceInstall: " + force + ", params: " + Arrays.toString(installParams);
    }
}
