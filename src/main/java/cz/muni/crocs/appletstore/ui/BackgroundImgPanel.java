package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
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
public class BackgroundImgPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundImgPanel.class);
    private Image bg;
    private Image orig;
    private int iWidth, iHeight;
    private int width = -1, height = -1;

    /**
     * Default split panel initialization
     */
    public BackgroundImgPanel(String imgPath) {
        setBorder(null);

        File f = findImageByExtension(imgPath);

        if (f == null) {
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

    private File findImageByExtension(String path) {
        if (path.isEmpty()) return null;

        int i = 0;
        while (i < Config.IMAGE_EXTENSIONS.length) {
            File f = new File(path + Config.IMAGE_EXTENSIONS[i]);
            if (f.exists()) return f;
            i++;
        }
        return null;
    }

    private void loadDefault() {
        orig = new BufferedImage(690, 540, BufferedImage.TYPE_INT_RGB);
        iWidth = 690;
        iHeight = 540;
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
