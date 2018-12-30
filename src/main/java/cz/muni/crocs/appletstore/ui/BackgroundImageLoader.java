package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.DataBuffer;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImageLoader {

    private BufferedImage background = null;
    private String imgName = "bg.jpg";

    public BackgroundImageLoader(String imgName, Component panel) {
        System.out.println("laoding...");
        load(imgName);
        MediaTracker mediaTracker = new MediaTracker(panel);
        mediaTracker.addImage(background, 0);
        System.out.println("loaded...");
        try {
            mediaTracker.waitForAll();
            applyFilter();
            save();
        } catch (InterruptedException e) {
            e.printStackTrace();
            defaultBg();
        }
    }

    private void save() {
        try {
            //TODO ask how works the system dirs
            File outputfile = new File(Config.APP_DATA_DIR, imgName);
            ImageIO.write(background, "jpg", outputfile);
            System.out.println("saving...");
            System.out.println(Config.APP_DATA_DIR + Config.SEP +imgName);
            Config.options.put(Config.OPT_KEY_BACKGROUND, Config.APP_DATA_DIR + Config.SEP + imgName);
        } catch (IOException e) {
            defaultBg();
        }
    }

    private void defaultBg() {
        Config.options.put(Config.OPT_KEY_BACKGROUND, Config.IMAGE_DIR + imgName);
        System.out.println(Config.IMAGE_DIR + imgName);
    }

    private void load(String name) {
        try {
            System.out.println("progress loading...");
            background = ImageIO.read(new File(name));
        } catch (IOException e) {
            //TODO possible error handling? show user message?
            defaultBg();
        }
    }

    private void applyFilter() {
        System.out.println("filter...");
        int radius = 8;
        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];
        Arrays.fill(data, weight);

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        background = op.filter(background, null);
//        int doubleEdgeSize = size * 2;
//        BufferedImage cut = new BufferedImage(
//                background.getWidth() - doubleEdgeSize,
//                background.getHeight() - doubleEdgeSize,
//                BufferedImage.TYPE_INT_ARGB);
//
//        Graphics2D graphics2D = cut.createGraphics();
//        graphics2D.drawImage(background, null, size, size);
//        graphics2D.dispose();
//        background = cut;
    }
    public BufferedImage get() {
        return background;
    }
}
