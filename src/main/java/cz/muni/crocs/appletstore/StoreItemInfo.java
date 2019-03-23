package cz.muni.crocs.appletstore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.Searchable;
import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.ui.Warning;
import cz.muni.crocs.appletstore.util.JSONStoreParser;
import cz.muni.crocs.appletstore.util.Sources;
import cz.muni.crocs.appletstore.util.URLAdapter;
import net.miginfocom.swing.MigLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItemInfo extends HintPanel {

    private JComboBox<String> versionComboBox;
    private JComboBox<String> compilerVersionComboBox;

    private final Color bg = new Color(255, 255, 255, 80);
    private final Font textFont = CustomFont.plain.deriveFont(14f);
    private final Font titleFont = CustomFont.plain.deriveFont(Font.BOLD, 20f);

    public StoreItemInfo(JsonObject dataSet, Searchable store) {
        super(Sources.options.get(Config.OPT_KEY_HINT).equals("true"));
        setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new MigLayout());

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

        String app_name = dataSet.get(Config.JSON_TAG_TITLE).getAsString();
        JLabel name = new JLabel(app_name);
        name.setFont(titleFont);
        add(name, "align left, gaptop 40, width ::350");

        JButton install = new JButton("<html><div style=\"margin: 1px 10px;\">" + Sources.language.get("CAP_install") + "</div></html>");
        install.setUI(new CustomButtonUI());
        install.setFont(CustomFont.plain.deriveFont(Font.BOLD, 20f));
        install.setForeground(Color.WHITE);
        install.setCursor(new Cursor(Cursor.HAND_CURSOR));
        install.setBackground(new Color(26, 196, 0));
        install.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String latestV = dataSet.get(Config.JSON_TAG_LATEST).getAsString();
                JsonArray sdks = dataSet.get(Config.JSON_TAG_BUILD).getAsJsonObject().get(latestV).getAsJsonArray();

                File file = new File(Config.APP_STORE_CAPS_DIR + Config.SEP +
                        app_name + Config.SEP + app_name + "_v" + latestV +
                        "_sdk" + sdks.get(sdks.size() - 1).getAsString() + ".casp");
                //todo remove casp
                System.out.println(file.getName());
                System.out.println(file.getAbsolutePath());
                if (!file.exists()) {
                    Informer.getInstance().showWarningToClose("E_install_not_found", Warning.Importance.INFO);
                }
            }
        });
        add(install, "align right, span 1 2, wrap");

        JLabel author = new JLabel(Sources.language.get("author") + dataSet.get(Config.JSON_TAG_AUTHOR).getAsString());
        author.setFont(CustomFont.plain.deriveFont(15f));
        add(author, "align left, gapbottom 40, width ::350, wrap");

        JTextPane mainInfo = new JTextPane();
        mainInfo.setContentType("text/html");
        mainInfo.setText("<html><div style=\"margin: 10px; width:600px\">" + dataSet.get(Config.JSON_TAG_DESC).getAsString() + "</div></html>");
        mainInfo.setBackground(new Color(255, 255, 255, 80));
        mainInfo.setOpaque(true);
        mainInfo.setEditable(false);
        mainInfo.setBorder(null);
        ((DefaultCaret)mainInfo.getCaret()).setUpdatePolicy(0);
        add(mainInfo, "span 4, gap 20, wrap");

        //WEBSITE
        addSubTitle("website", "H_website");

        final String urlAddress = dataSet.get(Config.JSON_TAG_URL).getAsString();
        JLabel url = new JLabel("<html><div style=\"margin: 5px;\"><b>" + urlAddress + "</b></div></html>");
        url.setOpaque(true);
        url.setBackground(bg);
        url.setCursor(new Cursor(Cursor.HAND_CURSOR));
        url.setFont(textFont);
        url.addMouseListener(new URLAdapter(urlAddress));

        add(url, "span 4, gaptop 10, gapleft 20, wrap");

        //INSTALL
        addSubTitle("use", "H_use");

        JTextPane installInfo = new JTextPane();
        installInfo.setContentType("text/html");
        installInfo.setText("<html><div style=\"margin: 10px; width:600px\">" + dataSet.get(Config.JSON_TAG_USAGE).getAsString() + "</div></html>");
        installInfo.setBackground(new Color(255, 255, 255, 80));
        installInfo.setOpaque(true);
        installInfo.setEditable(false);
        installInfo.setBorder(null);
        ((DefaultCaret)installInfo.getCaret()).setUpdatePolicy(0);
        add(installInfo, "span 4, gap 20, gaptop 20, wrap");

        //VERSION
        addSubTitle("custom_install", "H_custom_install");

        String[] versions = JSONStoreParser.jsonArrayToDataArray(dataSet.getAsJsonArray(Config.JSON_TAG_VERSION));
        versionComboBox = new JComboBox<>(versions);
        versionComboBox.setMaximumRowCount(7);
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
        compilerVersionComboBox.setMaximumRowCount(7);
        add(compilerVersionComboBox, "align right");

        JButton customInstall = new JButton("<html><div style=\"margin: 1px 10px;\">" + Sources.language.get("CAP_install") + "</div></html>");
        customInstall.setUI(new CustomButtonUI());
        customInstall.setFont(CustomFont.plain.deriveFont(Font.BOLD, 18f));
        customInstall.setForeground(Color.WHITE);
        customInstall.setCursor(new Cursor(Cursor.HAND_CURSOR));
        customInstall.setBackground(new Color(170, 166, 167));
        add(customInstall, "span 2, align right, wrap");
    }

    private void addSubTitle(String titleKey, String hintKey) {
        HintLabel title = new HintLabel(Sources.language.get(titleKey), Sources.language.get(hintKey));
        title.setFont(titleFont);
        title.setFocusable(true);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        add(title, "span 4, gaptop 20, wrap");
    }


    //todo or java resize approach new Label(imageIcon)
    private ImageIcon getIcon(String image) {
        File img = new File(Config.RESOURCES + image);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "applet_plain.png");
        BufferedImage newIcon = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        //draw image with clip
        Graphics2D graphics2D = newIcon.createGraphics();

        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setClip(new Arc2D.Float(4, 4, 110, 110, 0, 360, Arc2D.OPEN));
        try {
            graphics2D.drawImage(ImageIO.read(img), 0, 0, 120, 120, null);
        } catch (IOException e) {
            e.printStackTrace();
            return new ImageIcon(newIcon);
        }
        //remove clip
        graphics2D.setClip(null);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setColor(Color.WHITE);
        graphics2D.setStroke(new BasicStroke(6));
        graphics2D.drawArc(3, 3, 113, 113, 0, 360);
        graphics2D.dispose();
        return new ImageIcon(newIcon);
    }
}
