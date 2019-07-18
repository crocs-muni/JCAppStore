package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.iface.Item;
import cz.muni.crocs.appletstore.util.OptionsFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItem extends JPanel implements Item {

    private String searchQuery;

    public StoreItem(String title, String author, String version, String image) {
        searchQuery = title + author;
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel icon = new JLabel(
                "<html><img src=\"file:///" + getImgAddress(image) + "\" width=\"130\" height=\"130\"/> </html>");
        add(icon, gbc);

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBackground(Color.WHITE);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        title = adjustLength(title, 25);
        JLabel name = new JLabel("<html>" +
                "<div style=\"width:100px; height: 60px; margin: 5px\">" + title + "</div><html>");
        name.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(16f));
        container.add(name, gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        author = adjustLength(author, 15);
        JLabel info = new JLabel("<html><div style=\"width:85px; max-lines:1; margin: 5px\">" + author + "</div><html>");
        info.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(13f));
        info.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(info, gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        version = adjustLength(version, 5);
        JLabel appVersion = new JLabel("<html><div style=\"width:10px; text-overflow: ellipsis; margin: 5px\">" + version + "</div><html>");
        appVersion.setFont(OptionsFactory.getOptions().getDefaultFont().deriveFont(15f));
        container.add(appVersion, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(container, gbc);
    }

    public StoreItem(JsonObject dataSet) {
        this(dataSet.get(Config.JSON_TAG_TITLE).getAsString(),
                dataSet.get(Config.JSON_TAG_AUTHOR).getAsString(),
                dataSet.get(Config.JSON_TAG_LATEST).getAsString(),
                dataSet.get(Config.JSON_TAG_ICON).getAsString()
                );

    }

    //todo decide: html approach new Label("html")
    private String getImgAddress(String imgName) {
        File img = new File(Config.RESOURCES + imgName);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "applet_plain.png");
        return img.getAbsolutePath();
    }

    @Override
    public String getSearchQuery() {
        return searchQuery;
    }
}
