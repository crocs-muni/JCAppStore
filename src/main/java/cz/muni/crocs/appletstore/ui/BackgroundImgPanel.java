package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BackgroundImgPanel extends JPanel {

    private BufferedImage bg;

    public void load(String image) {
        try {
            bg = ImageIO.read(new File(Config.IMAGE_DIR + image));
        } catch (IOException e) {
            e.printStackTrace();
            bg = new BufferedImage(690, 540,BufferedImage.TYPE_INT_RGB);
            Graphics g = bg.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, bg.getWidth(), bg.getHeight());
            g.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bg.getScaledInstance(getWidth(),getHeight(),Image.SCALE_SMOOTH), 0, 0, this);
    }
}
