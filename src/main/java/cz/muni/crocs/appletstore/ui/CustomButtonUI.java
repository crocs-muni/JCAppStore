package cz.muni.crocs.appletstore.ui;


import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * Removing button focus border
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomButtonUI extends BasicButtonUI {

    @Override
    protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {
        //delete
    }
}
