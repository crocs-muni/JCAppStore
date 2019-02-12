package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.command.GetDetails;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.util.AppletInfo;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalItem extends JPanel {

    private String searchQuery;
    public final AppletInfo info;

    public LocalItem(String title, String imgName, String author, String version, AppletInfo info) throws IOException {
        this.info = info;
        searchQuery = title + author + ((info == null) ? "" : Arrays.toString(info.getAid().getBytes()));
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel icon = new JLabel(
                "<html><img src=\"file:///" + getImgAddress(imgName) +"\" width=\"130\" height=\"130\"/> </html>");
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
        name.setFont(CustomFont.plain.deriveFont(16f));
        container.add(name, gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        author = adjustLength(author, 15);
        JLabel infoPanel = new JLabel("<html><div style=\"width:85px; max-lines:1; margin: 5px\">" + author + "</div><html>");
        infoPanel.setFont(CustomFont.plain.deriveFont(13f));
        infoPanel.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(infoPanel, gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        version = adjustLength(version, 5);
        JLabel appVersion = new JLabel("<html><div style=\"width:10px; text-overflow: ellipsis; margin: 5px\">" + version + "</div><html>");
        appVersion.setFont(CustomFont.plain.deriveFont(15f));
        container.add(appVersion, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(container, gbc);
    }

    public LocalItem(AppletInfo info) throws IOException {
        this(
                (info.getName() == null) ? Arrays.toString(info.getAid().getBytes()) : info.getName(),
                (info.getImage() == null) ? "no_img.png" : info.getImage(),
                (info.getAuthor() == null) ? Config.translation.get(125) : info.getAuthor(),
                (info.getVersion() == null) ? Config.translation.get(125) : info.getVersion(),
                info
        );
    }

    private String adjustLength(String value, int length) {
        if (value.length() <= length) return value;
        return value.substring(0, length - 3) + "...";
    }

    //todo decide: html approach new Label("html")
    private String getImgAddress(String imgName) {
        File img = new File(Config.RESOURCES + imgName);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "no_img.png");
        return img.getAbsolutePath();
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (info != null && info.isSelected()) {
            Graphics2D g2d = (Graphics2D) g;
            Composite old = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(old);
        }
        super.paintComponent(g);
    }
}
