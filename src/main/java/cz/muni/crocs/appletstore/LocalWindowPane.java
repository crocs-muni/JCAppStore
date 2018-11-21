package cz.muni.crocs.appletstore;

import cz.muni.crocs.appletstore.ui.CustomFont;

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

        JPanel midContainer = new JPanel();
        midContainer.setOpaque(false);
        midContainer.setLayout(new BoxLayout(midContainer, BoxLayout.Y_AXIS));
        JLabel error = new JLabel(new ImageIcon(Config.IMAGE_DIR + "no-reader.png"));
        error.setAlignmentX(Component.CENTER_ALIGNMENT);
        error.setBorder(new EmptyBorder(10, 10,10 ,10 ));
        midContainer.add(error);
        JLabel errorMsg = new JLabel(Config.translation.get(2));
        errorMsg.setFont(CustomFont.plain.deriveFont(20f));
        errorMsg.setForeground(new Color(139, 139, 139));
        errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        midContainer.add(errorMsg);
        add(midContainer);
    }

    private void setupWindow() {

    }

}
