package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.Informer;
import cz.muni.crocs.appletstore.util.InformerFactory;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Background panel used as a base class for MainPanel, displays the background image
 * can display two panels - main application GUI and logger (default:hidden)
 *
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImgSplitPanel extends JSplitPane {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundImgSplitPanel.class);
    private Image bg;
    private Image orig;
    private int iWidth, iHeight;
    private int width = -1, height = -1;

    /**
     * Default split panel initialization
     */
    public BackgroundImgSplitPanel() {
        super(JSplitPane.VERTICAL_SPLIT);
        setup();
    }

    /**
     * Split panel initialization with custom directon
     * @param direction split panel direction, JSplitPane constant VERTICAL_SPLIT or HORIZONTAL_SPLIT
     */
    public BackgroundImgSplitPanel(int direction) {
        super(direction);
        setup();
    }

    /**
     * Set new background dimage
     * @param newBackground image to set
     */
    public void setNewBackground(BufferedImage newBackground) {
        width = getWidth();
        height = getHeight();
        orig = newBackground;
        updateBg(width, height);
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
                orig = ImageIO.read(f);
                iWidth = ((BufferedImage)orig).getWidth();
                iHeight = ((BufferedImage)orig).getHeight();
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Could not open background image file " + bg, e);
                loadDefault();
            }
        }
    }

    private void loadDefault() {
        try {
            orig = ImageIO.read(new File(Config.IMAGE_DIR + "bg.jpg"));
            iWidth = ((BufferedImage)orig).getWidth();
            iHeight = ((BufferedImage)orig).getHeight();
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Failed to load default background image, failsafe to plain color.", e);
            bg = new BufferedImage(690, 540,BufferedImage.TYPE_INT_RGB);
            iWidth = 690;
            iHeight = 540;
        }
    }

    private void updateBg(float w, float h) {
        float wscale = w / iWidth;
        float hscale = h / iHeight;
        float scale = Math.max(wscale, hscale);

        bg = orig.getScaledInstance((int)(iWidth*scale),(int)(iHeight*scale),Image.SCALE_SMOOTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();

        if (width != w || height != h) {
            updateBg(w, h);
            width = w;
            height = h;
        }
        g.drawImage(bg, 0, 0, this);
    }
}
