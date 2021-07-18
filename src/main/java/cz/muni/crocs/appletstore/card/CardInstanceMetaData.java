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
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *              CAUTION
 * ANY UPDATE TO THIS CLASS WILL MAKE SERIALIZATION OF OLD FILES
 * FAIL. USERS WILL LOSE ALL THE CARD METADATA.
 *
 * CHANGE serialVersionUID IF YOU MAKE ANY CHANGES TO THE MEMBER VARIABLES
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstanceMetaData implements Serializable {

    private final HashSet<AppletInfo> applets;
    private HashMap<String, HashMap<String, String>> jcAlgTestData;
    private static final long serialVersionUID = 2021071800000000001L;

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

    /**
     * Add applet instance data
     * @param info applet instance (application or SD/ISD) onůy
     */
    public void addAppletInstance(AppletInfo info) {
        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile) {
            throw new RuntimeException("Invalid instance inserted.");
        }
        applets.add(info);
    }

    /**
     * Add package data, package is tied to applet instance using its modules
     * if not recognized
     * @param info package instance (Executable load file) only
     */
    public void addAppletPackage(AppletInfo info) {
        if (info.getKind() != GPRegistryEntry.Kind.ExecutableLoadFile) {
            throw new RuntimeException("Invalid instance inserted.");
        }

        AppletInfo existing = getAppletInfo(info.getAid());
        if (info.getModules() == null || info.getModules().isEmpty()) {
            if (existing == null) applets.add(info);
            return; // do not handle naming if no instance present in modules
        }

        //remove package without modules of the same AID (all packages are present twice, once without modules)
        if (existing != null) applets.remove(existing);
        for (AID aid : info.getModules()) {
            //overwrite name regardless of existing one...
            //if (info.getName() != null && !info.getName().isEmpty()) break;

            AppletInfo instance = getAppletInfo(aid);

            if (instance != null) {
                info.setAppletName(instance.getName());
                break;
            }
        }
        applets.add(info);
    }

    protected void addAppletMetadataUnsafe(AppletInfo info) {
        applets.remove(info); //in case it is already present
        applets.add(info);
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
        }return null;
    }
}
