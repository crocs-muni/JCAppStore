package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.ui.CapFileView;
import pro.javacard.CAPFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * File chooser to obtain cap file
 * @author Jiří Horák
 * @version 1.0
 */
public class CapFileChooser {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    public static CAPFile getCapFile(File from) {
        CAPFile instcap = null;
        if (from == null) return null;
        try (FileInputStream fin = new FileInputStream(from)) {
            instcap = CAPFile.fromStream(fin);
        } catch (IOException e) {
            e.printStackTrace();
            InformerFactory.getInformer().showMessage(textSrc.getString("E_install_no_file_1") +
                    from.getAbsolutePath() + textSrc.getString("E_install_no_file_2"));
        }
        return instcap;
    }

    public static File chooseCapFile(File dir) {
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setFileView(new CapFileView());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(textSrc.getString("cap_files"), "cap"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File cap = fileChooser.getSelectedFile();
            if (!cap.exists()) {
                InformerFactory.getInformer().showMessage(textSrc.getString("E_install_no_file_1") +
                        cap.getAbsolutePath() + textSrc.getString("E_install_no_file_2"));
                return null;
            }
            return cap;
        }
        return null;
    }
}
