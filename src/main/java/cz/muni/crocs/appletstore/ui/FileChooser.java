package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileChooser {

    public static JFileChooser getSingleFile(File defaultFolder) {
        JFileChooser fileChooser = build(defaultFolder);
        fileChooser.setMultiSelectionEnabled(false);
        return fileChooser;    }

    public static JFileChooser getSingleFile(File defaultFolder, String filesDescription, String... extensions) {
        JFileChooser fileChooser = build(defaultFolder, filesDescription, extensions);
        fileChooser.setMultiSelectionEnabled(false);
        return fileChooser;
    }

    public static JFileChooser geMmultipleFiles(File defaultFolder, String filesDescription, String... extensions) {
        JFileChooser fileChooser = build(defaultFolder, filesDescription, extensions);
        fileChooser.setMultiSelectionEnabled(true);
        return fileChooser;
    }

    private static JFileChooser build(File defaultFolder) {
        JFileChooser fileChooser = new JFileChooser(defaultFolder);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        return fileChooser;
    }

    private static JFileChooser build(File defaultFolder, String filesDescription, String... extensions) {
        JFileChooser fileChooser = build(defaultFolder);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filesDescription, extensions));
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }
}
