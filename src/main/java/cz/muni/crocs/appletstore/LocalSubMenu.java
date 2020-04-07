package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Local submenu panel (refresh card, filter applets/packages/SD)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalSubMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JCheckBox sd = new JCheckBox();
    private JCheckBox app = new JCheckBox();
    private JCheckBox pkg = new JCheckBox();

    private JButton reload;

    /**
     * Create a local submenu
     */
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

    /**
     * Check if show SDs
     * @return true if showing security domains
     */
    public boolean showDomain() {
        return sd.isSelected();
    }

    /**
     * Check if show applets
     * @return true if showing applets
     */
    public boolean showApplet() {
        return app.isSelected();
    }

    /**
     * Check if show packages
     * @return true if showing packages
     */
    public boolean showPackage() {
        return pkg.isSelected();
    }

    /**
     * Check which kind is filtered
     * @param kind kind to check
     * @return true if kind showed
     */
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

    /**
     * Setup action for filtering
     * @param a action to do once a filter checkbox is clicked
     */
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
