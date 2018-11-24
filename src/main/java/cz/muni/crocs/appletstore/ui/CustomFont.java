package cz.muni.crocs.appletstore.ui;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomFont {

    //public static Font bold;
    public static Font plain;

    public static void refresh() {
        try {
            //TODO custom font or let the Courier?
            //bold = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/fonts/x.ttf"));
            plain = Font.createFont(Font.TRUETYPE_FONT, new File("src/main/resources/fonts/x.ttf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //ge.registerFont(bold);
            ge.registerFont(plain);
        } catch (IOException | FontFormatException e) {
            plain = new Font("Courier", Font.PLAIN, 14);
        }
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
