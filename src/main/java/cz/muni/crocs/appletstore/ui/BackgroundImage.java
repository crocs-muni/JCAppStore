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
public class BackgroundImage {

    private BufferedImage background = null;
    private String imgName = "bg.jpg";

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
        save();
    }

    private void save() {
        try {
            //TODO ask how works the system dirs
            File outputfile = new File(Config.IMAGE_DIR + imgName);
            ImageIO.write(background, "jpg", outputfile);
        } catch (IOException e) {
            Config.options.put("background", "bg.jpg");
        }
    }

    private void load(String name, int tries) {
        try {
            background = ImageIO.read(new File(Config.IMAGE_DIR + name));
            DataBuffer dataBuffer = background.getData().getDataBuffer();
            long sizeInBytes = ((long)dataBuffer.getSize()) * 4L;
            if (sizeInBytes > 1024L * 1024L * 2L) { //size greater than
                //TODO show bad file size
                load(imgName, 0);
            }
             imgName = name; //if succesfully loaded, update name to new image
        } catch (IOException e) {
            //TODO possible error handling? show user message?
            if (tries != 0) e.printStackTrace();
            else load("bg.jpg", 0);
        }
    }

    private void applyFilter() {
        int radius = 8;
        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];
        Arrays.fill(data, weight);

        Kernel kernel = new Kernel(size, size, data);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        background = op.filter(background, null);;
    }

    public BufferedImage get() {
        return background;
    }
}
