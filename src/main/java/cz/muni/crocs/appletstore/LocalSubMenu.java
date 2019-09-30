package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.Components;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalSubMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private JCheckBox sd = new JCheckBox();
    private JCheckBox app = new JCheckBox();
    private JCheckBox pkg = new JCheckBox();

    private JButton submit = Components.getButton(textSrc.getString("filter"), "", 12.f, Color.BLACK, Color.WHITE, false);

    public LocalSubMenu() {
        setLayout(new FlowLayout(FlowLayout.TRAILING, 8, 2));
        setOpaque(false);
        add(Box.createHorizontalGlue());


        add(new JLabel(textSrc.getString("sds")));
        add(sd);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(new JLabel(textSrc.getString("applets")));
        add(app);
        add(Box.createRigidArea(new Dimension(10, 0)));
        add(new JLabel(textSrc.getString("packages")));
        add(pkg);
        submit.setOpaque(false);
        add(submit);
        add(Box.createRigidArea(new Dimension(10, 0)));
        sd.setSelected(false);
        app.setSelected(true);
        pkg.setSelected(true);
        setPreferredSize(new Dimension(Integer.MAX_VALUE, sd.getPreferredSize().height));
    }

    public boolean showDomain() {
        return sd.isSelected();
    }

    public boolean showApplet() {
        return app.isSelected();
    }

    public boolean showPacakge() {
        return pkg.isSelected();
    }

    public boolean accept(GPRegistryEntry.Kind kind) {
        switch (kind) {
            case Application:
                return showApplet();
            case ExecutableLoadFile:
                return showPacakge();
            case SecurityDomain:
            case IssuerSecurityDomain:
                return showDomain();
            default:
                return true;
        }
    }

    void setOnSubmit(Action a) {
        submit.addActionListener(a);
    }
}
