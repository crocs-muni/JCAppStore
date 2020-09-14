package cz.muni.crocs.appletstore;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.action.InstallAction;
import cz.muni.crocs.appletstore.action.InstallBundle;
import cz.muni.crocs.appletstore.ui.*;
import cz.muni.crocs.appletstore.ui.TextField;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.util.*;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pro.javacard.gp.GPRegistryEntry;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Item detail in store
 * allows custom version installation,
 * provides item info
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItemInfo extends HintPanel {

    private static final Logger logger = LogManager.getLogger(StoreItemInfo.class);
    private static final ResourceBundle textSrc = ResourceBundle.getBundle("Lang",
            OptionsFactory.getOptions().getLanguageLocale());

    private boolean installed = false;
    private boolean allowInstall; //JCMemory will be blocked
    private JComboBox<String> versionComboBox;
    private JComboBox<String> compilerVersionComboBox;
    private static final ImageIcon website = new ImageIcon(Config.IMAGE_DIR + "web.png");

    /**
     * Create a detailed store info
     * @param dataSet json object from info_[lang].json file
     * @param store parent store panel
     * @param callBack callback to forward to install action
     */
    public StoreItemInfo(JsonObject dataSet, Searchable store, OnEventCallBack<Void, Void> callBack) {
        super(OptionsFactory.getOptions().getOption(Options.KEY_HINT).equals("true"));

        allowInstall = !dataSet.get(JsonParser.TAG_TITLE).getAsString().equals("JCMemory");

        //there was a problem with focus when using search feature, request focus
        requestFocusInWindow();

        setOpaque(false);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new MigLayout());

        buildHeader(dataSet, store, callBack);
        buildDescription(dataSet);
        buildWebsites(dataSet);
        checkDefaultSelected(dataSet);
        buildVersionAndCustomInstall(dataSet, new JsonStoreParser(), callBack);
    }

    private void buildHeader(JsonObject dataSet, Searchable store, OnEventCallBack<Void, Void> callback) {
        JLabel icon = new JLabel(getIcon(dataSet.get(JsonParser.TAG_ICON).getAsString()));
        icon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(icon, "span 2 2, gapleft 50");

        String appName = dataSet.get(JsonParser.TAG_TITLE).getAsString();
        JLabel name = new Title(appName + "  ", 20f);
        name.setForeground(Color.WHITE);
        add(name, "align left, gaptop 40, width ::350, id title");

        if (allowInstall) {
            buildMainInstallButton(dataSet, callback);
        } else {
            add(getButton("noinstall", new Color(162, 165, 162)));
        }

        JLabel author = new Title(textSrc.getString("author") + dataSet.get(JsonParser.TAG_AUTHOR).getAsString(), 15f);
        author.setForeground(Color.WHITE);
        add(author, "align left, gapbottom 40, width ::350, wrap");
    }

    private void buildMainInstallButton(JsonObject dataSet, OnEventCallBack<Void, Void> callback) {
        //get latest version info
        final String appletName = dataSet.get(JsonParser.TAG_NAME).getAsString();
        final String latestV = dataSet.get(JsonParser.TAG_LATEST).getAsString();
        final JsonArray sdks = dataSet.get(JsonParser.TAG_BUILD).getAsJsonObject()
                .get(latestV).getAsJsonArray();

        //check whether installed
        CardInstance card = CardManagerFactory.getManager().getCard();
        Set<AppletInfo> appletInfos = card == null ? null : card.getCardMetadata().getApplets();
        if (appletInfos != null) {
            for (AppletInfo applet : appletInfos) {
                String name = applet.getName();
                //installed from the store
                if (name != null && name.equals(dataSet.get(JsonParser.TAG_TITLE).getAsString())) {
                    installed = true;
                    break;
                }
            }
        }

        JButton install = getButton(installed ? "CAP_reinstall" : "CAP_install", new Color(140, 196, 128));

        install.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        fireInstall(dataSet, getInfoPack(dataSet, latestV, sdks, sdks.size() - 1), callback,
                                installed && OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE), e);
                    }
                });
        add(install, "align right, span 1 2, wrap");
    }

    private void checkDefaultSelected(JsonObject dataSet) {
        String selected = dataSet.get(JsonParser.TAG_DEFAULT_SELECTED).getAsString();
        if (selected != null && !selected.isEmpty()) {
            add(getNotice(textSrc.getString("W_default_app"), 14f, new Color(255, 220, 181),
                    new ImageIcon(Config.IMAGE_DIR + "info.png"), "margin: 10px; width:500px")
                    , "gap 20, span 4, gaptop 40, growx, wrap");
        }
    }

    private void buildDescription(JsonObject dataSet) {
        add(TextField.getTextField(
                dataSet.get(JsonParser.TAG_DESC).getAsString(),
                "margin: 10px; width:600px",
                new Color(255, 255, 255, 80)
        ), "span 4, gap 20, gaptop 40, wrap");

        addSubTitle("use", "H_use");
        add(TextField.getTextField(
                dataSet.get(JsonParser.TAG_USAGE).getAsString(),
                "margin: 10px; width:600px",
                new Color(255, 255, 255, 80)
        ), "span 4, gap 20, gaptop 20, wrap");
    }

    private void buildWebsites(JsonObject dataSet) {
        JsonObject websites = dataSet.get(JsonParser.TAG_URL).getAsJsonObject();
        if (websites == null || websites.size() == 0) return;

        addSubTitle("website", "H_website");

        Set<Map.Entry<String, JsonElement>> entrySet = websites.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String urlName = entry.getKey();
            String urlAddress = websites.get(urlName).getAsString();
            JLabel name = new HtmlText("<div style=\"margin: 5px;\"><b>" + urlName +
                    "</b></div>", 14f);
            name.setOpaque(false);
            name.setForeground(Color.white);

            add(name, "span 2, gaptop 10, gapleft 20");

            JLabel url = new HtmlText("<div style=\"margin: 5px;\">"
                    + urlAddress + "</div>", website, 14f, SwingConstants.RIGHT);
            url.setOpaque(false);
            url.setCursor(new Cursor(Cursor.HAND_CURSOR));
            url.addMouseListener(new URLAdapter(urlAddress));
            url.setForeground(Color.white);
            add(url, "span 2, gaptop 10, gapleft 5, wmax 500, wrap");
        }
    }

    private void buildVersionAndCustomInstall(JsonObject dataSet, JsonParser parser, OnEventCallBack<Void, Void> call) {
        if (!allowInstall) return;

        addSubTitle("custom_install", "H_custom_install");

        addText("custom_version", "H_custom_version", "gapleft 20, gaptop 20");
        addText("custom_sdk", "H_custom_sdk", "gapleft 20, gaptop 20, wrap");

        String[] versions = JsonParser.jsonArrayToStringArray(dataSet.getAsJsonArray(JsonParser.TAG_VERSION));
        versionComboBox = getBoxSelection(versions);

        versionComboBox.addActionListener(e -> {
            String[] compilerVersions = JsonParser.jsonArrayToStringArray(
                    dataSet.getAsJsonObject(
                            JsonParser.TAG_BUILD).getAsJsonArray((String) versionComboBox.getSelectedItem()
                    )
            );
            compilerVersionComboBox.setModel(new JComboBox<>(compilerVersions).getModel());
            compilerVersionComboBox.setSelectedIndex(0);
        });
        add(versionComboBox, "gapleft 50, gapleft 20");

        JsonObject builds = dataSet.getAsJsonObject(JsonParser.TAG_BUILD);
        String[] compilerVersions = JsonParser.jsonArrayToStringArray(builds.getAsJsonArray(versions[0]));
        compilerVersionComboBox = getBoxSelection(compilerVersions);
        add(compilerVersionComboBox, "gapleft 20");

        JButton customInst = getButton(installed ? "CAP_reinstall" : "CAP_install", new Color(155, 151, 152));
        customInst.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int versionIdx = getComboBoxSelected(versionComboBox, "E_invalid_version");
                        int compilerIdx = getComboBoxSelected(compilerVersionComboBox, "E_invalid_compiler");
                        if (versionIdx < 0 || compilerIdx < 0) {
                            InformerFactory.getInformer().showMessage(textSrc.getString("E_invalid_custom_install"));
                            return;
                        }

                        String version = versions[versionIdx];
                        JsonArray sdks = dataSet.get(JsonParser.TAG_BUILD).getAsJsonObject()
                                .get(version).getAsJsonArray();

                        fireInstall(dataSet, getInfoPack(dataSet, version, sdks, compilerIdx), call,
                                installed && OptionsFactory.getOptions().is(Options.KEY_SIMPLE_USE), e);
                    }
                });
        add(customInst, "gapleft 10");
    }

    private void addSubTitle(String titleKey, String hintKey) {
        HintLabel title = new HintTitle(textSrc.getString(titleKey), textSrc.getString(hintKey), 20f);
        title.setFocusable(true);
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        add(title, "span 4, gaptop 20, wrap");
    }

    private void addText(String titleKey, String hintKey, String constraints) {
        HintLabel label = new HintText(textSrc.getString(titleKey), textSrc.getString(hintKey), 16f);
        label.setFocusable(true);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(label, constraints);
    }

    private JComboBox<String> getBoxSelection(String[] values) {
        JComboBox<String> box = new StyledComboBox<>(values);
        box.setMaximumRowCount(5);
        return box;
    }

    private ImageIcon getIcon(String image) {
        File img;
        if (image == null || image.isEmpty()) {
            img = new File(Config.IMAGE_DIR + "applet_plain.png");
        } else {
            img = new File(Config.RESOURCES + image);
            img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "applet_plain.png");
        }
        BufferedImage newIcon = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
        //draw image with clip
        Graphics2D graphics2D = newIcon.createGraphics();

        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setClip(new Arc2D.Float(4, 4, 110, 110, 0, 360, Arc2D.OPEN));
        try {
            graphics2D.drawImage(ImageIO.read(img), 0, 0, 120, 120, null);
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Failed to attach label to the store item icon: " + img, e);
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

    private static int getComboBoxSelected(JComboBox<?> box, String errorKey) {
        int selected = box.getSelectedIndex();
        if (selected < 0) {
            InformerFactory.getInformer().showInfoToClose(textSrc.getString(errorKey), Notice.Importance.INFO);
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
        return new AppletInfo(dataSet.get(JsonParser.TAG_TITLE).getAsString(),
                dataSet.get(JsonParser.TAG_ICON).getAsString(),
                version,
                dataSet.get(JsonParser.TAG_AUTHOR).getAsString(),
                sdks.get(sdkIdx).getAsString(),
                null,
                hasKey(dataSet.get(JsonParser.TAG_KEYS).getAsString()),
                GPRegistryEntry.Kind.Application);
    }

    private static String getInstallFileName(String appletName, String version, String sdkVersion) {
        return Config.APP_STORE_CAPS_DIR + Config.S +
                appletName + Config.S + appletName + "_v" + version + "_sdk" + sdkVersion + ".cap";
    }

    private static void fireInstall(JsonObject dataPack, AppletInfo info, OnEventCallBack<Void, Void> call,
                                    boolean installed, MouseEvent e) {
        String name = dataPack.get(JsonParser.TAG_NAME).getAsString();
        String signer = dataPack.get(JsonParser.TAG_PGP_SIGNER).getAsString();
        String fingerprint = dataPack.get(JsonParser.TAG_PGP_FINGERPRINT).getAsString();
        JsonElement appNames = dataPack.get(JsonParser.TAG_APPLET_INSTANCE_NAMES);
        String defaultSelected = dataPack.get(JsonParser.TAG_DEFAULT_SELECTED).getAsString();

        File file = new File(getInstallFileName(name, info.getVersion(), info.getSdk()));
        logger.info("Prepare to install " + file.getAbsolutePath());

        if (!file.exists()) {
            logger.warn("Applet file not found.");
            InformerFactory.getInformer().showInfoToClose(textSrc.getString("E_install_not_found"),
                    Notice.Importance.INFO);
            return;
        }

        ArrayList<String> appletNamesData = null;
        if (appNames != null && appNames.isJsonArray()) {
            JsonArray array = appNames.getAsJsonArray();
            appletNamesData = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                appletNamesData.add(array.get(i).getAsString());
            }
        }

        new InstallAction(new InstallBundle(info.getName() + info.getVersion() + ", sdk " + info.getSdk(),
                info, file, signer, fingerprint, appletNamesData, Config.APP_STORE_CAPS_DIR + Config.S +
                name + Config.S, dataPack), installed, defaultSelected, call).mouseClicked(e);
    }

    private JButton getButton(String translationKey, Color background) {
        JButton button = new JButton("<html><div style=\"margin: 1px 10px;\">" +
                textSrc.getString(translationKey) + "</div></html>");
        button.setUI(new CustomButtonUI());
        button.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 20f));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
