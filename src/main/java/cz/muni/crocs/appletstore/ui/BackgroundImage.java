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
import java.util.Arrays;
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


        int radius = 8;
        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];
        Arrays.fill(data, weight);

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        //tbi is BufferedImage
        BufferedImage i = op.filter(background, null);
        background = i;
    }

    public BufferedImage get() {
        return background;
    }
}
