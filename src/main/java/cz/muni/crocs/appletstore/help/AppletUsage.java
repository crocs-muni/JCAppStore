package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.Components;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class AppletUsage extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public AppletUsage() {
        setLayout(new MigLayout());
        Font titleFont = OptionsFactory.getOptions().getTitleFont(Font.BOLD, 20f);
        Font textFont = OptionsFactory.getOptions().getFont();
        add(Components.getLabel(textSrc.getString("au_title"), titleFont.deriveFont(35f)), "wrap");
        add(Components.getTextField(textSrc.getString("au_introduction"), textFont, "width: 600px", null), "gapleft 10, wrap");
        add(Components.getLabel(textSrc.getString("au_host_title"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("au_host"), textFont, "width: 600px", null), "gapleft 10, wrap");
        add(Components.getLabel(textSrc.getString("au_no_host_title"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("au_no_host"), textFont, "width: 600px", null), "gapleft 10, wrap");
    }
}
