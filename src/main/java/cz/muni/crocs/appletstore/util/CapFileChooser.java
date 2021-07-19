package cz.muni.crocs.appletstore.util;

import cz.muni.crocs.appletstore.ui.CapFileView;
import cz.muni.crocs.appletstore.ui.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.javacard.CAPFile;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * File chooser to obtain cap file
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class CapFileChooser {
    private static final Logger logger = LoggerFactory.getLogger(CapFileChooser.class);
    private static final ResourceBundle textSrc = ResourceBundle
            .getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    /**
     * Create a CAPFile from a file
     * @param from File instance of the cap file
     * @return CAPFile representation
     */
    public static CAPFile getCapFile(File from) {
        CAPFile instcap = null;
        if (from == null) return null;
        try (FileInputStream fin = new FileInputStream(from)) {
            instcap = CAPFile.fromStream(fin);
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Could not load CAP file " + from, e);
            InformerFactory.getInformer().showInfoMessage(textSrc.getString("E_install_no_file_1") +
                    from.getAbsolutePath() + textSrc.getString("E_install_no_file_2"), "error.png");
        }
        return instcap;
    }

    /**
     * Choose a cap file from system, get the latest directory used
     * @return selected cap file or null if selection failed
     */
    public static File chooseCapFile() {
        return chooseCapFile(new File(OptionsFactory.getOptions().getOption(Options.KEY_LAST_SELECTION_LOCATION)));
    }

    /**
     * Choose a cap file from system
     * @param dir directory to start to search in
     * @return selected cap file or null if selection failed
     */
    public static File chooseCapFile(File dir) {
        JFileChooser fileChooser = FileChooser.getSingleFile(dir, textSrc.getString("cap_files"), "cap");
        fileChooser.setFileView(new CapFileView());

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File cap = fileChooser.getSelectedFile();
            if (!cap.exists()) {
                InformerFactory.getInformer().showInfoMessage(textSrc.getString("E_install_no_file_1") +
                        cap.getAbsolutePath() + textSrc.getString("E_install_no_file_2"), "error.png");
                return null;
            }
            OptionsFactory.getOptions().addOption(Options.KEY_LAST_SELECTION_LOCATION, dir.getAbsolutePath());
            return cap;
        }
        return null;
    }
}
