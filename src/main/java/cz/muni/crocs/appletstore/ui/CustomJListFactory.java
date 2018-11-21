package cz.muni.crocs.appletstore.ui;

import javax.swing.*;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class CustomJListFactory {

    private JList list = new JList();

    DefaultListModel<ListItem> model = new DefaultListModel<>();

    public CustomJListFactory add (String text, String imageUrl) {
        model.addElement(new ListItem(text, new ImageIcon(imageUrl)));
        return this;
    }

    public void clear() {
        model.clear();
    }

    public void setCellSize(int width, int height) {
        list.setFixedCellWidth(width);
        list.setFixedCellWidth(height);
    }

    public JList build() {
        list.setCellRenderer(new CusomRenderer());
        list.setModel(model);
        return list;
    }
}
