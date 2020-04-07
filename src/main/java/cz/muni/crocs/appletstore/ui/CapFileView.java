package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.io.File;

/**
 * FieView for FileChooser, defines icon for .cap files
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CapFileView extends FileView {

    private ImageIcon cap = new ImageIcon(Config.IMAGE_DIR + "applet_plain_x16.png");

    @Override
    public Icon getIcon(File f) {
        String name = f.getName();
        int lastDot = name.lastIndexOf(".");
        if(lastDot >- 1 && name.substring(lastDot).equals(".cap")) {
            return cap;
        } else return super.getIcon(f);
    }
}