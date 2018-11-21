package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class ListItem {

    private String text;
    private Icon image;

    ListItem(String text, Icon image) {
        this.text = text;
        this.image = image;

    }

    public String getText() {
        return text;
    }

    public Icon getImage() {
        return image;
    }
}
