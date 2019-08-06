package cz.muni.crocs.appletstore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.*;
import cz.muni.crocs.appletstore.ui.CustomButtonUI;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.ui.Warning;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Item detail in store
 * allows custom version installation,
 * provides item info & todo signatures
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItemInfo extends HintPanel {

    private static final Logger logger = LogManager.getLogger(StoreItemInfo.class);
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private final Font titleFont = OptionsFactory.getOptions().getDefaultFont().deriveFont(Font.BOLD, 20f);

    private JComboBox<String> versionComboBox;
    private JComboBox<String> compilerVersionComboBox;
    public StoreItemInfo(JsonObject dataSet, Searchable store, OnEventCallBack<Void, Void, Void> callBack) {
        super(OptionsFactory.getOptions().getOption(Options.KEY_HINT).equals("true"));
        setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new MigLayout());

        buildHeader(dataSet, store, callBack);
        buildDescription(dataSet);
        buildVersionAndCustomInstall(dataSet, new JsonStoreParser(), callBack);
    }

    private void buildHeader(JsonObject dataSet, Searchable store, OnEventCallBack<Void, Void, Void> callback) {
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

        String appName = dataSet.get(Config.JSON_TAG_TITLE).getAsString();
        JLabel name = new JLabel(appName);
        name.setFont(titleFont);
        add(name, "align left, gaptop 40, width ::350");

        JButton install = getButton("CAP_install", "margin: 1px 10px;",
                20f, Color.WHITE, new Color(26, 196, 0));
        install.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        String latestV = dataSet.get(Config.JSON_TAG_LATEST).getAsString();
                        JsonArray sdks = dataSet.get(Config.JSON_TAG_BUILD).getAsJsonObject()
                                .get(latestV).getAsJsonArray();
                        fireInstall(dataSet.get(Config.JSON_TAG_NAME).getAsString(),
                                getInfoPack(dataSet, latestV, sdks, sdks.size() - 1), callback, e);
                    }
                });
        add(install, "align right, span 1 2, wrap");

        JLabel author = new JLabel(textSrc.getString("author") + dataSet.get(Config.JSON_TAG_AUTHOR).getAsString());
        author.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(15f));
        add(author, "align left, gapbottom 40, width ::350, wrap");
    }

    private void buildDescription(JsonObject dataSet) {
        JTextPane mainInfo = new JTextPane();
        mainInfo.setContentType("text/html");
        mainInfo.setText("<html><div style=\"margin: 10px; width:600px\">" + dataSet.get(Config.JSON_TAG_DESC).getAsString() + "</div></html>");
        mainInfo.setBackground(new Color(255, 255, 255, 80));
        mainInfo.setOpaque(true);
        mainInfo.setEditable(false);
        mainInfo.setBorder(null);
        ((DefaultCaret) mainInfo.getCaret()).setUpdatePolicy(0);
        add(mainInfo, "span 4, gap 20, wrap");

        //WEBSITE
        addSubTitle("website", "H_website");

        final String urlAddress = dataSet.get(Config.JSON_TAG_URL).getAsString();
        JLabel url = new HtmlLabel("<div style=\"margin: 5px;\"><b>" + urlAddress + "</b></div>");
        url.setOpaque(true);
        Color bg = new Color(255, 255, 255, 80);
        url.setBackground(bg);
        url.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Font textFont = OptionsFactory.getOptions().getDefaultFont().deriveFont(14f);
        url.setFont(textFont);
        url.addMouseListener(new URLAdapter(urlAddress));
        add(url, "span 4, gaptop 10, gapleft 20, wrap");
    }

    private void buildVersionAndCustomInstall(JsonObject dataSet, JsonParser parser, OnEventCallBack<Void, Void, Void> call) {
        //INSTALL
        addSubTitle("use", "H_use");

        JTextPane installInfo = new JTextPane();
        installInfo.setContentType("text/html");
        installInfo.setText("<html><div style=\"margin: 10px; width:600px\">" + dataSet.get(Config.JSON_TAG_USAGE).getAsString() + "</div></html>");
        installInfo.setBackground(new Color(255, 255, 255, 80));
        installInfo.setOpaque(true);
        installInfo.setEditable(false);
        installInfo.setBorder(null);
        ((DefaultCaret) installInfo.getCaret()).setUpdatePolicy(0);
        add(installInfo, "span 4, gap 20, gaptop 20, wrap");

        //VERSION
        addSubTitle("custom_install", "H_custom_install");

        String[] versions = parser.jsonArrayToDataArray(dataSet.getAsJsonArray(Config.JSON_TAG_VERSION));
        versionComboBox = new JComboBox<>(versions);
        versionComboBox.setMaximumRowCount(7);
        //todo disables the build combobox exchange...?
        //versionComboBox.setSelectedItem(dataSet.get(Config.JSON_TAG_LATEST).getAsString());
        versionComboBox.addActionListener(e -> {
            String[] compilerVersions = parser.jsonArrayToDataArray(
                    dataSet.getAsJsonObject(
                            Config.JSON_TAG_BUILD).getAsJsonArray((String) versionComboBox.getSelectedItem()
                    )
            );
            compilerVersionComboBox.setModel(new JComboBox<>(compilerVersions).getModel());
        });
        add(versionComboBox, "align right");

        JsonObject builds = dataSet.getAsJsonObject(Config.JSON_TAG_BUILD);

        String[] compilerVersions = parser.jsonArrayToDataArray(builds.getAsJsonArray(versions[0]));
        compilerVersionComboBox = new JComboBox<>(compilerVersions);
        compilerVersionComboBox.setMaximumRowCount(7);
        add(compilerVersionComboBox, "align right");

        JButton customInst = getButton("CAP_install", "margin: 1px 10px;", 18f, Color.WHITE, new Color(170, 166, 167));
        customInst.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int versionIdx = getComboBoxSelected(versionComboBox, "E_invalid_version");
                        int compilerIdx = getComboBoxSelected(compilerVersionComboBox, "E_invalid_compiler");
                        if (versionIdx < 0 || compilerIdx < 0) {
                            InformerFactory.getInformer().showInfo(textSrc.getString("E_invalid_custom_install"));
                            return;
                        }

                        String version = versions[versionIdx];
                        JsonArray sdks = dataSet.get(Config.JSON_TAG_BUILD).getAsJsonObject()
                                .get(version).getAsJsonArray();

                        fireInstall(dataSet.get(Config.JSON_TAG_NAME).getAsString(),
                                getInfoPack(dataSet, version, sdks, compilerIdx), call, e);
                    }
                });
        add(customInst, "span 2, align right, wrap");
    }

    private JButton getButton(String textKey, String css, Float fontSize, Color foreground, Color background) {
        JButton button = new JButton("<html><div style=\"" + css + "\">"
                + textSrc.getString(textKey) + "</div></html>");
        button.setUI(new CustomButtonUI());
        button.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(Font.BOLD, fontSize));
        button.setForeground(foreground);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(background);
        return button;
    }

    private void addSubTitle(String titleKey, String hintKey) {
        HintLabel title = new HintLabel(textSrc.getString(titleKey), textSrc.getString(hintKey));
        title.setFont(titleFont);
        title.setFocusable(true);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        add(title, "span 4, gaptop 20, wrap");
    }

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

    private static int getComboBoxSelected(JComboBox box, String errorKey) {
        int selected = box.getSelectedIndex();
        if (selected < 0) {
            InformerFactory.getInformer().showWarningToClose(textSrc.getString(errorKey), Warning.Importance.INFO);
        }
        return selected;
    }

    private KeysPresence hasKey(String data) {
        switch (data.trim().toLowerCase()) {
            case "true":
                return KeysPresence.PRESENT;
            case "false":
                return KeysPresence.NO_KEYS;
            default:
                return KeysPresence.UNKNOWN;
        }
    }

    private AppletInfo getInfoPack(JsonObject dataSet, String version, JsonArray sdks, int sdkIdx) {
        return new AppletInfo(dataSet.get(Config.JSON_TAG_TITLE).getAsString(),
                dataSet.get(Config.JSON_TAG_ICON).getAsString(),
                version,
                dataSet.get(Config.JSON_TAG_AUTHOR).getAsString(),
                sdks.get(sdkIdx).getAsString(),
                hasKey(dataSet.get(Config.JSON_TAG_KEYS).getAsString()));
    }

    private static void fireInstall(String name, AppletInfo info, OnEventCallBack<Void, Void, Void> call, MouseEvent e) {

        File file = new File(Config.APP_STORE_CAPS_DIR + Config.SEP +
                name + Config.SEP + name + "_v" + info.getVersion() + "_sdk" + info.getSdk() + ".cap");
        logger.info("Install applet " + file.getAbsolutePath());

        if (!file.exists()) {
            logger.warn("Applet file not found.");
            InformerFactory.getInformer().showWarningToClose(textSrc.getString("E_install_not_found"),
                    Warning.Importance.INFO);
            return;
        }

        new InstallAction( info.getName() + info.getVersion() + ", sdk " + info.getSdk(), info, file, call).mouseClicked(e);
    }
}
