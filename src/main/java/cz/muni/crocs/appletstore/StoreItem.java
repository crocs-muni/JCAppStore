package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.ui.CustomFont;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreItem extends JPanel {

    private static final Color bg = new Color(255, 255, 255);
    private static final int iconDimen = 130;
    private static final int cornerRadius = 30;

    private String searchQuery;

    public StoreItem(String title, String imgName, String author, String version) throws IOException {
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
        JLabel info = new JLabel("<html><div style=\"width:85px; max-lines:1; margin: 5px\">" + author + "</div><html>");
        info.setFont(CustomFont.plain.deriveFont(13f));
        info.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(info, gbc);

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

    private String adjustLength(String value, int length) {
        if (value.length() <= length) return value;
        return value.substring(0, length - 3) + "...";
    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        Graphics2D graphics = (Graphics2D) g;
//        //graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        //Dimension rounded = new Dimension(cornerRadius, cornerRadius);
//        graphics.setColor(bg);
//        //graphics.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, rounded.width, rounded.height);
//        graphics.setStroke(new BasicStroke(2));
//        graphics.drawRect(1, 1, getWidth() - 2, getHeight() - 1);
//    }

    //todo decide: html approach new Label("html")
    private String getImgAddress(String imgName) {
        File img = new File(Config.RESOURCES + imgName);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "no_img.png");
        return img.getAbsolutePath();
    }
    //todo or java resize approach new Label(imageIcon)
    private ImageIcon getIcon(String image) throws IOException {
        File img = new File(Config.RESOURCES + image);
        img = (img.exists()) ? img : new File(Config.IMAGE_DIR + "no_img.png");

        BufferedImage newIcon = new BufferedImage(iconDimen, iconDimen, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = newIcon.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(ImageIO.read(img), 0, 0, iconDimen, iconDimen, null);
        graphics2D.dispose();
        return new ImageIcon(newIcon);
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
