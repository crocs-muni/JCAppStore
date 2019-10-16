package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.crypto.PGP;
import cz.muni.crocs.appletstore.ui.InputHintTextField;
import cz.muni.crocs.appletstore.util.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Application settings
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class ImportPGPKey extends JPanel {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private JTextField pgpkey;
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 5, 1);
    private CompoundBorder frame = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(1, 1, 1, 1), Color.BLACK),
            new EmptyBorder(new Insets(4, 4, 4, 4)));

    public ImportPGPKey() {
        setLayout(new MigLayout("fillx, gap 5px 5px"));
        buildPGP();
    }

    public void apply() {
        File key = new File(pgpkey.getText());
        if (!key.exists()) {
            showMsg(new Tuple<>("no_asc.png", textSrc.getString("key_no_key")));
            return;
        }

        try {
            PGP pgp = new PGP();
            String keyID = pgp.getKeyID(key);

            Tuple<String, String> msg = pgp.importKeyAndGetErrorMessage(key);
            if (msg != null) {
                showMsg(msg);
                return;
            }

            msg = pgp.setKeyTrustAndGetErroMessage(keyID, slider.getValue());
            if (msg != null) {
                showMsg(msg);
                return;
            }
            showMsg(new Tuple<>("key.png", textSrc.getString("key_imported_1") + " " + keyID + " "
                    + textSrc.getString("key_imported_2") + " "
                    + textSrc.getString("trust_" + slider.getValue())));
        } catch (LocalizedSignatureException e) {
            e.printStackTrace();
            showMsg(new Tuple<>("no_key.png", textSrc.getString("key_import_fail")));
        }
    }

    private void buildPGP() {
        JLabel title = new JLabel(textSrc.getString("pgp_key_loc"));
        title.setFont(OptionsFactory.getOptions().getFont());
        add(title, "");

        JButton specify = new JButton(new AbstractAction(textSrc.getString("key_specify_loc")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getKeyChooser();
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    pgpkey.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    pgpkey.setForeground(Color.BLACK);
                    ((InputHintTextField)pgpkey).setShowHint(false);
                }
            }
        });
        add(specify, "align right, wrap");

        pgpkey = new InputHintTextField("", textSrc.getString("H_pgp_loc"));
        pgpkey.setFont(OptionsFactory.getOptions().getFont(12f));
        pgpkey.setBorder(frame);
        add(pgpkey, "span 2, growx, wrap");

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(1, new JLabel(textSrc.getString("trust_1")) );
        labelTable.put(2, new JLabel(textSrc.getString("trust_2")) );
        labelTable.put(3, new JLabel(textSrc.getString("trust_3")) );
        labelTable.put(4, new JLabel(textSrc.getString("trust_4")) );
        labelTable.put(5, new JLabel(textSrc.getString("trust_5")) );
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.setPreferredSize(new Dimension(380, slider.getPreferredSize().height));
        add(slider, "span 2, growx, wrap");
    }

    private JFileChooser getKeyChooser() {
        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.length() <= 2 * (512 * 1024) &&
                        (f.isDirectory() || name.endsWith(".asc"));
            }

            @Override
            public String getDescription() {
                return "Public key files (.asc)";
            }
        });
        return fileChooser;
    }

    private void showMsg(Tuple<String, String> message) {
        JOptionPane.showMessageDialog(this,
                "<html><div width=\"350\"" + message.second + "</div></html>",
                textSrc.getString("info"),
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + message.first));
    }
}
