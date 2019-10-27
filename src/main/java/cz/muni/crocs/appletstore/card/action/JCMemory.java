package cz.muni.crocs.appletstore.card.action;

import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.InstallOpts;

public class JCMemory {

    public static AppletInfo getInfo() {
        AppletInfo info = new AppletInfo("JCMemory", "jcmem.png", "1.0", "CRoCS", "2.2.2");
        info.setAID(getAID());
        return info;
    }

    public static InstallOpts getInstallOptions() {
        return new InstallOpts(getAID(), getInfo(), true, new byte[0]);
    }

    public static String getAID() {
        return "";
    }

    public static String getAPDU() {
        return "";
    }
}
