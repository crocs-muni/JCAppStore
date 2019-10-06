package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.Informer;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImgPanel extends JPanel {

    private BufferedImage bg;
    private int width = -1, height = -1;

    public BackgroundImgPanel() {
        String bgImagname = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
        File f = new File(Config.APP_DATA_DIR + Config.S + bgImagname);

        if (bgImagname == null || bgImagname.isEmpty() || !f.exists()) {
            loadDefault();
        } else {
            try {
                bg = ImageIO.read(f);
            } catch (IOException e) {
                e.printStackTrace();
                loadDefault();
            }
        }
    }

    public void setNewBackground(BufferedImage newBackground) {
        bg = newBackground;
    }

    private void loadDefault() {
        try {
            bg = ImageIO.read(new File(Config.IMAGE_DIR + "bg.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            bg = new BufferedImage(690, 540,BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (width == getWidth() && height == getHeight()) {
            g.drawImage(bg, 0, 0, this);
        } else {
            g.drawImage(bg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH), 0, 0, this);
        }
    }
}
