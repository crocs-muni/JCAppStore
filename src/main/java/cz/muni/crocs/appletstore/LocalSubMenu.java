package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.ReloadAction;
import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.Text;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

/**
 * Local submenu panel (refresh card, filter applets/packages/SD)
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalSubMenu extends JPanel {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private final JCheckBox sd = new JCheckBox();
    private final JCheckBox app = new JCheckBox();
    private final JCheckBox pkg = new JCheckBox();
    private final JLabel pkgTitle;
    private final JLabel authenticated;

    /**
     * Create a local submenu
     */
    public LocalSubMenu() {
        setLayout(new FlowLayout(FlowLayout.TRAILING, 8, 2));
        setOpaque(false);

        authenticated = new JLabel("<html>" + textSrc.getString("not_authenticated") + "</html>");
        authenticated.setVisible(false);
        authenticated.setFont(OptionsFactory.getOptions().getFont(14f));
        authenticated.setForeground(Color.white);
        authenticated.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                InformerFactory.getInformer().showMessage(textSrc.getString("not_authenticated_title"),
                        "<html><div width=\"450px\">"+textSrc.getString("not_authenticated_desc")+"</div>", "lock_black.png");
            }
        });
        authenticated.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(authenticated);
        add(Box.createRigidArea(new Dimension(6, 0)));

        JButton reload = getButton("card_refresh");
        reload.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReloadAction(GUIFactory.Components().defaultActionEventCallback()).start();
            }
        });

        AbstractAction submit = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GUIFactory.Components().getSearchable().showItems(null);
            }
        };
        sd.setBorder(BorderFactory.createEmptyBorder());
        sd.addActionListener(submit);
        app.setBorder(BorderFactory.createEmptyBorder());
        app.addActionListener(submit);
        pkg.setBorder(BorderFactory.createEmptyBorder());
        pkg.addActionListener(submit);

        add(getLabel("applets"));
        add(app);
        add(Box.createRigidArea(new Dimension(6, 0)));
        add(getLabel("sds"));
        add(sd);
        add(Box.createRigidArea(new Dimension(6, 0)));

        pkgTitle = getLabel("packages");
        add(pkgTitle);
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

    public void showPackagesButton(boolean doShow) {
        pkg.setVisible(doShow);
        pkgTitle.setVisible(doShow);

        //packages hidden when card was not authenticated...
        authenticated.setVisible(!doShow);
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
