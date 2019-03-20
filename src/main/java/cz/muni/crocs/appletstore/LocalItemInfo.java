package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.card.CardManager;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.util.AppletInfo;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.smartcardio.CardException;
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

    private LocalWindowPane parent;

    public LocalItemInfo(LocalWindowPane parent) {
        super(Config.options.get(Config.OPT_KEY_HINT).equals("true"));
        this.parent = parent;

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

        rawApdu = new JLabel(Config.translation.get(141), new ImageIcon(
                Config.IMAGE_DIR + "raw_apdu.png"), SwingConstants.CENTER);
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

        uninstall = new JLabel(Config.translation.get(140), new ImageIcon(
                Config.IMAGE_DIR + "delete.png"), SwingConstants.CENTER);
        uninstall.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        uninstall.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!uninstall.isEnabled()) return;

                DeleteDialogWindow opts = new DeleteDialogWindow(nfo.getAid().toString(), nfo.getKind(), nfo.hasKeys());
                switch (showDialog(Config.translation.get(19), opts, "delete.png", 18, 116)) {
                    case JOptionPane.NO_OPTION:
                    case JOptionPane.CLOSED_OPTION:
                        return;
                    case JOptionPane.YES_OPTION: //continue
                }

                String msg = opts.confirm();
                if (msg != null) {
                    switch (showDialog(Config.translation.get(58), msg, "error.png", 20, 116)) {
                        case JOptionPane.NO_OPTION:
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.YES_OPTION: //continue
                    }
                }
                try {
                    CardManager.getInstance().uninstall(nfo, opts.willForce());
                    parent.setupWindow();
                } catch (CardException e1) {
                    e1.printStackTrace();
                    //todo log and notify
                }

            }
        });
        add(uninstall, "wrap");
    }

    private int showDialog(String title, Object msg, String imgname, int confirmBtn, int cancelBtn) {
        return JOptionPane.showOptionDialog(Config.getWindow(),
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                new ImageIcon(Config.IMAGE_DIR + imgname),
                new String[]{Config.translation.get(confirmBtn), Config.translation.get(cancelBtn)}, "error");
    }

    public void set(AppletInfo info) {
        nfo = info;
        name.setText("<html><p width=\"280\">" + info.getName() + "</p></html>", Config.translation.get(210));
        version.setText("<html><p width=\"280\">Version: " +
                ((info.getVersion().isEmpty()) ? "??" : info.getVersion()) + "</p></html>", Config.translation.get(211));
        id.setText("<html><p width=\"280\">ID: " + info.getAid().toString(), Config.translation.get(212));
        type.setText("<html><p width=\"280\">Type: " +
                getType(info.getKind()) + "</p></html>", Config.translation.get(213));
        domain.setText("<html><p width=\"280\">Domain assigned: " +
                ((info.getDomain() == null) ? "unknown" : info.getDomain().toString()), Config.translation.get(214));
        uninstall.setEnabled(info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile || info.getKind() == GPRegistryEntry.Kind.Application);
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
