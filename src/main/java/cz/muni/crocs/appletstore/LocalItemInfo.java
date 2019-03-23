package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.action.DeleteAction;
import cz.muni.crocs.appletstore.action.SendApduAction;
import cz.muni.crocs.appletstore.iface.OnEventCallBack;
import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.card.AppletInfo;
import cz.muni.crocs.appletstore.util.Sources;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalItemInfo extends HintPanel {

    private AppletInfo nfo;
    private HintLabel name = new HintLabel();
    private JLabel author = new JLabel();
    private HintLabel version = new HintLabel();
    private HintLabel id = new HintLabel();;
    private HintLabel type = new HintLabel();
    private HintLabel domain = new HintLabel();
    private JLabel uninstall;
    private JLabel rawApdu;

    private SendApduAction send;
    private DeleteAction delete;

    public LocalItemInfo(OnEventCallBack<Void, Void, Void> call) {
        super(Sources.options.get(Config.OPT_KEY_HINT).equals("true"));

        setOpaque(false);
        setLayout(new MigLayout());

        send = new SendApduAction(nfo, call);
        delete = new DeleteAction(nfo, call);

        name.setFont(CustomFont.plain.deriveFont(16f));
        name.setBorder(new EmptyBorder(30, 0, 10, 5));
        add(name, "span 2, wrap");

        author.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(author, "span 2, wrap");

        version.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(version, "span 2, wrap");

        id.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(id, "span 2, wrap");

        type.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(type, "span 2, wrap");

        domain.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(domain, "span 2, wrap");

        JLabel title = new JLabel(Sources.language.get("management"));
        title.setFont(CustomFont.plain.deriveFont(Font.BOLD, 13f));
        add(title, "span 2, gaptop 15, wrap");

        rawApdu = new JLabel(Sources.language.get("custom_command"), new ImageIcon(
                Config.IMAGE_DIR + "raw_apdu.png"), SwingConstants.CENTER);
        rawApdu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rawApdu.addMouseListener(send);
        add(rawApdu, "wrap");

        uninstall = new JLabel(Sources.language.get("uninstall"), new ImageIcon(
                Config.IMAGE_DIR + "delete.png"), SwingConstants.CENTER);
        uninstall.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        uninstall.addMouseListener(delete);
        add(uninstall, "wrap");
    }

    public void set(AppletInfo info) {
        delete.setInfo(info);
        send.setInfo(info);
        name.setText("<html><p width=\"280\">" + info.getName() + "</p></html>",
                Sources.language.get("H_name"));
        author.setText("<html><p width=\"280\">" + Sources.language.get("author") +
                info.getAuthor() + "</p></html>");
        version.setText("<html><p width=\"280\">" + Sources.language.get("version") +
                ((info.getVersion().isEmpty()) ? "??" : info.getVersion()) + "</p></html>",
                Sources.language.get("H_version"));
        id.setText("<html><p width=\"280\">ID: " + info.getAid().toString(),
                Sources.language.get("H_id"));
        type.setText("<html><p width=\"280\">" + Sources.language.get("type") +
                getType(info.getKind()) + "</p></html>", Sources.language.get("H_type"));
        domain.setText("<html><p width=\"280\">" + Sources.language.get("sd_assigned") +
                ((info.getDomain() == null) ? "unknown" : info.getDomain().toString()),
                Sources.language.get("H_sd_assinged"));
        uninstall.setEnabled(info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile
                || info.getKind() == GPRegistryEntry.Kind.Application);
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
