package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.util.JSONStoreParser;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItemInfo extends JPanel {

    JComboBox<String> versionComboBox;
    JComboBox<String> compilerVersionComboBox;


    public StoreItemInfo(JsonObject dataSet, Searchable store) {
        setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new MigLayout());

        final Color bg = new Color(255, 255, 255, 80);
        final Font textFont = CustomFont.plain.deriveFont(14f);
        final Font titleFont = CustomFont.plain.deriveFont(Font.BOLD, 20f);

        JLabel back = new JLabel(new ImageIcon(Config.IMAGE_DIR + "back.png"));
        back.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                store.showItems("");
            }
        });
        add(back, "span 1 2");

        JLabel icon = new JLabel(getIcon(dataSet.get(Config.JSON_TAG_ICON).getAsString()));
        icon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(icon, "span 1 2");

        JLabel name = new JLabel(dataSet.get(Config.JSON_TAG_TITLE).getAsString());
        name.setFont(titleFont);
        add(name, "align left, gaptop 40, width ::350");

        JButton install = new JButton("<html><div style=\"margin: 1px 10px;\">" + Config.translation.get(28) + "</div></html>");
        install.setUI(new CustomButtonUI());
        install.setFont(CustomFont.plain.deriveFont(Font.BOLD, 20f));
        install.setForeground(Color.WHITE);
        install.setCursor(new Cursor(Cursor.HAND_CURSOR));
        install.setBackground(new Color(26, 196, 0));
        add(install, "align right, span 1 2, wrap");

        JLabel author = new JLabel(Config.translation.get(132) + dataSet.get(Config.JSON_TAG_AUTHOR).getAsString());
        author.setFont(CustomFont.plain.deriveFont(15f));
        add(author, "align left, gapbottom 40, width ::350, wrap");

        JLabel mainInfo = new JLabel("<html><div style=\"margin: 10px; width:650px\">" + dataSet.get(Config.JSON_TAG_DESC).getAsString() + "</div></html>");
        mainInfo.setBackground(new Color(255, 255, 255, 80));
        mainInfo.setOpaque(true);
        mainInfo.setFont(textFont);
        add(mainInfo, "span 4, gap 20, wrap");

        JLabel urlTitle = new JLabel(Config.translation.get(130));
        urlTitle.setFont(titleFont);
        urlTitle.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(urlTitle, "span 2,  gapleft 40, gaptop 20");

        final String urlAddress = dataSet.get(Config.JSON_TAG_URL).getAsString();
        JLabel url = new JLabel("<html><div style=\"margin: 5px;\">" + urlAddress + "</div></html>");
        url.setOpaque(true);
        url.setBackground(bg);
        url.setCursor(new Cursor(Cursor.HAND_CURSOR));
        url.setFont(textFont.deriveFont(Font.BOLD));
        url.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(urlAddress));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        add(url, "span 2, gaptop 20, gapleft 40, wrap");

        JLabel installInfo = new JLabel("<html><div style=\"margin: 10px; width:650px\">" + dataSet.get(Config.JSON_TAG_USAGE).getAsString() + "</div></html>");
        installInfo.setBackground(new Color(255, 255, 255, 80));
        installInfo.setOpaque(true);
        installInfo.setFont(textFont);
        add(installInfo, "span 4, gap 20, gaptop 20, wrap");

        JLabel versionTitle = new JLabel(Config.translation.get(133));
        versionTitle.setFont(titleFont);
        versionTitle.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(versionTitle); //todo span 2

        String[] versions = JSONStoreParser.jsonArrayToDataArray(dataSet.getAsJsonArray(Config.JSON_TAG_VERSION));
        versionComboBox = new JComboBox<>(versions);
        versionComboBox.setMaximumRowCount(4);
        //todo disables the build combobox exchange...?
        //versionComboBox.setSelectedItem(dataSet.get(Config.JSON_TAG_LATEST).getAsString());
        versionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] compilerVersions = JSONStoreParser.jsonArrayToDataArray(
                        dataSet.getAsJsonObject(Config.JSON_TAG_BUILD).getAsJsonArray((String) versionComboBox.getSelectedItem()));
                compilerVersionComboBox.setModel(new JComboBox<>(compilerVersions).getModel());
            }
        });
        add(versionComboBox, "align right");

        JsonObject builds = dataSet.getAsJsonObject(Config.JSON_TAG_BUILD);


        String[] compilerVersions = JSONStoreParser.jsonArrayToDataArray(builds.getAsJsonArray(versions[0]));
        compilerVersionComboBox = new JComboBox<>(compilerVersions);
        add(compilerVersionComboBox, "align right");


        JButton customInstall = new JButton("<html><div style=\"margin: 1px 10px;\">" + Config.translation.get(28) + "</div></html>");
        customInstall.setUI(new CustomButtonUI());
        customInstall.setFont(CustomFont.plain.deriveFont(Font.BOLD, 18f));
        customInstall.setForeground(Color.WHITE);
        customInstall.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customInstall.setBackground(new Color(170, 166, 167));
        add(customInstall, "align left, wrap");
    }

    //todo or java resize approach new Label(imageIcon)
    private ImageIcon getIcon(String image) {
        File img = new File(Config.RESOURCES + image);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "no_img.png");
        BufferedImage newIcon = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        //draw image with clip
        Graphics2D graphics2D = newIcon.createGraphics();

        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setClip(new Arc2D.Float(6, 6, 108, 108, 0, 360, Arc2D.OPEN));
        try {
            graphics2D.drawImage(ImageIO.read(img), 0, 0, 120, 120, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //remove clip
        graphics2D.setClip(null);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setStroke(new BasicStroke(8));
        graphics2D.drawArc(4, 4, 112, 112, 0, 360);
        graphics2D.dispose();
        return new ImageIcon(newIcon);
    }
}
