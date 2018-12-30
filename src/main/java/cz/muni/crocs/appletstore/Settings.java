package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.BackgroundImageLoader;
import cz.muni.crocs.appletstore.ui.BackgroundImgPanel;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.CustomJmenu;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class Settings extends JPanel {

    private String bgImg = Config.options.get(Config.OPT_KEY_BACKGROUND);
    private final String defaultBgPath = Config.IMAGE_DIR + "bg.jpg";
    private AppletStore context;

    private CompoundBorder frame = BorderFactory.createCompoundBorder(
            new MatteBorder(new Insets(1, 1, 1, 1), Color.BLACK),
            new EmptyBorder(new Insets(4, 4, 4, 4)));

    public Settings(AppletStore context) {
        this.context = context;
        setPreferredSize(new Dimension(350, context.getHeight() / 2));
//        setBackground(new Color(255, 255, 255, 105));
        setLayout(new MigLayout("fillx"));

        JLabel bgTitle = new JLabel(Config.translation.get(117));
        bgTitle.setFont(CustomFont.plain);
        add(bgTitle);

        String path = Config.options.get(Config.OPT_KEY_BACKGROUND);
        if (path.equals(defaultBgPath)) {
            path = Config.translation.get(119);
        }

        if (path.length() > 50) {
            int len = path.length();
            path = "..." + path.substring(len - 47, len);
        }
        JLabel bgValue = new JLabel("<html>" + path + "</html>");
        bgValue.setFont(CustomFont.plain);
        bgValue.setBorder(frame);
        bgValue.setBackground(Color.WHITE);
        bgValue.setOpaque(true);

        JButton defaultBg = new JButton(new AbstractAction(Config.translation.get(120)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                bgImg = Config.translation.get(119);
                bgValue.setText(bgImg);
            }
        });
        add(defaultBg, "align right");

        JButton getNewBg = new JButton(new AbstractAction(Config.translation.get(118)) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", ImageIO.getReaderFileSuffixes()));
                fileChooser.setAcceptAllFileFilterUsed(false);
                int r = fileChooser.showOpenDialog(null);
                if (r == JFileChooser.APPROVE_OPTION) {
                    bgImg = fileChooser.getSelectedFile().getAbsolutePath();
                    bgValue.setText(bgImg);
                }
            }
        });
        add(getNewBg, "align right, wrap");

        add(bgValue, "span 3, growx, wrap");
    }


    private void saveBackgroundImage() {
        if (bgImg.equals(Config.options.get(Config.OPT_KEY_BACKGROUND))) {
            return;
        }
        if (bgImg.equals(Config.translation.get(119))) {
            try {
                Config.options.put(Config.OPT_KEY_BACKGROUND, Config.IMAGE_DIR + "bg.jpg");
                ((BackgroundImgPanel) context.getContentPane()).setNewBackground(
                        ImageIO.read(new File(defaultBgPath)));
            } catch (IOException e) {
                //todo show error
                e.printStackTrace();
            }
        } else {
            BackgroundImageLoader loader = new BackgroundImageLoader(bgImg, this);
            ((BackgroundImgPanel) context.getContentPane()).setNewBackground(loader.get());
        }

    }

    public void apply() {
        saveBackgroundImage();
    }


}
