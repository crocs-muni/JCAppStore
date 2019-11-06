package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.card.AppletInfo;
import pro.javacard.CAPFile;

import java.io.File;

public class InstallBundle {
    private String titleBar;
    private AppletInfo info;
    private File capfile;
    private String signer;
    private String identifier;

    public InstallBundle(String titleBar, AppletInfo info, File capfile, String signer, String identifier) {
        this.titleBar = titleBar;
        this.info = info;
        this.capfile = capfile;
        this.signer = signer;
        this.identifier = identifier;
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
}
