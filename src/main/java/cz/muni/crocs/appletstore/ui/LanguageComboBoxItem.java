package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.Config;
import cz.muni.crocs.appletstore.util.Language;
import cz.muni.crocs.appletstore.util.OptionsFactory;
import cz.muni.crocs.appletstore.util.Tuple;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class LanguageComboBoxItem extends JLabel implements ListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        setForeground(list.getForeground());
        setBackground((isSelected) ? list.getSelectionBackground() : list.getBackground());
        setIcon(new ImageIcon(Config.IMAGE_DIR + ((Language) value).getImageString()));
        setText(value.toString());
        setFont(OptionsFactory.getOptions().getFont());
        return this;
    }
}
