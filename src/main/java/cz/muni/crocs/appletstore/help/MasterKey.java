package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.Components;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class MasterKey extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public MasterKey() {
        setLayout(new MigLayout());
        Font titleFont = OptionsFactory.getOptions().getTitleFont(Font.BOLD, 20f);
        Font textFont = OptionsFactory.getOptions().getFont();
        add(Components.getLabel(textSrc.getString("mk_title"), titleFont.deriveFont(35f)), "wrap");
        add(Components.getTextField(textSrc.getString("mk_intro"), textFont, "width: 600px", null), "gapleft 10, wrap");
        add(Components.getLabel(textSrc.getString("mk_title_ini"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("mk_ini"), textFont, "width: 600px", null), "gapleft 10, wrap");
    }
}
