package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * App 'about' section to notify mainly about the version
 */
public class About extends BackgroundImgPanel {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    public About() {
        //image extension added automatically
        super(Config.IMAGE_DIR + "splash");

        setLayout(null);

        JLabel version = new JLabel(textSrc.getString("version") + Config.VERSION);
        version.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD,24f));
        version.setBounds(new Rectangle(80, 57, 200, 50));
        add(version);

        JLabel author = new JLabel(textSrc.getString("author") + "Jiří Horák");
        author.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 12f));
        author.setBounds(new Rectangle(30, 83, 200, 50));
        add(author);
    }
}
