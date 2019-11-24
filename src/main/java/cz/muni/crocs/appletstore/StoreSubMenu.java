package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class StoreSubMenu extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private JButton reload;

    public StoreSubMenu() {
        setLayout(new FlowLayout(FlowLayout.TRAILING, 8, 2));
        setOpaque(false);
        reload = new JButton("<html>" + textSrc.getString("store_refresh") + "</div></html>");
        reload.setUI(new CustomButtonUI());
        reload.setFont(OptionsFactory.getOptions().getFont(Font.BOLD, 12f));
        reload.setForeground(Color.WHITE);
        reload.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reload.setOpaque(false);
        add(reload);
    }

    void setOnReload(Action a) {
        reload.addActionListener(a);
    }
}
