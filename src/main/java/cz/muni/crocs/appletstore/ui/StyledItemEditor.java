package cz.muni.crocs.appletstore.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;

/**
 * Custom combo box editor style
 *
 * @author Jiří Horák
 * @version 1.0
 */
public class StyledItemEditor extends BasicComboBoxEditor {
    private String selected;

    private JPanel panel = new JPanel();
    private JLabel label = new JLabel();

    public StyledItemEditor() {
        label.setOpaque(false);
        label.setForeground(Color.BLACK);
        panel.add(label);
        panel.setBackground(Color.WHITE);
    }

    @Override
    public Component getEditorComponent() {
        return this.panel;
    }

    @Override
    public Object getItem() {
        return this.selected;
    }

    @Override
    public void setItem(Object item) {
        if (item == null) return;
        selected = (String)item;
        label.setText(selected);
    }
}
