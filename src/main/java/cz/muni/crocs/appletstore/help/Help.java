package cz.muni.crocs.appletstore.help;

import cz.muni.crocs.appletstore.ui.Title;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

/**
 * Help panel
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class Help extends JPanel {

    public Help() {
        setLayout(new MigLayout());
    }

    protected static JLabel getLabel(String text, float size) {
        JLabel label = new Title(text, size);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return label;
    }
}
