package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;
import cz.muni.crocs.appletstore.ui.ErrorPane;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LocalWindowPane extends JPanel {

    private AppletStore context;

    public LocalWindowPane(AppletStore context) {
        this.context = context;
        setOpaque(false);
        init();
    }

    public void init() {
        removeAll();
        revalidate();
        if (context.terminals.isFound()) {
            setupWindow();
        } else {
            noReaders();
        }
    }

    private void noReaders() {
        setLayout(new GridBagLayout());
        add(new ErrorPane(2, "no-reader.png"));
    }

    private void setupWindow() {
        setLayout(new GridBagLayout());
        add(new ErrorPane(8, "shop.png"));
        setBackground(Color.GREEN);
    }

}
