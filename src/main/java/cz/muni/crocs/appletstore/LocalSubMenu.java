package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.Components;
import cz.muni.crocs.appletstore.ui.Text;
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

    private JButton submit;
    private JButton reload;

    public LocalSubMenu() {
        setLayout(new FlowLayout(FlowLayout.TRAILING, 8, 2));
        setOpaque(false);

        submit = getButton("filter");
        reload = getButton("card_refresh");

        add(getLabel("sds"));
        add(sd);
        add(Box.createRigidArea(new Dimension(6, 0)));
        add(getLabel("applets"));
        add(app);
        add(Box.createRigidArea(new Dimension(6, 0)));
        add(getLabel("packages"));
        add(pkg);
        add(submit);
        add(Box.createRigidArea(new Dimension(15, 0)));
        add(reload);
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
    void setOnReload(Action a) { reload.addActionListener(a); }

    private JLabel getLabel(String translateKey) {
        JLabel label = new Text(textSrc.getString(translateKey));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton getButton(String translationKey) {
        JButton button = Components.getButton(textSrc.getString(translationKey), "",
                12.f, Color.BLACK, Color.WHITE, false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setOpaque(false);
        return button;
    }
}
