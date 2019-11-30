package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

public class CustomSignatureDialog extends JPanel {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());
    private String selectedSignatureFile;

    public CustomSignatureDialog() {
        setLayout(new MigLayout());

        add(new JLabel(textSrc.getString("custom_signature")), "wrap");
        JButton getNewBg = new JButton(textSrc.getString("custom_sig_loc"));
        getNewBg.setAction(new AbstractAction(textSrc.getString("custom_sig_loc")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getBasicFileChoser(Config.APP_LOCAL_DIR);
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    selectedSignatureFile = fileChooser.getSelectedFile().getAbsolutePath();
                    getNewBg.setText(selectedSignatureFile);
                }
            }
        });
        add(getNewBg, "align right, wrap");
    }

    private JFileChooser getBasicFileChoser(File defaultFolder) {
        JFileChooser fileChooser = new JFileChooser(defaultFolder);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }
}
