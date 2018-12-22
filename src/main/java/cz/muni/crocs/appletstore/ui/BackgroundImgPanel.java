package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

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

    BufferedImage bg;

    public BackgroundImgPanel() {
        String bgImagname = Config.options.get(Config.OPT_KEY_BACKGROUND);
        if (bgImagname == null) {
            loadDefault();
        } else {
            try {
                bg = ImageIO.read(new File(Config.IMAGE_DIR + bgImagname));
            } catch (IOException e) {
                e.printStackTrace();
                loadDefault();
            }
        }
    }

    private void loadDefault() {
//        //first run to blur the default image, otherwise just load
//        BackgroundImageLoader imgBuilder = new BackgroundImageLoader("bg.jpg", this);
//        bg = imgBuilder.get();
        try {
            bg = ImageIO.read(new File(Config.IMAGE_DIR + "bg.jpg"));
        } catch (IOException e) {
            //build white background
            //TODO show user unable to load default background
            bg = new BufferedImage(690, 540,BufferedImage.TYPE_INT_RGB);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //TODO on smaller width/height jsut cut off
        Image scaledImage = bg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
        g.drawImage(scaledImage, 0, 0, this);
    }
}
