package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.*;
import cz.muni.crocs.appletstore.ui.HtmlText;
import pro.javacard.gp.GPRegistryEntry.Kind;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Item that is found on card and can be either:
 * security domain
 * package
 * applet instance
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalItem extends JPanel implements Item {

    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());
    private static BufferedImage issuer = getIssuerImg();
    private static final int LABELDIMEN = 40;

    private static BufferedImage newItem;
    private static BufferedImage superSelected;

    private String searchQuery;
    private JPanel container;
    private String name; //either name or AID if name missing
    public final AppletInfo info;
    private Color selected = new Color(207, 244, 210);
    private CardManager manager;

    public LocalItem(String title, String imgName, String author, String version, AppletInfo info) {
        this.info = info;
        this.name = title;
        this.manager = CardManagerFactory.getManager();
        setAlignmentX(LEFT_ALIGNMENT);

        try {
            newItem = ImageIO.read(new File(Config.IMAGE_DIR + "newlabel.png"));
            superSelected = ImageIO.read(new File(Config.IMAGE_DIR + "main.png"));
        } catch (IOException e) {
            newItem = null;
            superSelected = null;
        }

        searchQuery = title + author + ((info == null) ? "" : Arrays.toString(info.getAid().getBytes()));
        setLayout(new GridBagLayout());
        setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel icon = new HtmlText("<img src=\"file:///" + getImgAddress(imgName) + "\" width=\"130\" height=\"130\"/>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                super.paintComponent(g);
                if (info != null && info.getKind() == Kind.IssuerSecurityDomain)
                    g2d.drawImage(issuer, null, 20, 4);
            }
        };
        add(icon, gbc);

        container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBackground(Color.WHITE);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        if (info != null && info.getName() == null)
            title = adjustLength(title, 15);
        else
            title = adjustLength(title, 25);
        container.add(getLabel(title, "width:100px; height: 60px; margin: 5px", 16f), gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_START;
        author = adjustLength(author, 15);
        JLabel infoPanel = getLabel(author, "width:85px; max-lines:1; margin: 5px", 13f);
        infoPanel.setHorizontalAlignment(SwingConstants.RIGHT);
        container.add(infoPanel, gbc);

        gbc.fill = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.LAST_LINE_END;
        version = adjustLength(version, 5);
        container.add(getLabel(version, "width:10px; text-overflow: ellipsis; margin: 5px", 15f), gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(container, gbc);
    }

    public LocalItem(AppletInfo info) {
        this(
                breakIfTooLong(getName(info)),
                (info.getImage() == null) ? "wrong-image-name" : info.getImage(),
                (info.getAuthor() == null) ? textSrc.getString("unknown") : info.getAuthor(),
                (info.getVersion() == null) ? "" : info.getVersion(),
                info
        );
    }

    private static String getName(AppletInfo info) {
        String name = (info.getKind() == Kind.ExecutableLoadFile ? textSrc.getString("package_for") : "");
        if (info.getName() == null) {
            return name + info.getAid().toString();
        }
        return name + info.getName();
    }

    private static String breakIfTooLong(String name) {
        int index = name.indexOf(' ');
        if ((index < 0 || index > 14) && name.length() > 14) {
            return name.substring(0, 14) + " " + name.substring(14);
        }
        return name;
    }

    @Override
    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public int compareTo(Item o) {
        if (!(o instanceof LocalItem))
            return 1;

        LocalItem other = (LocalItem) o;
        if (info.getKind() == Kind.IssuerSecurityDomain || info.getKind() == Kind.SecurityDomain) {
            if (other.info.getKind() == Kind.IssuerSecurityDomain || other.info.getKind() == Kind.SecurityDomain) {
                return name.compareTo(other.name);
            } else {
                return -1;
            }
        } else if (other.info.getKind() == Kind.IssuerSecurityDomain || other.info.getKind() == Kind.SecurityDomain) {
            return 1;
        }

        return 13 * name.compareTo(other.name) + info.getKind().compareTo(other.info.getKind());
    }

    @Override
    protected void paintComponent(Graphics g) {
        CardInstance card = manager.getCard();
        boolean isSelected = info != null && manager.isAppletStoreSelected(info.getAid());

        if (info != null) {
            Graphics2D g2d = (Graphics2D) g;

            if (isSelected) {
                container.setBackground(selected);
                Composite old = g2d.getComposite();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                container.setBackground(selected);
                g2d.setComposite(old);
            }
            if (info.getAid() != null && info.getAid().equals(manager.getLastAppletInstalledAid()) && newItem != null) {
                g2d.drawImage(newItem, getWidth() - LABELDIMEN, 0, LABELDIMEN, LABELDIMEN, null);
            } else if (info.getAid() != null && card != null && info.getAid().equals(card.getDefaultSelected()) && superSelected != null) {
                g2d.drawImage(superSelected, getWidth() - LABELDIMEN, 0, LABELDIMEN, LABELDIMEN, null);
            }
        }
        if (!isSelected) {
            container.setBackground(Color.WHITE);
        }
        super.paintComponent(g);
    }

    private JLabel getLabel(String text, String css, float size) {
        return new HtmlText("<div style=\"" + css + "\">" + text + "</div>", size);
    }

    private static BufferedImage getIssuerImg() {
        BufferedImage img;
        try {
            img = ImageIO.read(new File(Config.IMAGE_DIR + "issuer.png"));
        } catch (IOException e) {
            e.printStackTrace();
            img = new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
        }
        return img;
    }

    private String getImgAddress(String imgName) {
        File img = new File(Config.RESOURCES + imgName);
        if (info == null) {
            img = new File(Config.IMAGE_DIR + "applet_plain.png");
        } else if (!img.exists()) {
            switch (info.getKind()) {
                case IssuerSecurityDomain:
                case SecurityDomain:
                    img = new File(Config.IMAGE_DIR + "sd_plain.png");
                    break;
                case Application:
                    img = new File(Config.IMAGE_DIR + "applet_plain.png");
                    break;
                case ExecutableLoadFile:
                    img = new File(Config.IMAGE_DIR + "pkg_plain.png");
                    break;
            }
        }
        return img.getAbsolutePath();
    }
}
