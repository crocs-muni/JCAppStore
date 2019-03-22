package cz.muni.crocs.appletstore.ui;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomFont {

    //no instances
    private CustomFont() {}

    //public static Font bold;
    public static Font plain = refresh();

    public static Font refresh() {
        Font f;
        try {
            f = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/fonts/x.ttf"));
        } catch (IOException | FontFormatException e) {
            f = new Font("Courier", Font.PLAIN, 14);
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(f);
        return f;
    }

    /**
     *
     * @param style Font constant BOLD/NORMAL
     * @return modified font
     */
    public static Font modify(int size, int style) {
        return plain.deriveFont(style, size);
    }
}
