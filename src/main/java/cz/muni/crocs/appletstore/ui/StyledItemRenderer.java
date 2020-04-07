package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Custom cell renderer style
 *
 * @author Jiří Horák
 * @version 1.0
 * @param <T>
 */
public class StyledItemRenderer<T> extends JPanel implements ListCellRenderer<T> {
    private JLabel label = new Text();

    public StyledItemRenderer() {
        setPreferredSize(new Dimension(80, 20));
        add(label);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null)
            return this;
        label.setText(value.toString());
        setBackground(isSelected ? Color.WHITE : Color.LIGHT_GRAY);
        return this;
    }
}