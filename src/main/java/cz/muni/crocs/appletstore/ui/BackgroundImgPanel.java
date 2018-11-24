package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImgPanel extends JPanel {

    BufferedImage bg;

    public BackgroundImgPanel() {
        BackgroundImage imgBuilder = new BackgroundImage("bg.jpg", this);
        bg = imgBuilder.get();
        //setBackground(Color.RED);

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        //TODO on smaller width/height jsut cut off
        Image scaledImage = bg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH);
        g.drawImage(scaledImage, 0, 0, this);
    }
}
