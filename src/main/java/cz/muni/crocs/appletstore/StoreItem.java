package cz.muni.crocs.appletstore;

import com.google.gson.JsonObject;
import cz.muni.crocs.appletstore.ui.HtmlText;
import cz.muni.crocs.appletstore.ui.HtmlTitle;
import cz.muni.crocs.appletstore.util.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Item displayed in store
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItem extends JPanel implements Item {

    private String searchQuery;

    public StoreItem(String title, String author, String version, String image) {
        searchQuery = title + author;
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel icon = new JLabel(
                "<html><img src=\"file:///" + getImgAddress(image) + "\" width=\"130\" height=\"130\"/> </html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                super.paintComponent(g);
            }
        };
        add(icon, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        title = adjustLength(title, 25);
        container.add(getLabel(title, "width:100px; height: 60px; margin: 5px", 16f, true), gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        author = adjustLength(author, 15);
        JLabel info = getLabel(author, "width:85px; max-lines:1; margin: 5px", 13f, true);
        info.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(info, gbc);

        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        version = adjustLength(version, 5);
        container.add(getLabel(version, "width:10px; text-overflow: ellipsis; margin: 5px", 15f, false), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(container, gbc);
    }

    public StoreItem(JsonObject dataSet) {
        this(dataSet.get(JsonParser.TAG_TITLE).getAsString(),
                dataSet.get(JsonParser.TAG_AUTHOR).getAsString(),
                dataSet.get(JsonParser.TAG_LATEST).getAsString(),
                dataSet.get(JsonParser.TAG_ICON).getAsString()
                );
    }

    private JLabel getLabel(String text, String css, Float fontSize, boolean title) {
        return (title) ?
                new HtmlTitle("<div style=\"" + css + "\">" + text + "</div>", fontSize)
                :
                new HtmlText("<div style=\"" + css + "\">" + text + "</div>", fontSize);
    }

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
