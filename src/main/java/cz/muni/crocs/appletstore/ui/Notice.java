package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.iface.CallBack;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

/**
 * Notice to display in the store below main bar
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Notice extends JPanel {
    private static final ResourceBundle textSrc =
            ResourceBundle.getBundle("Lang", OptionsFactory.getOptions().getLanguageLocale());

    /**
     * Importance levels, decide the color
     */
    public enum Importance {
        FATAL, SEVERE, INFO
    }

    /**
     * Icon to display (retry when callback defined, close if supports closing).
     */
    public enum CallBackIcon {
        RETRY, CLOSE, OPEN_FOLDER, NO_ICON
    }

    /**
     * Cerate a notification
     * @param msg message to display
     * @param status status - color
     * @param type - icon type
     * @param onClick - callbacks to perform, the type should be either close or retry, not called if NO_ICON
     */
    public Notice(String msg, Importance status, CallBackIcon type, CallBack<?> ... onClick) {
        setLayout(new MigLayout("center, gapy 20, insets 0 20 0 20"));
        MouseAdapter call = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (CallBack<?> c : onClick) {
                    c.callBack();
                }
            }
        };

        String image;
        switch (status) {
            case FATAL:
                image = "error.png";
                setBackground(new Color(193, 39, 39));
                break;
            case SEVERE:
                image = "announcement.png";
                setBackground(new Color(193, 137, 56));
                break;
            case INFO:
                image = "info.png";
                setBackground(new Color(159, 193, 55));
                break;
            default:
                image = "info.png";
        }

        JLabel error = new Text(new ImageIcon(Config.IMAGE_DIR + image));
        error.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(error);

        JLabel errorMsg = new HtmlText("<div style=\"max-width:90%;\">" + msg + "</div>");
        errorMsg.setFont(OptionsFactory.getOptions().getTitleFont(12f));
        errorMsg.setForeground(Color.BLACK);
        add(errorMsg, "growx");

        switch (type) {
            case CLOSE:
                add(addIcon("close_black.png", call));
                break;
            case RETRY:
                add(addIcon("sync.png"));

                JLabel retry = new JLabel(textSrc.getString("retry"));
                retry.setFont(OptionsFactory.getOptions().getTitleFont(12f));
                retry.setForeground(Color.BLACK);
                add(retry);
                retry.addMouseListener(call);
                break;
            case OPEN_FOLDER:
                add(addIcon("folder-open.png", call));
                break;
            default:
                break;
        }
    }

    private JLabel addIcon(String imgName) {
        JLabel icon = new JLabel(new ImageIcon(Config.IMAGE_DIR + imgName));
        icon.setBorder(new EmptyBorder(0, 20, 0, 0));
        return icon;
    }

    private JLabel addIcon(String imgName, MouseAdapter callback) {
        JLabel icon = addIcon(imgName);
        icon.addMouseListener(callback);
        icon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return icon;
    }

}
