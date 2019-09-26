package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.Components;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class Cmd extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public Cmd() {
        setLayout(new MigLayout());
        Font titleFont = OptionsFactory.getOptions().getTitleFont(Font.BOLD, 20f);
        Font textFont = OptionsFactory.getOptions().getFont();
        add(Components.getLabel(textSrc.getString("cmd_title"), titleFont.deriveFont(35f)), "wrap");
        add(Components.getTextField(textSrc.getString("cmd_introduction"), textFont, "width: 600px", null), "gapleft 10, wrap");

        add(Components.getLabel(textSrc.getString("cmd_browse_title"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("cmd_browse"), textFont, "width: 600px", null), "gapleft 10, wrap");

        add(Components.getLabel(textSrc.getString("cmd_launch_linux_title"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("cmd_launch_linux"), textFont, "width: 600px", null), "gapleft 10, wrap");

        add(Components.getLabel(textSrc.getString("cmd_launch_win_title"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("cmd_launch_win"), textFont, "width: 600px", null), "gapleft 10, wrap");

        add(Components.getLabel(textSrc.getString("cmd_lanuch_Java_title"), titleFont), "wrap");
        add(Components.getTextField(textSrc.getString("cmd_lanuch_Java"), textFont, "width: 600px", null), "gapleft 10, wrap");
    }
}