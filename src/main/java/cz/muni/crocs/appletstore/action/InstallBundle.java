package cz.muni.crocs.appletstore.action;

import cz.muni.crocs.appletstore.card.AppletInfo;

import java.io.File;
import java.util.ArrayList;

public class InstallBundle {
    private String titleBar;
    private AppletInfo info;
    private File capfile;
    private String signer;
    private String identifier;
    private ArrayList<String> appletNames;

    public InstallBundle(String titleBar, AppletInfo info, File capfile, String signer, String identifier) {
        this.titleBar = titleBar;
        this.info = info;
        this.capfile = capfile;
        this.signer = signer;
        this.identifier = identifier;
    }

    public InstallBundle(String titleBar, AppletInfo info, File capfile, String signer, String identifier, ArrayList<String> appletNames) {
        this(titleBar, info, capfile, signer, identifier);
        this.appletNames = appletNames;
    }

    public static InstallBundle empty() {
        return new InstallBundle("", null, null, null, null);
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

    public String getIdentifier() {
        return identifier;
    }

    public void setCapfile(File file) {
        this.capfile = file;
    }

    public ArrayList<String> getAppletNames() {
        return appletNames == null ? new ArrayList<>() : appletNames;
    }
}
