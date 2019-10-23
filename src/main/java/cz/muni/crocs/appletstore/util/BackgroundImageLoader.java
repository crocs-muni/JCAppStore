package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class BackgroundImageLoader {
    private static final Logger logger = LogManager.getLogger(BackgroundImageLoader.class);

    private BufferedImage background = null;
    private String imgName = "bg.jpg";

    private int radius;
    private int size;

    public BackgroundImageLoader(String imgName, Component panel, int blurAmount) {
        radius = (blurAmount == 0) ? 0 : 2 + blurAmount * 2;
        size = radius * 2 + 1;

        load(imgName);
        MediaTracker mediaTracker = new MediaTracker(panel);
        mediaTracker.addImage(background, 0);
        try {
            mediaTracker.waitForAll();
            if (blurAmount > 0) applyFilter();
            save();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.warn("Image loading interrupted. " + Config.IMAGE_DIR + imgName, e);
            InformerFactory.getInformer().showInfo("E_image");
            defaultBg();
        }
    }

    private void save() {
        try {
            File outputfile = new File(Config.APP_DATA_DIR, imgName);
            ImageIO.write(background, "jpg", outputfile);
            OptionsFactory.getOptions().addOption(Options.KEY_BACKGROUND, Config.APP_DATA_DIR + Config.S + imgName);
        } catch (IOException e) {
            e.printStackTrace();
            defaultBg();
        }
    }

    private void defaultBg() {
        OptionsFactory.getOptions().addOption(Options.KEY_BACKGROUND, Config.IMAGE_DIR + imgName);
        try {
            background = ImageIO.read(new File(Config.IMAGE_DIR + imgName));
        } catch (IOException e) {
            logger.warn("Failed to read image: " + Config.IMAGE_DIR + imgName, e);
            e.printStackTrace();
        }
    }

    private void load(String name) {
        try {
            background = ImageIO.read(new File(name));
        } catch (IOException e) {
            InformerFactory.getInformer().showInfo("E_image");
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
        //NORMALIZE
        for (int i = 0; i < size; ++i){
            for (int j = 0; j < size; ++j) {
                data[i * size + j] = (float)(data[i * size + j] / sum);
                //System.out.print(data[i * size + j] + ", ");
            }
            //System.out.println();
        }
        return data;
    }

    private void applyFilter() {
        BufferedImageOp filter;
        background = new RescaleOp(.5f, 0f, null).filter(background, null);

        if (radius == 0) {
            return;
        }
        filter = new ConvolveOp(new Kernel(size, size, gaussianMatrix()), ConvolveOp.EDGE_NO_OP, null);
        background = filter.filter(background, null);

        int width = background.getWidth() - size * 2;
        int height = background.getHeight() - size * 2;

        BufferedImage cut = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = cut.createGraphics();
        graphics2D.drawImage(background, 0, 0, width, height, size, size, width, height, null);
        graphics2D.dispose();
        background = cut;
    }
    public BufferedImage get() {
        return background;
    }
}
