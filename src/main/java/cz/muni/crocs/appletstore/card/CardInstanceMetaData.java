package cz.muni.crocs.appletstore.card;

import pro.javacard.AID;
import pro.javacard.gp.GPRegistryEntry;

import java.io.Serializable;
import java.util.*;

/**
 * Exec. laod files are from GPPro returned twice, once instance
 * with executable load file with modules -> if contains and we found module version
 * replace
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstanceMetaData implements Serializable {

    private final HashSet<AppletInfo> applets;
    private HashMap<String, HashMap<String, String>> jcAlgTestData;

    private CardInstanceMetaData() {
        this(new HashSet<>(), null);
    }

    public CardInstanceMetaData(HashSet<AppletInfo> applets, HashMap<String, HashMap<String, String>> jcalgtest) {
        this.applets = applets;
        this.jcAlgTestData = jcalgtest;
    }

    public HashMap<String, HashMap<String, String>> getJCData() {
        return jcAlgTestData;
    }

    public void setJCData(HashMap<String, HashMap<String, String>> data) {
        jcAlgTestData = data;
    }

    public Set<AppletInfo> getApplets() {
        return Collections.unmodifiableSet(applets);
    }

    public boolean addAppletRequireModulesIfPkg(AppletInfo info) {
        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile &&
                (info.getModules() == null || info.getModules().isEmpty())) {
            return false;
        }
        return applets.add(info);
    }

    public boolean addAppletIgnoreModulesIfPkg(AppletInfo info) {
        return applets.add(info);
    }

    //delete single applet metadata
    void deleteAppletInfo(AID toDelete) {
        Iterator<AppletInfo> info = applets.iterator();
        while(info.hasNext()) {
            AppletInfo nfo = info.next();
            if (toDelete.equals(nfo.getAid())) {
                info.remove();
                return;
            }
        }
    }

    public boolean isAppletPresent(String aid) {
        return isAppletPresent(AID.fromString(aid));
    }

    public boolean isAppletPresent(AID aid) {
        AppletInfo found = getAppletInfo(aid);
        if (found == null) return false;
        return found.getKind() != GPRegistryEntry.Kind.ExecutableLoadFile;
    }

    public boolean isPackagePresent(String aid) {
        return isPackagePresent(AID.fromString(aid));
    }

    public boolean isPackagePresent(AID aid) {
        AppletInfo found = getAppletInfo(aid);
        if (found == null) return false;
        return found.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile;
    }

    /**
     * Clean up invalid data in applets
     */
    public void removeInvalidApplets() {
        for (Iterator<AppletInfo> i = applets.iterator(); i.hasNext(); ) {
            AppletInfo nfo = i.next();
            if (nfo.getAuthor() != null ||
                    nfo.getVersion() != null ||
                    nfo.getSdk() != null ||
                    nfo.getName() != null) continue;
            i.remove();
        }
    }

    public void insertOrRewriteApplet(AppletInfo item) {
        if(!applets.add(item)) {
            applets.remove(item);
            applets.add(item);
        }
    }

    public static CardInstanceMetaData empty() {
        return new CardInstanceMetaData();
    }

    private AppletInfo getAppletInfo(AID aid) {
        for (AppletInfo nfo : applets) {
            if (aid.equals(nfo.getAid())) {
                return nfo;
            }
        }
        return null;
    }
}
