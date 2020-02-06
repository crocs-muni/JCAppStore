package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.crypto.PGP;
import cz.muni.crocs.appletstore.ui.*;
import cz.muni.crocs.appletstore.util.*;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Application settings
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Settings extends JPanel {
    private static Logger logger = LoggerFactory.getLogger(Settings.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    private static final String DEFAULT_BG_PATH = Config.IMAGE_DIR + "bg.jpg";
    private static final Tuple[] LANGUAGES = new Tuple[]{
            new Tuple<>("en", "English")/*,
            new Tuple<>("cz", "Česky")*/
    };

    private JTextField pgp;
    private String bgImg = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 8, 1);
    private JComboBox<Language> languageBox;
    private JCheckBox hintEnabled = new JCheckBox();
    private JCheckBox simple = new JCheckBox();
    private JCheckBox jcMemoryKept = new JCheckBox();
    private JCheckBox implicitDelete = new JCheckBox();
    private BackgroundChangeable context;
    private CompoundBorder frame = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(1, 1, 1, 1), Color.BLACK),
            new EmptyBorder(new Insets(4, 4, 4, 4)));

    public Settings(BackgroundChangeable context) {
        this.context = context;
        setLayout(new MigLayout("fillx, gap 5px 5px"));
        buildPGP();
//        buildJCKeep();
//        buildImplicitDelete();
//        buildSimpleMode();
        buildLanguage();
//        buildHint();
        buildBackground();
    }

    public void apply() {
        saveBackgroundImage();
        saveLanguage();
//        saveHint();
//        saveSimpleMode();
//        saveJCKeep();
        savePGP();
//        saveImplicitDelete();
    }

    private void buildPGP() {
        add(new Text(textSrc.getString("pgp_loc")), "");

        JButton specify = new JButton(new AbstractAction(textSrc.getString("pgp_specify_loc")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getPGPFileChooser();
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    pgp.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    pgp.setForeground(Color.BLACK);
                    ((InputHintTextField)pgp).setShowHint(false);
                }
            }
        });
        add(specify, "span 2, align right, wrap");

        String path = OptionsFactory.getOptions().getOption(Options.KEY_PGP_LOCATION);
        if (path == null) path = "";

        pgp = new InputHintTextField(path, textSrc.getString("H_pgp_loc"));
        pgp.setFont(OptionsFactory.getOptions().getFont(12f));
        pgp.setBorder(frame);
        add(pgp, "span 3, growx, wrap");
    }

    private JFileChooser getPGPFileChooser() {
        return getShaderFileChoser(new File(System.getProperty("user.home")));
    }

    private void buildBackground() {
        add(new Text(textSrc.getString("background")), "");

        String path = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
        if (path.equals(DEFAULT_BG_PATH)) {
            path = textSrc.getString("default");
            slider.setEnabled(false);
        }
        cutString(path);
        JLabel bgValue = new HtmlText(path);
        bgValue.setFont(OptionsFactory.getOptions().getFont(12f));
        bgValue.setBorder(frame);

        JButton defaultBg = new JButton(new AbstractAction(textSrc.getString("reset_default")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                bgImg = textSrc.getString("default");
                bgValue.setText(bgImg);
                slider.setValue(1);
                slider.setEnabled(false);
            }
        });
        add(defaultBg, "align right");

        JButton getNewBg = new JButton(new AbstractAction(textSrc.getString("change")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getBGImageFileChooser();
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    bgImg = fileChooser.getSelectedFile().getAbsolutePath();
                    bgValue.setText(cutString(bgImg));
                    slider.setEnabled(true);
                }
            }
        });
        add(getNewBg, "align right, wrap");

        add(bgValue, "span 3, growx, wrap");

        //blur option
        add(new Text(textSrc.getString("blur")), "");
        slider.setEnabled(false);
        add(slider, "w 180, align right, span 2, wrap");
    }

    private void buildLanguage() {
        add(new Text(textSrc.getString("language")), "");

        languageBox = new JComboBox<>(LanguageImpl.values());
        LanguageComboBoxItem listItems = new LanguageComboBoxItem();
        languageBox.setMaximumRowCount(4);
        languageBox.setRenderer(listItems);
        languageBox.setSelectedItem(OptionsFactory.getOptions().getLanguage());
        add(languageBox, "align right, span 2, w 180, wrap");
    }

    private void buildImplicitDelete() {
        add(new Text(textSrc.getString("implicit_delete")), "");
        implicitDelete.setSelected(OptionsFactory.getOptions().is(Options.KEY_DELETE_IMPLICIT));
        add(implicitDelete, "align right, span 2, w 180, wrap");
    }

    private void buildSimpleMode() {
        add(new Text(textSrc.getString("enable_simple")), "");
        simple.setSelected(OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE));
        add(simple, "align right, span 2, w 180, wrap");
    }

    private void buildJCKeep() {
        add(new Text(textSrc.getString("enable_jcmemory")), "");
        jcMemoryKept.setSelected(OptionsFactory.getOptions().is(Options.KEY_KEEP_JCMEMORY));
        add(jcMemoryKept, "align right, span 2, w 180, wrap");
    }

    private void buildHint() {
        add(new Text(textSrc.getString("enable_hints")),"");
        hintEnabled.setSelected(OptionsFactory.getOptions().is(Options.KEY_HINT));
        add(hintEnabled, "align right, span 2, w 180, wrap");
    }

    private JFileChooser getShaderFileChoser(File defaultFolder) {
        JFileChooser fileChooser = new JFileChooser(defaultFolder);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }

    private JFileChooser getBGImageFileChooser() {
        JFileChooser fileChooser = getShaderFileChoser(FileSystemView.getFileSystemView().getDefaultDirectory());
        fileChooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                String name = f.getName().toLowerCase();
                return f.length() <= 2 * (512 * 1024) &&
                        (f.isDirectory() ||
                                name.endsWith(".png") ||
                                name.endsWith(".jpg") ||
                                name.endsWith(".jpeg") ||
                                name.endsWith(".bmp"));
            }

            @Override
            public String getDescription() {
                return textSrc.getString("image_limit");
            }
        });
        return fileChooser;
    }

    private String cutString(String value, int length) {
        if (value.length() > length) {
            int len = value.length();
            value = "..." + value.substring(len - length + 3, len);
        }
        return value;
    }

    private String cutString(String value) {
        return cutString(value, 45);
    }

    private void saveBackgroundImage() {
        if (bgImg.equals(OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND))) {
            return;
        }
        if (bgImg.equals(textSrc.getString("default"))) {
            try {
                OptionsFactory.getOptions().addOption(Options.KEY_BACKGROUND, Config.IMAGE_DIR + "bg.jpg");
                context.updateBackground(ImageIO.read(new File(DEFAULT_BG_PATH)));
            } catch (IOException e) {
                e.printStackTrace();
                logger.warn("Failed to load custom image " + bgImg, e);
                InformerFactory.getInformer().showMessage(textSrc.getString("E_background"));
            }
        } else {
            BackgroundImageLoader loader = new BackgroundImageLoader(bgImg, this, slider.getValue());
            context.updateBackground(loader.get());
        }
    }

    private void savePGP() {
        OptionsFactory.getOptions().addOption(Options.KEY_PGP_LOCATION, pgp.getText());
        PGP.invalidate();
    }

    private void saveLanguage() {
        Language lang = (Language)languageBox.getSelectedItem();
        if (lang == null) {
            lang = LanguageImpl.DEFAULT;
        }

        if (lang.equals(OptionsFactory.getOptions().getLanguage())) return;

        OptionsFactory.getOptions().setLanguage(lang);
        showAlertChange();
    }

    private void saveImplicitDelete() {
        OptionsFactory.getOptions().addOption(Options.KEY_DELETE_IMPLICIT, implicitDelete.isSelected() ? "true" : "false");
    }

    private void saveHint() {
        OptionsFactory.getOptions().addOption(Options.KEY_HINT, hintEnabled.isSelected() ? "true" : "false");
        HintPanel.enableHint(hintEnabled.isSelected());
    }

    private void saveSimpleMode() {
        OptionsFactory.getOptions().addOption(Options.KEY_SIMPLE_USE, simple.isSelected() ? "true" : "false");
    }

    private void saveJCKeep() {
        OptionsFactory.getOptions().addOption(Options.KEY_KEEP_JCMEMORY, jcMemoryKept.isSelected() ? "true" : "false");
    }

    /**
     * Change alert notification
     * display: changes will apply
     */
    private void showAlertChange() {
        JOptionPane.showMessageDialog(null,
                textSrc.getString("reset_to_apply"),
                textSrc.getString("reset_to_apply_title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
