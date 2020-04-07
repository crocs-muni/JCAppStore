package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

/**
 * Just a class for use in LanguageComboBoxItem, a bit of tuple logic..
 *
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
