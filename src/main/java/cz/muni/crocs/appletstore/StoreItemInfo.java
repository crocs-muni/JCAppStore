package cz.muni.crocs.appletstore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.card.CardManagerFactory;
import cz.muni.crocs.appletstore.card.KeysPresence;
import cz.muni.crocs.appletstore.card.action.InstallAction;
import cz.muni.crocs.appletstore.crypto.KeyBase;
import cz.muni.crocs.appletstore.crypto.LocalizedSignatureException;
import cz.muni.crocs.appletstore.ui.*;
import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.*;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
    private final Font titleFont = OptionsFactory.getOptions().getTitleFont(Font.BOLD, 20f);
    private final Font textFont = OptionsFactory.getOptions().getFont(Font.PLAIN, 16f);

    private boolean installed = false;
    private JComboBox<String> versionComboBox;
    private JComboBox<String> compilerVersionComboBox;
    private JLabel verifyIcon;

    public StoreItemInfo(JsonObject dataSet, Searchable store, OnEventCallBack<Void, Void, Void> callBack) {
        super(OptionsFactory.getOptions().getOption(Options.KEY_HINT).equals("true"));
        setOpaque(false);
        List<AppletInfo> appletInfos = CardManagerFactory.getManager().getInstalledApplets();
        if (appletInfos != null) {
            for (AppletInfo applet : appletInfos) {
                String name = applet.getName();
                if (name != null && name.equals(dataSet.get(Config.JSON_TAG_TITLE).getAsString())) {
                    installed = true;
                    break;
                }
            }
        }
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new MigLayout());

        buildHeader(dataSet, store, callBack);
        checkHostApp(dataSet);
        buildDescription(dataSet);
        buildVersionAndCustomInstall(dataSet, new JsonStoreParser(), callBack);
    }

     private void setVerifiedFromThread(String imgName, String hint) {
        SwingUtilities.invokeLater(() -> {
            verifyIcon.setIcon(new ImageIcon(Config.IMAGE_DIR + imgName));
            ((HintLabel)verifyIcon).setText("", hint);
            revalidate();
        });
    }

    void checkIntegrity(String filePath) {
        new Thread(() -> {
            try {
                Tuple<String, String> result = new KeyBase().verifySignature(filePath);
                setVerifiedFromThread(result.first, result.second);
            } catch (LocalizedSignatureException e) {
                setVerifiedFromThread("not_verified.png", textSrc.getString("H_verify_failed")
                        + (OptionsFactory.getOptions().getOption(Options.KEY_ERROR_MODE).equals("verbose") ?
                        e.getLocalizedMessage() : e.getLocalizedMessageWithoutCause()));
            }
        }).start();
    }

    private HintLabel getVerifiedIcon() {
        return new HintLabel(new ImageIcon(Config.IMAGE_DIR + "verify_loading.png"),
                textSrc.getString("H_keybase_loading"));
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
        add(back, "pos 0 0");

        JLabel icon = new JLabel(getIcon(dataSet.get(Config.JSON_TAG_ICON).getAsString()));
        icon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(icon, "span 2 2, gapleft 50");

        String appName = dataSet.get(Config.JSON_TAG_TITLE).getAsString();
        JLabel name = new JLabel(appName + "  ");
        name.setFont(titleFont);
        add(name, "align left, gaptop 40, width ::350, id title");

        verifyIcon = getVerifiedIcon();
        add(verifyIcon, "pos title.x2 title.y");

        buildMainInstallButton(dataSet, callback);

        JLabel author = new JLabel(textSrc.getString("author") + dataSet.get(Config.JSON_TAG_AUTHOR).getAsString());
        author.setFont(OptionsFactory.getOptions().getTitleFont(15f));
        add(author, "align left, gapbottom 40, width ::350, wrap");
    }

    private void buildMainInstallButton(JsonObject dataSet, OnEventCallBack<Void, Void, Void> callback) {
        JButton install = Components.getButton(textSrc.getString(installed ? "CAP_reinstall" : "CAP_install"),
                "margin: 1px 10px;", 20f, Color.WHITE, new Color(140, 196, 128), true);
        final String appletName = dataSet.get(Config.JSON_TAG_NAME).getAsString();
        final String latestV = dataSet.get(Config.JSON_TAG_LATEST).getAsString();
        final JsonArray sdks = dataSet.get(Config.JSON_TAG_BUILD).getAsJsonObject()
                .get(latestV).getAsJsonArray();
        String latestFilename = getInstallFileName(appletName, latestV, sdks.get(sdks.size() - 1).getAsString());

        checkIntegrity(latestFilename);

        install.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        fireInstall(appletName, getInfoPack(dataSet, latestV,
                                sdks, sdks.size() - 1), callback, installed, e);
                    }
                });
        add(install, "align right, span 1 2, wrap");
    }

    private void checkHostApp(JsonObject dataSet) {
        if (!dataSet.get(Config.JSON_TAG_HOST).getAsString().trim().toLowerCase().equals("true")) {
            add(Components.getNotice(
                    textSrc.getString("W_no_host_app"),
                    OptionsFactory.getOptions().getFont(),
                    new Color(255, 219, 148),
                    new ImageIcon(Config.IMAGE_DIR + "info.png"),
                    "margin: 10px; width:500px")
            , "gap 20, span 4, gaptop 40, growx, wrap");
        }
    }

    private void buildDescription(JsonObject dataSet) {
        add(Components.getTextField(
                dataSet.get(Config.JSON_TAG_DESC).getAsString(),
                OptionsFactory.getOptions().getFont(),
                "margin: 10px; width:600px",
                new Color(255, 255, 255, 80)
        ), "span 4, gap 20, gaptop 40, wrap");

        addSubTitle("website", "H_website");
        final String urlAddress = dataSet.get(Config.JSON_TAG_URL).getAsString();
        JLabel url = new HtmlLabel("<div style=\"margin: 5px;\"><b>" + urlAddress + "</b></div>");
        url.setOpaque(true);
        Color bg = new Color(255, 255, 255, 80);
        url.setBackground(bg);
        url.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Font textFont = OptionsFactory.getOptions().getFont(14f);
        url.setFont(textFont);
        url.addMouseListener(new URLAdapter(urlAddress));
        add(url, "span 4, gaptop 10, gapleft 20, wrap");

        addSubTitle("use", "H_use");
        add(Components.getTextField(
                dataSet.get(Config.JSON_TAG_USAGE).getAsString(),
                OptionsFactory.getOptions().getFont(),
                "margin: 10px; width:600px",
                new Color(255, 255, 255, 80)
        ), "span 4, gap 20, gaptop 20, wrap");
    }

    private void buildVersionAndCustomInstall(JsonObject dataSet, JsonParser parser, OnEventCallBack<Void, Void, Void> call) {
        addSubTitle("custom_install", "H_custom_install");

        addText("custom_version", "H_custom_version", "gapleft 20, gaptop 20");
        addText("custom_sdk", "H_custom_sdk", "gapleft 20, gaptop 20, wrap");

        String[] versions = parser.jsonArrayToDataArray(dataSet.getAsJsonArray(Config.JSON_TAG_VERSION));
        versionComboBox = getBoxSelection(versions);

        versionComboBox.addActionListener(e -> {
            String[] compilerVersions = parser.jsonArrayToDataArray(
                    dataSet.getAsJsonObject(
                            Config.JSON_TAG_BUILD).getAsJsonArray((String) versionComboBox.getSelectedItem()
                    )
            );
            compilerVersionComboBox.setModel(new JComboBox<>(compilerVersions).getModel());
        });
        add(versionComboBox, "gapleft 50, gapleft 20");

        JsonObject builds = dataSet.getAsJsonObject(Config.JSON_TAG_BUILD);
        String[] compilerVersions = parser.jsonArrayToDataArray(builds.getAsJsonArray(versions[0]));
        compilerVersionComboBox = getBoxSelection(compilerVersions);
        add(compilerVersionComboBox, "gapleft 20");

        JButton customInst = Components.getButton(
                textSrc.getString(installed ? "CAP_reinstall" : "CAP_install"),
                "margin: 1px 10px;",
                18f,
                Color.WHITE,
                new Color(155, 151, 152),
                true);
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
                                getInfoPack(dataSet, version, sdks, compilerIdx), call, installed, e);
                    }
                });
        add(customInst, "gapleft 10");
    }

    private void addSubTitle(String titleKey, String hintKey) {
        HintLabel title = Components.getHintLabel(
                textSrc.getString(titleKey),
                textSrc.getString(hintKey),
                titleFont,
                BorderFactory.createEmptyBorder(20, 20, 0, 20)
        );
        add(title, "span 4, gaptop 20, wrap");
    }

    private void addText(String titleKey, String hintKey, String constraints) {
        add(Components.getHintLabel(
                textSrc.getString(titleKey),
                textSrc.getString(hintKey),
                textFont,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ), constraints);
    }

    private JComboBox<String> getBoxSelection(String[] values) {
        JComboBox<String> box = new StyledComboBox<>(values);
        box.setMaximumRowCount(5);
        return box;
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
                null,
                hasKey(dataSet.get(Config.JSON_TAG_KEYS).getAsString()));
    }

    private static String getInstallFileName(String appletName, String version, String sdkVersion) {
        return Config.APP_STORE_CAPS_DIR + Config.SEP +
                appletName + Config.SEP + appletName + "_v" + version + "_sdk" + sdkVersion + ".cap";
    }

    private static void fireInstall(String name, AppletInfo info, OnEventCallBack<Void, Void, Void> call,
                                    boolean installed, MouseEvent e) {
        File file = new File(getInstallFileName(name, info.getVersion(), info.getSdk()));
        logger.info("Prepare to install " + file.getAbsolutePath());

        if (!file.exists()) {
            logger.warn("Applet file not found.");
            InformerFactory.getInformer().showWarningToClose(textSrc.getString("E_install_not_found"),
                    Warning.Importance.INFO);
            return;
        }

        new InstallAction(info.getName() + info.getVersion() + ", sdk " + info.getSdk(), info, file, installed, call).mouseClicked(e);
    }
}
