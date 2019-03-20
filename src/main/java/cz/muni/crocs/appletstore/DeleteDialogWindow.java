package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.util.AppletInfo;
import net.miginfocom.swing.MigLayout;
import pro.javacard.AID;
import pro.javacard.CAPFile;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class DeleteDialogWindow extends JPanel {

    private JCheckBox forceUninstall = new JCheckBox();
    private JCheckBox advanced = new JCheckBox();

    private AppletInfo.HasKeys keys;
    private GPRegistryEntry.Kind kind;


    public DeleteDialogWindow(String aid, GPRegistryEntry.Kind kind, AppletInfo.HasKeys hasKeys) {

        this.keys = hasKeys;
        this.kind = kind;

        setLayout(new MigLayout("width 250px"));
        add(new JLabel("<html><p width=\"600\">" + Config.translation.get(145) + "</p></html>"),
                "wrap, span 5, gapbottom 10");

        add(new JLabel("<html><p width=\"600\">" + Config.translation.get(16) + aid +
               "</p></html>"), "wrap, span 5, gapbottom 20");

        JLabel more = new JLabel(Config.translation.get(132));
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
        add(new JLabel(Config.translation.get(146)), "span 4, wrap");
        add(getHint(147), "span 5, wrap");
    }

    private JLabel getHint(int translationId) {
        JLabel hint = new JLabel("<html><p width=\"600\">" + Config.translation.get(translationId) + "</p></html>");
        hint.setForeground(Color.DARK_GRAY);
        return hint;
    }

    /**
     * Get confirm dialog msg
     * @return null if ok, otherwise msg warning
     */
    public String confirm() {
        if (keys == AppletInfo.HasKeys.PRESENT) {
            return Config.translation.get(26) + Config.translation.get(31) + Config.translation.get(148);
        }
        if (keys == AppletInfo.HasKeys.UNKNOWN) {
            return Config.translation.get(26) + Config.translation.get(30) + Config.translation.get(148);
        }
        if (kind == GPRegistryEntry.Kind.SecurityDomain || kind == GPRegistryEntry.Kind.IssuerSecurityDomain) {
            return Config.translation.get(167); //todo allow deleting SD?
        }
        return null;
    }

    public boolean willForce() {
        return advanced.isSelected() && forceUninstall.isSelected();
    }
}
