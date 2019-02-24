package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;


/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImageLoader {

    private BufferedImage background = null;
    private String imgName = "bg.jpg";

    private final int radius = 5;
    private final int size = radius * 2 + 1;

    public BackgroundImageLoader(String imgName, Component panel, int blurAmount) {
        load(imgName);
        MediaTracker mediaTracker = new MediaTracker(panel);
        mediaTracker.addImage(background, 0);
        try {
            mediaTracker.waitForAll();
            if (blurAmount > 0) applyFilter(blurAmount);
            save();
        } catch (InterruptedException e) {
            //todo error log
            Informer.getInstance().showInfo(152);
            e.printStackTrace();
            defaultBg();
        }
    }

    private void save() {
        try {
            //TODO ask how works the system dirs
            File outputfile = new File(Config.APP_DATA_DIR, imgName);
            ImageIO.write(background, "jpg", outputfile);
            Config.options.put(Config.OPT_KEY_BACKGROUND, Config.APP_DATA_DIR + Config.SEP + imgName);
        } catch (IOException e) {
            defaultBg();
        }
    }

    private void defaultBg() {
        Config.options.put(Config.OPT_KEY_BACKGROUND, Config.IMAGE_DIR + imgName);
        try {
            background = ImageIO.read(new File(Config.IMAGE_DIR + imgName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load(String name) {
        try {
            background = ImageIO.read(new File(name));
        } catch (IOException e) {
            //TODO possible error handling? show user message?
            defaultBg();
        }
    }

    private float[] gaussianMatrix() {
        final double sigma = 8d;
        double sum = 0;

        float[] data = new float[size * size];
        int index = 0;
        for (int i = -radius; i <= radius; ++i){
            for (int j = -radius; j <= radius; ++j) {
                data[index] = (float)
                        (Math.exp( (i * i + j * j) / (-2 * sigma * sigma)) / (2 * Math.PI * sigma * sigma));
                sum += data[index];
                ++index;
            }
        }
        //NORMALIZE and subtract 0.003 to darken the image
        for (int i = 0; i < size; ++i){
            for (int j = 0; j < size; ++j) {
                data[i * size + j] = (float)(data[i * size + j] / sum - 0.003);
            }
        }
        return data;
    }

    private void applyFilter(int blurAmount) {
        Kernel kernel = new Kernel(size, size, gaussianMatrix());
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        for (; blurAmount > 0; --blurAmount) {
            background = op.filter(background, null);
        }
        int width = background.getWidth() - size * 2;
        int height = background.getHeight() - size * 2;

        BufferedImage cut = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = cut.createGraphics();
        graphics2D.drawImage(background, 0, 0, width, height, size, size, width, height, null);
        graphics2D.dispose();
        background = cut;
    }
    public BufferedImage get() {
        return background;
    }
}
