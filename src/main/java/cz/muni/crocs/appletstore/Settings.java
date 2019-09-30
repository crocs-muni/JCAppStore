package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.ui.HtmlLabel;
import cz.muni.crocs.appletstore.ui.JtextFieldWithHint;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.CustomComboBoxItem;
import net.miginfocom.swing.MigLayout;

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

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private static final String DEFAULT_BG_PATH = Config.IMAGE_DIR + "bg.jpg";
    private static final Tuple[] LANGUAGES = new Tuple[]{
            new Tuple<>("en", "English"),
            new Tuple<>("cz", "Česky")
    };

    private JTextField keybase;
    private String bgImg = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
    private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 8, 1);
    private JComboBox<Tuple<String, String>> languageBox;
    private JCheckBox hintEnabled = new JCheckBox();
    private BackgroundChangeable context;
    private CompoundBorder frame = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(1, 1, 1, 1), Color.BLACK),
            new EmptyBorder(new Insets(4, 4, 4, 4)));

    public Settings(BackgroundChangeable context) {
        this.context = context;
        setPreferredSize(new Dimension(350, context.getHeight() / 2));
        setLayout(new MigLayout("fillx, gap 5px 5px"));
        addKeyBase();
        addLanguage();
        addHint();
        addBackground();
    }

    private void addKeyBase() {
        addTitleLabel(textSrc.getString("keybase_loc"), "");

        JButton specify = new JButton(new AbstractAction(textSrc.getString("keybase_specify_loc")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = getKeybaseFileChooser();
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    keybase.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    keybase.setForeground(Color.BLACK);
                }
            }
        });
        add(specify, "span 2, align right, wrap");

        String path = OptionsFactory.getOptions().getOption(Options.KEY_KEYBASE_LOCATION);
        if (path == null) path = "";

        keybase = new JtextFieldWithHint(path, textSrc.getString("H_keybase_loc"), 15);
        keybase.setFont(OptionsFactory.getOptions().getFont(12f));
        keybase.setBorder(frame);
        keybase.setBackground(Color.WHITE);
        add(keybase, "span 3, growx, wrap");
    }

    private void addBackground() {
        addTitleLabel(textSrc.getString("background"), "span 3, wrap");

        String path = OptionsFactory.getOptions().getOption(Options.KEY_BACKGROUND);
        if (path.equals(DEFAULT_BG_PATH)) {
            path = textSrc.getString("default");
            slider.setEnabled(false);
        }
        cutString(path);

        JLabel bgValue = new HtmlLabel(path);
        bgValue.setFont(OptionsFactory.getOptions().getFont(12f));
        bgValue.setBorder(frame);
        bgValue.setBackground(Color.WHITE);
        bgValue.setOpaque(true);
        add(bgValue, "span 3, growx, wrap");

        add(new JLabel()); //empty space

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

        //blur option
        addTitleLabel(textSrc.getString("blur"), "");
        slider.setEnabled(false);
        add(slider, "w 180, align right, span 2, wrap");
    }

    public void apply() {
        saveKeyBase();
        saveBackgroundImage();
        saveLanguage();
        saveHint();
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
                return "Images (png, jpeg/jpg, bmp) less than 1.5 MB";
            }
        });
        return fileChooser;
    }

    private JFileChooser getKeybaseFileChooser() {
        return getShaderFileChoser(new File(System.getProperty("user.home")));
    }

    private void addLanguage() {
        addTitleLabel(textSrc.getString("language"), "");

        languageBox = new JComboBox<>(LANGUAGES);
        CustomComboBoxItem listItems = new CustomComboBoxItem();
        languageBox.setMaximumRowCount(4);
        languageBox.setRenderer(listItems);
        add(languageBox, "align right, span 2, w 180, wrap");
    }

    private void addHint() {
        addTitleLabel(textSrc.getString("enable_hints"), "");
        hintEnabled.setSelected(OptionsFactory.getOptions().getOption(Options.KEY_HINT).equals("true"));
        add(hintEnabled, "align right, span 2, w 180, wrap");
    }

    private void addTitleLabel(String titleText, String constraints) {
        JLabel title = new JLabel(titleText);
        title.setFont(OptionsFactory.getOptions().getFont());
        add(title, constraints);
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
                InformerFactory.getInformer().showInfo(textSrc.getString("E_background"));
            }
        } else {
            BackgroundImageLoader loader = new BackgroundImageLoader(bgImg, this, slider.getValue());
            context.updateBackground(loader.get());
        }
    }

    private void saveLanguage() {
        if (LANGUAGES[languageBox.getSelectedIndex()].first.equals(OptionsFactory.getOptions().getOption(Options.KEY_LANGUAGE))) return;
        OptionsFactory.getOptions().addOption(Options.KEY_LANGUAGE, (String)LANGUAGES[languageBox.getSelectedIndex()].first);
        showAlertChange();
        //Config.translation = new Translation(Config.options.get(Options.KEY_LANGUAGE));
    }

    private void saveHint() {
        OptionsFactory.getOptions().addOption(Options.KEY_HINT, hintEnabled.isSelected() ? "true" : "false");
        HintPanel.enableHint(hintEnabled.isSelected());
    }

    private void saveKeyBase() {
        System.out.println(keybase.getText());
        OptionsFactory.getOptions().addOption(Options.KEY_KEYBASE_LOCATION, keybase.getText());
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
