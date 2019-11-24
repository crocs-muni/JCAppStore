package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalSubMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JCheckBox sd = new JCheckBox();
    private JCheckBox app = new JCheckBox();
    private JCheckBox pkg = new JCheckBox();

    private JButton reload;

    public LocalSubMenu() {
        setLayout(new FlowLayout(FlowLayout.TRAILING, 8, 2));
        setOpaque(false);

        reload = getButton("card_refresh");

        sd.setBorder(BorderFactory.createEmptyBorder());
        app.setBorder(BorderFactory.createEmptyBorder());
        pkg.setBorder(BorderFactory.createEmptyBorder());

        add(getLabel("applets"));
        add(app);
        add(Box.createRigidArea(new Dimension(6, 0)));
        add(getLabel("sds"));
        add(sd);
        add(Box.createRigidArea(new Dimension(6, 0)));
        add(getLabel("packages"));
        add(pkg);
        add(Box.createRigidArea(new Dimension(15, 0)));
        add(reload);
        add(Box.createRigidArea(new Dimension(10, 0)));
        app.setSelected(true);
        sd.setSelected(false);
        pkg.setSelected(false);
        setPreferredSize(new Dimension(Integer.MAX_VALUE, sd.getPreferredSize().height));
    }

    public boolean showDomain() {
        return sd.isSelected();
    }

    public boolean showApplet() {
        return app.isSelected();
    }

    public boolean showPackage() {
        return pkg.isSelected();
    }

    public boolean accept(GPRegistryEntry.Kind kind) {
        switch (kind) {
            case Application:
                return showApplet();
            case ExecutableLoadFile:
                return showPackage();
            case SecurityDomain:
            case IssuerSecurityDomain:
                return showDomain();
            default:
                return true;
        }
    }

    void setOnSubmit(Action a) {
        app.addActionListener(a);
        sd.addActionListener(a);
        pkg.addActionListener(a);
    }
    void setOnReload(Action a) { reload.addActionListener(a); }

    private JLabel getLabel(String translateKey) {
        JLabel label = new Text(textSrc.getString(translateKey));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(Color.WHITE);
        return label;
    }

    private JButton getButton(String translationKey) {
        JButton button = new JButton("<html>" + textSrc.getString(translationKey) + "</div></html>");
        button.setUI(new CustomButtonUI());
        button.setFont(OptionsFactory.getOptions().getFont(Font.BOLD, 12f));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setOpaque(false);
        return button;
    }
}
