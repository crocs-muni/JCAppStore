package cz.muni.crocs.appletstore.card;

import pro.javacard.gp.GPRegistryEntry;

import java.util.HashSet;

/**
 * Exec. laod files are from GPPro returned twice, once instance
 * with executable load file with modules -> if contains and we found module version
 * replace
 */
public class AppletSet extends HashSet<AppletInfo> {

    @Override
    public boolean add(AppletInfo info) {
        if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile && contains(info)
                && !info.getModules().isEmpty()) {
            remove(info);
        }
        return super.add(info);
    }
}
