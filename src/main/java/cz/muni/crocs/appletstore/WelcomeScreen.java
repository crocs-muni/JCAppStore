package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.*;
import cz.muni.crocs.appletstore.util.LoaderWorker;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.ProcessTrackable;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

/**
 * App loading - checks for card readers, initializes basic things
 * needed for app to start & loads settings
 */

public class WelcomeScreen extends JWindow implements ActionListener {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public WelcomeScreen() {
        BackgroundImgPanel container = new BackgroundImgPanel();
        container.load(Config.IMAGE_DIR + "canvas.jpg");

        setContentPane(container);

        container.setLayout(new MigLayout());
        container.setBackground(new Color(255, 255, 255, 60));


        container.add(new Title(textSrc.getString("welcome"), 28f), "wrap");
        container.add(new HtmlText("<div width=\"500\">" + textSrc.getString("tips") + "</div>"));

        setSize(container.getPreferredSize());

        setLocationRelativeTo(null);
        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                actionPerformed(null);
                e.consume();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.dispose();
    }
};