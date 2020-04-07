package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Dialog panel for applet deletion - additional information and confirmation window
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteDialogWindow extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JCheckBox forceUninstall = new JCheckBox();
    private KeysPresence keys;
    private GPRegistryEntry.Kind kind;

    /**
     * Create a new deletion dialog panel
     * @param aid applet/package aid to delete
     * @param kind kind (package or application)
     * @param hasKeys if applet can contain sensitive data, warn about the deletion
     */
    public DeleteDialogWindow(String aid, GPRegistryEntry.Kind kind, KeysPresence hasKeys) {
        setLayout(new MigLayout("width 250px"));
        this.keys = hasKeys;
        this.kind = kind;

        boolean implicit = OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE);
        boolean pkg = kind == GPRegistryEntry.Kind.ExecutableLoadFile;
        String question;
        if (!implicit) {
                question = textSrc.getString("H_uninstall_pkg");
                question += (pkg) ? textSrc.getString("H_uninstall_apk_pkg_remain") : "";
        } else {
            question = textSrc.getString("H_uninstall_apk");
        }

        add(new HtmlText("<p width=\"600\">" + question + "</p>"), "wrap, span 5, gapbottom 10");
        add(new HtmlText("<p width=\"600\">" + textSrc.getString(pkg && !implicit ? "pkg_id" : "app_id") + aid + "</p>"),
                "wrap, span 5, gapbottom 20");

        //enable force uninstall on applets only, packages are treated afterwards when found dependencies
        if (!implicit && kind == GPRegistryEntry.Kind.Application) {
            add(forceUninstall);
            add(new Text(textSrc.getString("chbox_force_delete")), "span 4, wrap");
            add(getHint("chbox_force_delete_expl"), "span 5, wrap");
        }
    }

    private JLabel getHint(String key) {
        JLabel hint = new HtmlText("<p width=\"600\">" + textSrc.getString(key) + "</p>", 11f);
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    /**
     * Get confirm dialog msg
     * @return null if ok, otherwise msg warning
     */
    public String confirm() {
        if (keys == KeysPresence.PRESENT) {
            return textSrc.getString("applet_uninstall") + textSrc.getString("contains") + textSrc.getString("W_personal_data");
        }
        if (keys == KeysPresence.UNKNOWN) {
            return textSrc.getString("applet_uninstall") + textSrc.getString("may_contain") + textSrc.getString("W_personal_data");
        }
        if (kind == GPRegistryEntry.Kind.SecurityDomain || kind == GPRegistryEntry.Kind.IssuerSecurityDomain) {
            return textSrc.getString("E_delete_sd");
        }
        return null;
    }

    public boolean willForce() {
        return forceUninstall.isSelected();
    }
}
