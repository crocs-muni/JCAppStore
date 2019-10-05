package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.Components;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class Keybase extends JPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public Keybase() {
        setLayout(new MigLayout());
        Font titleFont = OptionsFactory.getOptions().getTitleFont(Font.BOLD, 20f);
        Font textFont = OptionsFactory.getOptions().getFont();
        //todo describe keybase
    }
}
