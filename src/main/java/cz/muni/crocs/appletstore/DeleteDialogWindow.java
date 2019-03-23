package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.util.Sources;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteDialogWindow extends JPanel {

    private JCheckBox forceUninstall = new JCheckBox();
    private JCheckBox advanced = new JCheckBox();

    private KeysPresence keys;
    private GPRegistryEntry.Kind kind;


    public DeleteDialogWindow(String aid, GPRegistryEntry.Kind kind, KeysPresence hasKeys) {

        this.keys = hasKeys;
        this.kind = kind;

        setLayout(new MigLayout("width 250px"));
        add(new JLabel("<html><p width=\"600\">" + Sources.language.get("advanced_settings") + "</p></html>"),
                "wrap, span 5, gapbottom 10");

        add(new JLabel("<html><p width=\"600\">" + Sources.language.get("pkg_id") + aid +
               "</p></html>"), "wrap, span 5, gapbottom 20");

        JLabel more = new JLabel(Sources.language.get("advanced_settings"));
        more.setFont(CustomFont.plain.deriveFont(Font.BOLD, 12f));
        add(more, "span 2");

        advanced.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                forceUninstall.setEnabled(advanced.isSelected());
            }
        });
        add(advanced, "wrap");

        add(forceUninstall);
        forceUninstall.setEnabled(false);
        add(new JLabel(Sources.language.get("chbox_force_delete")), "span 4, wrap");
        add(getHint("chbox_force_delete_expl"), "span 5, wrap");
    }

    private JLabel getHint(String key) {
        JLabel hint = new JLabel("<html><p width=\"600\">" + Sources.language.get(key) + "</p></html>");
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    /**
     * Get confirm dialog msg
     * @return null if ok, otherwise msg warning
     */
    public String confirm() {
        if (keys == KeysPresence.PRESENT) {
            return Sources.language.get("applet") + Sources.language.get("contains") + Sources.language.get("W_personal_data");
        }
        if (keys == KeysPresence.UNKNOWN) {
            return Sources.language.get("applet") + Sources.language.get("may_contain") + Sources.language.get("W_personal_data");
        }
        if (kind == GPRegistryEntry.Kind.SecurityDomain || kind == GPRegistryEntry.Kind.IssuerSecurityDomain) {
            return Sources.language.get("E_delete_sd"); //todo allow deleting SD?
        }
        return null;
    }

    public boolean willForce() {
        return advanced.isSelected() && forceUninstall.isSelected();
    }
}
