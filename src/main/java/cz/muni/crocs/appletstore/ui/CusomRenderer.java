package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CusomRenderer extends DefaultListCellRenderer implements ListCellRenderer<Object> {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellMasFocus) {
        ListItem content = (ListItem)value;
        setText(content.getText());
        setIcon(content.getImage());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(true);
        return this;
    }
}
