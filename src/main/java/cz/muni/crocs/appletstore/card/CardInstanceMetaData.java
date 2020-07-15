package cz.muni.crocs.appletstore.card;

import pro.javacard.gp.GPRegistryEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Exec. laod files are from GPPro returned twice, once instance
 * with executable load file with modules -> if contains and we found module version
 * replace
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CardInstanceMetaData extends HashSet<AppletInfo> {

    private HashMap<String, HashMap<String, String>> jcAlgTestData = null;

    public HashMap<String, HashMap<String, String>> getJcAlgTestData() {
        return jcAlgTestData;
    }

    public void setJcAlgTestData(HashMap<String, HashMap<String, String>> jcAlgTestData) {
        this.jcAlgTestData = jcAlgTestData;
    }

    @Override
    public boolean add(AppletInfo info) {
        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile && contains(info)
                && !info.getModules().isEmpty()) {
            remove(info);
        }
        return super.add(info);
    }

    /**
     * Clean up invalid data in applets
     */
    public void removeInvalid() {
        for (Iterator<AppletInfo> i = iterator(); i.hasNext(); ) {
            AppletInfo nfo = i.next();
            if (nfo.getAuthor() != null ||
                    nfo.getVersion() != null ||
                    nfo.getSdk() != null ||
                    nfo.getName() != null) continue;
            i.remove();
        }
    }

    public static CardInstanceMetaData empty() {
        return new CardInstanceMetaData();
    }
}
