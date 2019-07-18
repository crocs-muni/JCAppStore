package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.sources.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomComboBoxItem extends JLabel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        Tuple<String, String> val = (Tuple<String, String>)value;
        setForeground(list.getForeground());
        setBackground((isSelected) ? list.getSelectionBackground() : list.getBackground());
        setIcon(new ImageIcon(Config.IMAGE_DIR + val.first + ".jpg"));
        setText(val.second);
        setFont(OptionsFactory.getOptions().getDefaultFont());
        return this;
    }
}
