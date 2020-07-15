package cz.muni.crocs.appletstore.action;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.card.AppletInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Class that contains installation data as passed to InstallAction from the store info panel
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class InstallBundle {
    private final String titleBar;
    private final AppletInfo info;
    private final String signer;
    private final String fingerprint;
    private File capfile;
    private ArrayList<String> appletNames;
    private String folder;
    private JsonObject dataSet;

    public InstallBundle(String titleBar, AppletInfo info, File capfile, String signer, String fingerprint) {
        this.titleBar = titleBar;
        this.info = info;
        this.capfile = capfile;
        this.signer = signer;
        this.fingerprint = fingerprint;
    }

    public InstallBundle(String titleBar, AppletInfo info, File capfile, String signer, String fingerprint,
                         ArrayList<String> appletNames, String folder, JsonObject dataSet) {
        this(titleBar, info, capfile, signer, fingerprint);
        this.appletNames = appletNames;
        this.folder = folder;
        this.dataSet = dataSet;
    }

    public static InstallBundle empty() {
        return new InstallBundle("", null, null, null, null);
    }

    public String getStoreFolder() {
        return folder;
    }

    public JsonObject getDataSet() {
        return dataSet;
    }

    public String getTitleBar() {
        return titleBar;
    }

    public AppletInfo getInfo() {
        return info;
    }

    public File getCapfile() {
        return capfile;
    }

    public String getSigner() {
        return signer;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setCapfile(File file) {
        this.capfile = file;
    }

    public ArrayList<String> getAppletNames() {
        return appletNames == null ? new ArrayList<>() : appletNames;
    }
}
