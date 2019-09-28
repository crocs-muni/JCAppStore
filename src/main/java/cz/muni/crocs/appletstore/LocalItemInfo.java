package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.util.OnEventCallBack;
import cz.muni.crocs.appletstore.util.Options;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.ui.HintLabel;
import cz.muni.crocs.appletstore.ui.HintPanel;
import cz.muni.crocs.appletstore.card.AppletInfo;
import net.miginfocom.swing.MigLayout;
import pro.javacard.gp.GPRegistryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Panel that shows applet info on click
 * allows deletion & communication with chosen applet
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalItemInfo extends HintPanel {
    private static ResourceBundle textSrc = ResourceBundle.getBundle("Lang", Locale.getDefault());

    private HintLabel name = new HintLabel();
    private JLabel author = new JLabel();
    private HintLabel version = new HintLabel();
    private HintLabel sdk = new HintLabel();
    private HintLabel id = new HintLabel();;
    private HintLabel type = new HintLabel();
    private HintLabel domain = new HintLabel();
    private JLabel uninstall;
    private JLabel rawApdu;

    private SendApduAction send;
    private DeleteAction delete;

    public LocalItemInfo(OnEventCallBack<Void, Void, Void> call) {
        super(OptionsFactory.getOptions().getOption(Options.KEY_HINT).equals("true"));

        setOpaque(false);
        setLayout(new MigLayout());

        send = new SendApduAction(null, call);
        delete = new DeleteAction(null, call);

        name.setFont(OptionsFactory.getOptions().getTitleFont(16f));
        name.setBorder(new EmptyBorder(30, 0, 10, 5));
        add(name, "span 2, wrap");

        author.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(author, "span 2, wrap");

        version.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(version, "span 2, wrap");

        sdk.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(sdk, "span 2, wrap");

        id.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(id, "span 2, wrap");

        type.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(type, "span 2, wrap");

        domain.setBorder(new EmptyBorder(5, 0, 5, 5));
        add(domain, "span 2, wrap");

        JLabel title = new JLabel(textSrc.getString("management"));
        title.setFont(OptionsFactory.getOptions().getTitleFont(Font.BOLD, 13f));
        add(title, "span 2, gaptop 15, wrap");

        rawApdu = new JLabel(textSrc.getString("custom_command"), new ImageIcon(
                Config.IMAGE_DIR + "raw_apdu.png"), SwingConstants.CENTER);
        rawApdu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        rawApdu.addMouseListener(send);
        add(rawApdu, "wrap");

        uninstall = new JLabel(textSrc.getString("uninstall"), new ImageIcon(
                Config.IMAGE_DIR + "delete.png"), SwingConstants.CENTER);
        uninstall.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        uninstall.addMouseListener(delete);
        add(uninstall, "wrap");
    }

    /**
     * Display info panel - show certain applet
     * @param info applet info to show
     */
    public void set(AppletInfo info) {
        if (info == null) {
            unset();
            return;
        }
        delete.setInfo(info);
        send.setInfo(info);

        setLabel(name, info.getName() == null ? info.getAid().toString() : info.getName(), "H_name");
        setLabel(author, textSrc.getString("author") + getValue(info.getAuthor()));
        setLabel(version, textSrc.getString("version") + getValue(info.getVersion()), "H_version");
        setLabel(sdk, textSrc.getString("sdk_version") + getValue(info.getSdk()), "H_custom_sdk");
        setLabel(id, textSrc.getString("aid") + info.getAid().toString(), "H_id");
        setLabel(type, textSrc.getString("type") + getType(info.getKind()), "H_type");
        setLabel(domain, textSrc.getString("sd_assigned") +
                ((info.getDomain() == null) ? textSrc.getString("unknown") : info.getDomain().toString()), "H_sd_assinged");
        uninstall.setEnabled(info.getKind() == GPRegistryEntry.Kind.ExecutableLoadFile
                || info.getKind() == GPRegistryEntry.Kind.Application);
        rawApdu.setEnabled(info.getKind() != GPRegistryEntry.Kind.ExecutableLoadFile);
        setVisible(true);
    }

    private String getValue(String maybe) {
        if (maybe == null || maybe.isEmpty())
            return textSrc.getString("unknown");
        return maybe;
    }

    private void setLabel(HintLabel label, String data, String keyHint) {
        label.setText("<html><p width=\"280\">" + data + "</p></html>", textSrc.getString(keyHint));
    }

    private void setLabel(JLabel label, String data) {
        label.setText("<html><p width=\"280\">" + data + "</p></html>");
    }

    /**
     * Hide the info panel
     */
    public void unset() {
        name.setText("", "");
        version.setText("", "");
        id.setText("", "");
        type.setText("", "");
        domain.setText("", "");
        setVisible(false);
        revalidate();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(300, Integer.MAX_VALUE);
    }

    private String getType(GPRegistryEntry.Kind kind) {
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