//    private List<AppletInfo> findCollisions(CardInstance card, InstallOpts options) {
//        ArrayList<AppletInfo> result = new ArrayList<>();
//        String[] toInstall = options.getCustomAIDs();
//        AID pkgId = code.getPackageAID();
//        if (toInstall == null) {
//            toInstall = options.getOriginalAIDs();
//        }
//
//        for (AppletInfo info : card.getInstalledApplets()) {
//            if (info.getKind() == GPRegistryEntry.Kind.Application) {
//                AID aid = info.getAid();
//                for (int i = 0; i < options.getOriginalAIDs().length; i++) {
//                    String tmpAid = toInstall.length <= i ? options.getOriginalAIDs()[i] : toInstall[i];
//                    if (tmpAid == null || tmpAid.isEmpty()) tmpAid = options.getOriginalAIDs()[i];
//                    if (aid.equals(AID.fromString(tmpAid))) {
//                        result.add(info);
//                    }
//                }
//            } else if (info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile && info.getAid().equals(pkgId)) {
//                result.add(info);
//            }
//        }
//        return result;
//    }


    private static JPanel getNotice(String text, float fontSize, Color background, ImageIcon icon, String css) {
        final int depth = 5;
        // idea from https://stackoverflow.com/questions/13368103/jpanel-drop-shadow
        JPanel container = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                int color = 0;
                int maxOp = 80;
                for (int i = 0; i < depth; i++) {
                    g.setColor(new Color(color, color, color, ((maxOp / depth) * i)));
                    g.drawRect(i, i, this.getWidth() - ((i * 2) + 1), this.getHeight() - ((i * 2) + 1));
                }
                g.setColor(background);
                g.fillRect(depth, depth, getWidth() - depth * 2, getHeight() - depth * 2);
            }
        };
        container.setBorder(BorderFactory.createCompoundBorder(
                container.getBorder(), BorderFactory.createEmptyBorder(depth, depth, depth, depth))
        );

        JLabel img = new JLabel(icon);
        JLabel desc = new HtmlText("<div style=\"" + css + "\">" + text + "</div>", fontSize);
        img.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        desc.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        container.add(img);
        container.add(desc);
        return container;
    }
}
