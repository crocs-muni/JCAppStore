package cz.muni.crocs.appletstore;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Icons {

    private static final File DIR = new File(Config.IMAGE_DIR, "/icons");
    private static final String[] EXTENSIONS = new String[]{
            "gif", "png", "bmp" //supported formats
    };

    private static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };

    public static ArrayList<BufferedImage> getImages() throws IOException {
        ArrayList<BufferedImage> icons = new ArrayList<>();
        if (DIR.isDirectory()) {
            for (final File f : Objects.requireNonNull(DIR.listFiles(IMAGE_FILTER))) {
                BufferedImage img;
                try {
                    img = ImageIO.read(f);
                    icons.add(img);
                } catch (final IOException e) {
                    e.printStackTrace();
                    //TODO: put default images
                }
            }
        } else {
            throw new IOException();
        }
        return icons;
    }
}
