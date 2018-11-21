package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImage {

    private BufferedImage background = null;

    private void load(String name, int tries) {
        //TODO cycling when not found repair
        try {
            background = ImageIO.read(new File(Config.IMAGE_DIR + name));
        } catch (IOException e) {
            if (tries != 0) e.printStackTrace();
            else load("bg-menu.jpg", 0);
        }
    }

    public BackgroundImage(String imgName, Component panel) {
        load(imgName, 1);
        MediaTracker mediaTracker = new MediaTracker(panel);
        mediaTracker.addImage(background, 0);

        try {
            mediaTracker.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
       applyFilter();

    }

    private void applyFilter() {
        float ninth = 1.0f / 8.0f;
        float[] blurMatrix = {
                ninth, ninth, ninth,
                ninth, ninth, ninth,
                ninth, ninth, ninth
        };
        BufferedImageOp filter = new ConvolveOp(new Kernel(3, 3, blurMatrix),
                ConvolveOp.EDGE_NO_OP, null);
        filter.filter(background, null);
    }

    public BufferedImage get() {
        return background;
    }
}
