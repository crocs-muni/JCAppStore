package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.util.AppletInfo;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalItemInfo extends HintPanel {

    private AppletInfo nfo;
    private HintLabel name = new HintLabel();
    private HintLabel version = new HintLabel();
    private HintLabel id = new HintLabel();;
    private HintLabel type = new HintLabel();
    private HintLabel domain = new HintLabel();
    private JLabel uninstall;
    private JLabel rawApdu;

    public LocalItemInfo() {
        setOpaque(false);
        setLayout(new MigLayout());

        name.setFont(CustomFont.plain.deriveFont(16f));
        name.setBorder(new EmptyBorder(30, 0, 10, 5));
        add(name, "span 2, wrap");

        version.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(version, "span 2, wrap");

        id.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(id, "span 2, wrap");

        type.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(type, "span 2, wrap");

        domain.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(domain, "span 2, wrap");

        JLabel title = new JLabel(Config.translation.get(139));
        title.setFont(CustomFont.plain.deriveFont(Font.BOLD, 13f));
        add(title, "span 2, gaptop 15, wrap");

        rawApdu = new JLabel(Config.translation.get(141), new ImageIcon(Config.IMAGE_DIR + "raw_apdu.png"), SwingConstants.CENTER);
        rawApdu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rawApdu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!rawApdu.isEnabled())
                    return;
                int result = JOptionPane.showConfirmDialog(
                        Config.getWindow(),
                        "TODO" /*todo create insert-apdu pane*/,
                        Config.translation.get(9) + nfo.getName(),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        new ImageIcon(Config.IMAGE_DIR + "info.png"));

                switch (result) {
                    case JOptionPane.NO_OPTION:
                    case JOptionPane.CLOSED_OPTION:
                        return;
                    case JOptionPane.YES_OPTION: //continue
                }
                //CardManager.getInstance().uninstall();
            }
        });
        add(rawApdu, "wrap");

        uninstall = new JLabel(Config.translation.get(140), new ImageIcon(Config.IMAGE_DIR + "delete.png"), SwingConstants.CENTER);
        uninstall.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        uninstall.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!uninstall.isEnabled())
                    return;
                int result = JOptionPane.showConfirmDialog(
                        Config.getWindow(),
                        "<html><p width=\"350\">" + Config.translation.get(142) + " <br />" +
                                (nfo.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile ?
                                        Config.translation.get(144) : Config.translation.get(143)) + "</p></html>",
                        Config.translation.get(8) + nfo.getName(),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        new ImageIcon(Config.IMAGE_DIR + "info.png"));

                switch (result) {
                    case JOptionPane.NO_OPTION:
                    case JOptionPane.CLOSED_OPTION:
                        return;
                    case JOptionPane.YES_OPTION: //continue
                }
                //CardManager.getInstance().uninstall();
            }
        });
        add(uninstall, "wrap");
    }

    public void set(AppletInfo info) {
        nfo = info;
        name.setText("<html><p width=\"280\">" + info.getName() + "</p></html>",
                "The applet or package name.\nThe ID is displayed,if not installed\nfrom appletStore. ");
        version.setText("<html><p width=\"280\">Version: " + ((info.getVersion().isEmpty()) ? "??" : info.getVersion()) + "</p></html>",
                "Applet version installed, unknown,\nif not installed from appletStore.");
        id.setText("<html><p width=\"280\">ID: " + info.getAid().toString(),
                "Applet or package unique ID.\nApplet usually contains a part of it's\nown package ID.");
        type.setText("<html><p width=\"280\">Type: " + getType(info.getKind()) + "</p></html>",
                "Each card consists of several objects:" +
                        "\nSecurity Domains - applets for management.\nFor example, those applets install and delete\n" +
                        "other applets. Issuer SD is a SD uploaded\nby the card issuer." +
                        "\nApplets - the instances of installed\nsoftware from package." +
                        "\nPackages - the context for each different\napplet installed.");
        domain.setText("<html><p width=\"280\">Domain assigned: " + ((info.getDomain() == null) ? "unknown" : info.getDomain().toString()),
                "The security domain assigned\nto this applet.");
        uninstall.setEnabled(info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile);
        rawApdu.setEnabled(info.getKind() != GPRegistryEntry.Kind.ExecutableLoadFile);
    }

    public void unset() {
        name.setText("", "");
        version.setText("", "");
        id.setText("", "");
        type.setText("", "");
        domain.setText("", "");
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, Integer.MAX_VALUE);
    }

    public String getType(GPRegistryEntry.Kind kind) {
        switch (kind) {
            case ExecutableLoadFile:
                return "package";
            case SecurityDomain:
                return "security domain";
            case IssuerSecurityDomain:
                return "issuer security domain";
            case Application:
                return "applet";
            default:
                return "unknown";
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(old);
        super.paint(g);
    }
}
