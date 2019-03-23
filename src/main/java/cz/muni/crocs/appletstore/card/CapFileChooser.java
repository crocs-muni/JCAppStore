package cz.muni.crocs.appletstore.card;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.Informer;
import cz.muni.crocs.appletstore.ui.CapFileView;
import cz.muni.crocs.appletstore.util.Sources;
import pro.javacard.CAPFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * File chooser to obtain cap file
 * @author Jiří Horák
 * @version 1.0
 */
public class CapFileChooser {

    public static CAPFile getCapFile(File from) {
        CAPFile instcap = null;
        try (FileInputStream fin = new FileInputStream(from)) {
            instcap = CAPFile.fromStream(fin);
        } catch (IOException e) {
            Informer.getInstance().showInfo(Sources.language.get("E_install_no_file_1") +
                    from.getAbsolutePath() + Sources.language.get("E_install_no_file_1´2"));
        }
        return instcap;
    }

    public static CAPFile chooseCapFile() {
        JFileChooser fileChooser = new JFileChooser(Config.APP_LOCAL_DIR);
        fileChooser.setFileView(new CapFileView());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(Sources.language.get("cap_files"), "cap"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File cap = fileChooser.getSelectedFile();
            if (!cap.exists()) {
                Informer.getInstance().showInfo(Sources.language.get("E_install_no_file_1") + cap.getAbsolutePath() + Sources.language.get("E_install_no_file_2"));
                return null;
            }
            return getCapFile(cap);
        }
        return null;
    }
}
