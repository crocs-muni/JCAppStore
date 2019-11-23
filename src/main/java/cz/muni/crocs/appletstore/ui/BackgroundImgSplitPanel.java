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
public class BackgroundImgSplitPanel extends JSplitPane {

    private Image bg;
    private int width = -1, height = -1;

    public BackgroundImgSplitPanel() {
        super(JSplitPane.VERTICAL_SPLIT);
        setup();
    }

    public BackgroundImgSplitPanel(int direction) {
        super(direction);
        setup();
    }

    public void setNewBackground(BufferedImage newBackground) {
        width = getWidth();
        height = getHeight();
        bg = newBackground.getScaledInstance(width,height,Image.SCALE_SMOOTH);
        revalidate();
        repaint();
    }

    private void setup() {
        setContinuousLayout(true);
        setUI(new CustomSplitPaneUI());
        setBorder(null);

        String bgImagname = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
        File f = new File(bgImagname);

        if (bgImagname.isEmpty() || !f.exists()) {
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
        int w = getWidth();
        int h = getHeight();

        if (width != w && height != h) {
            bg = bg.getScaledInstance(w,h,Image.SCALE_SMOOTH);
            width = w;
            height = h;
        }
        g.drawImage(bg, 0, 0, this);
    }
}
