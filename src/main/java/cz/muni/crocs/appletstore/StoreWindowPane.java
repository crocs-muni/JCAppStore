package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.ErrorPane;
import cz.muni.crocs.appletstore.util.InternetConnection;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class StoreWindowPane extends JPanel {

    private AppletStore context;

    public StoreWindowPane(AppletStore context) {
        this.context = context;
        setOpaque(false);
        init();
    }

    public void init() {
        removeAll();
        if (InternetConnection.isAvailable()) {
            setupWindow();
        } else {
            noConnection();
        }
        //TODO doesn't remove the no readers found error pane on second attempt
        revalidate();
        repaint();
    }

    private void noConnection() {
        setLayout(new GridBagLayout());
        add(new ErrorPane(4, "wifi_off.png"));
    }

    private void setupWindow() {

    }

}
